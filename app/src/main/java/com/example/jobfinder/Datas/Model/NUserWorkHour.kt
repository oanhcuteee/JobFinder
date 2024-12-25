package com.example.jobfinder.Datas.Model

import android.os.Parcel
import android.os.Parcelable

class NUserWorkHour : Parcelable {
    var workDate: String? = null
    var workTime: String? = null

    constructor() {
        // Constructor mặc định không làm gì cả
    }

    constructor(
        workDate: String?,
        workTime: String?,
    ) {
        this.workDate = workDate
        this.workTime = workTime
    }

    private constructor(parcel: Parcel) {
        workDate = parcel.readString()
        workTime = parcel.readString()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(workDate)
        parcel.writeString(workTime)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<NUserWorkHour> {
        override fun createFromParcel(parcel: Parcel): NUserWorkHour {
            return NUserWorkHour(parcel)
        }

        override fun newArray(size: Int): Array<NUserWorkHour?> {
            return arrayOfNulls(size)
        }
    }
}
