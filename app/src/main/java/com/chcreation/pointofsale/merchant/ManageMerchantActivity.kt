package com.chcreation.pointofsale.merchant

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.widget.doOnTextChanged
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.GlideDrawable
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.chcreation.pointofsale.*
import com.chcreation.pointofsale.checkout.PostCheckOutActivity
import com.chcreation.pointofsale.login.LoginActivity
import com.chcreation.pointofsale.model.ActivityLogs
import com.chcreation.pointofsale.model.AvailableMerchant
import com.chcreation.pointofsale.model.Merchant
import com.chcreation.pointofsale.presenter.MerchantPresenter
import com.chcreation.pointofsale.view.MainView
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import kotlinx.android.synthetic.main.activity_manage_merchant.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.support.v4.startActivity
import java.io.File
import java.io.IOException
import java.util.*

class ManageMerchantActivity : AppCompatActivity(), MainView {

    private lateinit var sharedPreference: SharedPreferences
    lateinit var editor: SharedPreferences.Editor
    private lateinit var mAuth: FirebaseAuth
    private lateinit var storage: StorageReference
    private lateinit var mDatabase : DatabaseReference
    private lateinit var presenter: MerchantPresenter
    private var merchant: Merchant? = null
    private var PICK_IMAGE_CAMERA  = 111
    private var CAMERA_PERMISSION  = 101
    private var PICK_IMAGE_GALLERY = 222
    private var READ_PERMISION = 202
    private var filePath: Uri? = null
    private var downloadUri: Uri? = null
    private var currentPhotoPath = ""
    private var bitmap: Bitmap? = null
    private var merchantCode = ""
    private var selectedLanguage = Locale.getDefault().language
    private var selectedCountry = Locale.getDefault().country
    private lateinit var spCurrencyAdapter: ArrayAdapter<String>
    private var languageItems = mutableListOf<String>("fr","en","en","ms","zh","in","tai","zh")
    private var countryItems = mutableListOf<String>("FR","US","GB","MY","SG","ID","TH","CN")
    private var currencyItems = mutableListOf<String>("Euro","US Dollar","Pound Sterling","Ringgit","SG Dollar","Rupiah","Thai Baht","Yuan")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_merchant)

        supportActionBar?.title = "Set Up Merchant"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        mAuth = FirebaseAuth.getInstance()
        mDatabase = FirebaseDatabase.getInstance().reference
        storage = FirebaseStorage.getInstance().reference
        presenter = MerchantPresenter(this,mAuth,mDatabase, this)
        sharedPreference =  this.getSharedPreferences("LOCAL_DATA", Context.MODE_PRIVATE)

        spMerchantCurrency.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                selectedCountry = countryItems[position]
                selectedLanguage = languageItems[position]
            }

        }

        spCurrencyAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item,currencyItems)
        spCurrencyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spMerchantCurrency.adapter = spCurrencyAdapter

        btnMerchant.isEnabled = false
        GlobalScope.launch {
            presenter.retrieveCurrentMerchant()
        }

        layoutMerchantDefaultImage.onClick {
            if (getMerchantCode(this@ManageMerchantActivity) == "")
                toast("You Can Add Photo After Create Merchant")
            else{
                selectImage()
            }
        }

        ivMerchantImage.onClick {
            selectImage()
        }

    }

    override fun onStart() {
        super.onStart()

        btnMerchant.onClick {
            btnMerchant.startAnimation(normalClickAnimation())
            pbMerchant.visibility = View.VISIBLE
            btnMerchant.isEnabled = false

            if (filePath != null)
                uploadImage()
            else
                saveMerchant("")

        }

        etMerchantName.doOnTextChanged { text, start, before, count ->
            if (!text.isNullOrEmpty()) {
                tvMerchantFirstName.text = text.first().toString().toUpperCase(Locale.getDefault())
            }
        }

    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            if (getMerchantCredential(this) == "")
                alert ("Do You Want to Exit?"){
                    title = "Exit"
                    yesButton {
                        removeAllSharedPreference(this@ManageMerchantActivity)
                        PostCheckOutActivity().clearCartData()
                        startActivity<LoginActivity>()
                        finish()
                    }
                    noButton {

                    }
                }.show()
            else
                finish()
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        if (getMerchantCredential(this) == "")
            alert ("Do You Want to Exit?"){
                title = "Exit"
                yesButton {
                    removeAllSharedPreference(this@ManageMerchantActivity)
                    PostCheckOutActivity().clearCartData()
                    startActivity<LoginActivity>()
                    super.onBackPressed()
                }
                noButton {

                }
            }.show()
        else
            super.onBackPressed()
    }

    private fun uploadImage(){
        if(filePath != null){
            val ref = storage.child(ETable.MERCHANT.toString())
                .child(mAuth.currentUser!!.uid)
                .child("${getMerchantCredential(this)}/${merchant!!.NAME.toString()}")

            val uploadTask = ref.putFile(filePath!!)

            val urlTask = uploadTask.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
                if (!task.isSuccessful) {
                    task.exception?.let {
                        throw it
                    }
                }
                return@Continuation ref.downloadUrl
            }).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    downloadUri = task.result
                    saveMerchant(downloadUri.toString())
                } else {
                    toast("Failed to Save Image")
                }
            }.addOnFailureListener{
                toast("Error : ${it.message}")
            }
        }else{
            Toast.makeText(this, "Please Upload an Image", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveMerchant(imageUrl: String){
        val merchantBusinessInfo = etMerchantBusinessInfo.text.toString()
        val merchantNoTelp = etMerchantNoTelp.text.toString()
        val merchantAddress = etMerchantAddress.text.toString()
        val merchantName = etMerchantName.text.toString()
        val currentDate = dateFormat().format(Date())

        if (merchantName == ""){
            toast("Please Fill Merchant Name !")
            pbMerchant.visibility = View.GONE
            btnMerchant.isEnabled = true
        }
        else if (merchant!!.NAME == "" && getMerchantCredential(this) == ""){
            alert("Continue to Create Merchant?"){
                title = "Confirmation!"
                yesButton {
                    btnMerchant.isEnabled = false
                    pbMerchant.visibility = View.VISIBLE
                    merchantCode = generateMerchantCode()
                    presenter.createNewMerchant(Merchant(merchantName,merchantBusinessInfo,merchantAddress,merchantNoTelp,"",null,null,
                        currentDate,currentDate, mAuth.currentUser!!.uid, mAuth.currentUser!!.uid,merchantCode
                        ,EMerchantMemberStatus.FREE_TRIAL.toString(),currentDate,EStatusUser.ACTIVE.toString(),selectedLanguage,selectedCountry,""),
                        AvailableMerchant(merchantName,EUserGroup.MANAGER.toString(),currentDate,currentDate,
                            mAuth.currentUser!!.uid,EStatusUser.ACTIVE.toString(),merchantCode))

                }
                noButton {
                    pbMerchant.visibility = View.GONE
                    btnMerchant.isEnabled = true
                }
            }.show()
        }
        else{
            if (getMerchantUserGroup(this@ManageMerchantActivity) == EUserGroup.WAITER.toString()){
                pbMerchant.visibility = View.GONE
                btnMerchant.isEnabled = true
                toast("Only Manager Can Update Merchant Status")
            }
            else{
                val image = if (imageUrl == "") merchant!!.IMAGE else imageUrl
                merchantCode = if (merchant!!.MERCHANT_CODE == "") merchant!!.NAME.toString() else merchant!!.MERCHANT_CODE.toString()

                btnMerchant.isEnabled = false
                pbMerchant.visibility = View.VISIBLE
                presenter.updateMerchant(Merchant(merchantName,merchantBusinessInfo,merchantAddress,merchantNoTelp,image,
                    merchant!!.USER_LIST,merchant!!.CAT,merchant!!.CREATED_DATE,
                    dateFormat().format(Date()),merchant!!.CREATED_BY,
                    mAuth.currentUser!!.uid,merchantCode,merchant!!.MEMBER_STATUS,merchant!!.MEMBER_DEADLINE,merchant!!.ACTIVE,selectedLanguage,selectedCountry,""),merchantName)

                presenter.saveActivityLogs(ActivityLogs("Update Merchant",mAuth.currentUser!!.uid,dateFormat().format(Date())))
            }
        }
    }

    private fun selectImage() { // Defining Implicit Intent to mobile gallery

        intent = Intent()
        val options = mutableListOf("Take a Photo", "Pick from Gallery")


        selector("select image",options) {
                dialogInterface, i ->
            when(i){
                0 -> {
                    if (ContextCompat.checkSelfPermission(this@ManageMerchantActivity, android.Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED)
                        ActivityCompat.requestPermissions(this@ManageMerchantActivity,
                            arrayOf(android.Manifest.permission.CAMERA),CAMERA_PERMISSION
                        )
                    else
                        openCamera()

                }
                1 -> {
                    if (ContextCompat.checkSelfPermission(this@ManageMerchantActivity, android.Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED)
                        ActivityCompat.requestPermissions(this@ManageMerchantActivity,
                            arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE,android.Manifest.permission.WRITE_EXTERNAL_STORAGE),READ_PERMISION
                        )
                    else
                        openGallery()
                }
            }
        }

    }
    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp: String = dateFormat().format(Date(1000))
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            currentPhotoPath = "$absolutePath"

        }

    }
    private fun openCamera(){
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            // Ensure that there's a camera activity to handle the intent
            takePictureIntent.resolveActivity(packageManager)?.also {
                // Create the File where the photo should go
                val photoFile: File? = try {
                    createImageFile()
                } catch (ex: IOException) {
                    null
                }
                // Continue only if the File was successfully created
                photoFile?.also {
                    val photoURI: Uri = FileProvider.getUriForFile(
                        this,
                        "com.example.android.fileprovider",
                        it
                    )
                    Log.d("uri: ",photoURI.toString())
                    filePath = photoURI
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    startActivityForResult(takePictureIntent, PICK_IMAGE_CAMERA)
                }
            }
        }
    }

    private fun openGallery(){
        intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(
            intent,
            PICK_IMAGE_GALLERY
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_CAMERA && resultCode == Activity.RESULT_OK) {
            try {
                bitmap = MediaStore.Images.Media
                    .getBitmap(
                        contentResolver,
                        filePath
                    )
                layoutMerchantDefaultImage.visibility = View.GONE
                ivMerchantImage.visibility = View.VISIBLE
                ivMerchantImage.setImageBitmap(rotateImage(
                    bitmap
                ))
            } catch (e: Exception) {
                filePath = null
                showError(this,e.message.toString())
                e.printStackTrace()
            }
        }
        else if (requestCode == PICK_IMAGE_GALLERY && resultCode == Activity.RESULT_OK && data != null && data.data != null
        ) { // Get the Uri of data
            filePath = data.data
            currentPhotoPath = getRealPathFromURI(
                filePath!!)
            try { // Setting image on image view using Bitmap
                bitmap = MediaStore.Images.Media
                    .getBitmap(
                        contentResolver,
                        filePath
                    )
                //currentPhotoPath = photoURI.toString()
                layoutMerchantDefaultImage.visibility = View.GONE
                ivMerchantImage.visibility = View.VISIBLE
                ivMerchantImage.setImageBitmap(rotateImage(
                    bitmap
                ))
            } catch (e: IOException) { // Log the exception
                showError(this,e.message.toString())
                e.printStackTrace()
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == CAMERA_PERMISSION){
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                openCamera()
            }
            else
                toast("Permission Denied")
        }
        else if (requestCode == READ_PERMISION){
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                openGallery()
            }
            else
                toast("Permission Denied")
        }
    }

    fun rotateImage(bitmapSource : Bitmap?) : Bitmap {
        val ei = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ExifInterface(File(currentPhotoPath))
        } else {
            ExifInterface(currentPhotoPath)
        }
        val orientation = ei.getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_UNDEFINED);

        var rotatedBitmap = null;
        var degree = 0F
        when(orientation) {

            ExifInterface.ORIENTATION_ROTATE_90 -> {
                degree = 90F
            }
            ExifInterface.ORIENTATION_ROTATE_180 -> {
                degree = 180F
            }
            ExifInterface.ORIENTATION_ROTATE_270 -> {
                degree = 270F
            }
            ExifInterface.ORIENTATION_NORMAL -> {
                degree = 360F
            }
        }

        val matrix = Matrix()
        matrix.postRotate(degree)
        return Bitmap.createBitmap(
            bitmapSource!!, 0, 0, bitmapSource.width, bitmapSource.height,
            matrix, true)
    }

    fun getRealPathFromURI(uri: Uri): String {
        @SuppressWarnings("deprecation")
        val cursor = this.managedQuery(uri, arrayOf(MediaStore.Images.Media.DATA), null, null, null);
        val column_index = cursor
            .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    private fun fetchData(){
        if (merchant!!.NAME != ""){

            btnMerchant.text = "Save"

            supportActionBar?.title = "Update Merchant"

            etMerchantAddress.setText(merchant!!.ADDRESS)
            etMerchantBusinessInfo.setText(merchant!!.BUSINESS_INFO)
            etMerchantName.setText(merchant!!.NAME)
            //etMerchantName.isEnabled = false
            etMerchantNoTelp.setText(merchant!!.NO_TELP)

            spMerchantCurrency.setSelection(languageItems.indexOf(merchant!!.LANGUAGE))

            if (merchant!!.IMAGE == ""){
                layoutMerchantDefaultImage.visibility = View.VISIBLE
                ivMerchantImage.visibility = View.GONE
                tvMerchantFirstName.text = merchant!!.NAME!!.first().toString().toUpperCase(Locale.getDefault())
            }else{
                layoutMerchantDefaultImage.visibility = View.GONE
                ivMerchantImage.visibility = View.VISIBLE

                pbMerchant.visibility = View.VISIBLE
                Glide.with(this).load(merchant!!.IMAGE).listener(object :
                    RequestListener<String, GlideDrawable> {
                    override fun onException(
                        e: java.lang.Exception?,
                        model: String?,
                        target: Target<GlideDrawable>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        pbMerchant.visibility = View.GONE
                        return false
                    }

                    override fun onResourceReady(
                        resource: GlideDrawable?,
                        model: String?,
                        target: Target<GlideDrawable>?,
                        isFromMemoryCache: Boolean,
                        isFirstResource: Boolean
                    ): Boolean {
                        pbMerchant.visibility = View.GONE
                        return false
                    }

                }).into(ivMerchantImage)
            }
        }
    }

    private fun generateMerchantCode() : String{
        return "M${mDatabase.push().key.toString()}"
    }

    override fun loadData(dataSnapshot: DataSnapshot, response: String) {
        if (response == EMessageResult.FETCH_MERCHANT_SUCCESS.toString()){
            if (dataSnapshot.exists()){
                val item = dataSnapshot.getValue(Merchant::class.java)
                merchant = item!!

            }
            btnMerchant.isEnabled = true
            pbMerchant.visibility = View.GONE
            fetchData()
        }
    }

    override fun response(message: String) {
        if (message == EMessageResult.SUCCESS.toString())
        {
            editor = sharedPreference.edit()
            editor.putString(ESharedPreference.MERCHANT_CODE.toString(),merchantCode)
            editor.putString(ESharedPreference.MERCHANT_NAME.toString(),etMerchantName.text.toString())
            editor.putString(ESharedPreference.USER_GROUP.toString(),EUserGroup.MANAGER.toString())
            editor.putString(ESharedPreference.MERCHANT_CREDENTIAL.toString(), mAuth.currentUser?.uid)
            editor.putString(ESharedPreference.ADDRESS.toString(),etMerchantAddress.text.toString())
            editor.putString(ESharedPreference.NO_TELP.toString(),etMerchantNoTelp.text.toString())
            editor.putString(ESharedPreference.MERCHANT_MEMBER_STATUS.toString(),EMerchantMemberStatus.FREE_TRIAL.toString())
            editor.putString(ESharedPreference.COUNTRY.toString(),selectedCountry)
            editor.putString(ESharedPreference.LANGUAGE.toString(),selectedLanguage)
            editor.apply()

            toast("Create Merchant Success")

            startActivity<MainActivity>()
            finish()

            btnMerchant.isEnabled = true
            pbMerchant.visibility = View.GONE
        }
        if (message == EMessageResult.UPDATE.toString())
        {
            val image = if (downloadUri.toString() == "" || downloadUri == null) merchant!!.IMAGE else downloadUri.toString()

            editor = sharedPreference.edit()
            editor.putString(ESharedPreference.ADDRESS.toString(),etMerchantAddress.text.toString())
            editor.putString(ESharedPreference.NO_TELP.toString(),etMerchantNoTelp.text.toString())
            editor.putString(ESharedPreference.MERCHANT_NAME.toString(),etMerchantName.text.toString())
            editor.putString(ESharedPreference.MERCHANT_IMAGE.toString(),image)
            editor.putString(ESharedPreference.MERCHANT_CODE.toString(),merchantCode)
            editor.putString(ESharedPreference.COUNTRY.toString(),selectedCountry)
            editor.putString(ESharedPreference.LANGUAGE.toString(),selectedLanguage)
            editor.apply()

            toast("Update Success")
            finish()

            btnMerchant.isEnabled = true
            pbMerchant.visibility = View.GONE
        }else{
            toast(message)
            btnMerchant.isEnabled = true
            pbMerchant.visibility = View.GONE
        }

    }
}
