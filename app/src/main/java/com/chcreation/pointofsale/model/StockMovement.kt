package com.chcreation.pointofsale.model

data class StockMovement(
    var QTY: Float? = 0F,
    var STATUS: String? = "",
    var STATUS_CODE: String? = "",
    var PROD_CODE: String? = "",
    var PROD_KEY: Int? = 0,
    var TRANS_KEY: Int? = 0,
    var NOTE: String? = "",
    var CREATED_DATE: String? = "",
    var UPDATED_DATE: String? = "",
    var UPDATED_BY: String? = ""
)