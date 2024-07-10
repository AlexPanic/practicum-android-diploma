package ru.practicum.android.diploma.search.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ru.practicum.android.diploma.common.domain.ErrorType
import ru.practicum.android.diploma.common.domain.Success
import ru.practicum.android.diploma.common.domain.VacancyBase
import ru.practicum.android.diploma.search.domain.api.SearchInteractor
import ru.practicum.android.diploma.search.domain.models.SearchResult
import ru.practicum.android.diploma.search.domain.models.VacancyNotFoundType
import ru.practicum.android.diploma.search.presentation.models.SearchState
import ru.practicum.android.diploma.util.SingleLiveEvent

class SearchViewModel(
    private val interactor: SearchInteractor,
) : ViewModel() {
    companion object {
        private const val SEARCH_DEBOUNCE_DELAY = 5000L
        private const val ITEMS_PER_PAGE = 5
    }

    private var latestSearchText: String? = null
    private var searchJob: Job? = null

    private val _toast = SingleLiveEvent<String>()
    fun observeToast(): LiveData<String> = _toast

    private val _state = MutableLiveData<SearchState>()
    fun observeState(): LiveData<SearchState> = _state

    fun searchDebounce(changedText: String, page: Int) {
        if (changedText.isEmpty() || latestSearchText == changedText) {
            return
        }
        latestSearchText = changedText
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(SEARCH_DEBOUNCE_DELAY)
            searchRequest(changedText, page)
        }
    }

    private fun searchRequest(newSearchText: String, page: Int) {
        if (newSearchText.isNotEmpty()) {
            renderState(SearchState.Loading)
            viewModelScope.launch {
                interactor.findVacancies(newSearchText, page, ITEMS_PER_PAGE)
                    .collect { pair ->
                        processResult(pair.first, pair.second)
                    }
            }
        }
    }

    private fun processResult(found: SearchResult?, errorType: ErrorType) {
        val vacancies = mutableListOf<VacancyBase>()
        if (found != null) {
            vacancies.addAll(found.vacancies)
        }
        when (errorType) {
            is Success -> {
                renderState(
                    SearchState.Content(vacancies)
                )
            }

            is VacancyNotFoundType -> {
                renderState(
                    SearchState.Empty
                )
            }

            else -> {
                renderState(
                    SearchState.Error(errorType)
                )
            }

        }
    }

    private fun renderState(state: SearchState) {
        _state.postValue(state)
    }
}
