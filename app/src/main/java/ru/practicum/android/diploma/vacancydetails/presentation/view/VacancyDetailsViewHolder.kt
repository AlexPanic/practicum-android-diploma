package ru.practicum.android.diploma.vacancydetails.presentation.view

import android.text.Html
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import ru.practicum.android.diploma.R
import ru.practicum.android.diploma.R.drawable.logo_placeholder_image
import ru.practicum.android.diploma.databinding.ItemVacancyDetailsViewBinding
import ru.practicum.android.diploma.util.Formatter
import ru.practicum.android.diploma.vacancydetails.domain.models.VacancyDetails

class VacancyDetailsViewHolder(private val binding: ItemVacancyDetailsViewBinding) :
    RecyclerView.ViewHolder(binding.root) {

    private val trackCornerRadius: Int = itemView.context.resources.getDimensionPixelSize(R.dimen.logo_corner_radius)

    fun bind(vacancy: VacancyDetails) {
        binding.nameVacancyTv.text = "${vacancy.name}, ${vacancy.employerInfo.areaName}"
        binding.nameCompanyTv.text = vacancy.employerInfo.employerName
        binding.adressCompanyTv.text = vacancy.details.address?.city ?: vacancy.employerInfo.areaName
        binding.experienceTv.text = vacancy.details.experience?.name ?: ""
        binding.formatWorkTv.text = "${vacancy.details.employment?.name ?: ""}, ${vacancy.details.schedule?.name ?: ""}"

        binding.vacancySalaryTv.text = Formatter.formatSalary(itemView.context, vacancy.salaryInfo)

        Glide.with(itemView)
            .load(vacancy.employerInfo.employerLogoUrl)
            .placeholder(logo_placeholder_image)
            .error(logo_placeholder_image)
            .transform(CenterCrop(), RoundedCorners(trackCornerRadius))
            .into(binding.logoCompanyIv)

        binding.vacancyResponsibilitiesTv.text =
            Html.fromHtml(vacancy.details.description, Html.FROM_HTML_MODE_LEGACY)
        binding.vacancyRequirementsTv.text =
            Html.fromHtml(vacancy.details.keySkill.joinToString("\n• ", "• "), Html.FROM_HTML_MODE_LEGACY)
        binding.vacancyConditionsTv.text =
            Html.fromHtml(vacancy.details.description, Html.FROM_HTML_MODE_LEGACY)
        binding.vacancyKeySkillsTv.text =
            Html.fromHtml(vacancy.details.keySkill.joinToString("\n• ", "• "), Html.FROM_HTML_MODE_LEGACY)

        if (vacancy.details.keySkill.isNotEmpty()) {
            binding.vacancyKeySkillsTv.visibility = View.VISIBLE
            binding.keySkills.visibility = View.VISIBLE

        } else {
            binding.vacancyKeySkillsTv.visibility = View.GONE
            binding.keySkills.visibility = View.GONE
        }

        if (vacancy.details.contacts != null) {
            binding.vacancyContactsTv.visibility = View.VISIBLE
            binding.contacts.visibility = View.VISIBLE
            binding.vacancyContactsTv.text = vacancy.details.contacts.email
            val phoneComment = vacancy.details.contacts.phone?.firstOrNull()?.comment
            if (!phoneComment.isNullOrEmpty()) {
                binding.vacancyContactsCommentTv.visibility = View.VISIBLE
                binding.vacancyContactsCommentTv.text = phoneComment
            } else {
                binding.vacancyContactsCommentTv.visibility = View.GONE
            }
        } else {
            binding.vacancyContactsTv.visibility = View.GONE
            binding.contacts.visibility = View.GONE
            binding.vacancyContactsCommentTv.visibility = View.GONE
        }
    }
}
