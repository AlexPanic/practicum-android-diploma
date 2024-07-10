package ru.practicum.android.diploma.search.presentation.view

import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import ru.practicum.android.diploma.R
import ru.practicum.android.diploma.common.domain.VacancyBase
import ru.practicum.android.diploma.databinding.ItemVacancyViewBinding
import ru.practicum.android.diploma.util.Formatter

class VacancySearchViewHolder(
    private val clickListener: VacancySearchAdapter.SearchClickListener,
    private val binding: ItemVacancyViewBinding,
) : RecyclerView.ViewHolder(binding.root) {

    private val trackCornerRadius: Int = itemView.context.resources.getDimensionPixelSize(R.dimen.logo_corner_radius)

    fun bind(item: VacancyBase) {
        binding.vacancyNameAndCity.text = "${item.name}, ${item.employerInfo.areaName}"
        binding.companyName.text = item.employerInfo.employerName
        binding.salaryText.text = Formatter.formatSalary(itemView.context, item.salaryInfo)
        Glide.with(itemView).load(item.employerInfo.employerLogoUrl).placeholder(R.drawable.logo_placeholder_image)
            .transform(CenterCrop(), RoundedCorners(trackCornerRadius)).into(binding.vacancyLogo)

        itemView.setOnClickListener { clickListener.onVacancyClick(item) }
    }
}
