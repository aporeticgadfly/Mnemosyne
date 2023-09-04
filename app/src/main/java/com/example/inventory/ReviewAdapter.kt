package com.example.inventory

import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.inventory.data.ListItemItem
import java.util.ArrayList

class ReviewAdapter(
    private val context: Context,
    private val items: MutableList<ListItemItem>,
    private val percentages: MutableList<Double>?
) : RecyclerView.Adapter<ReviewAdapter.ItemViewHolder>() {

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder.
    // Each data item is just an Affirmation object.
    class ItemViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {
        val firstText: TextView = view.findViewById(R.id.first_text)
        val progress: ProgressBar = view.findViewById(R.id.indeterminateBar)
        val percentage: TextView = view.findViewById(R.id.percentage)
    }

    /**
     * Create new views (invoked by the layout manager)
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        // create a new view
        val adapterLayout = LayoutInflater.from(parent.context)
            .inflate(R.layout.session_history, parent, false)

        return ItemViewHolder(adapterLayout)
    }

    /**
     * Replace the contents of a view (invoked by the layout manager)
     */
    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        if (items != null && percentages != null && items.size != 0 && percentages.size != 0) {
            holder.firstText.text = items[position].text
            holder.progress.progress = percentages[position].toInt()
            holder.percentage.text = percentages[position].toInt().toString() + "%"
        }
    }

    /**
     * Return the size of your dataset (invoked by the layout manager)
     */
    override fun getItemCount(): Int {
        if (items != null) {
            return items.size
        }
        return 0
    }

    fun updateData(newItems: MutableList<ListItemItem>, newPercentages: MutableList<Double>) {
        items?.clear()
        items?.addAll(newItems)

        percentages?.clear()
        percentages?.addAll(newPercentages)

        notifyDataSetChanged()
    }
}