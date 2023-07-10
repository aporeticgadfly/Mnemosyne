//@author: Santiago Orellana, June 2023
package com.example.inventory

import android.app.Notification.Action
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.DocumentsContract
import android.provider.MediaStore.Audio.Radio
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.ExpandableListView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast
import android.widget.Toolbar
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.menu.ActionMenuItem
import androidx.appcompat.view.menu.ActionMenuItemView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.navigation.Navigation.findNavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.onNavDestinationSelected
import com.example.inventory.R
import com.example.inventory.data.ListItem
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import org.apache.commons.csv.CSVPrinter
import java.nio.file.Files
import java.nio.file.Paths
import java.util.Calendar

class MainActivity : AppCompatActivity(R.layout.activity_main) {
    private lateinit var addList: ImageView
    private lateinit var lin_main: LinearLayout
    private lateinit var title_text: TextView
    private lateinit var editBtn: Button
    private lateinit var delBtn: Button
    private lateinit var topAppBar: MaterialToolbar
    private lateinit var mDrawerLayout: DrawerLayout
    private var listCbs: MutableList<Int> = arrayListOf()
    private var listArr: MutableList<ListItem> = arrayListOf()
    private val viewModel: MnemosyneViewModel by viewModels {
        MnemosyneViewModelFactory(
            (this?.application as Mnemosyne).database
                .itemDao()
        )
    }

    class TreeNode<T>(value:T){
        var value:T = value
        var parent:TreeNode<T>? = null

        var children:MutableList<TreeNode<T>> = mutableListOf()

        fun addChild(node:TreeNode<T>){
            children.add(node)
            node.parent = this
        }
        override fun toString(): String {
            var s = "${value}"
            if (!children.isEmpty()) {
                s += " {" + children.map { it.toString() } + " }"
            }
            return s
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private val getFile = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->

        if (uri != null) {
            var path = contentResolver.query(uri, null, null, null, null)

            val reader = Files.newBufferedReader(Paths.get(path.toString()))
            val csvParser = CSVParser(reader, CSVFormat.DEFAULT
                .withHeader("Title", "Items")
                .withIgnoreHeaderCase()
                .withTrim())
            for (csvRecord in csvParser) {
                val title = csvRecord.get("Title")
                val items = csvRecord.get("Items")
                println("Record No - " + csvRecord.recordNumber)
                println("---------------")
                println("Title : $title")
                println("Items : $items")
                println("---------------")
                var itemArr : MutableList<String> = items.split("#@!").toMutableList()

                viewModel.addNewItem(title, itemArr)
            }
            this.recreate()
        }
    }

