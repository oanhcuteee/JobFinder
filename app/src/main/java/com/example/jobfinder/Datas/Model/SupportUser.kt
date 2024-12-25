package com.example.jobfinder.Datas.Model

import android.os.Parcel
import android.os.Parcelable

class SupportUser : Parcelable {
    var supportId: String? = null
    var supportName: String? = null
    var status: String? = null
    var description: String? = null
    var userId: String? = null

    constructor() {
        // Default constructor
    }

    constructor(
        supportId: String?,
        supportName: String?,
        status: String?,
        description: String?,
        userId: String?
    ) {
        this.supportId = supportId
        this.supportName = supportName
        this.status = status
        this.description = description
        this.userId = userId
    }

    private constructor(parcel: Parcel) {
        supportId = parcel.readString()
        supportName = parcel.readString()
        status = parcel.readString()
        description = parcel.readString()
        userId = parcel.readString()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(supportId)
        parcel.writeString(supportName)
        parcel.writeString(status)
        parcel.writeString(description)
        parcel.writeString(userId)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<SupportUser> {
        override fun createFromParcel(parcel: Parcel): SupportUser {
            return SupportUser(parcel)
        }

        override fun newArray(size: Int): Array<SupportUser?> {
            return arrayOfNulls(size)
        }
    }
}