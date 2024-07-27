package ru.practicum.android.diploma.search.ui

import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import ru.practicum.android.diploma.R
import ru.practicum.android.diploma.common.domain.ErrorType
import ru.practicum.android.diploma.common.domain.NoInternetError
import ru.practicum.android.diploma.common.presentation.EditTextSearchIcon
import ru.practicum.android.diploma.search.domain.models.VacanciesNotFoundType
import ru.practicum.android.diploma.search.presentation.models.SearchState
import ru.practicum.android.diploma.util.getCountableVacancies

class SearchFragmentHelper(
    val fragment: SearchFragment
) {

    private var nextPageRequestSending = false

    fun isNextPageRequest(): Boolean {
        return nextPageRequestSending
    }

    fun setNextPageRequest(newVal: Boolean) {
        nextPageRequestSending = newVal
    }

    fun showLoading() {
        with(fragment.binding) {
            searchProgressBar.isVisible = true
            placeHolderImage.isVisible = false
            placeHolderText.isVisible = false
            searchResultsRV.isVisible = false
            vacanciesCountText.isVisible = false
        }
        hideKeyboard()
    }

    fun showStartPage() {
        with(fragment.binding) {
            placeHolderImage.isVisible = true
            placeHolderImage.setImageResource(R.drawable.image_search_empty)
            searchProgressBar.isVisible = false
            placeHolderText.isVisible = false
            searchResultsRV.isVisible = false
            vacanciesCountText.isVisible = false
        }
        showNextPagePreloader(false)
        nextPageRequestSending = false
    }

    fun showContent(state: SearchState.Content) {
        with(fragment.binding) {
            searchProgressBar.isVisible = false
            vacanciesCountText.isVisible = true
            vacanciesCountText.text = getCountableVacancies(state.found, fragment.resources)
            placeHolderImage.isVisible = false
            placeHolderText.isVisible = false
            searchResultsRV.isVisible = true
        }
        nextPageRequestSending = false
    }

    fun showErrorOrEmptySearch(type: ErrorType) {
        hideKeyboard()

        when (type) {
            is VacanciesNotFoundType -> {
                with(fragment.binding) {
                    vacanciesCountText.isVisible = true
                    vacanciesCountText.text = fragment.getString(R.string.no_vacancies)
                    placeHolderImage.setImageResource(R.drawable.image_nothing_found)
                }
            }

            is NoInternetError -> {
                with(fragment.binding) {
                    vacanciesCountText.isVisible = false
                    placeHolderImage.setImageResource(R.drawable.image_no_internet_error)
                }
            }

            else -> {
                with(fragment.binding) {
                    vacanciesCountText.isVisible = false
                    placeHolderImage.setImageResource(R.drawable.image_search_server_error)
                }
            }
        }
        with(fragment.binding) {
            placeHolderText.text = getErrorMessage(type)
            searchResultsRV.isVisible = false
            searchProgressBar.isVisible = false
            placeHolderImage.isVisible = true
            placeHolderText.isVisible = true
        }
    }

    private fun getErrorMessage(type: ErrorType, isNextPage: Boolean = false): String {
        return if (isNextPage) {
            when (type) {
                is NoInternetError -> fragment.getString(R.string.no_internet_next_page)
                else -> fragment.getString(R.string.server_error_next_page)
            }
        } else {
            when (type) {
                is VacanciesNotFoundType -> fragment.getString(R.string.no_vacancies)
                is NoInternetError -> fragment.getString(R.string.no_internet)
                else -> fragment.getString(R.string.server_error)
            }
        }
    }

    fun showNextPagePreloader(show: Boolean, message: String? = null) {
        fragment.binding.searchNewItemsProgressBar.isVisible = show
        if (show) {
            fragment.binding.searchResultsRV.scrollTo(0, fragment.binding.searchResultsRV.bottom)
            nextPageRequestSending = true
        }
        if (message != null) {
            showToast(message)
        }
    }

    fun showNextPageError(type: ErrorType) {
        val errorMessage = getErrorMessage(type, isNextPage = true)
        showNextPagePreloader(false, errorMessage)
        nextPageRequestSending = false
    }

    fun setEditEndIcon(searchMask: String?) {
        fragment.binding.editTextSearchLayout.endIconDrawable = ContextCompat.getDrawable(
            fragment.requireContext(),
            if (searchMask?.isEmpty() == true) {
                EditTextSearchIcon.SEARCH_ICON.drawableId
            } else {
                EditTextSearchIcon.CLEAR_ICON.drawableId
            }
        )
    }

    fun showToast(toastMessage: String) {
        Toast.makeText(fragment.requireContext(), toastMessage, Toast.LENGTH_SHORT).show()
    }

    fun hideKeyboard() {
        val view: View? = fragment.activity?.currentFocus
        if (view != null) {
            val inputMethodManager =
                fragment.activity?.getSystemService(AppCompatActivity.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }
}
