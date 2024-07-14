package ru.practicum.android.diploma.search.presentation.view

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.widget.NestedScrollView
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.practicum.android.diploma.R
import ru.practicum.android.diploma.common.data.ErrorType
import ru.practicum.android.diploma.common.data.NoInternetError
import ru.practicum.android.diploma.common.domain.VacancyBase
import ru.practicum.android.diploma.common.presentation.EditTextSearchIcon
import ru.practicum.android.diploma.databinding.FragmentSearchBinding
import ru.practicum.android.diploma.search.domain.models.VacanciesNotFoundType
import ru.practicum.android.diploma.search.presentation.models.SearchState
import ru.practicum.android.diploma.search.presentation.viewmodel.SearchViewModel
import ru.practicum.android.diploma.util.debounce
import ru.practicum.android.diploma.util.getCountableVacancies
import ru.practicum.android.diploma.vacancydetails.presentation.view.VacancyDetailsFragment

class SearchFragment : Fragment() {

    companion object {
        private const val SEARCH_MASK = ""
        private const val CLICK_DEBOUNCE_DELAY_MILLIS = 200L
    }

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!
    private val viewModel by viewModel<SearchViewModel>()

    private var onVacancyClickDebounce: (VacancyBase) -> Unit = { _ -> }
    private val vacancySearchAdapter = VacancySearchAdapter { vacancy -> onVacancyClickDebounce(vacancy) }

    private var currentPage = 0
    private var totalPages = 0
    private var isNextPageLoading = false
    private var searchMask = SEARCH_MASK

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.searchResultsRV.adapter = vacancySearchAdapter
        onVacancyClickDebounce = debounce<VacancyBase>(
            CLICK_DEBOUNCE_DELAY_MILLIS,
            viewLifecycleOwner.lifecycleScope,
            false
        ) { track ->
            openVacancy(track)
        }

        // показать стартовую выборку только с 0-й страницей
        // иначе при переходе между фрагментами выборка теряется
        // (между фрагментами сохраняется currentPage)
        if (currentPage == 0) {
            showStartPage()
        }

