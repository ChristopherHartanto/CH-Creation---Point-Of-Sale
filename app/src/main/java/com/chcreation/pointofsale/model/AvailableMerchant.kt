package com.chcreation.pointofsale.model

data class AvailableMerchant(
    var NAME: String? = "", // will not use and get name by fetch using merchant code 16-09-2020
    var USER_GROUP: String? = "",
    var CREATED_DATE: String? = "",
    var UPDATED_DATE: String? = "",
    var CREDENTIAL: String? = "",
    var STATUS: String? = "",
    var MERCHANT_CODE: String? = ""
)