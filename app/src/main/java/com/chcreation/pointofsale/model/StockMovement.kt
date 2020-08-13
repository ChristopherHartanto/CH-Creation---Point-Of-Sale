package com.chcreation.pointofsale.model

data class StockMovement(
    var QTY: Int? = 0,
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