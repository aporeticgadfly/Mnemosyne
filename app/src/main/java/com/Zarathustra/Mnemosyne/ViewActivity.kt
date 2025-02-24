package com.Zarathustra.Mnemosyne

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.Zarathustra.Mnemosyne.data.ListItem
import com.google.android.material.navigation.NavigationView

class ViewActivity : AppCompatActivity() {
    private lateinit var items : RecyclerView
    private lateinit var title : TextView
    private lateinit var mDrawerLayout: DrawerLayout
    private val viewModel: MnemosyneViewModel by viewModels {
        MnemosyneViewModelFactory(
            (this?.application as Mnemosyne).database
                .itemDao()
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view)
        items = findViewById(R.id.lin_layout)
        title = findViewById(R.id.title)

        val id = intent.getIntExtra("id", 0)

        val listObserver = Observer<ListItem> { list ->
            title.text = list.list_title
            Log.d("", list.list_items.toString())
            items.adapter = ViewAdapter(this, list.list_items)
        }

        viewModel.retrieveItem(id).observe(this, listObserver)

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