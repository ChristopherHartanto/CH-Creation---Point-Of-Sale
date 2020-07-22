package com.chcreation.pointofsale.model

data class Customer(
    var NAME: String? = "No Data",
    var EMAIL: String? = "",
    var CREATED_DATE: String? = "",
    var UPDATED_DATE: String? = "",
    var PHONE: String? = "",
    var ADDRESS: String? = "",
    var NOTE: String? = "",
    var CODE: String? = ""
)