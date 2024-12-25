package com.example.jobfinder.Datas.Model.AdminModel

import android.os.Parcel
import android.os.Parcelable

class RegisteredUserModel : Parcelable {
    var userType:String? = null
    var registeredDate: String? = null
    var amount: String? = null

    constructor() {
        // Constructor mặc định không làm gì cả
    }

    constructor(
        userType:String?,
        registeredDate: String?,
        amount: String?,
    ) {
        this.userType= userType
        this.registeredDate = registeredDate
        this.amount = amount
    }

    private constructor(parcel: Parcel) {
        userType = parcel.readString()
        registeredDate = parcel.readString()
        amount = parcel.readString()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(userType)
        parcel.writeString(registeredDate)
        parcel.writeString(amount)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<RegisteredUserModel> {
        override fun createFromParcel(parcel: Parcel): RegisteredUserModel {
            return RegisteredUserModel(parcel)
        }

        override fun newArray(size: Int): Array<RegisteredUserModel?> {
            return arrayOfNulls(size)
        }
    }
}
