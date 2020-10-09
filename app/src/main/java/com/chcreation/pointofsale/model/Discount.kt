package com.chcreation.pointofsale.model

import com.chcreation.pointofsale.EStatusCode

data class Discount(
    var NAME: String? = "",
    var PERCENT: Float? = 0F,
    var CODE: String? = "",
    var STATUS_CODE: String? = EStatusCode.DELETE.toString(),
    var CREATED_DATE: String? = "",
    var UPDATED_DATE: String? = "",
    var CREATED_BY: String? = "",
    var UPDATED_BY: String? = ""
)