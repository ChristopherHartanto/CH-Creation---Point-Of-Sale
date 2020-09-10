package com.chcreation.pointofsale.model

import com.chcreation.pointofsale.EStatusCode
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

data class Merchant(
    var NAME: String? = "",
    var BUSINESS_INFO: String? = "",
    var ADDRESS: String? = "",
    var NO_TELP: String? = "",
    var IMAGE: String? = "",
    var USER_LIST: String? = "",
    var CAT: String? = "",
    var CREATED_DATE: String? = "",
    var UPDATED_DATE: String? = "",
    var CREATED_BY: String? = "",
    var UPDATED_BY: String? = ""
)

data class Cat(
    var CAT: String? = "",
    var CREATED_DATE: String? = "",
    var UPDATED_DATE: String? = "",
    var UPDATED_BY: String? = "",
    var STATUS_CODE: String? = EStatusCode.ACTIVE.toString()
)

data class UserList(
    var USER_CODE: String? = "",
    var USER_GROUP: String? = "",
    var STATUS_CODE: String? = "",
    var CREATED_DATE: String? = "",
    var UPDATED_DATE: String? = ""
)