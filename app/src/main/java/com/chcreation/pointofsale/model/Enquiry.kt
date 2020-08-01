package com.chcreation.pointofsale.model

import com.chcreation.pointofsale.EStatusCode

data class Enquiry(
    var TRANS_KEY: Int? = 0,
    var CUST_CODE: String? = "",
    var PROD_KEY: Int? = 0,
    var PROD_CODE: String? = "",
    var MANAGE_STOCK: Boolean? = false,
    var STOCK: Int? = 0,
    var STATUS_CODE : String? = "",
    var CREATED_DATE: String? = "",
    var UPDATED_DATE: String? = "",
    var CREATED_BY: String? = "",
    var UPDATED_BY: String? = ""
)