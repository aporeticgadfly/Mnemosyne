package com.example.inventory

import android.R
import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView


class ExpandableListViewAdapter(activity: Activity) : BaseAdapter() {
    private val name: MutableList<String>
    private val layoutInflater: LayoutInflater

    init {
        layoutInflater = activity.layoutInflater
        name = ArrayList()
    }

    fun addMessage(message: String) {
        name.add(message)
        notifyDataSetChanged()
    }

    fun removeMessage(message: String) {
        name.remove(message)
        notifyDataSetChanged()
    }

    override fun getCount(): Int {
        return name.size
    }

    override fun getItem(i: Int): Any {
        return name[i]
    }

    override fun getItemId(i: Int): Long {
        return i.toLong()
    }

    override fun getViewTypeCount(): Int {
        return 2
    }

    override fun getItemViewType(i: Int): Int {
        return 0
    }

    override fun getView(i: Int, convertView: View, viewGroup: ViewGroup): View {
        /*
        var convertView = convertView
        val direction = getItemViewType(i)
        if (convertView == null) {

            //LayoutInflater inflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.contact_list, viewGroup, false)
        }
        val message = name[i]
        val txtMessage = convertView.findViewById<View>(R.id.contactName) as TextView
        txtMessage.text = message*/
        return convertView
    }
}