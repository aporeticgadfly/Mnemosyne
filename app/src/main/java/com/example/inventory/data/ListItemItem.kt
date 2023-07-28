package com.example.inventory.data

import android.os.Parcel
import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity
data class ListItemItem(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @ColumnInfo(name = "text")
    var text: String
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString() ?: ""
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeString(text)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ListItemItem> {
        override fun createFromParcel(parcel: Parcel): ListItemItem {
            return ListItemItem(parcel)
        }

        override fun newArray(size: Int): Array<ListItemItem?> {
            return arrayOfNulls(size)
        }
    }
}