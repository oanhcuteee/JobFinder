package com.example.jobfinder.Datas.Model

import android.os.Parcel
import android.os.Parcelable

class UserBasicInfoModel : Parcelable {
    var user_id: String? = null
    var name: String? = null
    var email: String? = null
    var phone_num: String? = null
    var address: String? = null

    constructor() {
        // Default constructor
    }

    constructor(
        user_id: String?,
        name: String?,
        email: String?,
        phone_num: String?,
        address: String?
    ) {
        this.user_id = user_id
        this.name = name
        this.email = email
        this.phone_num = phone_num
        this.address = address
    }

    private constructor(parcel: Parcel) {
        user_id = parcel.readString()
        name = parcel.readString()
        email = parcel.readString()
        phone_num = parcel.readString()
        address = parcel.readString()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(user_id)
        parcel.writeString(name)
        parcel.writeString(email)
        parcel.writeString(phone_num)
        parcel.writeString(address)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<UserBasicInfoModel> {
        override fun createFromParcel(parcel: Parcel): UserBasicInfoModel {
            return UserBasicInfoModel(parcel)
        }

        override fun newArray(size: Int): Array<UserBasicInfoModel?> {
            return arrayOfNulls(size)
        }
    }
}
