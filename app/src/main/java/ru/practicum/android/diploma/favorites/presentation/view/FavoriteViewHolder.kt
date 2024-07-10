package ru.practicum.android.diploma.favorites.presentation.view

import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import ru.practicum.android.diploma.R
import ru.practicum.android.diploma.common.domain.VacancyBase
import ru.practicum.android.diploma.databinding.ItemFavoriteViewBinding
import ru.practicum.android.diploma.util.Formatter

class FavoriteViewHolder(
    private val clickListener: FavoriteAdapter.FavoriteClickListener,
    private val binding: ItemFavoriteViewBinding,
) : RecyclerView.ViewHolder(binding.root) {

    private val trackCornerRadius: Int =
        itemView.context.resources.getDimensionPixelSize(R.dimen.logo_corner_radius)

    private val stringBuilderNameAndCity = StringBuilder()

    fun bind(item: VacancyBase) {
        binding.titleFavoriteItem.text = stringBuilderNameAndCity
            .append(item.name + ", " + item.employerInfo.areaName)
        binding.subtitleFavoriteItem.text = item.employerInfo.employerName
        binding.priceFavoriteItem.text = Formatter.formatSalary(itemView.context, item.salaryInfo)

        Glide.with(itemView)
            .load(item.employerInfo.employerLogoUrl)
            .placeholder(R.drawable.logo_placeholder_image)
            .transform(CenterCrop(), RoundedCorners(trackCornerRadius))
            .into(binding.imageItemFavorite)

        itemView.setOnClickListener { clickListener.onFavoriteClick(item) }
    }
}
