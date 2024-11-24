package com.example.uts_map_mangan

import android.os.Parcel
import android.os.Parcelable
import java.util.Date
import java.util.UUID

data class MealSnack(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val calories: Int = 0,
    val time: String = "",
    val category: String = "",
    val pictureUrl: String = "",
    val timestamp: Date = Date(),
    val accountId: String = ""
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: UUID.randomUUID().toString(),
        parcel.readString() ?: "",
        parcel.readInt(),
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        Date(parcel.readLong()),
        parcel.readString() ?: ""
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(name)
        parcel.writeInt(calories)
        parcel.writeString(time)
        parcel.writeString(category)
        parcel.writeString(pictureUrl)
        parcel.writeLong(timestamp.time)
        parcel.writeString(accountId)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<MealSnack> {
        override fun createFromParcel(parcel: Parcel): MealSnack {
            return MealSnack(parcel)
        }

        override fun newArray(size: Int): Array<MealSnack?> {
            return arrayOfNulls(size)
        }
    }
}