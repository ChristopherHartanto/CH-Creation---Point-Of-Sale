package com.chcreation.pointofsale.model

data class Cart(
    var NAME: String? = "No Data",
    var PROD_KEY: Int? = 0,
    var PROD_CODE: String? = "",
    var MANAGE_STOCK : Boolean? = false,
    var PRICE: Int? = 0,
    var Qty: Int? = 0
)