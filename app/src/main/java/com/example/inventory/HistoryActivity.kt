package com.example.inventory

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.example.inventory.data.ListItemItem
import com.example.inventory.data.Session
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.navigation.NavigationView

class HistoryActivity : AppCompatActivity() {
    private lateinit var topAppBar: MaterialToolbar
    private lateinit var mDrawerLayout: DrawerLayout
    private var score_total: Int = 0
    private var time_total: Int = 0
    private var items : java.util.ArrayList<itemInfo> = arrayListOf()
    private var distinctItems : List<itemInfo> = arrayListOf()
    private var percentages : java.util.ArrayList<Int> = arrayListOf()
    private val viewModel: MnemosyneViewModel by viewModels {
        MnemosyneViewModelFactory(
            (this?.application as Mnemosyne).database
                .itemDao()
        )
    }

    data class itemInfo (
        val id : Int,
        val text: String
            )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)
        val titleText : TextView = findViewById(R.id.hist_title)
        val avgScore : TextView = findViewById(R.id.avg_score)
        val avgTime : TextView = findViewById(R.id.avg_time)
        val progressBar: ProgressBar = findViewById(R.id.indeterminateBarMain)
        val history: RecyclerView = findViewById(R.id.lin_layout)

        val historyAdapter = HistoryAdapter(this, items, percentages)
        history.adapter = historyAdapter

        val sessionsObserver = Observer<MutableList<Session>> { sessions ->
            if(sessions.size == 0) {
                titleText.text = "No sessions for this list yet"
                avgScore.visibility = View.INVISIBLE
                avgTime.visibility = View.INVISIBLE
                progressBar.visibility = View.INVISIBLE
            }
            else {

                //fill in title
                titleText.text = sessions[0].list_title

                //calculate and fill in avg score and time
                for(session in sessions) {
                    score_total += session.correct.size - session.wrong.size
                    time_total += session.time_taken
                }
                avgScore.text = ((score_total/sessions.size)*100).toString()
                avgTime.text = ((time_total/sessions.size)*100).toString()

                //use avg score for progressmain
                progressBar.progress = (score_total/sessions.size)*100

                //fill in adapter and progress bars
                //for each id, get the object of the id in all of the sessions. if the object appears in correct, add 1 to the int field of the map. if incorrect, minus 1
                //must count number of times occurred; must use latest edit as basis for litmus
                var united = sessions[sessions.size-1].correct + sessions[sessions.size-1].wrong
                var genCounter : Int = 0
                var tallyCounter: Int = 0
                var percentage: Int = 0
                for(item in united) {
                    for (session in sessions) {
                        for(wrong in session.wrong) {
                            if (item.id == wrong.id) {
                                genCounter++
                                val itemInfoInstance = itemInfo(item.id, item.text)
                                items.add(itemInfoInstance)
                                break
                            }
                        }
                        for(corr in session.correct) {
                            if (item.id == corr.id) {
                                genCounter++
                                tallyCounter++
                                val itemInfoInstance = itemInfo(item.id, item.text)
                                items.add(itemInfoInstance)
                                break
                            }
                        }
                    }
                    percentage = (tallyCounter/genCounter)*100
                    percentages.add(percentage)
                }
                distinctItems = items.distinct()
                historyAdapter.updateData(distinctItems.toMutableList(), percentages.toMutableList())

            }
        }

        //get sessions w list id
        val id = intent.getIntExtra("id", 0)
        viewModel.getSessionsView(id).observe(this, sessionsObserver)

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

            // Handle navigation view item clicks here
            when (menuItem.itemId) {
                // make rb visible so that view can know which list to display
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

                //go to settings
                R.id.settings_activity -> {
                    val settingsIntent = Intent(this, SettingsActivity::class.java)
                    this.startActivity(settingsIntent)
                }
            }
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