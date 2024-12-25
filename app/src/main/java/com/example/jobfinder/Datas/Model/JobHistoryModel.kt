package com.example.jobfinder.Datas.Model

import android.os.Parcel
import android.os.Parcelable

class JobHistoryModel : Parcelable {
    var jobId: String? = null
    var jobTitle: String? = null
    var endDate: String? = null
    var jobType: String? = null
    var BUserId: String? = null
    var rating: String? = null
    var review: String? = null
    var nUserId:String? = null
    var bUserName:String? = null
    var nUserName:String? = null

    constructor()

    constructor(
        jobId: String?,
        jobTitle: String?,
        endDate: String?,
        jobType: String?,
        BUserId: String?,
        rating: String?,
        review: String?,
        nUserId:String?,
        bUserName:String?,
        nUserName:String?
    ) {
        this.jobId = jobId
        this.jobTitle = jobTitle
        this.endDate = endDate
        this.jobType = jobType
        this.BUserId = BUserId
        this.rating = rating
        this.review = review
        this.nUserId = nUserId
        this.bUserName = bUserName
        this.nUserName = nUserName
    }

    private constructor(parcel: Parcel) {
        jobId = parcel.readString()
        jobTitle = parcel.readString()
        endDate = parcel.readString()
        jobType = parcel.readString()
        BUserId = parcel.readString()
        rating = parcel.readString()
        review = parcel.readString()
        nUserId= parcel.readString()
        bUserName= parcel.readString()
        nUserName= parcel.readString()

    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(jobId)
        parcel.writeString(jobTitle)
        parcel.writeString(endDate)
        parcel.writeString(jobType)
        parcel.writeString(BUserId)
        parcel.writeString(rating)
        parcel.writeString(review)
        parcel.writeString(nUserId)
        parcel.writeString(bUserName)
        parcel.writeString(nUserName)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<JobHistoryModel> {
        override fun createFromParcel(parcel: Parcel): JobHistoryModel {
            return JobHistoryModel(parcel)
        }

        override fun newArray(size: Int): Array<JobHistoryModel?> {
            return arrayOfNulls(size)
        }
    }
}
