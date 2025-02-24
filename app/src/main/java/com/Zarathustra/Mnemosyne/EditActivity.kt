package com.Zarathustra.Mnemosyne

import android.content.Intent
import android.os.Build
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
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import com.Zarathustra.Mnemosyne.data.ListItem
import com.Zarathustra.Mnemosyne.data.ListItemItem
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.navigation.NavigationView

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
        setContentView(R.layout.activity_edit)

        addItem = findViewById(R.id.add_item)
        listTitle = findViewById(R.id.editTitle)
        linear_layout2 = findViewById(R.id.lin_layout)

        val editObserver = Observer<ListItem> { list ->
            Log.d("edit", list.toString())
            listTitle.setText(list.list_title)
            for(x in 0 until list.list_items.size) {
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
                    val item: ListItemItem = ListItemItem(0, list_text)
                    listArr.add(item)
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