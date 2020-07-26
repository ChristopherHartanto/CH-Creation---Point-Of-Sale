package com.chcreation.pointofsale.model

data class Cart(
    var NAME: String? = "No Data",
    var PROD_CODE: String? = "",
    var PRICE: Int? = 0,
    var Qty: Int? = 0
)