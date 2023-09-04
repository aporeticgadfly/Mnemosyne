package com.example.inventory

import android.app.Application
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.Chronometer
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.preference.PreferenceManager
import com.example.inventory.data.ListItem
import com.example.inventory.data.ListItemItem
import com.example.inventory.data.Session
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.navigation.NavigationView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.launch
import java.util.ArrayList

class PlayActivity : AppCompatActivity() {
    private lateinit var listTitle: TextView
    private lateinit var numGuessed: TextView
    private lateinit var elapsedTime: Chronometer
    private lateinit var guessInput: EditText
    private lateinit var finishBtn: Button
    private lateinit var topAppBar: MaterialToolbar
    private lateinit var submitBtn: Button
    private lateinit var previousScore: TextView
    private lateinit var mDrawerLayout: DrawerLayout
    private lateinit var previousLin: LinearLayout
    private var num_guessed: Int = 0
    private var sessionObj: MutableMap<String, MutableList<ListItemItem>> = mutableMapOf("correct" to mutableListOf(), "wrong" to mutableListOf())
    private var sessionWrongObj: MutableMap<String, MutableList<ListItemItem>> = mutableMapOf("correct" to mutableListOf(), "wrong" to mutableListOf())

    private val viewModel: MnemosyneViewModel by viewModels {
        MnemosyneViewModelFactory(
            (this?.application as Mnemosyne).database
                .itemDao()
        )
    }

