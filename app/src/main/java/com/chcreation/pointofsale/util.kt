package com.chcreation.pointofsale

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.TranslateAnimation
import androidx.core.view.marginBottom
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.toast
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*


private lateinit var sharedPreference: SharedPreferences

var RESULT_CLOSE_ALL = 1111

fun removeAllSharedPreference(context: Context){
    sharedPreference =  context.getSharedPreferences("LOCAL_DATA", Context.MODE_PRIVATE)
    val editor = sharedPreference.edit()
    editor.putString(ESharedPreference.NAME.toString(),"")
    editor.putString(ESharedPreference.EMAIL.toString(),"")
    editor.putString(ESharedPreference.MERCHANT_NAME.toString(),"")
    editor.putString(ESharedPreference.MERCHANT_CODE.toString(),"")
    editor.putString(ESharedPreference.MERCHANT_IMAGE.toString(),"")
    editor.putString(ESharedPreference.MERCHANT_CREDENTIAL.toString(),"")
    editor.putString(ESharedPreference.MEMBER_DEADLINE.toString(),"")
    editor.putString(ESharedPreference.NO_TELP.toString(),"")
    editor.putString(ESharedPreference.USER_GROUP.toString(),"")
    editor.putString(ESharedPreference.ADDRESS.toString(),"")
    editor.putString(ESharedPreference.CUSTOM_RECEIPT.toString(),ECustomReceipt.RECEIPT1.toString())
    editor.putString(ESharedPreference.SINCERE.toString(),"Thank You")
    editor.putString(ESharedPreference.MERCHANT_MEMBER_STATUS.toString(),EMerchantMemberStatus.FREE_TRIAL.toString())
    editor.putString(ESharedPreference.LANGUAGE.toString(),Locale.getDefault().language)
    editor.putString(ESharedPreference.COUNTRY.toString(),Locale.getDefault().country)
    editor.putBoolean(ESharedPreference.CUSTOMER_ADDRESS.toString(),false)
    editor.putBoolean(ESharedPreference.CUSTOMER_NO_TEL.toString(),false)
    editor.putBoolean(ESharedPreference.CUSTOMER_NAME.toString(),false)
    editor.putBoolean(ESharedPreference.RECEIPT_MERCHANT_ICON.toString(),false)
    editor.apply()
}

fun removeMerchantSharedPreference(context: Context){
    sharedPreference =  context.getSharedPreferences("LOCAL_DATA", Context.MODE_PRIVATE)
    val editor = sharedPreference.edit()
    editor.putString(ESharedPreference.MERCHANT_NAME.toString(),"")
    editor.putString(ESharedPreference.MERCHANT_CODE.toString(),"")
    editor.putString(ESharedPreference.MERCHANT_IMAGE.toString(),"")
    editor.putString(ESharedPreference.MERCHANT_CREDENTIAL.toString(),"")
    editor.putString(ESharedPreference.MEMBER_DEADLINE.toString(),"")
    editor.putString(ESharedPreference.NO_TELP.toString(),"")
    editor.putString(ESharedPreference.USER_GROUP.toString(),"")
    editor.putString(ESharedPreference.ADDRESS.toString(),"")
    editor.putString(ESharedPreference.CUSTOM_RECEIPT.toString(),ECustomReceipt.RECEIPT1.toString())
    editor.putString(ESharedPreference.SINCERE.toString(),"Thank You")
    editor.putString(ESharedPreference.MERCHANT_MEMBER_STATUS.toString(),EMerchantMemberStatus.FREE_TRIAL.toString())
    editor.putString(ESharedPreference.LANGUAGE.toString(),Locale.getDefault().language)
    editor.putString(ESharedPreference.COUNTRY.toString(),Locale.getDefault().country)
    editor.putBoolean(ESharedPreference.CUSTOMER_ADDRESS.toString(),false)
    editor.putBoolean(ESharedPreference.CUSTOMER_NO_TEL.toString(),false)
    editor.putBoolean(ESharedPreference.CUSTOMER_NAME.toString(),false)
    editor.putBoolean(ESharedPreference.RECEIPT_MERCHANT_ICON.toString(),false)
    editor.apply()
}

fun getLanguage(context: Context) : String{
    sharedPreference =  context.getSharedPreferences("LOCAL_DATA", Context.MODE_PRIVATE)

    return sharedPreference.getString(ESharedPreference.LANGUAGE.toString(),Locale.getDefault().language).toString()
}

