package com.example.inventory

import androidx.room.TypeConverter

class Converters {
        @TypeConverter
        fun fromArray(value: MutableList<String>?) : String? {
            var s: String = ""
            if (value != null) {
                for (x in value) {
                    s = s + x + "delimiter"
                }
            }
            return s
        }

        @TypeConverter
        fun toArray(value: String?) : MutableList<String>? {
            var a = mutableListOf<String>()
            var b = listOf<String>()
            if (value != null) {
                b = value.split("delimiter")
            }
            if (b != null) {
                for (x in b) {
                    a.add(x)
                }
            }
            return a
        }
}