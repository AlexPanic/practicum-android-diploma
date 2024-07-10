package ru.practicum.android.diploma.favorites.presentation.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ru.practicum.android.diploma.common.domain.VacancyBase
import ru.practicum.android.diploma.databinding.ItemFavoriteViewBinding

class FavoriteAdapter(
    private val favoriteClickListener: FavoriteClickListener,
) : RecyclerView.Adapter<FavoriteViewHolder>() {

    var favoriteItems: ArrayList<VacancyBase> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriteViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return FavoriteViewHolder(
            favoriteClickListener,
            ItemFavoriteViewBinding.inflate(layoutInflater, parent, false)
        )
    }

    override fun onBindViewHolder(holder: FavoriteViewHolder, position: Int) {
        holder.bind(favoriteItems[position])
    }

    override fun getItemCount(): Int {
        return favoriteItems.size
    }

    fun interface FavoriteClickListener {
        fun onFavoriteClick(item: VacancyBase)
    }

}
