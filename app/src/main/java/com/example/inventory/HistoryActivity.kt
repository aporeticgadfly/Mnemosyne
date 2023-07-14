package com.example.inventory

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.example.inventory.data.ListItemItem
import com.example.inventory.data.Session

class HistoryActivity : AppCompatActivity() {
    val titleText : TextView = findViewById(R.id.title)
    val avgScore : TextView = findViewById(R.id.avg_score)
    val avgTime : TextView = findViewById(R.id.avg_time)
    val progressBar: ProgressBar = findViewById(R.id.indeterminateBarMain)
    val history: RecyclerView = findViewById(R.id.lin_layout)
    private var score_total: Int = 0
    private var time_total: Int = 0
    private var items : java.util.ArrayList<String> = arrayListOf()
    private var percentages : java.util.ArrayList<Int> = arrayListOf()
    private val viewModel: MnemosyneViewModel by viewModels {
        MnemosyneViewModelFactory(
            (this?.application as Mnemosyne).database
                .itemDao()
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

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
                                items.add(item.text)
                                break
                            }
                        }
                        for(corr in session.correct) {
                            if (item.id == corr.id) {
                                genCounter++
                                tallyCounter++
                                items.add(item.text)
                                break
                            }
                        }
                    }
                    percentage = (tallyCounter/genCounter)*100
                    percentages.add(percentage)
                }

                history.adapter = HistoryAdapter(this, items, percentages)
            }
        }

        //get sessions w list id
        val id = intent.getIntExtra("id", 0)
        viewModel.getSessionsView(id).observe(this, sessionsObserver)
    }
}