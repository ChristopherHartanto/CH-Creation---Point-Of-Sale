package com.chcreation.pointofsale.model

import com.chcreation.pointofsale.EStatusCode

data class WholeSale(
    var MIN_QTY: Int? = 0,
    var MAX_QTY: Int? = 0,
    var PRICE: Int? = 0,
    var STATUS_CODE: String? = EStatusCode.ACTIVE.toString()
)