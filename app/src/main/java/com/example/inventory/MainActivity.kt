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
import android.net.Uri
import android.os.Bundle
import android.provider.DocumentsContract
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import android.widget.Toolbar
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.navigation.Navigation.findNavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.onNavDestinationSelected
import com.example.inventory.R
import com.example.inventory.data.ListItem
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar

class MainActivity : AppCompatActivity(R.layout.activity_main) {
    private lateinit var addList: ImageView
    private lateinit var lin_main: LinearLayout
    private lateinit var title_text: TextView
    private lateinit var editBtn: Button
    private lateinit var delBtn: Button
    private lateinit var topAppBar: MaterialToolbar
    private lateinit var mDrawerLayout: DrawerLayout
    private var listArr: MutableList<ListItem> = arrayListOf()
    private val viewModel: InventoryViewModel by viewModels {
        InventoryViewModelFactory(
            (this?.application as InventoryApplication).database
                .itemDao()
        )
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        /*val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController*/
        lin_main = findViewById(R.id.lin_main)
        val listObserver = Observer<MutableList<ListItem>> { listArr ->
            if (listArr.isNotEmpty()) {
                for (x in 0 until listArr.size) {
                    Log.d("", "x: " + x)
                    var view: View = LayoutInflater.from(this).inflate(R.layout.listmain, null)
                    title_text = view.findViewById(R.id.title_text)
                    title_text.text = listArr.elementAt(x).list_title
                    view.setOnClickListener {
                        val playIntent = Intent(this, PlayActivity::class.java)
                        playIntent.putExtra("id", listArr.elementAt(x).id.toInt());
                        playIntent.putExtra("title", listArr.elementAt(x).list_title);

                        this.startActivity(playIntent)
                    }
                    editBtn = view.findViewById(R.id.edit_list)
                    editBtn.setOnClickListener {
                        val editIntent = Intent(this, EditActivity::class.java)
                        editIntent.putExtra("id", listArr.elementAt(x).id);
                        this.startActivity(editIntent)
                    }
                    delBtn = view.findViewById(R.id.dele_list)
                    delBtn.setOnClickListener {
                        viewModel.deleteItem(listArr.elementAt(x).id)
                        val contextView = findViewById<View>(R.id.lin_main)
                        Toast.makeText(this, "List Deleted", Toast.LENGTH_SHORT).show()
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
        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.topAppBar)
        setSupportActionBar(toolbar)
        val actionbar: ActionBar? = supportActionBar
        actionbar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.menu)
        }
        mDrawerLayout = findViewById(R.id.my_drawer_layout)

        val navigationView: NavigationView = findViewById(R.id.navigation)
        navigationView.setNavigationItemSelectedListener { menuItem ->
            // set item as selected to persist highlight
            menuItem.isChecked = true
            // close drawer when item is tapped
            mDrawerLayout.closeDrawers()

            // Handle navigation view item clicks here.
            when (menuItem.itemId) {
                // TODO:
                R.id.view_fragment -> {
                    Toast.makeText(this, "View", Toast.LENGTH_LONG).show()
                }

                R.id.settings_fragment -> {
                    Toast.makeText(this, "Setting", Toast.LENGTH_LONG).show()
                }
            }
            // Add code here to update the UI based on the item selected
            // For example, swap UI fragments here

            true

        }
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                mDrawerLayout.openDrawer(GravityCompat.START)
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }
}

