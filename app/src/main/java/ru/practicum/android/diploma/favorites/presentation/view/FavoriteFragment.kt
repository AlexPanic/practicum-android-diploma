package ru.practicum.android.diploma.favorites.presentation.view

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import ru.practicum.android.diploma.vacancydetails.presentation.view.VacancyDetailsFragment

private const val CLICK_DEBOUNCE_DELAY = 1000L

class FavoriteFragment : Fragment() {

    private var _binding: FragmentFavoriteBinding? = null
    private val binding get() = _binding!!
    private val viewModelFavourites: FavouritesViewModel by viewModel()
    private var isClickAllowed = true
    private val favoriteAdapter = FavoriteAdapter { vacancy -> openDetailsFragment(vacancy) }

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
                    favoriteAdapter.favoriteItems = state.vacancies
                    favoriteAdapter.notifyDataSetChanged()
                    showFavouriteVacancies()
                    hidePlaceholders()
                }

                FavouritesStates.Empty -> {
                    showEmptyPlaceholders()
                }

                is FavouritesStates.Error -> {
                    showErrorPlaceholders()
                }
            }
        }
    }

    private fun showErrorPlaceholders() {
        binding.failedToGetVacancies.visibility = View.VISIBLE
        binding.imageNothingFoundFavorite.visibility = View.VISIBLE
    }

    private fun showEmptyPlaceholders() {
        binding.imageEmptyFavorite.visibility = View.VISIBLE
        binding.emptyListFavorite.visibility = View.VISIBLE
    }

    private fun hidePlaceholders() {
        binding.imageEmptyFavorite.visibility = View.GONE
        binding.emptyListFavorite.visibility = View.GONE
        binding.failedToGetVacancies.visibility = View.GONE
        binding.imageNothingFoundFavorite.visibility = View.GONE
    }

    private fun showFavouriteVacancies() {
        binding.favoriteRV.visibility = View.VISIBLE
    }

    private fun initRecycle() {
        val recycleFavourites = binding.favoriteRV
        recycleFavourites.adapter = favoriteAdapter
        recycleFavourites.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
    }

    private fun openDetailsFragment(vacancy: VacancyBase) {
        val context = requireContext()
        if (clickDebounce() && isInternetAvailable(context)) {
            findNavController().navigate(
                R.id.action_favoritesFragment_to_vacancyDetailsFragment,
                VacancyDetailsFragment.createArgs(vacancy.hhID)
            )
        }
    }

    private fun isInternetAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork
        val activeNetwork = network?.let { connectivityManager.getNetworkCapabilities(it) }
        return activeNetwork?.run {
            hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
        } ?: false
    }

    private fun clickDebounce(): Boolean {
        val current = isClickAllowed
        if (isClickAllowed) {
            isClickAllowed = false
            viewLifecycleOwner.lifecycleScope.launch {
                delay(CLICK_DEBOUNCE_DELAY)
                isClickAllowed = true
            }
        }
        return current
    }
}
