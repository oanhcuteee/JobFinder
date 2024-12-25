package com.example.jobfinder.Datas.Model

import android.os.Parcel
import android.os.Parcelable

class IncomeByJobTypeModel : Parcelable {
    var jobType: String? = null
    var incomeAmount: String? = null

    constructor() {
        // Constructor mặc định không làm gì cả
    }

    constructor(
        jobType: String?,
        incomeAmount: String?,
    ) {
        this.jobType = jobType
        this.incomeAmount = incomeAmount
    }

    private constructor(parcel: Parcel) {
        jobType = parcel.readString()
        incomeAmount = parcel.readString()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(jobType)
        parcel.writeString(incomeAmount)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<IncomeByJobTypeModel> {
        override fun createFromParcel(parcel: Parcel): IncomeByJobTypeModel {
            return IncomeByJobTypeModel(parcel)
        }

        override fun newArray(size: Int): Array<IncomeByJobTypeModel?> {
            return arrayOfNulls(size)
        }
    }
}
