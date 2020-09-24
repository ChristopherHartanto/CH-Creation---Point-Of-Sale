package com.chcreation.pointofsale.model

import com.chcreation.pointofsale.EStatusCode

data class Product(
    var NAME: String? = "No Data",
    var PRICE: Int? = 0,
    var DESC: String? = "No Data",
    var COST: Int? = 0,
    var MANAGE_STOCK: Boolean = false,
    var STOCK: Int? = 0,
    var IMAGE: String? = "",
    var PROD_CODE: String? = "",
    var UOM_CODE: String? = "Unit",
    var CAT: String? = "All",
    var CODE: String? = "",
    var STATUS_CODE: String? = EStatusCode.ACTIVE.toString(),
    var CREATED_DATE: String? = "",
    var UPDATED_DATE: String? = "",
    var CREATED_BY: String? = "",
    var UPDATED_BY: String? = "",
    var WHOLE_SALE: String? = ""
)