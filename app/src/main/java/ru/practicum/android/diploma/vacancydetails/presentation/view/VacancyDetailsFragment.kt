package ru.practicum.android.diploma.vacancydetails.presentation.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import ru.practicum.android.diploma.databinding.FragmentVacancyDetailsBinding
import ru.practicum.android.diploma.vacancydetails.presentation.viewmodel.DetailsViewModel
import ru.practicum.android.diploma.vacancydetails.presentation.models.DetailsState

class VacancyDetailsFragment : Fragment() {

    private var _binding: FragmentVacancyDetailsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: DetailsViewModel by viewModel { parametersOf(arguments?.getString(ARGS_VACANCY_ID)) }
    private lateinit var adapter: VacancyDetailsAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentVacancyDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeViewModel()

        viewModel.getVacancy()
    }

    private fun setupRecyclerView() {
        adapter = VacancyDetailsAdapter(emptyList())
        binding.vacancyDetailsRV.layoutManager = LinearLayoutManager(context)
        binding.vacancyDetailsRV.adapter = adapter
    }

    private fun observeViewModel() {
        viewModel.observeVacancyState().observe(viewLifecycleOwner) { state ->
            when (state) {
                is DetailsState.Loading -> {
                    binding.detailsProgressBar.visibility = View.VISIBLE
                    binding.vacancyDetailsRV.visibility = View.GONE
                    binding.imageVacancyNotFoundError.visibility = View.GONE
                    binding.notFoundOrDeletedVacancy.visibility = View.GONE
                }

                is DetailsState.Content -> {
                    binding.detailsProgressBar.visibility = View.GONE
                    binding.vacancyDetailsRV.visibility = View.VISIBLE
                    adapter = VacancyDetailsAdapter(listOf(state.vacancy))
                    binding.vacancyDetailsRV.adapter = adapter
                }

                is DetailsState.Error -> {
                    binding.detailsProgressBar.visibility = View.GONE
                    binding.vacancyDetailsRV.visibility = View.GONE
                    binding.imageVacancyNotFoundError.visibility = View.VISIBLE
                    binding.notFoundOrDeletedVacancy.visibility = View.VISIBLE
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARGS_VACANCY_ID = "vacancyID"

        fun createArgs(vacancyID: String): Bundle =
            bundleOf(ARGS_VACANCY_ID to vacancyID)
    }
}
