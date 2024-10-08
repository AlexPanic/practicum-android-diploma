package ru.practicum.android.diploma.favorites.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.practicum.android.diploma.R
import ru.practicum.android.diploma.common.domain.VacancyBase
import ru.practicum.android.diploma.databinding.FragmentFavoriteBinding
import ru.practicum.android.diploma.favorites.presentation.models.FavouritesStates
import ru.practicum.android.diploma.favorites.presentation.viewmodel.FavouritesViewModel
import ru.practicum.android.diploma.search.ui.VacancyAdapter
import ru.practicum.android.diploma.vacancydetails.ui.VacancyDetailsFragment

class FavoriteFragment : Fragment() {

    companion object {
        private const val CLICK_DEBOUNCE_DELAY = 200L
    }

    private var _binding: FragmentFavoriteBinding? = null
    private val binding get() = _binding!!
    private val viewModelFavourites: FavouritesViewModel by viewModel()
    private var isClickAllowed = true
    private val favoriteAdapter = VacancyAdapter { vacancy -> openDetailsFragment(vacancy) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentFavoriteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRecycle()
        viewModelFavourites.getAllFavouriteVacanciesView()
        viewModelFavourites.state.observe(viewLifecycleOwner) { state ->
            when (state) {
                is FavouritesStates.NotEmpty -> {
                    favoriteAdapter.vacancies = state.vacancies
                    favoriteAdapter.notifyDataSetChanged()
                    showFavouriteVacancies()
                    hidePlaceholders()
                }

                FavouritesStates.Empty -> {
                    showEmptyPlaceholders()
                    hideFavouriteVacancies()
                }

                is FavouritesStates.Error -> {
                    showErrorPlaceholders()
                }
            }
        }
    }

    private fun showErrorPlaceholders() {
        binding.failedToGetVacancies.isVisible = true
        binding.imageNothingFoundFavorite.isVisible = true
    }

    private fun showEmptyPlaceholders() {
        binding.imageEmptyFavorite.isVisible = true
        binding.emptyListFavorite.isVisible = true
    }

    private fun hidePlaceholders() {
        binding.imageEmptyFavorite.isVisible = false
        binding.emptyListFavorite.isVisible = false
        binding.failedToGetVacancies.isVisible = false
        binding.imageNothingFoundFavorite.isVisible = false
    }

    private fun showFavouriteVacancies() {
        binding.favoriteRV.isVisible = true
    }

    private fun hideFavouriteVacancies() {
        binding.favoriteRV.isVisible = false
    }

    private fun initRecycle() {
        val recycleFavourites = binding.favoriteRV
        recycleFavourites.adapter = favoriteAdapter
        recycleFavourites.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
    }

    private fun openDetailsFragment(vacancy: VacancyBase) {
        if (clickDebounce()) {
            findNavController().navigate(
                R.id.action_favoritesFragment_to_vacancyDetailsFragment,
                VacancyDetailsFragment.createArgs(vacancy.hhID)
            )
        }
    }

    private fun clickDebounce(): Boolean {
        val current = isClickAllowed
        if (isClickAllowed) {
            isClickAllowed = false
            lifecycleScope.launch {
                delay(CLICK_DEBOUNCE_DELAY)
                isClickAllowed = true
            }
        }
        return current
    }
}
