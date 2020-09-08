package com.chcreation.pointofsale

import android.content.Context
import android.content.SharedPreferences
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.TranslateAnimation
import androidx.core.view.marginBottom
import org.jetbrains.anko.startActivity
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

private lateinit var sharedPreference: SharedPreferences

var RESULT_CLOSE_ALL = 1111

fun removeAllSharedPreference(context: Context){
    sharedPreference =  context.getSharedPreferences("LOCAL_DATA", Context.MODE_PRIVATE)
    val editor = sharedPreference.edit()
    editor.putString(ESharedPreference.NAME.toString(),"")
    editor.putString(ESharedPreference.EMAIL.toString(),"")
    editor.putString(ESharedPreference.MERCHANT.toString(),"")
    editor.putString(ESharedPreference.MERCHANT_CREDENTIAL.toString(),"")
    editor.putString(ESharedPreference.NO_TELP.toString(),"")
    editor.putString(ESharedPreference.USER_GROUP.toString(),"")
    editor.putString(ESharedPreference.ADDRESS.toString(),"")
    editor.putString(ESharedPreference.CUSTOM_RECEIPT.toString(),ECustomReceipt.RECEIPT1.toString())
    editor.putString(ESharedPreference.SINCERE.toString(),"Thank You")
    editor.putBoolean(ESharedPreference.CUSTOMER_ADDRESS.toString(),false)
    editor.putBoolean(ESharedPreference.CUSTOMER_NO_TEL.toString(),false)
    editor.putBoolean(ESharedPreference.CUSTOMER_NAME.toString(),false)
    editor.putBoolean(ESharedPreference.RECEIPT_MERCHANT_ICON.toString(),false)
    editor.apply()
}

fun getName(context: Context) : String{
    sharedPreference =  context.getSharedPreferences("LOCAL_DATA", Context.MODE_PRIVATE)

    return sharedPreference.getString(ESharedPreference.NAME.toString(),"").toString()
}

fun getEmail(context: Context) : String{
    sharedPreference =  context.getSharedPreferences("LOCAL_DATA", Context.MODE_PRIVATE)

    return sharedPreference.getString(ESharedPreference.EMAIL.toString(),"").toString()
}

fun getMerchant(context: Context) : String{
    sharedPreference =  context.getSharedPreferences("LOCAL_DATA", Context.MODE_PRIVATE)

    return sharedPreference.getString(ESharedPreference.MERCHANT.toString(),"").toString()
}

fun getMerchantCredential(context: Context) : String{
    sharedPreference =  context.getSharedPreferences("LOCAL_DATA", Context.MODE_PRIVATE)

    return sharedPreference.getString(ESharedPreference.MERCHANT_CREDENTIAL.toString(),"").toString()
}

