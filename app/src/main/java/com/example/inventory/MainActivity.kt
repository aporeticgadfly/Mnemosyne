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
                    Log.d("", "x: "+ x)
                    var view: View = LayoutInflater.from(this).inflate(R.layout.listmain, null)
                    title_text = view.findViewById(R.id.title_text)
                    title_text.text = listArr.elementAt(x).list_title
                    view.setOnClickListener {
                        val playIntent = Intent(this, PlayActivity::class.java)
                        Log.d("", "to play:" + listArr.elementAt(x).id + listArr.elementAt(x).list_title)
                        playIntent.putExtra("id", listArr.elementAt(x).id);
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
                        this.recreate()
                        val contextView = findViewById<View>(R.id.dele_list)
                        Snackbar.make(contextView, "List Deleted", Snackbar.LENGTH_SHORT)
                            .show()
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
        topAppBar = findViewById(R.id.topAppBar)
        //dont know if needed
        /*topAppBar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                // TODO: MAKE THIS STUFF WORK BALAHHHHH
                R.id.search -> {
                    // edittext has to show up somewhere; save current state? then search through titles for matches
                    //how to return to default state without edittext
                    true
                }
                R.id.more -> {
                    // Handle more item (inside overflow menu) press
                    var moreItem: MenuItem = findViewById(R.id.more)
                    moreItem.setOnMenuItemClickListener { menuItem ->
                        when (menuItem.itemId) {
                            R.id.import_item -> {
                                //implicit intent w file manager to choose file; have to do error checking to see if really xl file; detect separator?
                                //after implicit, choose title to give to it in popup or error popup saying invalid filetype
                                val importIntent = Intent()*/
                                    //.setType("*/*")
                                   /* .setAction(Intent.ACTION_GET_CONTENT)

                                startActivityForResult(Intent.createChooser(importIntent, "Select a file"), 111)

                                true
                            }
                            R.id.export_item -> {
                                //popup for confirmation
                                //add checkboxes to all lists
                                //implicit intent w file manager to choose save location
                                val CREATE_FILE = 1

                                fun createFile(pickerInitialUri: Uri) {
                                    val newFragment = ExportDialogFragment()
                                    newFragment.show(supportFragmentManager, "export")

                                    val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                                        addCategory(Intent.CATEGORY_OPENABLE)
                                        type = "application/pdf"
                                        putExtra(Intent.EXTRA_TITLE, "invoice.pdf")

                                        // Optionally, specify a URI for the directory that should be opened in
                                        // the system file picker before your app creates the document.
                                        putExtra(DocumentsContract.EXTRA_INITIAL_URI, pickerInitialUri)
                                    }
                                    startActivityForResult(intent, CREATE_FILE)
                                }

                                true
                            }
                            else -> false
                        }

                    }
                    true
                }
                else -> false
            }
        }
        //
        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.topAppBar)
        setSupportActionBar(toolbar)
        val actionbar: ActionBar? = supportActionBar
        actionbar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.menu)
        }

        mDrawerLayout = findViewById<DrawerLayout>(R.id.my_drawer_layout)

        val navigationView: NavigationView = findViewById(R.id.navigation)
        navigationView.setNavigationItemSelectedListener { menuItem ->
            // set item as selected to persist highlight
            menuItem.isChecked = true
            // close drawer when item is tapped
            mDrawerLayout.closeDrawers()

            // Handle navigation view item clicks here.
            // TODO: CHANGE FRAGMENTS BLAHHHHHH; all other fragments can only back
            when (menuItem.itemId) {

                R.id.nav_home -> {
                    //just change fragment
                }
                R.id.nav_view -> {
                    //short item press to select item, default to first one; pass id
                    mDrawerLayout.findNavController().navigate(R.id.viewFragment)
                }
                R.id.nav_settings -> {
                    //just change fragment
                    mDrawerLayout.findNavController().navigate(R.id.settingsFragment)
                }
            }
            // Add code here to update the UI based on the item selected
            // For example, swap UI fragments here

            true
        }
    }

    //appbar - toolbar button click
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                mDrawerLayout.openDrawer(GravityCompat.START)
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 111 && resultCode == RESULT_OK) {
            val selectedFile = data?.data // The URI with the location of the file
        }*/
    }
    }

