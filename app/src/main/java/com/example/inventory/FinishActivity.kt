package com.example.inventory

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.RecyclerView
import com.example.inventory.data.ListItemItem
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
    private var correct : MutableList<ListItemItem> = arrayListOf()
    private var wrong : MutableList<ListItemItem> = arrayListOf()
    private val viewModel: MnemosyneViewModel by viewModels {
        MnemosyneViewModelFactory(
            (this?.application as Mnemosyne).database
                .itemDao()
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_finish)
        //get session id from play

        listTitle = findViewById(R.id.list_title)
        numRight = findViewById(R.id.num_right)
        numWrong = findViewById(R.id.num_missed)
        elapsedTime = findViewById(R.id.elapsed_time)
        retryBtn = findViewById(R.id.retry_btn)
        homeBtn = findViewById(R.id.home_btn)
        items = findViewById(R.id.items)

        val sessionID = intent.getLongExtra("sessionID", 0)
        if (sessionID != null) {
            val finishThread = Thread {
                val sessionObj = viewModel.retrieveSession(sessionID.toLong())
                correct = sessionObj.correct
                wrong = sessionObj.wrong
                val time_taken = intent.getStringExtra("time")
                val list_title = intent.getStringExtra("title")
                val id = intent.getIntExtra("id", 0)

                elapsedTime.text = time_taken
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
            }
            finishThread.start()
        }

        //recyclerview of items

        mDrawerLayout = findViewById(R.id.my_drawer_layout)

        topAppBar = findViewById(R.id.topAppBar)
        setSupportActionBar(topAppBar)
        val actionbar: ActionBar? = supportActionBar
        actionbar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.menu)
        }

        val navigationView: NavigationView = findViewById(R.id.navigation)
        navigationView.setNavigationItemSelectedListener { menuItem ->
            // set item as selected to persist highlight
            menuItem.isChecked = true
            // close drawer when item is tapped
            mDrawerLayout.closeDrawers()

            // Handle navigation view item clicks here.
            when (menuItem.itemId) {
                R.id.main_activity -> {
                    val mainIntent = Intent(this, MainActivity::class.java)
                    this.startActivity(mainIntent)
                }

                R.id.view_activity -> {
                    Toast.makeText(this, "Can only access through Home", Toast.LENGTH_LONG).show()
                }

                R.id.history_activity -> {
                    Toast.makeText(this, "Can only access through Home", Toast.LENGTH_LONG).show()
                }

                R.id.settings_activity -> {
                    val settingsIntent = Intent(this, SettingsActivity::class.java)
                    this.startActivity(settingsIntent)
                }
            }
            // Add code here to update the UI based on the item selected
            // For example, swap UI fragments here

            true

        }
    }
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        return when (item.itemId) {
            android.R.id.home -> {
                mDrawerLayout.openDrawer(GravityCompat.START)
                true
            }

            else -> {
                true
            }
        }
    }
}