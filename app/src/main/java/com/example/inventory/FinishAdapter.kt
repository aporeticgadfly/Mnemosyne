package com.example.inventory

import android.content.Context
import android.media.Image
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.util.ArrayList

class FinishAdapter(
    private val context: Context,
    private val correct: java.util.ArrayList<String>?,
    private val wrong: ArrayList<String>?
) : RecyclerView.Adapter<FinishAdapter.ItemViewHolder>() {

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder.
    // Each data item is just an Affirmation object.
    class ItemViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {
        val textView: TextView = view.findViewById(R.id.finishitem)
        val imageView: ImageView = view.findViewById(R.id.finishimage)
    }

    /**
     * Create new views (invoked by the layout manager)
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        // create a new view
        val adapterLayout = LayoutInflater.from(parent.context)
            .inflate(R.layout.finishitem, parent, false)

        return ItemViewHolder(adapterLayout)
    }

    /**
     * Replace the contents of a view (invoked by the layout manager)
     */
    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        if (wrong != null && correct != null) {
            if (position < wrong.size) {
                holder.textView.setText(wrong[position])
                holder.imageView.setImageResource(R.drawable.wrong)
            } else {
                holder.textView.setText(correct[position - wrong.size])
                holder.imageView.setImageResource(R.drawable.check)
            }
        }

        //CHANGE IMAGEVIEWS
    }

    /**
     * Return the size of your dataset (invoked by the layout manager)
     */
    override fun getItemCount(): Int {
        if (wrong != null && correct != null) {
            return correct.size + wrong.size
        }
        else {
            return 0
        }
    }
}