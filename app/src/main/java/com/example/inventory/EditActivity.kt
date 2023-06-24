package com.example.inventory

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class EditActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit)

        //use passed id to call get item
        //populate title with list_title
        //call addview by contentsize, populate each textfield with index to content
        //otherwise same code as createview
    }
}