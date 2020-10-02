package com.chcreation.pointofsale.model

import com.chcreation.pointofsale.EStatusCode

data class Payment(
    var CREATED_DATE: String? = "",
    var TOTAL_RECEIVED: Float? = 0F,
    var PAYMENT_METHOD: String? = "",
    var NOTE: String? = "All",
    var USER_CODE: String? = "",
    var STATUS_CODE: String? = EStatusCode.DONE.toString(),
    var UPDATED_DATE: String? = "",
    var CREATED_BY: String? = "",
    var UPDATED_BY: String? = ""
)