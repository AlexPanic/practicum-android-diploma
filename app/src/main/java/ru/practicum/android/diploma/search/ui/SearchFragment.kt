package ru.practicum.android.diploma.search.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.practicum.android.diploma.R
import ru.practicum.android.diploma.common.domain.VacancyBase
import ru.practicum.android.diploma.common.presentation.EditTextSearchIcon
import ru.practicum.android.diploma.databinding.FragmentSearchBinding
import ru.practicum.android.diploma.filters.settingsfilters.ui.SettingsFiltersFragment
import ru.practicum.android.diploma.search.domain.models.VacanciesNotFoundType
import ru.practicum.android.diploma.search.presentation.models.SearchState
import ru.practicum.android.diploma.search.presentation.viewmodel.SearchViewModel
import ru.practicum.android.diploma.util.debounce
import ru.practicum.android.diploma.vacancydetails.ui.VacancyDetailsFragment

class SearchFragment : Fragment() {
    companion object {
        private const val SEARCH_MASK = ""
        private const val CLICK_DEBOUNCE_DELAY_MILLIS = 200L
        private const val SHOW_PROGRESS_BAR_DEBOUNCE_DELAY_MILLIS = 500L
        private const val RESTART_FLAG = "restartLastSearch"
        private const val SET_SEARCH_MASK = "setSearchMask"
        fun createArgs(restartLastSearch: Boolean, searchMask: String?): Bundle =
            bundleOf(RESTART_FLAG to restartLastSearch, SET_SEARCH_MASK to searchMask)
    }

    private var _binding: FragmentSearchBinding? = null
    val binding get() = _binding!!
    private val viewModel by viewModel<SearchViewModel>()
    private val helper = SearchFragmentHelper(this)
    private var onVacancyClickDebounce: (VacancyBase) -> Unit = { _ -> }
    private val vacancySearchAdapter = VacancyAdapter { vacancy -> onVacancyClickDebounce(vacancy) }
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

        // подпишемся на результаты поиска первой страницы
        viewModel.observeState().observe(viewLifecycleOwner) { state ->
            searchStateCheck(state)
        }
        // подпишемся на результаты поиска следующих страниц
        viewModel.observeNextPageState().observe(viewLifecycleOwner) { state ->
            nextPageStateCheck(state)
        }
        // подпишемся на тост
        viewModel.observeToast().observe(viewLifecycleOwner) {
            helper.showToast(it)
        }
        // настроим слежение за объектами фрагмента
        setBindings()

        // маска может прийти аргументом
        val setSearchMask = arguments?.getString(SET_SEARCH_MASK)
        if (setSearchMask != null) {
            searchMask = setSearchMask
            binding.editTextSearchLayout.editText?.setText(searchMask)
        }

