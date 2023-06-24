/*
 * Copyright (C) 2021 The Android Open Source Project.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.inventory

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.example.inventory.R
import com.example.inventory.data.ListItem

class MainActivity : AppCompatActivity(R.layout.activity_main) {
    private lateinit var addList: ImageView
    private lateinit var lin_main: LinearLayout
    private lateinit var title_text: TextView
    private lateinit var editBtn: Button
    private lateinit var delBtn: Button
    private var listArr: MutableList<ListItem> = arrayListOf()
    private val viewModel: InventoryViewModel by viewModels {
        InventoryViewModelFactory(
            (this?.application as InventoryApplication).database
                .itemDao()
        )
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lin_main = findViewById(R.id.lin_main)
        val listObserver = Observer<MutableList<ListItem>> { listArr ->
            Log.d("", "arr size: "+ listArr)
            if (listArr.isNotEmpty()) {
                for (x in 0 until listArr.size) {
                    Log.d("", "x: "+ x)
                    var view: View = LayoutInflater.from(this).inflate(R.layout.listmain, null)
                    title_text = view.findViewById(R.id.title_text)
                    title_text.text = listArr.elementAt(x).list_title
                    editBtn = view.findViewById(R.id.edit_list)
                    editBtn.setOnClickListener {
                        val editIntent = Intent(this, EditActivity::class.java)
                        editIntent.putExtra("id", listArr.elementAt(x).id);
                        this.startActivity(editIntent)
                    }
                    delBtn = view.findViewById(R.id.dele_list)
                    delBtn.setOnClickListener {
                        viewModel.deleteItem(listArr.elementAt(x).id)
                        this.recreate()
                    }
                    //buttonlisteners? must pass id
                    lin_main.addView(view)
                    Log.d("", "view added")
                }
            }
        }
        viewModel.retrieveItems().observe(this, listObserver)

        addList = findViewById(R.id.add_list)
        addList.setOnClickListener {
            val addListIntent = Intent(this, CreateActivity::class.java)
            this.startActivity(addListIntent)
        }
    }
}