    fun startFinish(sessionObj: MutableMap<String, MutableList<ListItemItem>>, list_title: String, id: Int, flag: Boolean, reviewFlag : Boolean) {
        val time = elapsedTime.text.toString().split(":")
        val seconds = (time[0].toInt()*60) + time[1].toInt()
        val session: Session = Session(0, list_title.toString(), id, seconds,
            sessionObj["correct"]!!, sessionObj["wrong"]!!
        )
        viewModel.insertSessionItem(session) {sessionID ->
            val play1Thread = Thread {
                viewModel.viewModelScope.launch {
                    val sessionNum = viewModel.sessionNum()
                    val applicationContext = application.applicationContext
                    val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)
                    val seek = sharedPreferences.getInt("setHistory", 100)
                    if (sessionNum > seek) {
                        viewModel.deleteLast()
                    }
                }
                val finishIntent = Intent(this, FinishActivity::class.java)
                finishIntent.putExtra("sessionID", sessionID)
                finishIntent.putExtra("time", elapsedTime.text.toString())
                finishIntent.putExtra("title", list_title)
                finishIntent.putExtra("id", id)
                finishIntent.putExtra("flag", flag)
                if(reviewFlag == false) {
                    finishIntent.putExtra("reviewFlag", reviewFlag)
                }
                //if flag, put more? not listitemitem but regular list
                this.startActivity(finishIntent)
            }
            play1Thread.start()
        }
    }

    fun startWrongFinish(sessionWrongObj: MutableMap<String, MutableList<ListItemItem>>, list_title: String, id: Int, flag: Boolean) {

        val gson = Gson()
        val correctJson = gson.toJson(sessionWrongObj["correct"])
        val wrongJson = gson.toJson(sessionWrongObj["wrong"])
        val finishIntent = Intent(this, FinishActivity::class.java)
        finishIntent.putExtra("time", elapsedTime.text.toString())
        finishIntent.putExtra("title", list_title)
        finishIntent.putExtra("id", id)
        finishIntent.putExtra("flag", flag)
        finishIntent.putExtra("correct", correctJson)
        finishIntent.putExtra("wrong", wrongJson)
        this.startActivity(finishIntent)

    }
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_play)

        val id = intent.getIntExtra("id", 0)
        val list_title = intent.getStringExtra("title")
        val flag = intent.getBooleanExtra("flag", false)

        listTitle = findViewById(R.id.list_title)
        numGuessed = findViewById(R.id.num_guessed)
        elapsedTime = findViewById(R.id.elapsed_time)
        guessInput = findViewById(R.id.editTextTextPersonName3)
        finishBtn = findViewById(R.id.finish_button)
        previousScore = findViewById(R.id.previous_count)
        previousLin = findViewById(R.id.previous)

        elapsedTime.start()
        numGuessed.text = num_guessed.toString()

        val playObserver = Observer<ListItem> { list ->
            listTitle.text = list.list_title

            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
            val instantVal = sharedPreferences.getBoolean("noInstant", false)
            submitBtn = findViewById(R.id.submitButton)

            if (instantVal == true) {
                //show button, change algorithm
                submitBtn.visibility = View.VISIBLE
                submitBtn.setOnClickListener {
                    val textVal = guessInput.text
                    for (x in list.list_items.indices) {
                        if (x < list.list_items.size) {
                            if (textVal.toString() == list.list_items[x].text && textVal.toString() != "") {
                                sessionObj["correct"]?.add(list.list_items[x])
                                list.list_items.removeAt(x)
                                guessInput.setText("")
                                num_guessed++
                                numGuessed.text = num_guessed.toString()
                            }
                        }
                    }
                    if (list.list_items.size == 0) {
                        startFinish(sessionObj, list.list_title, list.id.toInt(), flag, reviewFlag = false)
                    }
                }
            } else {
                //hide button, change algorithm
                submitBtn.visibility = View.GONE
                guessInput.addTextChangedListener(object :
                    TextWatcher { //may be too computationally heavy
                    override fun afterTextChanged(s: Editable?) {
                        for (x in list.list_items.indices) {
                            if (x < list.list_items.size) {
                                if (s.toString() == list.list_items[x].text && s.toString() != "") {
                                    sessionObj["correct"]?.add(list.list_items[x])
                                    list.list_items.removeAt(x)
                                    guessInput.setText("")
                                    num_guessed++
                                    numGuessed.text = num_guessed.toString()
                                }
                            }
                        }
                        if (list.list_items.size == 0) {
                            startFinish(sessionObj, list.list_title, list.id.toInt(), flag, reviewFlag = false)
                        }
                    }

                    override fun beforeTextChanged(
                        s: CharSequence?,
                        start: Int,
                        count: Int,
                        after: Int
                    ) {
                    }

                    override fun onTextChanged(
                        s: CharSequence?,
                        start: Int,
                        before: Int,
                        count: Int
                    ) {
                    }
                })
            }

            finishBtn.setOnClickListener {
                val time = elapsedTime.text.toString().split(":")
                val seconds = (time[0].toInt() * 60) + time[1].toInt()
                sessionObj["wrong"]=list.list_items
                val session: Session = Session(
                    0, list_title.toString(), id, seconds,
                    sessionObj["correct"]!!, sessionObj["wrong"]!!
                )
                val playThread = Thread {
                    viewModel.insertSessionItem(session) { sessionID ->
                        viewModel.viewModelScope.launch {
                            val num = viewModel.sessionNum()
                            val applicationContext = application.applicationContext
                            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)
                            val seek = sharedPreferences.getInt("setHistory", 100)
                            if (num > seek) {
                                viewModel.deleteLast()
                            }
                            // Use 'num' here
                        }

                        val finishIntent = Intent(this, FinishActivity::class.java)
                        finishIntent.putExtra("sessionID", sessionID)
                        finishIntent.putExtra("time", elapsedTime.text.toString())
                        finishIntent.putExtra("title", list_title)
                        finishIntent.putExtra("id", id)
                        finishIntent.putExtra("flag", flag)
                        this.startActivity(finishIntent)
                    }
                }
                playThread.start()
            }
        }

        val lastObserver = Observer<Session?> { session ->
            //if no session, make previous invisible
            if (session == null) {
                previousLin.visibility = View.GONE
            }
            else {
                val total = session.correct.size + session.wrong.size
                val string = session.correct.size.toString() + "/" + total
                previousScore.text = string
            }
        }

        val wrongObserver = Observer<Session> { session ->
            Log.d("session", session.toString())
            //hide
            //DRY, refactor
            listTitle.text = session.list_title

            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
            val instantVal = sharedPreferences.getBoolean("noInstant", false)
            submitBtn = findViewById(R.id.submitButton)

            if (instantVal == true) {
                //show button, change algorithm
                submitBtn.visibility = View.VISIBLE
                submitBtn.setOnClickListener {
                    val textVal = guessInput.text
                    for (x in session.wrong.indices) {
                        if (x < session.wrong.size) {
                            if (textVal.toString() == session.wrong[x].text && textVal.toString() != "") {
                                sessionWrongObj["correct"]?.add(session.wrong[x])
                                session.wrong.removeAt(x)
                                guessInput.setText("")
                                num_guessed++
                                numGuessed.text = num_guessed.toString()
                            }
                        }
                    }
                    if (session.wrong.size == 0) {
                        startWrongFinish(sessionWrongObj, session.list_title, session.list_id, flag)
                    }
                }
            } else {
                //hide button, change algorithm
                submitBtn.visibility = View.GONE
                guessInput.addTextChangedListener(object :
                    TextWatcher { //may be too computationally heavy
                    override fun afterTextChanged(s: Editable?) {
                        for (x in session.wrong.indices) {
                            if (x < session.wrong.size) {
                                if (s.toString() == session.wrong[x].text && s.toString() != "") {
                                    sessionWrongObj["correct"]?.add(session.wrong[x])
                                    session.wrong.removeAt(x)
                                    guessInput.setText("")
                                    num_guessed++
                                    numGuessed.text = num_guessed.toString()
                                }
                            }
                        }
                        if (session.wrong.size == 0) {
                            startWrongFinish(sessionWrongObj, session.list_title, session.list_id, flag)
                        }
                    }

                    override fun beforeTextChanged(
                        s: CharSequence?,
                        start: Int,
                        count: Int,
                        after: Int
                    ) {
                    }

                    override fun onTextChanged(
                        s: CharSequence?,
                        start: Int,
                        before: Int,
                        count: Int
                    ) {
                    }
                })
            }

            finishBtn.setOnClickListener {
                if (list_title != null) {
                    sessionWrongObj["wrong"]=session.wrong
                    startWrongFinish(sessionWrongObj, list_title, id, flag)
                }
            }

            previousLin.visibility = View.GONE
        }

        val passedId = intent.getIntExtra("id", 0)
        val reviewFlag = intent.getBooleanExtra("reviewFlag", false)
        if (passedId != null) {
            if(reviewFlag == true) {
                val gson = Gson()
                val list_title = intent.getStringExtra("title")
                val flag = intent.getBooleanExtra("flag", false)
                val id = intent.getIntExtra("id", 0)
                val j = intent.getStringExtra("j")
                val itemType = object : TypeToken<ArrayList<ListItemItem>>() {}.type
                val receivedList = gson.fromJson<ArrayList<ListItemItem>>(j, itemType)
                listTitle.text = list_title

                val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
                val instantVal = sharedPreferences.getBoolean("noInstant", false)
                submitBtn = findViewById(R.id.submitButton)

                if (instantVal == true) {
                    //show button, change algorithm
                    submitBtn.visibility = View.VISIBLE
                    submitBtn.setOnClickListener {
                        val textVal = guessInput.text
                        for (x in receivedList.indices) {
                            if (x < receivedList.size) {
                                if (textVal.toString() == receivedList[x].text && textVal.toString() != "") {
                                    sessionObj["correct"]?.add(receivedList[x])
                                    receivedList.removeAt(x)
                                    guessInput.setText("")
                                    num_guessed++
                                    numGuessed.text = num_guessed.toString()
                                }
                            }
                        }
                        if (receivedList.size == 0) {
                            if (list_title != null) {
                                startFinish(sessionObj, list_title, id, flag, reviewFlag)
                            }
                        }
                    }
                } else {
                    //hide button, change algorithm
                    submitBtn.visibility = View.GONE
                    guessInput.addTextChangedListener(object :
                        TextWatcher { //may be too computationally heavy
                        override fun afterTextChanged(s: Editable?) {
                            for (x in receivedList.indices) {
                                if (x < receivedList.size) {
                                    if (s.toString() == receivedList[x].text && s.toString() != "") {
                                        sessionObj["correct"]?.add(receivedList[x])
                                        receivedList.removeAt(x)
                                        guessInput.setText("")
                                        num_guessed++
                                        numGuessed.text = num_guessed.toString()
                                    }
                                }
                            }
                            if (receivedList.size == 0) {
                                if (list_title != null) {
                                    startFinish(sessionObj, list_title, id, flag, reviewFlag)
                                }
                            }
                        }

                        override fun beforeTextChanged(
                            s: CharSequence?,
                            start: Int,
                            count: Int,
                            after: Int
                        ) {
                        }

                        override fun onTextChanged(
                            s: CharSequence?,
                            start: Int,
                            before: Int,
                            count: Int
                        ) {
                        }
                    })
                }

                finishBtn.setOnClickListener {
                    val time = elapsedTime.text.toString().split(":")
                    val seconds = (time[0].toInt() * 60) + time[1].toInt()
                    sessionObj["wrong"]=receivedList
                    val session: Session = Session(
                        0, list_title.toString(), id, seconds,
                        sessionObj["correct"]!!, sessionObj["wrong"]!!
                    )
                    val playThread = Thread {
                        viewModel.insertSessionItem(session) { sessionID ->
                            viewModel.viewModelScope.launch {
                                val sessionNum = viewModel.sessionNum()
                                val applicationContext = application.applicationContext
                                val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)
                                val seek = sharedPreferences.getInt("setHistory", 100)
                                if (sessionNum > seek) {
                                    viewModel.deleteLast()
                                }
                            }
                            val finishIntent = Intent(this, FinishActivity::class.java)
                            finishIntent.putExtra("sessionID", sessionID)
                            finishIntent.putExtra("time", elapsedTime.text.toString())
                            finishIntent.putExtra("title", list_title)
                            finishIntent.putExtra("id", id)
                            finishIntent.putExtra("flag", flag)
                            this.startActivity(finishIntent)
                        }
                    }
                    playThread.start()
                }
            }
            else {
                if(flag == false) {
                    viewModel.retrieveItem(passedId).observe(this, playObserver)
                    viewModel.getLastSession(passedId).observe(this, lastObserver)
                }
                else {
                    viewModel.getLastSession(passedId).observe(this, wrongObserver)
                }
            }
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

            // Handle navigation view item clicks here
            when (menuItem.itemId) {
                R.id.main_activity -> {
                    val mainIntent = Intent(this, MainActivity::class.java)
                    this.startActivity(mainIntent)
                }
                // make rb visible so that view can know which list to display
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