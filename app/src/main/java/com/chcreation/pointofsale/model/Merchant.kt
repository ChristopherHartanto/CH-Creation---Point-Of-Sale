package com.chcreation.pointofsale.model

import com.chcreation.pointofsale.EMerchantMemberStatus
import com.chcreation.pointofsale.EStatusCode
import com.chcreation.pointofsale.EStatusUser
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
    var UPDATED_BY: String? = "",
    var MERCHANT_CODE: String? = "",
    var MEMBER_STATUS: String? = EMerchantMemberStatus.FREE_TRIAL.toString(),
    var MEMBER_DEADLINE: String? = "",
    var ACTIVE: String? = EStatusUser.ACTIVE.toString(),
    var LANGUAGE: String? = Locale.getDefault().language,
    var COUNTRY: String? = Locale.getDefault().country,
    var OPT_FIELD: String? = ""
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