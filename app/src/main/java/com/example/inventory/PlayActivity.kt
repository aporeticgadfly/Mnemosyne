package com.example.inventory

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class PlayActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_play)

        //getitem w passed id
        //on userinput check if matches any item in content; alg may need to be complex
        //if does empty input
        //on each match, if no more matched remaining go to finish
        //flag for no automatic empty?
        //update running scores on each match
        //give up listener
    }
}