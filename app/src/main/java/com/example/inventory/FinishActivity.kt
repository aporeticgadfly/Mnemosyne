package com.example.inventory

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.ActionBar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.navigation.NavigationView
import org.w3c.dom.Text

class FinishActivity : AppCompatActivity() {
    private lateinit var listTitle: TextView
    private lateinit var numRight: TextView
    private lateinit var numWrong: TextView
    private lateinit var elapsedTime: TextView
    private lateinit var retryBtn: Button
    private lateinit var homeBtn: Button
    private lateinit var items: RecyclerView
    private lateinit var topAppBar: MaterialToolbar
    private lateinit var mDrawerLayout: DrawerLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_finish)
        //get session id from play
        val correct = intent.getStringArrayListExtra("correct")
        val wrong = intent.getStringArrayListExtra("wrong")
        val time_taken = intent.getStringExtra("time")
        val list_title = intent.getStringExtra("title")
        val id = intent.getIntExtra("id", 0)

        listTitle = findViewById(R.id.list_title)
        numRight = findViewById(R.id.num_right)
        numWrong = findViewById(R.id.num_missed)
        elapsedTime = findViewById(R.id.elapsed_time)
        retryBtn = findViewById(R.id.retry_btn)
        homeBtn = findViewById(R.id.home_btn)
        items = findViewById(R.id.items)

        elapsedTime.setText(time_taken)
        if (correct != null) {
            numRight.text = correct.size.toString()
        }
        if (wrong != null) {
            numWrong.text = wrong.size.toString()
        }
        listTitle.text = list_title

        retryBtn.setOnClickListener {
            val retryIntent = Intent(this, PlayActivity::class.java)
            retryIntent.putExtra("id", id.toInt())
            retryIntent.putExtra("title", list_title)
            this.startActivity(retryIntent)
        }

        homeBtn.setOnClickListener {
            val homeIntent = Intent(this, MainActivity::class.java)
            this.startActivity(homeIntent)
        }

        items.adapter = FinishAdapter(this, correct, wrong)
        //recyclerview of items

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