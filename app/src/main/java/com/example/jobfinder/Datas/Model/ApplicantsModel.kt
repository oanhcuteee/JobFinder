package com.example.jobfinder.Datas.Model

import android.os.Parcel
import android.os.Parcelable

class ApplicantsModel : Parcelable {
    var userId: String? = null
    var applicantDes: String? = null
    var appliedDate: String? = null
    var userName: String? = null

    constructor() {
        // Constructor mặc định không làm gì cả
    }

    constructor(
        userId: String?,
        applicantDes: String?,
        appliedDate: String?,
        userName: String?
    ) {
        this.userId = userId
        this.applicantDes = applicantDes
        this.appliedDate = appliedDate
        this.userName = userName
    }

    private constructor(parcel: Parcel) {
        userId = parcel.readString()
        applicantDes = parcel.readString()
        appliedDate = parcel.readString()
        userName = parcel.readString()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(userId)
        parcel.writeString(applicantDes)
        parcel.writeString(appliedDate)
        parcel.writeString(userName)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ApplicantsModel> {
        override fun createFromParcel(parcel: Parcel): ApplicantsModel {
            return ApplicantsModel(parcel)
        }

        override fun newArray(size: Int): Array<ApplicantsModel?> {
            return arrayOfNulls(size)
        }
    }
}
