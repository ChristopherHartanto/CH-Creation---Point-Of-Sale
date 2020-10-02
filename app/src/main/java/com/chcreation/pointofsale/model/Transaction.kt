package com.chcreation.pointofsale.model

import com.chcreation.pointofsale.EStatusCode

data class Transaction(
    var TOTAL_PRICE: Float? = 0F,
    var TOTAL_OUTSTANDING: Float? = 0F,
    var DISCOUNT: Float? = 0F,
    var TAX: Float? = 0F,
    var PAYMENT_METHOD: String? = "",
    var DETAIL: String? = "",
    var CUST_CODE: String? = "",
    var NOTE: String? = "All",
    var TRANS_CODE: String? = "",
    var STATUS_CODE: String? = EStatusCode.DONE.toString(),
    var CREATED_DATE: String? = "",
    var UPDATED_DATE: String? = "",
    var CREATED_BY: String? = "",
    var UPDATED_BY: String? = ""
)