package ru.practicum.android.diploma.vacancydetails.presentation.view

import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.practicum.android.diploma.R
import ru.practicum.android.diploma.common.data.BadRequestError
import ru.practicum.android.diploma.common.data.ErrorType
import ru.practicum.android.diploma.common.data.NoInternetError
import ru.practicum.android.diploma.databinding.FragmentVacancyDetailsBinding
import ru.practicum.android.diploma.databinding.ItemVacancyDetailsViewBinding
import ru.practicum.android.diploma.util.Formatter
import ru.practicum.android.diploma.vacancydetails.domain.models.DetailsNotFoundType
import ru.practicum.android.diploma.vacancydetails.presentation.models.DetailsState
import ru.practicum.android.diploma.vacancydetails.presentation.viewmodel.DetailsViewModel

class VacancyDetailsFragment : Fragment() {

    private var _binding: FragmentVacancyDetailsBinding? = null
    private val binding get() = _binding!!
    private val viewModel by viewModel<DetailsViewModel>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentVacancyDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val vacancyId = requireArguments().getString(ARGS_VACANCY_ID)
        if (vacancyId != null) {
            viewModel.getVacancy(vacancyId)
        }
        viewModel.observeVacancyState().observe(viewLifecycleOwner) { state ->
            when (state) {
                is DetailsState.Content -> {
                    binding.testVac.text = Html.fromHtml(
                        "${state.vacancy.name} ${state.vacancy.details}",
                        Html.FROM_HTML_MODE_LEGACY
                    )
                    val vacancyDetailsBinding = ItemVacancyDetailsViewBinding.bind(binding.root)
                    vacancyDetailsBinding.nameVacancyTv.text = Html.fromHtml(
                        "${state.vacancy.name}," +
                            " ${state.vacancy.employerInfo.areaName}",
                        Html.FROM_HTML_MODE_LEGACY
                    )
                    vacancyDetailsBinding.nameCompanyTv.text = Html.fromHtml(
                        state.vacancy.employerInfo.employerName,
                        Html.FROM_HTML_MODE_LEGACY
                    )
                    vacancyDetailsBinding.adressCompanyTv.text = Html.fromHtml(
                        state.vacancy.employerInfo.areaName,
                        Html.FROM_HTML_MODE_LEGACY
                    )
                    vacancyDetailsBinding.vacancySalaryTv.text = Html.fromHtml(
                        Formatter.formatSalary(requireContext(), state.vacancy.salaryInfo),
                        Html.FROM_HTML_MODE_LEGACY
                    )
                    vacancyDetailsBinding.vacancyKeySkillsTv.text = Html.fromHtml(
                        state.vacancy.details.keySkill.toString(),
                        Html.FROM_HTML_MODE_LEGACY
                    )
                    vacancyDetailsBinding.vacancyResponsibilitiesTv.text = Html.fromHtml(
                        state.vacancy.details.description,
                        Html.FROM_HTML_MODE_LEGACY
                    )
                    val trackCornerRadius: Int = resources.getDimensionPixelSize(R.dimen.logo_corner_radius)
                    Glide.with(view)
                        .load(state.vacancy.employerInfo.employerLogoUrl)
                        .placeholder(R.drawable.logo_placeholder_image)
                        .transform(CenterCrop(), RoundedCorners(trackCornerRadius))
                        .into(vacancyDetailsBinding.logoCompanyIv)
                    binding.detailsProgressBar.isVisible = false
                    binding.itemVacancyDetails.itemVacancyDetailsDescription.isVisible = true
                }

                is DetailsState.Error -> {
                    showTypeErrorOrEmpty(state.errorType)
                }

                is DetailsState.Empty -> {
                    showTypeErrorOrEmpty(DetailsNotFoundType())
                }

                is DetailsState.Loading -> {
                    showLoading()
                }
            }

        }
        // showToast("vacancyId=$vacancyId")
        binding.favoriteVacansyIv.setOnClickListener {}

        binding.arrowBackIv.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun showTypeErrorOrEmpty(type: ErrorType) {
        binding.detailsProgressBar.isVisible = false
        binding.errorPlaceholderCl.isVisible = true
        binding.itemVacancyDetails.itemVacancyDetailsDescription.isVisible = false
        when (type) {
            is BadRequestError -> {
                binding.errorPlaceholderTv.isVisible = true
                binding.errorPlaceholderTv.text = getString(R.string.server_error)

                binding.errorPlaceholderIv.isVisible = true
                binding.errorPlaceholderIv.setImageResource(R.drawable.image_vacancy_server_error)
            }
            is NoInternetError -> {
                binding.errorPlaceholderTv.isVisible = true
                binding.errorPlaceholderTv.text = getString(R.string.no_internet)

                binding.errorPlaceholderIv.isVisible = true
                binding.errorPlaceholderIv.setImageResource(R.drawable.image_no_internet_error)
            }
            else -> {
                binding.errorPlaceholderTv.isVisible = true
                binding.errorPlaceholderTv.text = getString(R.string.not_found_or_deleted_vacancy)

                binding.errorPlaceholderIv.isVisible = true
                binding.errorPlaceholderIv.setImageResource(R.drawable.image_vacancy_not_found_error)
            }
        }
    }

    private fun showLoading() {
        binding.itemVacancyDetails.itemVacancyDetailsDescription.isVisible = false
        binding.detailsProgressBar.isVisible = true
        binding.errorPlaceholderIv.isVisible = false
        binding.errorPlaceholderTv.isVisible = false
    }

    companion object {
        private const val ARGS_VACANCY_ID = "vacancyID"

        fun createArgs(vacancyID: String): Bundle =
            bundleOf(ARGS_VACANCY_ID to vacancyID)
    }
}
