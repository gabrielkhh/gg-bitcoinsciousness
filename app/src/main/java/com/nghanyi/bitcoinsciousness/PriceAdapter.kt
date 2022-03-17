package com.nghanyi.bitcoinsciousness

import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class PriceAdapter(private val data: List<Price>): RecyclerView.Adapter<PriceAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = View.inflate(parent.context, R.layout.item_row, null)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val price = data[position]
        if (position == 0) {
            holder.dateTextView.text = "Date"
            holder.openTextView.text = "Open"
            holder.highTextView.text = "High"
            holder.lowTextView.text = "Low"
            holder.closeTextView.text = "Close"
            holder.volumeTextView.text = "Volume"
        } else {
            holder.dateTextView.text = price.date
            holder.openTextView.text = price.open.toString()
            holder.highTextView.text = price.high.toString()
            holder.lowTextView.text = price.low.toString()
            holder.closeTextView.text = price.close.toString()
            holder.volumeTextView.text = price.volume.toString()
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val dateTextView : TextView = itemView.findViewById(R.id.textViewDate)
        val openTextView : TextView = itemView.findViewById(R.id.textViewOpen)
        val highTextView : TextView = itemView.findViewById(R.id.textViewHigh)
        val lowTextView : TextView = itemView.findViewById(R.id.textViewLow)
        val closeTextView : TextView = itemView.findViewById(R.id.textViewClose)
        val volumeTextView : TextView = itemView.findViewById(R.id.textViewVolume)
    }

}
