// PrayerTimesAdapter.kt
package com.example.qiblacallbeta

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class PrayerTimesAdapter(private val prayerTimesList: List<PrayerTimeItem>) :
    RecyclerView.Adapter<PrayerTimesAdapter.PrayerTimeViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PrayerTimeViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.prayer_time_item, parent, false)
        return PrayerTimeViewHolder(view)
    }

    override fun onBindViewHolder(holder: PrayerTimeViewHolder, position: Int) {
        val prayerTimeItem = prayerTimesList[position]
        holder.prayerName.text = prayerTimeItem.name
        holder.prayerTime.text = prayerTimeItem.time
    }

    override fun getItemCount(): Int {
        return prayerTimesList.size
    }

    class PrayerTimeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val prayerName: TextView = itemView.findViewById(R.id.prayerNameItem)
        val prayerTime: TextView = itemView.findViewById(R.id.prayerTimeItem)
    }
}
