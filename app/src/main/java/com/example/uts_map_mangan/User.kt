import android.os.Parcel
import android.os.Parcelable

data class User(
    val name: String?,
    val goal: String?,
    val weight: String?,
    val height: String?,
    val gender: String?,
    val birthDate: String?
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeString(goal)
        parcel.writeString(weight)
        parcel.writeString(height)
        parcel.writeString(gender)
        parcel.writeString(birthDate)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<User> {
        override fun createFromParcel(parcel: Parcel): User {
            return User(parcel)
        }

        override fun newArray(size: Int): Array<User?> {
            return arrayOfNulls(size)
        }
    }
}