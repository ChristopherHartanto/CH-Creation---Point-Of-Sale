package com.chcreation.pointofsale.model

import java.util.*
import kotlin.collections.HashMap

data class Merchant(
    var NAME: String? = "No Data",
    var BUSINESS_INFO: String? = "No Data",
    var ADDRESS: String? = "",
    var NO_TELP: String? = "",
    //var CAT: HashMap<Any,HashMap<Any,Cart>>? = null,
    var CREATED_DATE: String? = "",
    var UPDATED_DATE: String? = "",
    var CREATED_BY: String? = "",
    var UPDATED_BY: String? = ""
)