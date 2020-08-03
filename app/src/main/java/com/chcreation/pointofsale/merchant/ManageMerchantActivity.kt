package com.chcreation.pointofsale.merchant

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.chcreation.pointofsale.*
import com.chcreation.pointofsale.model.AvailableMerchant
import com.chcreation.pointofsale.model.Merchant
import com.chcreation.pointofsale.presenter.MerchantPresenter
import com.chcreation.pointofsale.view.MainView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_manage_merchant.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.selector
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.toast
import java.io.File
import java.io.IOException
import java.util.*

class ManageMerchantActivity : AppCompatActivity(), MainView {

    private lateinit var sharedPreference: SharedPreferences
    lateinit var editor: SharedPreferences.Editor
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDatabase : DatabaseReference
    private lateinit var presenter: MerchantPresenter
    private var merchant = Merchant()
    private var PICK_IMAGE_CAMERA  = 111
    private var CAMERA_PERMISSION  = 101
    private var PICK_IMAGE_GALLERY = 222
    private var READ_PERMISION = 202
    private var filePath: Uri? = null
    private var currentPhotoPath = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_merchant)

        mAuth = FirebaseAuth.getInstance()
        mDatabase = FirebaseDatabase.getInstance().reference
        presenter = MerchantPresenter(this,mAuth,mDatabase, this)
        sharedPreference =  this.getSharedPreferences("LOCAL_DATA", Context.MODE_PRIVATE)

        GlobalScope.launch {
            presenter.retrieveMerchants()
        }
    }

    override fun onStart() {
        super.onStart()

        btnNewMerchant.onClick {

            btnNewMerchant.startAnimation(normalClickAnimation())
            btnNewMerchant.isEnabled = false
            pbMerchant.visibility = View.VISIBLE

            val merchantBusinessInfo = etMerchantBusinessInfo.text.toString()
            val merchantNoTelp = etMerchantNoTelp.text.toString()
            val merchantAddress = etMerchantAddress.text.toString()
            val merchantName = etMerchantName.text.toString()
            val currentDate = dateFormat().format(Date())

            if (merchantName == ""){
                toast("Please Fill Merchant Name !")
                return@onClick
            }
            if (merchant == Merchant())
            presenter.createNewMerchant(Merchant(merchantName,merchantBusinessInfo,merchantAddress,merchantNoTelp,"",
                currentDate,currentDate, mAuth.currentUser!!.uid, mAuth.currentUser!!.uid),
                AvailableMerchant(merchantName,EUserGroup.MANAGER.toString(),currentDate,currentDate,
                    mAuth.currentUser!!.uid,EStatusUser.ACTIVE.toString()))
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
                        openCamera()
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

    private fun fetchData(){
        etMerchantAddress.setText(merchant.ADDRESS)
        etMerchantBusinessInfo.setText(merchant.BUSINESS_INFO)
        etMerchantName.setText(merchant.NAME)
        etMerchantNoTelp.setText(merchant.NO_TELP)
    }

    override fun loadData(dataSnapshot: DataSnapshot, response: String) {
        if (response == EMessageResult.FETCH_MERCHANT_SUCCESS.toString()){
            if (dataSnapshot.exists()){
                val item = dataSnapshot.getValue(Merchant::class.java)
                merchant = item!!

                fetchData()
            }
        }
    }

    override fun response(message: String) {
        if (message == EMessageResult.SUCCESS.toString())
        {
            editor = sharedPreference.edit()
            editor.putString(ESharedPreference.MERCHANT.toString(),etMerchantName.text.toString())
            editor.putString(ESharedPreference.USER_GROUP.toString(),EUserGroup.MANAGER.toString())
            editor.putString(ESharedPreference.MERCHANT_CREDENTIAL.toString(), mAuth.currentUser?.uid)
            editor.apply()

            toast("Create Merchant Success")

            startActivity<MainActivity>()
            finish()
        }
        btnNewMerchant.isEnabled = true
        pbMerchant.visibility = View.GONE
    }
}