        // перезапускаем поиск, если пришел флаг
        val restartLastSearch = arguments?.getBoolean(RESTART_FLAG) ?: false
        if (restartLastSearch && searchMask.isNotEmpty()) {
            viewModel.searchByClick(searchMask)
        }
    }

    // обработка состояний поиска первой страницы
    private fun searchStateCheck(state: SearchState) {
        when (state) {
            SearchState.Default -> helper.showStartPage()

            is SearchState.Loading -> {
                helper.showLoading()
            }

            is SearchState.Content -> {
                helper.showContent(state)
                loadVacancies(state.vacancies)
            }

            is SearchState.Empty -> {
                helper.showErrorOrEmptySearch(VacanciesNotFoundType())
            }

            is SearchState.Error -> {
                helper.showErrorOrEmptySearch(state.errorType)
            }

            else -> {}
        }
    }

    // обработка состояний поиска следующих страниц
    private fun nextPageStateCheck(state: SearchState) {
        when (state) {
            is SearchState.Default -> {
                helper.showNextPagePreloader(false)
                helper.setNextPageRequest(false)
            }

            is SearchState.AtBottom -> {
                if (vacancySearchAdapter.vacancies.isNotEmpty()) {
                    helper.showNextPagePreloader(false, getString(R.string.bottom_of_list))
                }
            }

            is SearchState.Loading -> {}

            is SearchState.Content -> {
                loadVacancies(state.vacancies)
                helper.showNextPagePreloader(false)
                helper.setNextPageRequest(false)
            }

            is SearchState.Empty -> {
                helper.showErrorOrEmptySearch(VacanciesNotFoundType())
                helper.showNextPagePreloader(false)
                helper.setNextPageRequest(false)
            }

            is SearchState.Error -> {
                helper.showNextPageError(state.errorType)
            }
        }
    }

    // изменения в поле поиска
    private fun bindEditSearch() {
        // следим за изменением в поисковой строке
        binding.editTextSearchLayout.editText?.doOnTextChanged { text, _, _, _ ->
            searchMask = text.toString().trim()
            // иконка в поле поиска
            helper.setEditEndIcon(searchMask)
            if (searchMask.trim().isEmpty()) {
                viewModel.clearSearch()
                helper.showStartPage()
                helper.setNextPageRequest(true)
            } // без условия hasFocus срабатывает при возврате на фрагмент
            else if (binding.editTextSearchLayout.hasFocus()) {
                viewModel.initSearch()
                // запуск поискового запроса
                viewModel.searchDebounce(searchMask)
            }
        }

        // очистка поискового запроса кнопкой
        binding.editTextSearchLayout.setEndIconOnClickListener {
            searchMask = ""
            // иконка в поле поиска
            binding.editTextSearchLayout.endIconDrawable =
                ContextCompat.getDrawable(requireContext(), EditTextSearchIcon.SEARCH_ICON.drawableId)
            binding.editTextSearchLayout.editText?.setText(searchMask)
            viewModel.clearSearch()
            vacancySearchAdapter.vacancies.clear()
            helper.showNextPagePreloader(false)
            helper.setNextPageRequest(true)
        }

        binding.editTextSearchLayout.editText?.setOnEditorActionListener { _, actionId, _ ->
            // поиск по нажатию Done на клавиатуре
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                viewModel.deletePreviousFilters()
                viewModel.searchByClick(binding.tietSearchMask.text.toString())
            }
            false
        }
    }

    // настроим слежку за изменениями во фрагменте
    @SuppressLint("ClickableViewAccessibility")
    private fun setBindings() {
        // обработка изменений в строке поиска
        bindEditSearch()
        // мониторим скроллинг списка вакансий для загрузки новой страницы
        binding.searchResultsRV.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                // если показан нижний элемент при скролле вниз, а также не запущен тот же запрос
                if (dy > 0 && !recyclerView.canScrollVertically(1) && !helper.isNextPageRequest()) {
                    // отправим запрос следующей страницы
                    loadNextPage()
                    // прокрутим адаптер вниз, иначе лоадер закрывает последнюю вакансию текущей страницы
                    binding.searchResultsRV.smoothScrollToPosition(vacancySearchAdapter.vacancies.size)
                }
            }
        })
        // иконка кнопки фильтров
        binding.buttonFilters.setImageResource(viewModel.filtersOn().drawableId)
        // переход в фильтры
        bindOpenFilters()
    }

    private fun bindOpenFilters() {
        binding.buttonFilters.setOnClickListener {
            findNavController().navigate(
                R.id.action_searchFragment_to_filterFragment,
                SettingsFiltersFragment.createArgs(searchMask)
            )
        }
    }

    private fun loadVacancies(vacancies: MutableList<VacancyBase>) {
        vacancySearchAdapter.vacancies.clear()
        vacancySearchAdapter.vacancies.addAll(vacancies)
        vacancySearchAdapter.notifyDataSetChanged()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        arguments?.clear()
        _binding = null
    }

    private fun loadNextPage() {
        if (!helper.isNextPageRequest()) {
            helper.showNextPagePreloader(true)
            lifecycleScope.launch {
                // дадим прогресс бару покрутиться чтобы показать что процесс идет
                delay(SHOW_PROGRESS_BAR_DEBOUNCE_DELAY_MILLIS)
                // запускаем поиск следующей страницы
                viewModel.nextPageSearch()
            }
        }
    }

    private fun openVacancy(vacancy: VacancyBase) {
        findNavController().navigate(
            R.id.action_searchFragment_to_vacancyDetailsFragment,
            VacancyDetailsFragment.createArgs(vacancy.hhID)
        )
    }
}
