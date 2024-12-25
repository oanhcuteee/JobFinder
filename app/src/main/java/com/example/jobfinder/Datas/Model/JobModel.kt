package com.example.jobfinder.Datas.Model

import android.os.Parcel
import android.os.Parcelable


class JobModel : Parcelable {
    var jobId: String? = null
    var jobTitle: String? = null
    var startTime: String? = null
    var endTime: String? = null
    var empAmount: String? = null
    var salaryPerEmp: String? = null
    var address: String? = null
    var jobDes: String? = null
    var totalSalary: String? = null
    var postDate: String? = null
    var numOfRecruited: String? = null
    var BUserName: String? = null
    var jobType: String?=null
    var BUserId: String? =null
    var status:String? = null
    var startHr: String?=null
    var endHr:String? =null

    constructor() {
        // Default constructor required for Firebase
    }

    constructor(
        jobId: String?,
        jobTitle: String?,
        startTime: String?,
        endTime: String?,
        empAmount: String?,
        salaryPerEmp: String?,
        address: String?,
        jobDes: String?,
        totalSalary: String?,
        postDate: String?,
        numOfRecruited: String?,
        BUserName: String?,
        jobType: String?,
        BUserId: String?,
        status: String?,
        startHr:String?,
        endHr:String?
    ) {
        this.jobId = jobId
        this.jobTitle = jobTitle
        this.startTime = startTime
        this.endTime = endTime
        this.empAmount = empAmount
        this.salaryPerEmp = salaryPerEmp
        this.address = address
        this.jobDes = jobDes
        this.totalSalary = totalSalary
        this.postDate = postDate
        this.numOfRecruited = numOfRecruited
        this.BUserName = BUserName
        this.jobType = jobType
        this.BUserId = BUserId
        this.status = status
        this.startHr= startHr
        this.endHr= endHr
    }



    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(jobId)
        parcel.writeString(jobTitle)
        parcel.writeString(startTime)
        parcel.writeString(endTime)
        parcel.writeString(empAmount)
        parcel.writeString(salaryPerEmp)
        parcel.writeString(address)
        parcel.writeString(jobDes)
        parcel.writeString(totalSalary)
        parcel.writeString(postDate)
        parcel.writeString(numOfRecruited)
        parcel.writeString(BUserName)
        parcel.writeString(jobType)
        parcel.writeString(BUserId)
        parcel.writeString(status)
        parcel.writeString(startHr)
        parcel.writeString(endHr)
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
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString()
    )

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<JobModel> {
        override fun createFromParcel(parcel: Parcel): JobModel {
            return JobModel(parcel)
        }

        override fun newArray(size: Int): Array<JobModel?> {
            return arrayOfNulls(size)
        }
    }

}