        // подпишемся на результаты поиска
        viewModel.observeState().observe(viewLifecycleOwner) { state ->

            Log.d("mine", "STATE=${state.javaClass}")

            when (state) {
                is SearchState.Content -> {
                    isNextPageLoading = false
                    showContent(state)
                }

                is SearchState.Loading -> {
                    showLoading()
                }

                is SearchState.Empty -> {
                    isNextPageLoading = false
                    showErrorOrEmptySearch(VacanciesNotFoundType())
                }

                is SearchState.Error -> {
                    isNextPageLoading = false
                    showErrorOrEmptySearch(state.errorType)
                }
            }
        }
        // настроим слежение за объектами фрагмента
        setBindings()
    }

    // обработка изменений в поле для поиска
    private fun bindEditTextSearch() {
        with(binding.editTextSearch) {
            // изменение текста
            doOnTextChanged { text, start, before, count ->
                // любое изменение сбрасывает текущий поиск
                resetSearchParams()
                searchMask = text.toString().trim()
                // иконка в поле поиска
                setEditTextIconBySearchMask()
                // запуск поискового запроса
                if (binding.editTextSearch.hasFocus() && searchMask.isNotEmpty()) {
                    viewModel.searchDebounce(searchMask, currentPage)
                }
            }
            // обработка нажатия на поле (кнопка стереть)
            setOnTouchListener { v, event ->
                v.performClick()
                if (event.action == MotionEvent.ACTION_UP
                    && event.rawX >= binding.editTextSearch.right
                    - binding.editTextSearch.compoundDrawables[2].bounds.width()
                ) {
                    // сбросим параметры для нового поиска
                    resetSearchParams()
                    searchMask = ""
                    setEditTextIconBySearchMask()
                    binding.editTextSearch.setText(searchMask)
                    showStartPage()
                    true
                }
                false
            }
            setOnEditorActionListener { _, actionId, _ ->
                // поиск по нажатию Done на клавиатуре
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    viewModel.searchByClick(binding.editTextSearch.text.toString())
                    true
                }
                false
            }
        }
    }

    // настроим слежку за изменениями во фрагменте
    @SuppressLint("ClickableViewAccessibility")
    private fun setBindings() {
        // обработка изменений в строке поиска
        bindEditTextSearch()
        // мониторим скроллинг списка вакансий для загрузки новой страницы
        binding.idNestedSV.setOnScrollChangeListener(
            NestedScrollView.OnScrollChangeListener { v, scrollX, scrollY, oldScrollX, oldScrollY ->
                // если вернулись с другого фрагмента кнопкой назад NestedView скроллится на ту же позицию
                val scrolledTo = v.getChildAt(0).measuredHeight - v.measuredHeight
                val deltaScroll = scrollY - oldScrollY
                // поэтому если сразу проскроллилось от самого верха до низа (дельта скроллинга = всей высоте элемента), то подгружать не надо
                if (!isNextPageLoading && deltaScroll < scrollY && scrolledTo == scrollY) {
                    loadNextPage()
                }
            }
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // переключить видимость прелоадера следующей страницы и может показать тост
    private fun nextPagePreloaderToggle(show: Boolean, message: String? = null) {
        if (binding.searchNewItemsProgressBar.isVisible != show) {
            binding.searchNewItemsProgressBar.isVisible = show
            if (message != null) {
                showToast(message, short = true)
            }
        }
    }

    // показать прелоадер первой страницы
    private fun showLoading() {
        with(binding) {
            searchProgressBar.isVisible = true
            placeHolderImage.isVisible = false
            placeHolderText.isVisible = false
            searchResultsRV.isVisible = false
            vacanciesCountText.isVisible = false
        }
        hideKeyboard()
    }

    private fun showStartPage() {
        Log.d("mine", "showStartPage")
        with(binding) {
            placeHolderImage.isVisible = true
            placeHolderImage.setImageResource(R.drawable.image_search_empty)
            searchProgressBar.isVisible = false
            placeHolderText.isVisible = false
            searchResultsRV.isVisible = false
            vacanciesCountText.isVisible = false
        }
        val adapterListSize = vacancySearchAdapter.vacancies.size
        if (adapterListSize > 0) {
            vacancySearchAdapter.vacancies.clear()
            vacancySearchAdapter.notifyItemRangeRemoved(0, adapterListSize)
        }
    }

    private fun showContent(state: SearchState.Content) {
        currentPage = state.page
        totalPages = state.pages
        val vacancies = state.vacancies
        with(binding) {
            searchProgressBar.isVisible = false
            vacanciesCountText.isVisible = true
            vacanciesCountText.text = getCountableVacancies(state.found, resources)
            placeHolderImage.isVisible = false
            placeHolderText.isVisible = false
            searchResultsRV.isVisible = true
        }
        if (currentPage > 0) {
            nextPagePreloaderToggle(false)
            for (newVac in vacancies) {
                // при перезагрузке фрагмента придет последний state с вакансиями - добавлять их снова не надо
                if (!vacancySearchAdapter.vacancies.contains(newVac)) {
                    vacancySearchAdapter.vacancies.add(newVac)
                    vacancySearchAdapter.notifyItemInserted(vacancySearchAdapter.vacancies.size - 1)
                }
            }
        } else {
            vacancySearchAdapter.vacancies.clear()
            vacancySearchAdapter.vacancies.addAll(vacancies)
            vacancySearchAdapter.notifyDataSetChanged()
        }

        binding.vacanciesCountText.append(" (${vacancySearchAdapter.vacancies.size})")
    }

    private fun resetSearchParams() {
        viewModel.stopSearch()
        currentPage = 0
    }

    private fun loadNextPage() {
        Log.d("mine", "loadNextPage(${currentPage + 2})")
        if (isNextPageLoading) {
            return
        }
        if (currentPage + 1 == totalPages) {
            showToast(getString(R.string.bottom_of_list))
        } else {
            isNextPageLoading = true
            nextPagePreloaderToggle(show = true)
            viewModel.searchDebounce(searchMask, currentPage + 1)
        }
    }

    private fun getErrorMessage(type: ErrorType): String {
        return when (type) {
            is VacanciesNotFoundType -> getString(R.string.no_vacancies)
            is NoInternetError -> getString(R.string.no_internet)
            else -> getString(R.string.server_error)
        }
    }

    private fun showErrorOrEmptySearch(type: ErrorType) {
        Log.d("mine", "size=${vacancySearchAdapter.vacancies.size}")
        hideKeyboard()
        val errorMessage = getErrorMessage(type)
        if (vacancySearchAdapter.vacancies.size > 0) {
            nextPagePreloaderToggle(false, errorMessage)
        } else {
            when (type) {
                is VacanciesNotFoundType -> {
                    with(binding) {
                        vacanciesCountText.isVisible = true
                        vacanciesCountText.text = getString(R.string.no_vacancies)
                        placeHolderImage.setImageResource(R.drawable.image_nothing_found)
                    }
                }

                is NoInternetError -> {
                    with(binding) {
                        vacanciesCountText.isVisible = false
                        placeHolderImage.setImageResource(R.drawable.image_no_internet_error)
                    }
                }

                else -> {
                    with(binding) {
                        vacanciesCountText.isVisible = false
                        placeHolderImage.setImageResource(R.drawable.image_search_server_error)
                    }
                }
            }
            with(binding) {
                placeHolderText.text = errorMessage
                searchResultsRV.isVisible = false
                searchProgressBar.isVisible = false
                placeHolderImage.isVisible = true
                placeHolderText.isVisible = true
            }
        }

    }

    // на основе поля searchMask покажем нужную иконку
    private fun setEditTextIconBySearchMask() {
        val icon = if (searchMask.isNotEmpty()) {
            // стереть
            EditTextSearchIcon.CLEAR_ICON
        } else {
            // поиск (пустая маска)
            EditTextSearchIcon.SEARCH_ICON
        }
        val newIcon = ContextCompat.getDrawable(requireContext(), icon.drawableId)
        newIcon?.setBounds(0, 0, newIcon.intrinsicWidth, newIcon.intrinsicHeight)
        binding.editTextSearch.setCompoundDrawables(null, null, newIcon, null)
    }

    private fun openVacancy(vacancy: VacancyBase) {
        findNavController().navigate(
            R.id.action_searchFragment_to_vacancyDetailsFragment,
            VacancyDetailsFragment.createArgs(vacancy.hhID)
        )
    }

    private fun showToast(additionalMessage: String, short: Boolean = false) {
        Toast.makeText(requireContext(), additionalMessage, if (short) Toast.LENGTH_SHORT else Toast.LENGTH_LONG).show()
    }

    private fun hideKeyboard() {
        val view: View? = activity?.currentFocus
        if (view != null) {
            val inputMethodManager =
                activity?.getSystemService(AppCompatActivity.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }
}
