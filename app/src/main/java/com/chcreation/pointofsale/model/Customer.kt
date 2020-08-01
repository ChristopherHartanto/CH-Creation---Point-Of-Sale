package com.chcreation.pointofsale.model

import com.chcreation.pointofsale.EStatusCode

data class Customer(
    var NAME: String? = "No Data",
    var EMAIL: String? = "",
    var CREATED_DATE: String? = "",
    var UPDATED_DATE: String? = "",
    var PHONE: String? = "",
    var ADDRESS: String? = "",
    var NOTE: String? = "",
    var CODE: String? = "",
    var IMAGE: String? = "",
    var STATUS_CODE: String? = EStatusCode.ACTIVE.toString()
)