package com.example.jobfinder.Datas.Model

import android.os.Parcel
import android.os.Parcelable

class CheckInFromBUserModel : Parcelable {
    var NUserId: String? = null
    var date: String? = null
    var checkInTime: String? = null
    var checkOutTime: String? = null
    var status: String? = null
    var salary :String? = null

    constructor() {
        // Constructor mặc định không làm gì cả
    }

    constructor(
        NUserId: String?,
        date: String?,
        checkInTime: String?,
        checkOutTime: String?,
        status: String?,
        salary:String?
    ) {
        this.NUserId = NUserId
        this.date = date
        this.checkInTime = checkInTime
        this.checkOutTime = checkOutTime
        this.status = status
        this.salary =salary
    }

    private constructor(parcel: Parcel) {
        NUserId = parcel.readString()
        date = parcel.readString()
        checkInTime = parcel.readString()
        checkOutTime = parcel.readString()
        status = parcel.readString()
        salary = parcel.readString()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(NUserId)
        parcel.writeString(date)
        parcel.writeString(checkInTime)
        parcel.writeString(checkOutTime)
        parcel.writeString(status)
        parcel.writeString(salary)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<CheckInFromBUserModel> {
        override fun createFromParcel(parcel: Parcel): CheckInFromBUserModel {
            return CheckInFromBUserModel(parcel)
        }

        override fun newArray(size: Int): Array<CheckInFromBUserModel?> {
            return arrayOfNulls(size)
        }
    }
}