fun getCountry(context: Context) : String{
    sharedPreference =  context.getSharedPreferences("LOCAL_DATA", Context.MODE_PRIVATE)

    return sharedPreference.getString(ESharedPreference.COUNTRY.toString(),Locale.getDefault().country).toString()
}

fun getDeviceId(context: Context) : String{
    sharedPreference =  context.getSharedPreferences("LOCAL_DATA", Context.MODE_PRIVATE)

    return sharedPreference.getString(ESharedPreference.DEVICE_ID.toString(),"").toString()
}

fun getName(context: Context) : String{
    sharedPreference =  context.getSharedPreferences("LOCAL_DATA", Context.MODE_PRIVATE)

    return sharedPreference.getString(ESharedPreference.NAME.toString(),"").toString()
}

fun getEmail(context: Context) : String{
    sharedPreference =  context.getSharedPreferences("LOCAL_DATA", Context.MODE_PRIVATE)

    return sharedPreference.getString(ESharedPreference.EMAIL.toString(),"").toString()
}

fun getMerchantCode(context: Context) : String{
    sharedPreference =  context.getSharedPreferences("LOCAL_DATA", Context.MODE_PRIVATE)

    return sharedPreference.getString(ESharedPreference.MERCHANT_CODE.toString(),"").toString()
}

fun getMerchantName(context: Context) : String{
    sharedPreference =  context.getSharedPreferences("LOCAL_DATA", Context.MODE_PRIVATE)

    return sharedPreference.getString(ESharedPreference.MERCHANT_NAME.toString(),"").toString()
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

fun getMerchantMemberStatus(context: Context) : String{
    sharedPreference =  context.getSharedPreferences("LOCAL_DATA", Context.MODE_PRIVATE)

    return sharedPreference.getString(ESharedPreference.MERCHANT_MEMBER_STATUS.toString()
        ,EMerchantMemberStatus.FREE_TRIAL.toString()).toString()
}

fun getMerchantMemberDeadline(context: Context) : String{
    sharedPreference =  context.getSharedPreferences("LOCAL_DATA", Context.MODE_PRIVATE)

    return sharedPreference.getString(ESharedPreference.MEMBER_DEADLINE.toString(),"").toString()
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

fun getWeekOfMonth(convertDate: String): Int {
    val date = dateFormat().parse(convertDate)
    val calendar = Calendar.getInstance()
    calendar.time = date
    return calendar.get(Calendar.WEEK_OF_MONTH)
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

fun compareDate(firstDate: String,secondDate: String) : Int {
    val date1 = dateFormat().parse(firstDate)
    val date2 = dateFormat().parse(secondDate)
    return if (date1.after(date2))
        1
    else
        2
}

fun currencyFormat(language: String, country: String) : NumberFormat{  //  ex : currencyFormat().format(10000)
    val locale = Locale(language,country)
    //    format.maximumFractionDigits = 0
//    format.currency = Currency.getInstance("IDR")
    return NumberFormat.getCurrencyInstance(locale)
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

fun isInt(n: Number) = n.toDouble() % 1 == 0.0

fun showError(context: Context,message: String)
{
    ErrorActivity.errorMessage = message
    context.startActivity<ErrorActivity>()
}


fun sendEmail(subject: String,text: String,context: Context){
    val mIntent = Intent(Intent.ACTION_SEND)
    mIntent.data = Uri.parse("mailto:")
    mIntent.type = "text/plain"
    mIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf("ch.creation1608@gmail.com"))
    mIntent.putExtra(Intent.EXTRA_SUBJECT, subject)
    mIntent.putExtra(Intent.EXTRA_TEXT, text)
    try {
        context.startActivity(Intent.createChooser(mIntent, "Choose Email Application"))
    }
    catch (e: Exception){
        context.toast(e.message.toString())
        e.printStackTrace()
    }
}

fun openGooglePlay(context: Context){
    val appPackageName: String = context.packageName // getPackageName() from Context or Activity object

    try {
        context.startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse("market://details?id=$appPackageName")
            )
        )
    } catch (anfe: ActivityNotFoundException) {
        context.startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://play.google.com/store/apps/details?id=$appPackageName")
            )
        )
    }
}

fun encodeEmail(email:String): String{
    val index = if (email == "") 0 else email.indexOf('.',0)
    return email.substring(0,index)
}

//https://www.websitepolicies.com/policies/view/mjijhUBA
//ch.creation1608@gmail.com
//3634315896