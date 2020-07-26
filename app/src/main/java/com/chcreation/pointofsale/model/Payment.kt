package com.chcreation.pointofsale.model

data class Payment(
    var CREATED_DATE: String? = "",
    var TOTAL_RECEIVED: Int? = 0,
    var PAYMENT_METHOD: String? = "",
    var NOTE: String? = "All",
    var USER_CODE: String? = ""
)