fun getMerchantImage(context: Context) : String{
    sharedPreference =  context.getSharedPreferences("LOCAL_DATA", Context.MODE_PRIVATE)

    return sharedPreference.getString(ESharedPreference.MERCHANT_IMAGE.toString(),"").toString()
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

fun getMerchantSincere(context: Context) : String{
    sharedPreference =  context.getSharedPreferences("LOCAL_DATA", Context.MODE_PRIVATE)

    return sharedPreference.getString(ESharedPreference.SINCERE.toString(),"Thank You").toString()
}

fun getMerchantReceiptCustName(context: Context) : Boolean{
    sharedPreference =  context.getSharedPreferences("LOCAL_DATA", Context.MODE_PRIVATE)

    return sharedPreference.getBoolean(ESharedPreference.CUSTOMER_NAME.toString(),false)
}

fun getMerchantReceiptCustNoTel(context: Context) : Boolean{
    sharedPreference =  context.getSharedPreferences("LOCAL_DATA", Context.MODE_PRIVATE)

    return sharedPreference.getBoolean(ESharedPreference.CUSTOMER_NO_TEL.toString(),false)
}

fun getMerchantReceiptCustAddress(context: Context) : Boolean{
    sharedPreference =  context.getSharedPreferences("LOCAL_DATA", Context.MODE_PRIVATE)

    return sharedPreference.getBoolean(ESharedPreference.CUSTOMER_ADDRESS.toString(),false)
}

fun getMerchantReceiptImage(context: Context) : Boolean{
    sharedPreference =  context.getSharedPreferences("LOCAL_DATA", Context.MODE_PRIVATE)

    return sharedPreference.getBoolean(ESharedPreference.RECEIPT_MERCHANT_ICON.toString(),false)
}

fun getMerchantReceiptTemplate(context: Context) : String{
    sharedPreference =  context.getSharedPreferences("LOCAL_DATA", Context.MODE_PRIVATE)

    return sharedPreference.getString(ESharedPreference.CUSTOM_RECEIPT.toString(),ECustomReceipt.RECEIPT1.toString()).toString()
}

fun normalClickAnimation() : AlphaAnimation = AlphaAnimation(10F,0.5F)

fun slideUp(view: View) {
    view.setVisibility(View.VISIBLE)
    val animate = TranslateAnimation(
        0F,  // fromXDelta
        0F,  // toXDelta
        view.height.toFloat() + view.marginBottom.toFloat(),  // fromYDelta
        0F
    ) // toYDelta
    animate.duration = 500
    animate.fillAfter = true
    view.startAnimation(animate)
}

// slide the view from its current position to below itself
fun slideDown(view: View) {
    val animate = TranslateAnimation(
        0F,  // fromXDelta
        0F,  // toXDelta
        0F,  // fromYDelta
        view.height.toFloat() + view.marginBottom.toFloat()
    ) // toYDelta
    animate.duration = 500
    animate.fillAfter = true
    view.startAnimation(animate)
}

fun dateFormat() : SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

fun getYear(convertDate: String): Int {
    val date = dateFormat().parse(convertDate)
    val calendar = Calendar.getInstance()
    calendar.time = date
    return calendar.get(Calendar.YEAR)
}

fun getMonth(convertDate: String): Int {
    val date = dateFormat().parse(convertDate)
    val calendar = Calendar.getInstance()
    calendar.time = date
    return calendar.get(Calendar.MONTH)
}

fun getCurrentMonth(): Int {
    val calendar = Calendar.getInstance()
    return calendar.get(Calendar.MONTH)
}

fun getCurrentYear(): Int {
    val calendar = Calendar.getInstance()
    return calendar.get(Calendar.YEAR)
}

fun getDateOfMonth(convertDate: String): Int {
    val date = dateFormat().parse(convertDate)
    val calendar = Calendar.getInstance()
    calendar.time = date
    return calendar.get(Calendar.DAY_OF_MONTH)
}

fun parseDateFormat(date: String) : String {
    val currentFormat = dateFormat().parse(date)
    val newFormat = SimpleDateFormat("dd MMM yyyy").format(currentFormat).toString()

    return newFormat
}

fun parseDateFormatFull(date: String) : String {
    val currentFormat = dateFormat().parse(date)
    val newFormat = SimpleDateFormat("dd MMM yyyy HH:mm:ss").format(currentFormat).toString()

    return newFormat
}

fun parseTimeFormat(date: String) : String {
    var currentFormat = dateFormat().parse(date)
    var newFormat = SimpleDateFormat("HH:mm:ss").format(currentFormat).toString()

    return newFormat
}

fun indonesiaCurrencyFormat() : NumberFormat{  //  ex : indoCurrencyFormat().format(10000)
    val locale = Locale("in","ID")
    val format = NumberFormat.getCurrencyInstance(locale)
//    format.maximumFractionDigits = 0
//    format.currency = Currency.getInstance("IDR")
    return  format
}

fun receiptFormat(number: Int) : String {
    var value = ""
    if (number < 10000)
        value = "#"+String.format("%05d",number)
    else if (number < 100000)
        value = "#"+String.format("%06d",number)
    else
        value = "#"+String.format("%07d",number)

    return value
}

fun showError(context: Context,message: String)
{
    ErrorActivity.errorMessage = message
    context.startActivity<ErrorActivity>()
}

fun encodeEmail(email:String): String{
    val index = if (email == "") 0 else email.indexOf('.',0)
    return email.substring(0,index)
}