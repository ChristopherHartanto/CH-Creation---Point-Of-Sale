package com.chcreation.pointofsale.model

import com.chcreation.pointofsale.EStatusUser
import com.chcreation.pointofsale.EUserMemberStatus

data class User(
    var NAME: String? = "",
    var EMAIL: String? = "",
    var CREATED_DATE: String? = "",
    var UPDATED_DATE: String? = "",
    var MEMBER_STATUS: String? = EUserMemberStatus.FREE_TRIAL.toString(),
    var ACTIVE: String? = EStatusUser.ACTIVE.toString()
)