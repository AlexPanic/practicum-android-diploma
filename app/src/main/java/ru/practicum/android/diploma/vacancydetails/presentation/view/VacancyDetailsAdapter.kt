package ru.practicum.android.diploma.vacancydetails.presentation.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ru.practicum.android.diploma.databinding.ItemVacancyDetailsViewBinding
import ru.practicum.android.diploma.vacancydetails.domain.models.VacancyDetails

class VacancyDetailsAdapter(private val vacancies: List<VacancyDetails>) :
    RecyclerView.Adapter<VacancyDetailsViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VacancyDetailsViewHolder {
        val binding = ItemVacancyDetailsViewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VacancyDetailsViewHolder(binding)
    }

    override fun onBindViewHolder(holder: VacancyDetailsViewHolder, position: Int) {
        val vacancy = vacancies[position]
        holder.bind(vacancy)
    }

    override fun getItemCount(): Int {
        return vacancies.size
    }
}
