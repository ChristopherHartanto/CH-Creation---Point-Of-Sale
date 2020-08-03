package com.chcreation.pointofsale

import android.content.Context
import android.content.SharedPreferences
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import org.jetbrains.anko.startActivity
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

private lateinit var sharedPreference: SharedPreferences

var RESULT_CLOSE_ALL = 1111

fun getName(context: Context) : String{
    sharedPreference =  context.getSharedPreferences("LOCAL_DATA", Context.MODE_PRIVATE)

    return sharedPreference.getString(ESharedPreference.NAME.toString(),"").toString()
}

fun getMerchant(context: Context) : String{
    sharedPreference =  context.getSharedPreferences("LOCAL_DATA", Context.MODE_PRIVATE)

    return sharedPreference.getString(ESharedPreference.MERCHANT.toString(),"").toString()
}

fun getMerchantCredential(context: Context) : String{
    sharedPreference =  context.getSharedPreferences("LOCAL_DATA", Context.MODE_PRIVATE)

    return sharedPreference.getString(ESharedPreference.MERCHANT_CREDENTIAL.toString(),"").toString()
}

fun getMerchantUserGroup(context: Context) : String{
    sharedPreference =  context.getSharedPreferences("LOCAL_DATA", Context.MODE_PRIVATE)

    return sharedPreference.getString(ESharedPreference.USER_GROUP.toString(),"").toString()
}

fun getMerchantAddress(context: Context) : String{
    sharedPreference =  context.getSharedPreferences("LOCAL_DATA", Context.MODE_PRIVATE)

    return sharedPreference.getString(ESharedPreference.ADDRESS.toString(),"").toString()
}

fun getMerchantNoTel(context: Context) : String{
    sharedPreference =  context.getSharedPreferences("LOCAL_DATA", Context.MODE_PRIVATE)

    return sharedPreference.getString(ESharedPreference.NO_TELP.toString(),"").toString()
}

fun normalClickAnimation() : AlphaAnimation = AlphaAnimation(3F,0.6F)

fun dateFormat() : SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

fun parseDateFormat(date: String) : String {
    var currentFormat = dateFormat().parse(date)
    var newFormat = SimpleDateFormat("dd MMM yyyy").format(currentFormat).toString()

    return newFormat
}

fun parseTimeFormat(date: String) : String {
    var currentFormat = dateFormat().parse(date)
    var newFormat = SimpleDateFormat("HH:mm:ss").format(currentFormat).toString()

    return newFormat
}

fun indonesiaCurrencyFormat() : NumberFormat{  //  ex : indoCurrencyFormat().format(10000)
    val format = NumberFormat.getCurrencyInstance()
    format.maximumFractionDigits = 0
    format.currency = Currency.getInstance("IDR")
    return  format
}

fun receiptFormat(number: Int) : String = "#"+String.format("%05d",number)

fun showError(context: Context,message: String)
{
    ErrorActivity.errorMessage = message
    context.startActivity<ErrorActivity>()
}