package com.example.inventory

import android.content.Context
import android.graphics.Color
import android.media.Image
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.inventory.data.ListItemItem
import java.util.ArrayList

class FinishAdapter(
    private val context: Context,
    private val correct: MutableList<ListItemItem>?,
    private val wrong: MutableList<ListItemItem>?
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
                holder.textView.text = wrong[position].text
                holder.imageView.setImageResource(R.drawable.wrong)
                holder.textView.setTextColor(Color.parseColor("#ff0000"));
            } else {
                holder.textView.text = correct[position - wrong.size].text
                //holder.imageView.setImageResource(R.drawable.check)
            }
        }

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
