package ru.practicum.android.diploma.search.presentation.view

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.widget.NestedScrollView
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.practicum.android.diploma.R
import ru.practicum.android.diploma.common.data.ErrorType
import ru.practicum.android.diploma.common.data.NoInternetError
import ru.practicum.android.diploma.common.domain.VacancyBase
import ru.practicum.android.diploma.databinding.FragmentSearchBinding
import ru.practicum.android.diploma.search.domain.models.VacanciesNotFoundType
import ru.practicum.android.diploma.search.presentation.models.SearchState
import ru.practicum.android.diploma.search.presentation.viewmodel.SearchViewModel
import ru.practicum.android.diploma.util.getCountableVacancies
import ru.practicum.android.diploma.vacancydetails.presentation.view.VacancyDetailsFragment

class SearchFragment : Fragment() {

    companion object {
        private const val SEARCH_MASK = ""
    }

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!
    private val viewModel by viewModel<SearchViewModel>()

    private val vacancySearchAdapter = VacancySearchAdapter { vacancy -> openVacancy(vacancy) }

    private var currentPage = 0
    private var totalPages = 0
    private var searchMask = SEARCH_MASK

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.searchResultsRV.adapter = vacancySearchAdapter

        showEmptySearch()

        viewModel.observeState().observe(viewLifecycleOwner) { state ->
            when (state) {
                is SearchState.Content -> {
                    showContent(state)
                }

                is SearchState.Loading -> {
                    showLoading()
                }

                is SearchState.Empty -> {
                    showErrorOrEmptySearch(VacanciesNotFoundType())
                }

                is SearchState.Error -> {
                    showErrorOrEmptySearch(state.errorType)
                }

                else -> {}
            }
        }

        doBindings()
    }

    private fun doBindings() {
        binding.editTextSearch.doOnTextChanged { text, start, before, count ->
            searchMask = text.toString()
            if (searchMask.isNotEmpty()) {
                changeDrawableClearText(binding.editTextSearch)
                viewModel.searchDebounce(searchMask, 0)
            } else {
                changeDrawableSearchIcon(binding.editTextSearch)
                viewModel.stopSearch()
            }
        }

        binding.editTextSearch.setOnTouchListener { v, event ->
            v.performClick()
            if (event.action == MotionEvent.ACTION_UP &&
                event.rawX >= binding.editTextSearch.right - binding.editTextSearch.compoundDrawables[2].bounds.width()
            ) {
                changeDrawableSearchIcon(binding.editTextSearch)
                binding.editTextSearch.setText(SEARCH_MASK)
                showEmptySearch()
                true
            }
            false
        }

        binding.idNestedSV.setOnScrollChangeListener(
            NestedScrollView.OnScrollChangeListener { v, scrollX, scrollY, oldScrollX, oldScrollY ->
                if (scrollY == v.getChildAt(0).measuredHeight - v.measuredHeight
                    && !binding.searchNewItemsProgressBar.isVisible
                ) {
                    loadNextPage()
                }
            }
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

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

    private fun showEmptySearch() {
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
        val vacancies = state.vacancies
        with(binding) {
            searchProgressBar.isVisible = false
            vacanciesCountText.isVisible = true
            vacanciesCountText.text = getCountableVacancies(state.found, resources)
            placeHolderImage.isVisible = false
            placeHolderText.isVisible = false
            searchResultsRV.isVisible = true
        }
        currentPage = state.page
        totalPages = state.pages
        if (currentPage > 0) {
            binding.searchNewItemsProgressBar.isVisible = false
            for (newVac in vacancies) {
                vacancySearchAdapter.vacancies.add(newVac)
                vacancySearchAdapter.notifyItemInserted(vacancySearchAdapter.vacancies.size - 1)
            }
        } else {
            vacancySearchAdapter.vacancies.clear()
            vacancySearchAdapter.vacancies.addAll(vacancies)
            vacancySearchAdapter.notifyDataSetChanged()
        }
    }

    private fun loadNextPage() {
        if (currentPage + 1 == totalPages) {
            showToast(getString(R.string.bottom_of_list))
        } else {
            hideKeyboard()
            binding.searchNewItemsProgressBar.isVisible = true
            viewModel.searchDebounce(searchMask, currentPage + 1)
        }
    }

    private fun showErrorOrEmptySearch(type: ErrorType) {
        hideKeyboard()
        with(binding) {
            searchResultsRV.isVisible = false
            searchProgressBar.isVisible = false
            placeHolderImage.isVisible = true
            placeHolderText.isVisible = true
        }
        when (type) {
            is VacanciesNotFoundType -> {
                with(binding) {
                    vacanciesCountText.isVisible = true
                    vacanciesCountText.text = getString(R.string.no_vacancies)
                    placeHolderImage.setImageResource(R.drawable.image_nothing_found)
                    placeHolderText.text = getString(R.string.failed_to_get_vacancies)
                }
            }

            is NoInternetError -> {
                with(binding) {
                    vacanciesCountText.isVisible = false
                    placeHolderImage.setImageResource(R.drawable.image_no_internet_error)
                    placeHolderText.text = getString(R.string.no_internet)
                }
            }

            else -> {
                with(binding) {
                    vacanciesCountText.isVisible = false
                    placeHolderImage.setImageResource(R.drawable.image_search_server_error)
                    placeHolderText.text = getString(R.string.server_error)
                }
            }
        }

    }

    private fun changeDrawableClearText(editText: EditText) {
        // Меняем иконку на другую
        val newIcon = ContextCompat.getDrawable(requireContext(), R.drawable.clear_24px_input_edittext_button)
        newIcon?.setBounds(0, 0, newIcon.intrinsicWidth, newIcon.intrinsicHeight)
        editText.setCompoundDrawables(null, null, newIcon, null) // Устанавливаем новую иконку справа
    }

    private fun changeDrawableSearchIcon(editText: EditText) {
        // Меняем иконку на другую
        val newIcon = ContextCompat.getDrawable(requireContext(), R.drawable.search_24px_input_edittext_icon)
        newIcon?.setBounds(0, 0, newIcon.intrinsicWidth, newIcon.intrinsicHeight)
        editText.setCompoundDrawables(null, null, newIcon, null) // Устанавливаем новую иконку справа
    }

    private fun openVacancy(vacancy: VacancyBase) {
        findNavController().navigate(
            R.id.action_searchFragment_to_vacancyDetailsFragment,
            VacancyDetailsFragment.createArgs(vacancy.hhID)
        )
    }

    private fun showToast(additionalMessage: String) {
        Toast.makeText(requireContext(), additionalMessage, Toast.LENGTH_SHORT).show()
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
