package com.zarathustra.mnemosyne

import kotlinx.coroutines.*
import android.content.Context
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ListItemAdapter(
    private val mContext: Context,
    private val mOnItemClickListener: OnItemClickListener,
    private val mItemList: MutableList<ItemModel> = ArrayList()) : RecyclerView.Adapter<ListItemAdapter.ViewHolder>() {
    var mHandler: Handler? = null

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textInputEditText: TextInputEditText
        val itemDelete: Button

        init {
            // Define click listener for the ViewHolder's View
            textInputEditText = view.findViewById(R.id.list_itemlist)
            itemDelete = view.findViewById(R.id.dele_item)
        }
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.createiteminput, viewGroup, false)

        val holder = ViewHolder(view)

        /*val textWatcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                // adapterPosition give the actual position of the item in the RecyclerView
                val position = holder.adapterPosition
                val model = mItemList[position]
                mOnItemClickListener.onUpdate(position, model, s)
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        }
        holder.textInputEditText.addTextChangedListener(textWatcher)*/

        // to delete the item in recycler view
        holder.itemDelete.setOnClickListener {
            val position = holder.adapterPosition
            val model = mItemList[position]
            mOnItemClickListener.onDelete(model)
        }

        return holder
    }

    fun addItem(model: ItemModel) {

        mItemList.add(model)
        // notifyDataSetChanged() // this method is costly avoid it whenever possible
        GlobalScope.launch(Dispatchers.IO) {
            withContext(Dispatchers.Main) {
                notifyItemInserted(mItemList.size)
            }
        }
    }

    fun updateItem(model: ItemModel?) {

        if (model == null) return // we cannot update the value because it is null

        for (item in mItemList) {
            // search by id
            if (item.id == model.id) {
                val position = mItemList.indexOf(model)
                mItemList[position] = model
                GlobalScope.launch(Dispatchers.IO) {
                    withContext(Dispatchers.Main) {
                        notifyItemChanged(position)
                    }
                }
                break // we don't need to continue any more iterations
            }
        }
    }

    fun removeItem(model: ItemModel) {
        val position = mItemList.indexOf(model)
        mItemList.remove(model)
        GlobalScope.launch(Dispatchers.IO) {
            withContext(Dispatchers.Main) {
                notifyItemRemoved(position)
            }
        }
    }

    fun getNextItemId(): Int {
        var id = 1
        if (mItemList.isNotEmpty()) {
            // .last is equivalent to .size() - 1
            // we want to add 1 to that id and return it
            id = mItemList.last().id + 1
        }
        return id
    }

    /*fun submitValues() {
        for (x in mItemList.indices) {
            View view = recyclerView2.getChildAt(x)
            EditText itemEditText = (EditText) view.findViewById(R.id.list_itemlist)
            String item = itemEditText.getText().toString()

            //INSERT TO DB
        }
    }*/

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val item = mItemList[position]
        viewHolder.textInputEditText.setText(item.content)
        // Get element from your dataset at this position and replace the
        // contents of the view with that element
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = mItemList.size

    data class ItemModel(
        var id: Int = 0,
        var content: String = ""
    )

    interface OnItemClickListener {
        /**
         * when the user clicks on delete icon this method will be invoked to remove item at position.
         */
        fun onDelete(model: ItemModel)

    }
}