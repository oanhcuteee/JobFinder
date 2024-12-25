package com.example.jobfinder.Datas.Model

class bankAndCardNumModel(
    var bankName: String?,
    var cardNum: String?
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as bankAndCardNumModel

        if (bankName != other.bankName) return false
        if (cardNum != other.cardNum) return false

        return true
    }

    override fun hashCode(): Int {
        var result = bankName?.hashCode() ?: 0
        result = 31 * result + (cardNum?.hashCode() ?: 0)
        return result
    }
}
