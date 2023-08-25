package com.example.inventory

import android.R.layout
import android.R.id
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Editable
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
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.inventory.data.ListItemItem
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import java.util.Stack
import kotlin.properties.Delegates

class CreateActivity : AppCompatActivity() {
    private lateinit var addItem: ImageView
    private lateinit var listTitle: EditText
    private lateinit var backBtn: Button
    private lateinit var saveBtn: Button
    private lateinit var itemLin: LinearLayout
    private lateinit var itemList: EditText
    private lateinit var topAppBar: MaterialToolbar
    private lateinit var mDrawerLayout: DrawerLayout
    private var list_text: String = ""
    private var title_text: String = ""
    private var listArr: MutableList<ListItemItem> = arrayListOf()
    private var count: Int = 0
    private var countArr: MutableList<Int> = arrayListOf()

    //private lateinit var updateItem: EditText
    //private lateinit var itemList: LinearLayout
    private lateinit var linear_layout2: LinearLayout

    private val viewModel: MnemosyneViewModel by viewModels {
        MnemosyneViewModelFactory(
            (this?.application as Mnemosyne).database
                .itemDao()
        )
    }


    /**
     * The adapter which we have prepared.
     */
    //private lateinit var ListItemAdapter: ListItemAdapter

    /**
     * To hold the reference to the items to be updated as a stack.
     * We can just remove and get the item with [Stack] in one shot.
     */
    //private var modelToBeUpdated: Stack<ListItemAdapter.ItemModel> = Stack()

    /**
     * The listener which we have defined in [OnProductClickListener]. Will be added to the adapter
     * which constructing the adapter
     */

    private val mOnItemClickListener = object : ListItemAdapter.OnItemClickListener {

        /*override fun onUpdate(position: Int, model: ListItemAdapter.ItemModel, s: Editable?) {

                // store this model that we want to update
                // we will .pop() it when we want to update
                // the item in the adapter
                modelToBeUpdated.add(model)
                model.content=s.toString()
                ListItemAdapter.updateItem(model)


            }*/

        override fun onDelete(model: ListItemAdapter.ItemModel) {
            //ListItemAdapter.removeItem(model)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create)

        addItem = findViewById(R.id.add_item)
        listTitle = findViewById(R.id.editTitle)
        linear_layout2 = findViewById(R.id.lin_layout)


        addItem.setOnClickListener {

            val view: View = LayoutInflater.from(this).inflate(R.layout.createiteminput, null)
            lateinit var delBtn: Button
            delBtn = view.findViewById(R.id.dele_item)

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
            //ListItemAdapter.submitValues()
            listTitle = findViewById(R.id.editTitle)
            title_text = listTitle.text.toString()
            val layoutChildren: Int = linear_layout2.childCount
            for (x in 0 until layoutChildren) {
                itemLin = linear_layout2.getChildAt(x) as LinearLayout
                itemList = itemLin.findViewById(R.id.list_itemlist)
                list_text = itemList.text.toString()
                val item: ListItemItem = ListItemItem(0, list_text)
                listArr.add(item)
            }
            viewModel.addNewItem(title_text, listArr)
            val saveIntent = Intent(this, MainActivity::class.java)
            this.startActivity(saveIntent)
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
