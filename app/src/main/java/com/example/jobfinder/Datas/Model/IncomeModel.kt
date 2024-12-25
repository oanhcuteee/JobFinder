package com.example.jobfinder.Datas.Model

import android.os.Parcel
import android.os.Parcelable

class IncomeModel : Parcelable {
    var incomeDate: String? = null
    var incomeAmount: String? = null

    constructor() {
        // Constructor mặc định không làm gì cả
    }

    constructor(
        incomeDate: String?,
        incomeAmount: String?,
    ) {
        this.incomeDate = incomeDate
        this.incomeAmount = incomeAmount
    }

    private constructor(parcel: Parcel) {
        incomeDate = parcel.readString()
        incomeAmount = parcel.readString()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(incomeDate)
        parcel.writeString(incomeAmount)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<IncomeModel> {
        override fun createFromParcel(parcel: Parcel): IncomeModel {
            return IncomeModel(parcel)
        }

        override fun newArray(size: Int): Array<IncomeModel?> {
            return arrayOfNulls(size)
        }
    }
}
