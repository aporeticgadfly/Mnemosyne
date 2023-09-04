package com.example.inventory

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.RecyclerView
import com.example.inventory.data.ListItem
import com.example.inventory.data.ListItemItem
import com.example.inventory.data.Session
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.navigation.NavigationView
import com.google.gson.Gson

class ReviewActivity : AppCompatActivity() {
    private lateinit var review : RecyclerView
    private lateinit var title : TextView
    private lateinit var playBtn : Button
    private var score_total: Int = 0
    private var time_total: Int = 0
    private var itemsArr : java.util.ArrayList<ListItemItem> = arrayListOf()
    private var distinctItems : List<ListItemItem> = arrayListOf()
    private var percentages : MutableList<Double> = mutableListOf()
    private lateinit var mDrawerLayout: DrawerLayout
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

    data class ItemsPercentages(val item: ListItemItem, var genCounter: Double, var tallyCounter : Double)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_review)

        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val reviewThreshold = sharedPreferences.getInt("reviewThreshold", 75)

        review = findViewById(R.id.lin_layout)
        title = findViewById(R.id.title)
        playBtn = findViewById(R.id.playbtn)

        val list_id = intent.getIntExtra("id", 0)

        val reviewAdapter = ReviewAdapter(this, itemsArr, percentages)
        review.adapter = reviewAdapter

        val sessionsObserver = Observer<MutableList<Session>> { sessions ->
            if(sessions.size == 0) {
                title.text = "No sessions for this list yet"
                review.visibility = View.INVISIBLE
                playBtn.visibility = View.GONE
            }
            else {
                Log.d("sessions", sessions.toString())
                title.text = sessions[sessions.size-1].list_title

                //fill in adapter and progress bars
                //for each id, get the object of the id in all of the sessions. if the object appears in correct, add 1 to the int field of the map. if incorrect, minus 1
                //must count number of times occurred; must use latest edit as basis for litmus
                val itpArr: MutableList<ItemsPercentages> = mutableListOf()
                var united = sessions[sessions.size-1].correct + sessions[sessions.size-1].wrong
                for (item in united) {
                    val emptyItems = ItemsPercentages(item, 0.0, 0.0)
                    itpArr.add(emptyItems)
                }
                var percentage: Int = 0
                for(item in itpArr) {
                    for (session in sessions) {
                        for(wrong in session.wrong) {
                            if (item.item.id == wrong.id) {
                                item.genCounter++
                                break
                            }
                        }
                        for(corr in session.correct) {
                            if (item.item.id == corr.id) {
                                item.genCounter++
                                item.tallyCounter++
                                break
                            }
                        }
                    }
                }
                Log.d("itp", itpArr.toString())
                var items : MutableList<ListItemItem> = mutableListOf()
                Log.d("review threshold", reviewThreshold.toString())
                for (item in itpArr) {
                    if ((item.tallyCounter/item.genCounter*100) <= reviewThreshold) {
                        percentages.add(item.tallyCounter/item.genCounter*100)
                        items.add(item.item)
                    }
                }
                Log.d("percentages", percentages.toString())
                reviewAdapter.updateData(items, percentages.toMutableList())

            }
        }

        //get sessions w list id
        viewModel.getSessionsView(list_id).observe(this, sessionsObserver)

        playBtn.setOnClickListener {
            val gson = Gson()
            val itemInfoListJson = gson.toJson(itemsArr)
            val playIntent = Intent(this, PlayActivity::class.java)
            playIntent.putExtra("id", list_id)
            playIntent.putExtra("title", title.text)
            playIntent.putExtra("flag", false)////
            playIntent.putExtra("j", itemInfoListJson)
            playIntent.putExtra("reviewFlag", true)
            this.startActivity(playIntent)
        }

        mDrawerLayout = findViewById(R.id.my_drawer_layout)

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