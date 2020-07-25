package com.chcreation.pointofsale

import android.content.Context
import android.content.SharedPreferences
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

private lateinit var sharedPreference: SharedPreferences

fun getMerchant(context: Context) : String{
    sharedPreference =  context.getSharedPreferences("LOCAL_DATA", Context.MODE_PRIVATE)

    return sharedPreference.getString("merchant","").toString()
}

fun normalClickAnimation() : AlphaAnimation = AlphaAnimation(3F,0.6F)

fun dateFormat() : SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

fun indonesiaCurrencyFormat() : NumberFormat{  //  ex : indoCurrencyFormat().format(10000)
    val format = NumberFormat.getCurrencyInstance()
    format.maximumFractionDigits = 0
    format.currency = Currency.getInstance("IDR")
    return  format
}

fun receiptFormat(number: Int) : String = "#"+String.format("%05d",number)