    val requestPermis = registerForActivityResult(ActivityResultContracts.RequestPermission()) {res: Boolean ->
        if (res) {
            Log.d("perm", "granted")
        }
        else {
            Log.d("perm", "not granted")
        }

    }


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //sets up top toolbar
        topAppBar = findViewById(R.id.topAppBar)
        setSupportActionBar(topAppBar)
        val actionbar: ActionBar? = supportActionBar
        actionbar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.menu)
        }
        // TODO: change
        //observer that reads list of stored lists from database using livedata
        lin_main = findViewById(R.id.lin_main)
        val listObserver = Observer<MutableList<ListItem>> { listArr ->
            if (listArr.isNotEmpty()) { //null check
                val layoutChildren: Int = lin_main.childCount
                //removes any views that didnt get destroyed so no false duplicates
                for(x in 0 until layoutChildren) {
                    lin_main.removeViewAt(x)
                }

                //
                //writing the tree
                var currentNode : TreeNode<String>? = null
                var previousNode : TreeNode<String>? = null
                var intermediateNode : TreeNode<String>? = null
                var nodeArr : MutableList<TreeNode<String>?> = mutableListOf()

                for (x in 0 until listArr.size) {
                    for (y in 0 until listArr[x].list_title.split("::").size) {
                        if (y == 0) {
                            var rootNode = TreeNode<String>(listArr[x].list_title.split("::")[y])
                            previousNode = rootNode
                        }
                        else {
                            currentNode = TreeNode<String>(listArr[x].list_title.split("::")[y])
                            previousNode?.addChild(currentNode)
                            intermediateNode = previousNode
                            previousNode = currentNode

                        }
                    }
                    if (currentNode != null) {
                        intermediateNode?.addChild(currentNode)
                    }
                    nodeArr.add(intermediateNode)
                }
                //follow
                for (x in nodeArr) {
                    //move to root node
                    var rootNode : TreeNode<String>? = null
                    while(x?.parent != null) {
                        rootNode = x
                    }
                    //write root node to lin_main
                    var expand = ExpandableListView(this)
                    expand. = rootNode?.value
                    lin_main.addView(expand)
                    //call recursfun w current node and view
                    recursFun(rootNode, expand)
                }

                private fun recursFun(currentNode: TreeNode<String>, currentView: ExpandableListView) {
                    if(currentNode.children == null) {
                        //write regular lin_main to currentview

                        currentView.addView()
                    }
                    else {
                        for(child in currentNode.children) {
                            var expand: ExpandableListView =
                                expand.text = rootNode.value
                            currentView.addView(expand)
                            recursFun(child, expand)
                        }
                    }
                }

                for (x in 0 until listArr.size) {


                    //inflates view for each row that has one list
                    var view: View = LayoutInflater.from(this).inflate(R.layout.listmain, null)
                    title_text = view.findViewById(R.id.title_text)
                    //sets up an invisible id for duplicates and collisions and such
                    var list_id : TextView = view.findViewById(R.id.list_id)
                    list_id.text = listArr.elementAt(x).id.toString()
                    list_id.visibility = View.GONE

                    //sets title
                    title_text.text = listArr.elementAt(x).list_title

                    //sets an invisible radio button that is displayed when the user navigates to view to choose which list to view
                    var rb : RadioButton = view.findViewById(R.id.radiob)
                    rb.visibility = View.GONE
                    rb.setOnClickListener {
                        val viewIntent = Intent(this, ViewActivity::class.java)
                        viewIntent.putExtra("id", listArr.elementAt(x).id.toInt())
                        this.startActivity(viewIntent)
                    }

                    //sets an invisible checkbox that is displayed when a user tries to export lists to choose which lists to export
                    var cb: CheckBox = view.findViewById(R.id.checkb)
                    cb.visibility = View.GONE
                    cb.setOnCheckedChangeListener { buttonView, isChecked ->
                        if(isChecked) {
                            listCbs.add(listArr.elementAt(x).id)
                        }
                        else {
                            listCbs.remove(listArr.elementAt(x).id)
                        }
                    }

                    //if view is clicked anywhere else, go to play to play that quiz
                    view.setOnClickListener {
                        val playIntent = Intent(this, PlayActivity::class.java)
                        playIntent.putExtra("id", listArr.elementAt(x).id.toInt())
                        playIntent.putExtra("title", listArr.elementAt(x).list_title)

                        this.startActivity(playIntent)
                    }

                    //go to page for editing list
                    editBtn = view.findViewById(R.id.edit_list)
                    editBtn.setOnClickListener {
                        val editIntent = Intent(this, EditActivity::class.java)
                        editIntent.putExtra("id", listArr.elementAt(x).id);
                        this.startActivity(editIntent)
                    }

                    //delete list from db and then recreate page
                    delBtn = view.findViewById(R.id.dele_list)
                    delBtn.setOnClickListener {
                        viewModel.deleteItem(listArr.elementAt(x).id)
                        Toast.makeText(this, "List Deleted", Toast.LENGTH_SHORT).show()
                        this.recreate()
                    }
                    //add the listview
                    lin_main.addView(view)
                }
            }
        }
        viewModel.retrieveItems().observe(this, listObserver)

        //listener for createactivity to add a new list to db
        addList = findViewById(R.id.add_list)
        addList.setOnClickListener {
            val addListIntent = Intent(this, CreateActivity::class.java)
            this.startActivity(addListIntent)
        }

        //sets up navview for the drawer menu
        mDrawerLayout = findViewById(R.id.my_drawer_layout)
        val navigationView: NavigationView = findViewById(R.id.navigation)
        navigationView.setNavigationItemSelectedListener { menuItem ->
            // set item as selected to persist highlight
            menuItem.isChecked = true
            // close drawer when item is tapped
            mDrawerLayout.closeDrawers()

            // Handle navigation view item clicks here
            when (menuItem.itemId) {
                // make rb visible so that view can know which list to display
                R.id.view_activity -> {
                    val layoutChildren: Int = lin_main.childCount
                    for(x in 0 until layoutChildren) {
                        val vert: LinearLayout = lin_main.getChildAt(x) as LinearLayout
                        val horizont: LinearLayout = vert.getChildAt(0) as LinearLayout
                        val rb: RadioButton = horizont.getChildAt(0) as RadioButton

                        rb.visibility = View.VISIBLE
                    }
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
    //manages navigation buttons apart from drawer
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        return when (item.itemId) {
            android.R.id.home -> {
                mDrawerLayout.openDrawer(GravityCompat.START)
                true
            }
            //search for lists
            R.id.search -> {
                // edittext has to show up somewhere; save current state? then search through titles for matches
                //how to return to default state without edittext
                true
            }
            //does nothing on its own, just opens submenu
            R.id.more -> {

                true
            }
            //replaces search when made to appear; finalizes action of export
            R.id.confirm -> {

                val permission = "Manifest.permission.WRITE_EXTERNAL_STORAGE"
                val requestCode = 1

                // Check if permission is already granted
                if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                    // Permission has not been granted, request it
                    ActivityCompat.requestPermissions(this, arrayOf(permission), requestCode)
                } else {
                    // Permission already granted
                    // Proceed with exporting/importing
                }



                val createThread = Thread {
                    val writer = Files.newBufferedWriter(Paths.get(getExternalFilesDir(null)?.absolutePath + Calendar.getInstance().time))
                    Log.d("path", getExternalFilesDir(null)?.absolutePath + Calendar.getInstance().time)
                    val csvPrinter = CSVPrinter(writer, CSVFormat.DEFAULT
                        .withHeader("Title", "Items"))

                    for(x in listCbs) {
                        var createItem: ListItem = viewModel.retrieveSyncItem(x)

                        var itemsString : String = ""
                        for (item in createItem.list_items) {
                            itemsString += item
                            itemsString += "#@!"
                        }
                        //trim final delimiter here
                        var itemsTrimmed = itemsString.substring(0, itemsString.length - 3)
                        csvPrinter.printRecord("${createItem.list_title}", "${itemsTrimmed}")

                    }
                    csvPrinter.flush()
                    csvPrinter.close()
                    MediaScannerConnection.scanFile(this,
                        arrayOf(getExternalFilesDir(null)?.absolutePath + Calendar.getInstance().time), null, null)
                    runOnUiThread {
                        val layoutChildren: Int = lin_main.childCount
                        for(x in 0 until layoutChildren) {
                            val vert: LinearLayout = lin_main.getChildAt(x) as LinearLayout
                            val horizont: LinearLayout = vert.getChildAt(0) as LinearLayout
                            val cb: CheckBox = horizont.getChildAt(1) as CheckBox

                            cb.isChecked = false
                            cb.visibility = View.GONE
                        }

                        val topAppBar: MaterialToolbar = findViewById(R.id.topAppBar)
                        topAppBar.menu.findItem(R.id.search).isVisible = true
                        topAppBar.menu.findItem(R.id.confirm).isVisible = false

                        Toast.makeText(this, "List(s) Exported", Toast.LENGTH_SHORT).show()
                    }
                }

                createThread.start()
                true
            }
            //submenu item of more; makes cbs visible for each list so user can choose which lists to export
            R.id.export_item -> {
                val layoutChildren: Int = lin_main.childCount
                for(x in 0 until layoutChildren) {
                    val vert: LinearLayout = lin_main.getChildAt(x) as LinearLayout
                    val horizont: LinearLayout = vert.getChildAt(0) as LinearLayout
                    val cb: CheckBox = horizont.getChildAt(1) as CheckBox

                    cb.visibility = View.VISIBLE
                }
                //makes confirm button visible in lieu of search button
                val topAppBar: MaterialToolbar = findViewById(R.id.topAppBar)
                topAppBar.menu.findItem(R.id.search).isVisible = false
                topAppBar.menu.findItem(R.id.confirm).isVisible = true

                true
            }
            //opens up file selector to choose what to import
            R.id.import_item -> {
                getFile.launch("text/csv")
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    //inflates top toolbar menu
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.top_app_bar, menu)
        return true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        val createDocumentLauncher = registerForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) { uri ->
            if (uri != null) {
                // Handle the URI returned from the file picker
                // Proceed with exporting or importing
            } else {
                // User canceled or an error occurred
            }
        }

        createDocumentLauncher.launch(intent)
    }

}

