package com.chcreation.pointofsale.model

data class Product(
    var NAME: String? = "No Data",
    var PRICE: Int? = 0,
    var DESC: String? = "No Data",
    var COST: Int? = 0,
    var STOCK: Int? = 0,
    var IMAGE: String? = "",
    var PROD_CODE: String? = "",
    var UOM_CODE: String? = "Unit",
    var CAT: String? = "All"
)