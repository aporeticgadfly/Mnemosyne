package com.example.inventory

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.Chronometer
import android.widget.EditText
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import com.example.inventory.data.ListItem
import com.example.inventory.data.ListItemItem
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.navigation.NavigationView

class PlayActivity : AppCompatActivity() {
    private lateinit var listTitle: TextView
    private lateinit var numGuessed: TextView
    private lateinit var elapsedTime: Chronometer
    private lateinit var guessInput: EditText
    private lateinit var finishBtn: Button
    private lateinit var topAppBar: MaterialToolbar
    private lateinit var mDrawerLayout: DrawerLayout
    private var num_guessed: Int = 0
    private var sessionObj: MutableMap<String, MutableList<ListItemItem>> = mutableMapOf("correct" to mutableListOf(), "wrong" to mutableListOf())

    private val viewModel: MnemosyneViewModel by viewModels {
        MnemosyneViewModelFactory(
            (this?.application as Mnemosyne).database
                .itemDao()
        )
    }

    fun startFinish(sessionObj: MutableMap<String, MutableList<ListItemItem>>, list_title: String, id: Int) {
        val finishIntent = Intent(this, FinishActivity::class.java)
        finishIntent.putExtra("correct", ArrayList(sessionObj["correct"]))
        finishIntent.putExtra("wrong", ArrayList(sessionObj["wrong"]))
        finishIntent.putExtra("time", elapsedTime.text.toString())
        finishIntent.putExtra("title", list_title)
        finishIntent.putExtra("id", id)
        this.startActivity(finishIntent)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_play)

        val id = intent.getIntExtra("id", 0)
        val list_title = intent.getStringExtra("title")

        listTitle = findViewById(R.id.list_title)
        numGuessed = findViewById(R.id.num_guessed)
        elapsedTime = findViewById(R.id.elapsed_time)
        guessInput = findViewById(R.id.editTextTextPersonName3)
        finishBtn = findViewById(R.id.finish_button)

        //flag for no automatic empty?

        elapsedTime.start()
        numGuessed.text = num_guessed.toString()

        val playObserver = Observer<ListItem> { list ->
            listTitle.text = list.list_title
            list.list_items.removeAt(list.list_items.size - 1)
            Log.d("", list.list_items.toString())

            guessInput.addTextChangedListener(object : TextWatcher { //may be too computationally heavy
                override fun afterTextChanged(s: Editable?) {
                    for(x in list.list_items.indices) {
                        if (x < list.list_items.size) {
                        if (s.toString() == list.list_items[x].text && s.toString() != "") {
                            sessionObj["correct"]?.add(list.list_items[x])
                            list.list_items.removeAt(x)
                            guessInput.setText("")
                            num_guessed++
                            numGuessed.text = num_guessed.toString()
                        }
                        }
                    }
                    if(list.list_items.size == 0) {
                        startFinish(sessionObj, list.list_title, list.id.toInt())
                    }
                }

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                }
            })

            finishBtn.setOnClickListener {
                val finishIntent = Intent(this, FinishActivity::class.java)
                sessionObj["wrong"] = list.list_items
                finishIntent.putExtra("correct", ArrayList(sessionObj["correct"]))
                finishIntent.putExtra("wrong", ArrayList(sessionObj["wrong"]))
                finishIntent.putExtra("time", elapsedTime.text.toString())
                finishIntent.putExtra("title", list_title)
                finishIntent.putExtra("id", id.toInt())
                this.startActivity(finishIntent)
            }
        }

        val passedId = intent.getIntExtra("id", 0)
        if (passedId != null) {
            viewModel.retrieveItem(passedId.toInt()).observe(this, playObserver)
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