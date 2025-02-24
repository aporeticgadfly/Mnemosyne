package com.Zarathustra.Mnemosyne

import android.util.Log
import androidx.room.TypeConverter
import com.Zarathustra.Mnemosyne.data.ListItemItem
import java.lang.IndexOutOfBoundsException

class Converters {
        @TypeConverter
        fun fromArray(value: MutableList<ListItemItem>?) : String {
            var s: String = ""
            if (value != null) {
                for (x in value) {
                    s = s + x.id + "delimiter" + x.text + "delimiter"
                }
            }
            Log.d("s", s)
            return s
        }

        @TypeConverter
        fun toArray(value: String?) : MutableList<ListItemItem> {
            var a = mutableListOf<ListItemItem>()
            var b = listOf<String>()
            var ids = mutableListOf<String>()
            var texts = mutableListOf<String>()

            if (value != null) {
                b = value.split("delimiter")
                for((index, unit) in b.withIndex()) {
                    if(index % 2 == 0) {
                        ids.add(unit)
                    }
                    else {
                        texts.add(unit)
                    }
                }
            }
            if (b != null) {
                for ((index, x) in ids.withIndex()) {
                    try {
                        val item: ListItemItem = ListItemItem(ids[index].toInt(), texts[index])
                        a.add(item)
                    }
                    catch (e: IndexOutOfBoundsException) {

                    }
                    catch (e: NumberFormatException) {

                    }
                }
            }
            return a
        }
}