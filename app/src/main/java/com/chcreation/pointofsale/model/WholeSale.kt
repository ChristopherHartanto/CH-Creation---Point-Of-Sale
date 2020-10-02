package com.chcreation.pointofsale.model

import com.chcreation.pointofsale.EStatusCode

data class WholeSale(
    var MIN_QTY: Float? = 0F,
    var MAX_QTY: Float? = 0F,
    var PRICE: Float? = 0F,
    var STATUS_CODE: String? = EStatusCode.ACTIVE.toString()
)