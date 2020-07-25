package com.chcreation.pointofsale.model

data class Transaction(
    var CREATED_DATE: String? = "",
    var TOTAL_PRICE: Int? = 0,
    var TOTAL_OUTSTANDING: Int? = 0,
    var TOTAL_RECEIVED: Int? = 0,
    var DISCOUNT: Int? = 0,
    var PAYMENT_METHOD: String? = "",
    var DETAIL: String? = "",
    var CUST_CODE: String? = "",
    var NOTE: String? = "All",
    var TRANS_CODE: String? = "",
    var USER_CODE: String? = ""
)