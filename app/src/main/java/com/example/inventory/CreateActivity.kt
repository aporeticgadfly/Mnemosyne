package com.example.inventory

import android.R.layout
import android.R.id
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.util.Stack
import kotlin.properties.Delegates


class CreateActivity : AppCompatActivity() {
        private lateinit var addItem: ImageView
        private lateinit var listTitle: EditText
        private lateinit var backBtn: Button
        private lateinit var saveBtn: Button
        private lateinit var itemLin: LinearLayout
        private lateinit var itemList: EditText
        private var list_text: String = ""
        private var title_text: String = ""
        private var listArr: MutableList<String> = arrayListOf()
        private var count: Int = 0
        private var countArr: MutableList<Int> = arrayListOf()

        //private lateinit var updateItem: EditText
        //private lateinit var itemList: LinearLayout
        private lateinit var linear_layout2: LinearLayout

        private val viewModel: InventoryViewModel by viewModels {
            InventoryViewModelFactory(
                (this?.application as InventoryApplication).database
                    .itemDao()
            )
        }



        /**
         * The adapter which we have prepared.
         */
        //private lateinit var ListItemAdapter: ListItemAdapter

        /**
         * To hold the reference to the items to be updated as a stack.
         * We can just remove and get the item with [Stack] in one shot.
         */
        //private var modelToBeUpdated: Stack<ListItemAdapter.ItemModel> = Stack()

        /**
         * The listener which we have defined in [OnProductClickListener]. Will be added to the adapter
         * which constructing the adapter
         */

        private val mOnItemClickListener = object : ListItemAdapter.OnItemClickListener {

            /*override fun onUpdate(position: Int, model: ListItemAdapter.ItemModel, s: Editable?) {

                // store this model that we want to update
                // we will .pop() it when we want to update
                // the item in the adapter
                modelToBeUpdated.add(model)
                model.content=s.toString()
                ListItemAdapter.updateItem(model)


            }*/

            override fun onDelete(model: ListItemAdapter.ItemModel) {
                //ListItemAdapter.removeItem(model)
            }
        }

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_create)

            // initialize the recycler view
            //itemList = findViewById(R.id.lin_layout)
            //itemList.layoutManager = LinearLayoutManager(this)
            //itemList.setHasFixedSize(true)

            //ListItemAdapter = ListItemAdapter(this, mOnItemClickListener = mOnItemClickListener)
            //itemList.adapter = ListItemAdapter

            addItem = findViewById(R.id.add_item)
            listTitle = findViewById(R.id.editTitle)
            linear_layout2 = findViewById(R.id.lin_layout)


            addItem.setOnClickListener {

                // prepare id on incremental basis
                //val id = ListItemAdapter.getNextItemId()

                // prepare model for use
                //val model = com.example.inventory.ListItemAdapter.ItemModel(id, content="")

                // add model to the adapter
                //ListItemAdapter.addItem(model)


                val view: View = LayoutInflater.from(this).inflate(R.layout.createiteminput, null)
                lateinit var delBtn: Button
                delBtn = view.findViewById(R.id.dele_item)
                countArr.add(count)
                
                delBtn.setOnClickListener {
                    linear_layout2.removeViewAt(countArr.indexOf(count))
                    countArr.removeAt(countArr.indexOf(count))
                }
                count++
                linear_layout2.addView(view)


            }

            backBtn = findViewById(R.id.back)
            backBtn.setOnClickListener {
                val backIntent = Intent(this, MainActivity::class.java)
                this.startActivity(backIntent)
            }

            saveBtn = findViewById(R.id.save)

            saveBtn.setOnClickListener {
                //ListItemAdapter.submitValues()
                listTitle = findViewById(R.id.editTitle)
                title_text = listTitle.text.toString()
                val layoutChildren: Int = linear_layout2.childCount
                //Log.d("", "layoutkids: " + layoutChildren)
                for(x in 0 until layoutChildren) {
                    //Log.d("", "x: " + x)
                    itemLin = linear_layout2.getChildAt(x) as LinearLayout
                    itemList = itemLin.findViewById(R.id.list_itemlist)
                    list_text = itemList.text.toString()
                    listArr.add(list_text)
                }
                viewModel.addNewItem(title_text, listArr)
                val saveIntent = Intent(this, MainActivity::class.java)
                this.startActivity(saveIntent)
            }


        }
}