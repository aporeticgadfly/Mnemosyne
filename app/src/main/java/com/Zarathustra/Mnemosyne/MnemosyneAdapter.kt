package com.Zarathustra.Mnemosyne

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.Zarathustra.Mnemosyne.data.ListItem

class MnemosyneAdapter(
    private val children: MutableList<MainActivity.TreeNode<String>>,
    private val context: Context,
    private val viewModel: MnemosyneViewModel,
    var hovFlag : Boolean,
    var listCbs: MutableList<Int>,
    var listArr: MutableList<ListItem>

) : RecyclerView.Adapter<MnemosyneAdapter.ItemViewHolder>() {

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder.
    // Each data item is just an Affirmation object.
    class ItemViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {
        val textView: TextView? = view.findViewById(R.id.expand_title)

        val title_text : TextView? = view.findViewById(R.id.title_text)
        var list_id: TextView? = view.findViewById(R.id.list_id)
        var rb: RadioButton? = view.findViewById(R.id.radiob)
        var cb: CheckBox? = view.findViewById(R.id.checkb)
        var editBtn : Button? = view.findViewById(R.id.edit_list)
        var delBtn : Button? = view.findViewById(R.id.dele_list)

    }

    private fun showConfirmationDialog(id: Int, title: String) {
        val builder = AlertDialog.Builder(context)
        val inflater = LayoutInflater.from(context)
        val dialogView = inflater.inflate(R.layout.dialog_confirmation, null)

        builder.setView(dialogView)
            .setTitle("Play options")
            .setMessage("Play whole list or only wrong items from last session?")
            .setPositiveButton("Whole List") { dialog, _ ->
                val playIntent = Intent(context, PlayActivity::class.java)
                playIntent.putExtra("id", id)
                playIntent.putExtra("title", title)
                playIntent.putExtra("flag", false)
                context.startActivity(playIntent)
                dialog.dismiss()
            }
            .setPositiveButton("Only Wrong Items") { dialog, _ ->
                val playIntent = Intent(context, PlayActivity::class.java)
                playIntent.putExtra("id", id)
                playIntent.putExtra("title", title)
                playIntent.putExtra("flag", true)
                context.startActivity(playIntent)
                dialog.dismiss()
            }
            .setNeutralButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }

        val dialog = builder.create()
        dialog.show()
    }

    override fun getItemViewType(position: Int): Int {
        if (children[position].children.size == 0) {
            return 0
        }
        else {
            return 1
        }
    }

    /**
     * Create new views (invoked by the layout manager)
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        // create a new view
        if (viewType == 0) {
            val leafLayout = LayoutInflater.from(parent.context)
                .inflate(R.layout.listmain, parent, false)

            return ItemViewHolder(leafLayout)
        }
        else {
            val adapterLayout = LayoutInflater.from(parent.context)
                .inflate(R.layout.expand, parent, false)

            return ItemViewHolder(adapterLayout)
        }
    }

    /**
     * Replace the contents of a view (invoked by the layout manager)
     */
    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {

        //write children? values
        if (children[position].children.size != 0 ) {
            holder.textView?.text = children[position].value
        }
        else {
            var currentList : ListItem = ListItem(0, "", mutableListOf())

            holder.title_text?.text = children[position].value
            for(list in listArr) {
                if (list.list_title == children[position].value) {
                    currentList = list
                    break
                }
            }

            //sets up an invisible id for duplicates and collisions and such
            holder.list_id?.text = currentList.id.toString()
            holder.list_id?.visibility = View.GONE

            //sets an invisible radio button that is displayed when the user navigates to view to choose which list to view
            holder.rb?.visibility = View.GONE
            holder.rb?.setOnClickListener {
                if (hovFlag == false) {
                    val viewIntent = Intent(context, ViewActivity::class.java)
                    viewIntent.putExtra("id", currentList.id.toInt())
                    context.startActivity(viewIntent)
                }
                else {
                    val historyIntent = Intent(context, HistoryActivity::class.java)
                    historyIntent.putExtra("id", currentList.id.toInt())
                    context.startActivity(historyIntent)
                }

            }

            //sets an invisible checkbox that is displayed when a user tries to export lists to choose which lists to export
            holder.cb?.visibility = View.GONE
            holder.cb?.setOnCheckedChangeListener { buttonView, isChecked ->
                if (isChecked) {
                    listCbs.add(currentList.id)
                } else {
                    listCbs.remove(currentList.id)
                }
            }

            //if view is clicked anywhere else, go to play to play that quiz
            holder.itemView.setOnClickListener {
                showConfirmationDialog(currentList.id, currentList.list_title)
            }

            //go to page for editing list
            holder.editBtn?.setOnClickListener {
                val editIntent = Intent(context, EditActivity::class.java)
                editIntent.putExtra("id", currentList.id);
                context.startActivity(editIntent)
            }

            //delete list from db and then recreate page
            holder.delBtn?.setOnClickListener {
                viewModel.deleteItem(currentList.id)
                Toast.makeText(context, "List Deleted", Toast.LENGTH_SHORT).show()
                //context.recreate()
            }
        }
    }

    /**
     * Return the size of your dataset (invoked by the layout manager)
     */
    override fun getItemCount(): Int {
        return children.size
    }
}