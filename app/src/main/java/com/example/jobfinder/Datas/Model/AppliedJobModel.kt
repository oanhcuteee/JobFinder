package com.example.jobfinder.Datas.Model

import android.os.Parcel
import android.os.Parcelable

class AppliedJobModel: Parcelable {
    var buserId :String?= null
    var jobId :String?= null
    var appliedDate: String?= null
    var jobTitle: String?= null
    var startHr :String?= null
    var endHr: String?= null
    var salary:String?= null
    var postedDate: String? = null
    var startTime:String? = null
    var endTime:String?= null
    constructor() {
        // Constructor mặc định không làm gì cả
    }

    constructor(
        buserId :String?,
        jobId :String?,
        appliedDate: String?,
        jobTitle: String?,
        startHr :String?,
        endHr: String?,
        salary:String?,
        postedDate: String?,
        startTime: String?,
        endTime: String?
    ) {
        this.buserId = buserId
        this.jobId = jobId
        this.appliedDate = appliedDate
        this.jobTitle = jobTitle
        this.startHr = startHr
        this.endHr = endHr
        this.salary = salary
        this.postedDate = postedDate
        this.startTime = startTime
        this.endTime = endTime
    }
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(buserId)
        parcel.writeString(jobId)
        parcel.writeString(appliedDate)
        parcel.writeString(jobTitle)
        parcel.writeString(startHr)
        parcel.writeString(endHr)
        parcel.writeString(salary)
        parcel.writeString(postedDate)
        parcel.writeString(startTime)
        parcel.writeString(endTime)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<AppliedJobModel> {
        override fun createFromParcel(parcel: Parcel): AppliedJobModel {
            return AppliedJobModel(parcel)
        }

        override fun newArray(size: Int): Array<AppliedJobModel?> {
            return arrayOfNulls(size)
        }
    }
}