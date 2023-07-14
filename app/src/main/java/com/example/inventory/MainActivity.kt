//@author: Santiago Orellana, June 2023
package com.example.inventory

import android.content.ContentValues
import android.content.Intent
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.MediaStore.Audio.Radio
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.example.inventory.data.ListItem
import com.example.inventory.data.ListItemItem
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.navigation.NavigationView
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import org.apache.commons.csv.CSVPrinter
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Paths
import java.time.LocalDateTime
import java.util.Calendar
import java.util.Date
import java.util.function.BinaryOperator

class MainActivity : AppCompatActivity(R.layout.activity_main) {
    private lateinit var addList: ImageView
    private lateinit var lin_main: LinearLayout
    private lateinit var title_text: TextView
    private lateinit var editBtn: Button
    private lateinit var delBtn: Button
    private lateinit var topAppBar: MaterialToolbar
    private lateinit var mDrawerLayout: DrawerLayout
    private var hovFlag : Boolean = false
    private var listCbs: MutableList<Int> = arrayListOf()
    private var listArr: MutableList<ListItem> = arrayListOf()
    private val viewModel: MnemosyneViewModel by viewModels {
        MnemosyneViewModelFactory(
            (this?.application as Mnemosyne).database
                .itemDao()
        )
    }

    class TreeNode<T>(value: T) {
        var value: T = value
        var parent: TreeNode<T>? = null

        var children: MutableList<TreeNode<T>> = mutableListOf()

        fun addChild(node: TreeNode<T>) {
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

        private fun recursEquals(original: MutableList<TreeNode<T>>, other: MutableList<TreeNode<T>>) : Boolean {
            if(original.size != 0 && other.size != 0) {
                if (original.size == other.size) {
                    for (x in 0 until original.size) {
                        if (original[x].children != other[x].children) {
                            return false
                        }
                    }
                    for (x in 0 until original.size) {
                        val result = recursEquals(original[x].children, other[x].children)
                        return result
                    }
                }
            }
            else if (original.size == 0 && other.size == 0) {
                return true
            }
            return false
        }

        fun equals(other: TreeNode<T>) : Boolean {
            if(value == other.value) {
                val result = recursEquals(children, other.children)
                return result
            }
            return false
        }

        fun getAncestor() : TreeNode<T> {
            if (parent == null) {
                return this
            }
            while (parent != null) {
                    var ancestor = parent!!.getAncestor()
                    return ancestor
            }
            return this
        }
    }

    // TODO: fix parser
    @RequiresApi(Build.VERSION_CODES.O)
    private val getFile =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->

            if (uri != null) {
                val resolver = applicationContext.contentResolver
                val reader = resolver.openInputStream(uri)?.bufferedReader(charset = Charset.defaultCharset())

                val csvParser = CSVParser(
                    reader, CSVFormat.DEFAULT
                        .withHeader("Title", "Items")
                        .withSkipHeaderRecord()
                        .withIgnoreHeaderCase()
                        .withTrim()
                )
                for ((index, csvRecord) in csvParser.withIndex()) {
                    val title = csvRecord.get("Title")
                    val items = csvRecord.get("Items")
                    println("Record No - " + csvRecord.recordNumber)
                    println("---------------")
                    println("Title : $title")
                    println("Items : $items")
                    println("---------------")
                    var itemArr: MutableList<ListItemItem> = mutableListOf()
                    for((index, item) in items.split("#@!").withIndex()) {
                        itemArr[index].text = item
                    }
                    itemArr.removeLast()

                    viewModel.addNewItem(title, itemArr)
                }
                this.recreate()
            }
        }

