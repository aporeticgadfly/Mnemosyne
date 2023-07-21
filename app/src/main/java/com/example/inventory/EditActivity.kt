package com.example.inventory

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import com.example.inventory.data.ListItem
import com.example.inventory.data.ListItemItem
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import java.util.Stack

class EditActivity : AppCompatActivity() {
    private lateinit var addItem: ImageView
    private lateinit var listTitle: EditText
    private lateinit var backBtn: Button
    private lateinit var saveBtn: Button
    private lateinit var itemLin: LinearLayout
    private lateinit var itemLin2: LinearLayout
    private lateinit var itemList: EditText
    private lateinit var topAppBar: MaterialToolbar
    private lateinit var mDrawerLayout: DrawerLayout
    private var list_text: String = ""
    private var title_text: String = ""
    private var listArr: MutableList<ListItemItem> = arrayListOf()
    private var count: Int = 0
    private var countArr: MutableList<Int> = arrayListOf()
    private var nonNullId: Int = 0

    private lateinit var linear_layout2: LinearLayout

    private val viewModel: MnemosyneViewModel by viewModels {
        MnemosyneViewModelFactory(
            (this?.application as Mnemosyne).database
                .itemDao()
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create)

        addItem = findViewById(R.id.add_item)
        listTitle = findViewById(R.id.editTitle)
        linear_layout2 = findViewById(R.id.lin_layout)

        val editObserver = Observer<ListItem> { list ->
            Log.d("edit", list.toString())
            listTitle.setText(list.list_title)
            for(x in 0 until list.list_items.size-1) {
                val view: View = LayoutInflater.from(this).inflate(R.layout.createiteminput, null)
                lateinit var delBtn: Button
                delBtn = view.findViewById(R.id.dele_item)

                delBtn.setOnClickListener {
                    linear_layout2.removeView(view)
                    Toast.makeText(this, "List Item Deleted", Toast.LENGTH_SHORT).show()
                }
                lateinit var itemEdit: EditText
                itemEdit = view.findViewById(R.id.list_itemlist)
                itemEdit.setText(list.list_items[x].text)
                linear_layout2.addView(view)
            }

            addItem.setOnClickListener {

                val view: View = LayoutInflater.from(this).inflate(R.layout.createiteminput, null)
                lateinit var delBtn: Button
                delBtn = view.findViewById(R.id.dele_item)
                countArr.add(count)

                delBtn.setOnClickListener {
                    linear_layout2.removeView(view)
                    val contextView = findViewById<View>(R.id.lin_layout)
                    Toast.makeText(this, "List Item Deleted", Toast.LENGTH_SHORT).show()
                }
                linear_layout2.addView(view)


            }

            backBtn = findViewById(R.id.back)
            backBtn.setOnClickListener {
                val backIntent = Intent(this, MainActivity::class.java)
                this.startActivity(backIntent)
            }

            saveBtn = findViewById(R.id.save)

            saveBtn.setOnClickListener {
                //must diff with previous values
                listTitle = findViewById(R.id.editTitle)
                title_text = listTitle.text.toString()
                val layoutChildren: Int = linear_layout2.childCount

                for(x in 0 until layoutChildren) {
                    itemLin = linear_layout2.getChildAt(x) as LinearLayout
                    itemList = itemLin.findViewById(R.id.list_itemlist)
                    list_text = itemList.text.toString()
                    listArr[x].text = list_text
                }
                val passedId = intent.getIntExtra("id", 0)
                if (passedId != null) {
                    nonNullId = passedId.toInt()
                }
                viewModel.updateItem(nonNullId, title_text, listArr)
                val saveIntent = Intent(this, MainActivity::class.java)
                this.startActivity(saveIntent)
            }
        }

        val passedId = intent.getIntExtra("id", 0)
        if (passedId != null) {
            viewModel.retrieveItem(passedId.toInt()).observe(this, editObserver)
        }



        topAppBar = findViewById(R.id.topAppBar)
        //dont know if needed
        /*
        topAppBar.setOnMenuItemClickListener { menuItem ->
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
                                true
                            }
                            R.id.export_item -> {
                                //popup for confirmation
                                //add checkboxes to all lists
                                //implicit intent w file manager to choose save location
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
            // TODO: CHANGE FRAGMENTS BLAHHHHHH ALSO ADD THIS STUFF TO ALL OF THE VIEWS BLAHHHHH
            when (menuItem.itemId) {

                R.id.nav_home -> {
                    //just change fragment
                }
                R.id.nav_view -> {
                    //short item press to select item, default to first one; pass id
                }
                R.id.nav_settings -> {
                    //just change fragment
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
        }*/
    }
}