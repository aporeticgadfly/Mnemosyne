package com.Zarathustra.Mnemosyne.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Session(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @ColumnInfo(name = "list_title")
    val list_title: String,
    @ColumnInfo(name = "list_id")
    val list_id: Int,
    @ColumnInfo(name = "time_taken")
    val time_taken: Int,
    @ColumnInfo(name = "correct")
    val correct: MutableList<ListItemItem>,
    @ColumnInfo(name = "wrong")
    val wrong: MutableList<ListItemItem>
)