    private fun recursFun(currentNode: TreeNode<String>, currentView: View) {
        if(currentNode.children == null) {
            //write regular to currentview
            var leaf: View = LayoutInflater.from(this).inflate(R.layout.listmain, null)
            var currentList : ListItem = ListItem(0, "", mutableListOf())

            title_text = leaf.findViewById(R.id.title_text)
            title_text.text = currentNode.value

            for(list in listArr) {
                if (list.list_title == currentNode.value) {
                    currentList = list
                    break
                }
            }

            //sets up an invisible id for duplicates and collisions and such
            var list_id: TextView = leaf.findViewById(R.id.list_id)
            list_id.text = currentList.id.toString()
            list_id.visibility = View.GONE

            //sets an invisible radio button that is displayed when the user navigates to view to choose which list to view
            var rb: RadioButton = leaf.findViewById(R.id.radiob)
            rb.visibility = View.GONE
            rb.setOnClickListener {
                if (hovFlag == false) {
                    val viewIntent = Intent(this, ViewActivity::class.java)
                    viewIntent.putExtra("id", currentList.id.toInt())
                    this.startActivity(viewIntent)
                }
                else {
                    val historyIntent = Intent(this, HistoryActivity::class.java)
                    historyIntent.putExtra("id", currentList.id.toInt())
                    this.startActivity(historyIntent)
                }

            }

            //sets an invisible checkbox that is displayed when a user tries to export lists to choose which lists to export
            var cb: CheckBox = leaf.findViewById(R.id.checkb)
            cb.visibility = View.GONE
            cb.setOnCheckedChangeListener { buttonView, isChecked ->
                if (isChecked) {
                    listCbs.add(currentList.id)
                } else {
                    listCbs.remove(currentList.id)
                }
            }

            //if view is clicked anywhere else, go to play to play that quiz
            leaf.setOnClickListener {
                val playIntent = Intent(this, PlayActivity::class.java)
                playIntent.putExtra("id", currentList.id.toInt())
                playIntent.putExtra("title", currentList.list_title)

                this.startActivity(playIntent)
            }

            //go to page for editing list
            editBtn = leaf.findViewById(R.id.edit_list)
            editBtn.setOnClickListener {
                val editIntent = Intent(this, EditActivity::class.java)
                editIntent.putExtra("id", currentList.id);
                this.startActivity(editIntent)
            }

            //delete list from db and then recreate page
            delBtn = leaf.findViewById(R.id.dele_list)
            delBtn.setOnClickListener {
                viewModel.deleteItem(currentList.id)
                Toast.makeText(this, "List Deleted", Toast.LENGTH_SHORT).show()
                this.recreate()
            }
            var recycler : RecyclerView = currentView.findViewById(R.id.expandRecycler)
            // TODO:  
            recycler.addView(leaf)
        }
        else {
            for(child in currentNode.children) {
                var expand: View = LayoutInflater.from(this).inflate(R.layout.expand, null)
                var expandTitle : TextView = expand.findViewById(R.id.expand_title)
                var recycler : RecyclerView = expand.findViewById(R.id.expandRecycler)
                expandTitle.text = child.value
                // TODO:  
                recycler.addView(expand)
                recursFun(child, expand)
            }
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
        //observer that reads list of stored lists from database using livedata
        lin_main = findViewById(R.id.lin_main)
        val listObserver = Observer<MutableList<ListItem>> { listArr ->
            if (listArr.isNotEmpty()) { //null check
                val layoutChildren: Int = lin_main.childCount
                //removes any views that didnt get destroyed so no false duplicates
                for (x in 0 until layoutChildren) {
                    lin_main.removeViewAt(x)
                }

                //
                //writing the tree
                var currentNode: TreeNode<String>? = null
                var previousNode: TreeNode<String>? = null
                var intermediateNode: TreeNode<String>? = null
                var nodeArr: MutableList<TreeNode<String>?> = mutableListOf()
                var finalArr: MutableList<TreeNode<String>?> = mutableListOf()
                var matchFlag: Boolean = false
                var matchFlag2: Boolean = false
                var matchFlag3: Boolean = false

                for (x in 0 until listArr.size) {
                    for (y in 0 until listArr[x].list_title.split("::").size) {
                        if (y == 0) {
                            for (node in nodeArr) {
                                val ancestor = node?.getAncestor()
                                if (ancestor?.value == listArr[x].list_title.split("::")[y]) {
                                    previousNode = ancestor
                                    matchFlag = true
                                    break
                                }
                            }
                            if (matchFlag == false) {
                                var rootNode = TreeNode<String>(listArr[x].list_title.split("::")[y])
                                previousNode = rootNode
                            }
                            else {
                                matchFlag = false
                            }
                        } else {
                            currentNode = TreeNode<String>(listArr[x].list_title.split("::")[y])
                            if (previousNode?.children != null) {
                                for (node in previousNode.children!!) {
                                    if (node.value == currentNode.value) {
                                        matchFlag2 = true
                                        break
                                    }
                                }
                                if (matchFlag2 != true) {
                                    previousNode.addChild(currentNode)
                                    intermediateNode = previousNode
                                    previousNode = currentNode
                                }
                                else {
                                    matchFlag2 = false
                                }
                            }
                            else {
                                previousNode?.addChild(currentNode)
                                intermediateNode = previousNode
                                previousNode = currentNode
                            }
                        }
                    }
                    //check if on this level, other nodes w same value
                    /*if (currentNode != null) {
                        intermediateNode?.addChild(currentNode)
                    }*/
                    nodeArr.add(intermediateNode)
                }
                for ((index, node) in nodeArr.withIndex()) {
                    if(index == 0) {
                        finalArr.add(node)
                        continue
                    }
                    for (x in 0 until finalArr.size) {
                        if (node != null) {
                            if(node.equals(finalArr[x])) {
                                matchFlag3 = true
                                break
                            }
                        }
                    }
                    if (matchFlag3 == false) {
                        finalArr.add(node)
                    }
                    else {
                        matchFlag3 = false
                    }
                }
                Log.d("tree", finalArr.toString())


                for (x in finalArr) {
                    //move to root node
                    var y = x?.getAncestor()
                    //write root node to lin_main
                    var expand: View = LayoutInflater.from(this).inflate(R.layout.expand, null)
                    var expandTitle : TextView = expand.findViewById(R.id.expand_title)
                    expandTitle.text = y?.value
                    // TODO:  
                    lin_main.addView(expand)
                    //call recursfun w current node and view
                    if (y != null) {
                        recursFun(y, expand)
                    }
                }

                /*for (x in 0 until listArr.size) {


                    //inflates view for each row that has one list
                    var view: View = LayoutInflater.from(this).inflate(R.layout.listmain, null)
                    title_text = view.findViewById(R.id.title_text)
                    //sets up an invisible id for duplicates and collisions and such
                    var list_id: TextView = view.findViewById(R.id.list_id)
                    list_id.text = listArr.elementAt(x).id.toString()
                    list_id.visibility = View.GONE

                    //sets title
                    title_text.text = listArr.elementAt(x).list_title

                    //sets an invisible radio button that is displayed when the user navigates to view to choose which list to view
                    var rb: RadioButton = view.findViewById(R.id.radiob)
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
                        if (isChecked) {
                            listCbs.add(listArr.elementAt(x).id)
                        } else {
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
                }*/
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
                    for (x in 0 until layoutChildren) {
                        val vert: LinearLayout = lin_main.getChildAt(x) as LinearLayout
                        val horizont: LinearLayout = vert.getChildAt(0) as LinearLayout
                        val rb: RadioButton = horizont.getChildAt(0) as RadioButton
                        hovFlag = false

                        rb.visibility = View.VISIBLE
                    }
                    topAppBar.menu.findItem(R.id.more_item).isVisible = false
                    topAppBar.menu.findItem(R.id.cancel_item).isVisible = true
                }

                R.id.history_activity -> {
                    val layoutChildren: Int = lin_main.childCount
                    for (x in 0 until layoutChildren) {
                        val vert: LinearLayout = lin_main.getChildAt(x) as LinearLayout
                        val horizont: LinearLayout = vert.getChildAt(0) as LinearLayout
                        val rb: RadioButton = horizont.getChildAt(0) as RadioButton
                        hovFlag = true

                        rb.visibility = View.VISIBLE
                    }
                    topAppBar.menu.findItem(R.id.more_item).isVisible = false
                    topAppBar.menu.findItem(R.id.cancel_item).isVisible = true
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

            R.id.cancel_item -> {
                val layoutChildren: Int = lin_main.childCount
                for (x in 0 until layoutChildren) {
                    val vert: LinearLayout = lin_main.getChildAt(x) as LinearLayout
                    val horizont: LinearLayout = vert.getChildAt(0) as LinearLayout
                    val cb: CheckBox = horizont.getChildAt(1) as CheckBox
                    val rb: RadioButton = horizont.getChildAt(0) as RadioButton

                    rb.visibility = View.GONE
                    cb.visibility = View.GONE
                }
                topAppBar.menu.findItem(R.id.search).isVisible = true
                topAppBar.menu.findItem(R.id.confirm).isVisible = false
                topAppBar.menu.findItem(R.id.more_item).isVisible = true
                topAppBar.menu.findItem(R.id.cancel_item).isVisible = false
                true
            }
            //replaces search when made to appear; finalizes action of export
            R.id.confirm -> {

                //ask for permission
                //if given, do a createdocumentforresult

                val createThread = Thread {

                val resolver = applicationContext.contentResolver
                val values = ContentValues()
                values.put(MediaStore.MediaColumns.DISPLAY_NAME, LocalDateTime.now().toString())
                values.put(MediaStore.MediaColumns.MIME_TYPE, "text/csv")
                values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                    val uri = resolver.insert(MediaStore.Files.getContentUri("external"), values)
                    if (uri != null) {
                        val writer = resolver.openOutputStream(uri)?.bufferedWriter(charset = Charset.defaultCharset())
                        val csvPrinter = CSVPrinter(
                            writer, CSVFormat.DEFAULT
                                .withHeader("Title", "Items")
                        )

                        for (x in listCbs) {
                            var createItem: ListItem = viewModel.retrieveSyncItem(x)

                            var itemsString: String = ""
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
                    }


                    MediaScannerConnection.scanFile(
                        this,
                        arrayOf(getExternalFilesDir(null)?.absolutePath + Calendar.getInstance().time),
                        null,
                        null
                    )
                    runOnUiThread {
                        val layoutChildren: Int = lin_main.childCount
                        for (x in 0 until layoutChildren) {
                            val vert: LinearLayout = lin_main.getChildAt(x) as LinearLayout
                            val horizont: LinearLayout = vert.getChildAt(0) as LinearLayout
                            val cb: CheckBox = horizont.getChildAt(1) as CheckBox

                            cb.isChecked = false
                            cb.visibility = View.GONE
                        }

                        val topAppBar: MaterialToolbar = findViewById(R.id.topAppBar)
                        topAppBar.menu.findItem(R.id.search).isVisible = true
                        topAppBar.menu.findItem(R.id.confirm).isVisible = false
                        topAppBar.menu.findItem(R.id.more_item).isVisible = true
                        topAppBar.menu.findItem(R.id.cancel_item).isVisible = false

                        Toast.makeText(this, "List(s) Exported", Toast.LENGTH_SHORT).show()
                    }
                }

                createThread.start()
                true
            }
            //submenu item of more; makes cbs visible for each list so user can choose which lists to export
            R.id.export_item -> {
                val layoutChildren: Int = lin_main.childCount
                for (x in 0 until layoutChildren) {
                    val vert: LinearLayout = lin_main.getChildAt(x) as LinearLayout
                    val horizont: LinearLayout = vert.getChildAt(0) as LinearLayout
                    val cb: CheckBox = horizont.getChildAt(1) as CheckBox

                    cb.visibility = View.VISIBLE
                }
                //makes confirm button visible in lieu of search button
                val topAppBar: MaterialToolbar = findViewById(R.id.topAppBar)
                topAppBar.menu.findItem(R.id.search).isVisible = false
                topAppBar.menu.findItem(R.id.confirm).isVisible = true
                topAppBar.menu.findItem(R.id.more_item).isVisible = false
                topAppBar.menu.findItem(R.id.cancel_item).isVisible = true

                true
            }
            //opens up file selector to choose what to import
            R.id.import_item -> {
                getFile.launch("*/*")
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
}


