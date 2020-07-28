package com.chcreation.pointofsale.model

import com.chcreation.pointofsale.EStatusCode

data class Enquiry(
    var TRANS_CODE: Int? = 0,
    var CUST_CODE: String? = "",
    var PRODUCT_KEY: Int? = 0,
    var MANAGE_STOCK: Int? = 0,
    var STOCK: Int? = 0,
    var STATUS_CODE : String? = "",
    var CREATED_DATE: String? = "",
    var UPDATED_DATE: String? = "",
    var CREATED_BY: String? = "",
    var UPDATED_BY: String? = ""
)