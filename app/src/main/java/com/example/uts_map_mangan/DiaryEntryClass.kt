package com.example.uts_map_mangan

import android.os.Parcel
import android.os.Parcelable
import java.util.Date
import java.util.UUID

data class DiaryEntryClass(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val calories: Long = 0,
    val time: String = "",
    val category: String = "",
    val pictureUrl: String = "",
    val timestamp: Date = Date(),
    val accountId: String = ""
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: UUID.randomUUID().toString(),
        parcel.readString() ?: "",
        parcel.readLong(),
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        Date(parcel.readLong()),
        parcel.readString() ?: ""
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(name)
        parcel.writeLong(calories)
        parcel.writeString(time)
        parcel.writeString(category)
        parcel.writeString(pictureUrl)
        parcel.writeLong(timestamp.time)
        parcel.writeString(accountId)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<DiaryEntryClass> {
        override fun createFromParcel(parcel: Parcel): DiaryEntryClass {
            return DiaryEntryClass(parcel)
        }

        override fun newArray(size: Int): Array<DiaryEntryClass?> {
            return arrayOfNulls(size)
        }
    }
}