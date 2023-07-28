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
import android.widget.ImageButton
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
import androidx.preference.PreferenceManager
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
    private var globalListArr: MutableList<ListItem> = arrayListOf()
    private var leafArr: MutableList<LinearLayout> = arrayListOf()
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

        fun fullName() : String {
            if (parent == null) {
                return this.value.toString()
            }
            while (parent != null) {
                return parent!!.fullName().toString() + "::" + this.value
            }
            return this.value.toString()
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
                    for(item in items.split("#@!")) {
                        val listItemItem = ListItemItem(id = 0, text = item)
                        itemArr.add(listItemItem)
                    }
                    //itemArr.removeLast()
                    viewModel.addNewItem(title, itemArr)
                }
                this.recreate()
            }
        }

    private fun recursFun(currentNode: TreeNode<String>, currentView: View) {
        //use children; level by level?
        //each iteration writes children nodes, then passes children nodes/views to itself again
        //in branch, set title; in leaf set up all else
        //each node can have leaves and branches; must detect all leaves and branches on each node; leaf is a child node that has no children
        var expandLin: LinearLayout = currentView.findViewById(R.id.expandLin)
        for (child in currentNode.children) {
            if (child.children.size == 0) {
                val leafLayout = LayoutInflater.from(this).inflate(R.layout.listmain, null)
                var currentList: ListItem = ListItem(0, "", mutableListOf())

                title_text = leafLayout.findViewById(R.id.title_text)
                title_text.text = child.value

                for (list in globalListArr) {
                    if (list.list_title == child.fullName()) {
                        //MUST BE FULLY QUALIFIED NAME; DO AT OTHER PLACE TOO
                        currentList = list
                        break
                    }
                }

                //sets up an invisible id for duplicates and collisions and such
                var list_id: TextView = leafLayout.findViewById(R.id.list_id)
                list_id.text = currentList.id.toString()
                list_id.visibility = View.GONE

                //sets an invisible radio button that is displayed when the user navigates to view to choose which list to view
                var rb: RadioButton = leafLayout.findViewById(R.id.radiob)
                rb.visibility = View.GONE
                rb.setOnClickListener {
                    if (hovFlag == false) {
                        val viewIntent = Intent(this, ViewActivity::class.java)
                        viewIntent.putExtra("id", currentList.id.toInt())
                        this.startActivity(viewIntent)
                    } else {
                        val historyIntent = Intent(this, HistoryActivity::class.java)
                        historyIntent.putExtra("id", currentList.id.toInt())
                        this.startActivity(historyIntent)
                    }

                }

                //sets an invisible checkbox that is displayed when a user tries to export lists to choose which lists to export
                var cb: CheckBox = leafLayout.findViewById(R.id.checkb)
                cb.visibility = View.GONE
                cb.setOnCheckedChangeListener { buttonView, isChecked ->
                    if (isChecked) {
                        listCbs.add(currentList.id)
                    } else {
                        listCbs.remove(currentList.id)
                    }
                }

                //if view is clicked anywhere else, go to play to play that quiz
                leafLayout.setOnClickListener {
                    val playIntent = Intent(this, PlayActivity::class.java)
                    playIntent.putExtra("id", currentList.id.toInt())
                    playIntent.putExtra("title", currentList.list_title)
                    this.startActivity(playIntent)
                }

                //go to page for editing list
                editBtn = leafLayout.findViewById(R.id.edit_list)
                editBtn.setOnClickListener {
                    val editIntent = Intent(this, EditActivity::class.java)
                    editIntent.putExtra("id", currentList.id);
                    this.startActivity(editIntent)
                }

                //delete list from db and then recreate page
                delBtn = leafLayout.findViewById(R.id.dele_list)
                delBtn.setOnClickListener {
                    viewModel.deleteItem(currentList.id)
                    leafArr.remove(leafLayout)
                    Toast.makeText(this, "List Deleted", Toast.LENGTH_SHORT).show()
                    this.recreate()
                }
                leafArr.add(leafLayout as LinearLayout)
                expandLin.addView(leafLayout)
            } else {
                val expandLayout = LayoutInflater.from(this).inflate(R.layout.expand, null)
                val title: TextView = expandLayout.findViewById(R.id.expand_title)
                title.text = child.value
                expandLin.addView(expandLayout)
                var imgBtn : ImageButton = expandLayout.findViewById(R.id.imagebtn)
                var expan : LinearLayout = expandLayout.findViewById(R.id.expandLin)
                imgBtn.setOnClickListener {
                    if(expan.visibility == View.GONE) {
                        expan.visibility = View.VISIBLE
                    }
                    else {
                        expan.visibility = View.GONE
                    }
                }
                recursFun(child, expandLayout)
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
                globalListArr = listArr
                //removes any views that didnt get destroyed so no false duplicates
                /*for (leaf in leafArr) {
                    val listID : TextView = leaf.findViewById(R.id.list_id)
                    viewModel.deleteItem(listID.text.toString().toInt())
                    leafArr.remove(leaf)
                }*/

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
                var nodeSkipFlag : Boolean = false

                for (x in 0 until globalListArr.size) {
                    if (globalListArr[x].list_title.split("::").size == 1) {
                        var rootNode = TreeNode<String>(globalListArr[x].list_title.split("::")[0])
                        finalArr.add(rootNode)
                        continue
                    }
                    for (y in 0 until globalListArr[x].list_title.split("::").size) {
                        //if is root node, check if already used
                        if (y == 0) {
                            for (node in nodeArr) {
                                val ancestor = node?.getAncestor()
                                if (ancestor?.value == globalListArr[x].list_title.split("::")[y]) {
                                    previousNode = ancestor
                                    matchFlag = true
                                    nodeSkipFlag = true
                                    break
                                }
                            }
                            if (matchFlag == false) {
                                var rootNode = TreeNode<String>(globalListArr[x].list_title.split("::")[y])
                                previousNode = rootNode
                            }
                            else {
                                matchFlag = false
                            }
                        } else {
                            currentNode = TreeNode<String>(globalListArr[x].list_title.split("::")[y])
                            if (previousNode?.children != null) {
                                for ((index, node) in previousNode!!.children.withIndex()) {
                                    if (node.value == currentNode.value) {
                                        intermediateNode = previousNode
                                        previousNode = previousNode.children[index]
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
                    if (nodeSkipFlag == true) {
                        nodeSkipFlag = false
                    }
                    else {
                        nodeArr.add(intermediateNode)
                    }
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

                for (x in finalArr) {
                    //move to root node
                    var y = x?.getAncestor()
                    //write root node to lin_main
                    //handle leaves
                    if (y?.children?.size == 0 || y?.children == null) {

                        val leafLayout = LayoutInflater.from(this).inflate(R.layout.listmain, null)
                        var currentList: ListItem = ListItem(0, "", mutableListOf())

                        title_text = leafLayout.findViewById(R.id.title_text)
                        title_text.text = y?.value
                        for (list in globalListArr) {
                            if (list.list_title == y?.value) {
                                currentList = list
                                break
                            }
                        }

                        //sets up an invisible id for duplicates and collisions and such
                        var list_id: TextView = leafLayout.findViewById(R.id.list_id)
                        list_id.text = currentList.id.toString()
                        list_id.visibility = View.GONE

                        //sets an invisible radio button that is displayed when the user navigates to view to choose which list to view
                        var rb: RadioButton = leafLayout.findViewById(R.id.radiob)
                        rb.visibility = View.GONE
                        rb.setOnClickListener {
                            if (hovFlag == false) {
                                val viewIntent = Intent(this, ViewActivity::class.java)
                                viewIntent.putExtra("id", currentList.id.toInt())
                                this.startActivity(viewIntent)
                            } else {
                                val historyIntent = Intent(this, HistoryActivity::class.java)
                                historyIntent.putExtra("id", currentList.id.toInt())
                                this.startActivity(historyIntent)
                            }

                        }

                        //sets an invisible checkbox that is displayed when a user tries to export lists to choose which lists to export
                        var cb: CheckBox = leafLayout.findViewById(R.id.checkb)
                        cb.visibility = View.GONE
                        cb.setOnCheckedChangeListener { buttonView, isChecked ->
                            if (isChecked) {
                                listCbs.add(currentList.id)
                            } else {
                                listCbs.remove(currentList.id)
                            }
                        }

                        //if view is clicked anywhere else, go to play to play that quiz
                        leafLayout.setOnClickListener {
                            val playIntent = Intent(this, PlayActivity::class.java)
                            playIntent.putExtra("id", currentList.id.toInt())
                            playIntent.putExtra("title", currentList.list_title)
                            this.startActivity(playIntent)
                        }

                        //go to page for editing list
                        editBtn = leafLayout.findViewById(R.id.edit_list)
                        editBtn.setOnClickListener {
                            val editIntent = Intent(this, EditActivity::class.java)
                            editIntent.putExtra("id", currentList.id);
                            this.startActivity(editIntent)
                        }

                        //delete list from db and then recreate page
                        delBtn = leafLayout.findViewById(R.id.dele_list)
                        delBtn.setOnClickListener {
                            viewModel.deleteItem(currentList.id)
                            leafArr.remove(leafLayout)
                            Toast.makeText(this, "List Deleted", Toast.LENGTH_SHORT).show()
                            this.recreate()
                        }
                        leafArr.add(leafLayout as LinearLayout)
                        lin_main.addView(leafLayout)
                    }
                    else {
                        var expand: View = LayoutInflater.from(this).inflate(R.layout.expand, null)
                        var expandTitle : TextView = expand.findViewById(R.id.expand_title)
                        var expandLin : LinearLayout = expand.findViewById(R.id.expandLin)
                        expandTitle.text = y?.value
                        var imgBtn : ImageButton = expand.findViewById(R.id.imagebtn)
                        imgBtn.setOnClickListener {
                            if(expandLin.visibility == View.GONE) {
                                expandLin.visibility = View.VISIBLE
                            }
                            else {
                                expandLin.visibility = View.GONE
                            }
                        }
                        lin_main.addView(expand)
                        //call recursfun w current node and view
                        if (y != null) {
                            recursFun(y, expand)
                        }
                    }
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
                    for (leaf in leafArr) {
                        val horizont: LinearLayout = leaf.getChildAt(0) as LinearLayout
                        val rb: RadioButton = horizont.getChildAt(0) as RadioButton
                        hovFlag = false

                        rb.visibility = View.VISIBLE
                    }
                    topAppBar.menu.findItem(R.id.more_item).isVisible = false
                    topAppBar.menu.findItem(R.id.cancel_item).isVisible = true
                }

                R.id.history_activity -> {
                    for (leaf in leafArr) {
                        val horizont: LinearLayout = leaf.getChildAt(0) as LinearLayout
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

            //does nothing on its own, just opens submenu
            R.id.more -> {

                true
            }

            R.id.cancel_item -> {
                for (leaf in leafArr) {
                    val horizont: LinearLayout = leaf.getChildAt(0) as LinearLayout
                    val cb: CheckBox = horizont.getChildAt(1) as CheckBox
                    val rb: RadioButton = horizont.getChildAt(0) as RadioButton

                    rb.visibility = View.GONE
                    cb.visibility = View.GONE
                }
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
                    val prefs = PreferenceManager.getDefaultSharedPreferences(this)
                    val folder = prefs.getString("exportFolder", "")

                val resolver = applicationContext.contentResolver
                val values = ContentValues()
                values.put(MediaStore.MediaColumns.DISPLAY_NAME, LocalDateTime.now().toString())
                values.put(MediaStore.MediaColumns.MIME_TYPE, "text/csv")
                values.put(MediaStore.MediaColumns.RELATIVE_PATH, folder)
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
                                itemsString += item.text
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
                        for (leaf in leafArr) {
                            val horizont: LinearLayout = leaf.getChildAt(0) as LinearLayout
                            val cb: CheckBox = horizont.getChildAt(1) as CheckBox

                            cb.isChecked = false
                            cb.visibility = View.GONE
                        }

                        val topAppBar: MaterialToolbar = findViewById(R.id.topAppBar)
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
                for (leaf in leafArr) {
                    val horizont: LinearLayout = leaf.getChildAt(0) as LinearLayout
                    val cb: CheckBox = horizont.getChildAt(1) as CheckBox

                    cb.visibility = View.VISIBLE
                }
                //makes confirm button visible in lieu of search button
                val topAppBar: MaterialToolbar = findViewById(R.id.topAppBar)
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


