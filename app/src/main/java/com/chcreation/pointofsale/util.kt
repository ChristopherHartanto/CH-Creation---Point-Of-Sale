package com.chcreation.pointofsale

import android.content.Context
import android.content.SharedPreferences

private lateinit var sharedPreference: SharedPreferences


fun getMerchant(context: Context) : String{
    sharedPreference =  context.getSharedPreferences("LOCAL_DATA", Context.MODE_PRIVATE)

    return sharedPreference.getString("merchant","").toString()
}