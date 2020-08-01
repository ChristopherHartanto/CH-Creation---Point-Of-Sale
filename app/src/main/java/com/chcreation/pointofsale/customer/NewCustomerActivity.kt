package com.chcreation.pointofsale.customer

import android.app.Activity
import android.content.Intent
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
import android.widget.ArrayAdapter
import androidx.core.content.FileProvider
import com.chcreation.pointofsale.*
import com.chcreation.pointofsale.model.Customer
import com.chcreation.pointofsale.presenter.CustomerPresenter
import com.chcreation.pointofsale.presenter.ProductPresenter
import com.chcreation.pointofsale.view.MainView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.activity_new_customer.*
import kotlinx.android.synthetic.main.activity_new_product.*
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.selector
import org.jetbrains.anko.toast
import java.io.File
import java.io.IOException
import java.util.*

class NewCustomerActivity : AppCompatActivity(), MainView {


    private lateinit var presenter: CustomerPresenter
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDatabase : DatabaseReference
    private lateinit var storage: StorageReference
    private var categoryItems : MutableList<String> = mutableListOf()
    private var selectedCategory = ""
    private var positionSpinner = 0
    private var manageStock = true
    private var PICK_IMAGE_CAMERA  = 111
    private var PICK_IMAGE_GALLERY = 222
    private var currentPhotoPath = ""
    private var filePath:Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_customer)

        supportActionBar!!.title = "New Customer"

        mAuth = FirebaseAuth.getInstance()
        mDatabase = FirebaseDatabase.getInstance().reference
        storage = FirebaseStorage.getInstance().reference

        presenter = CustomerPresenter(this,mAuth,mDatabase,this)

        btnCustomerSave.onClick {

            btnCustomerSave.startAnimation(normalClickAnimation())

            val email = etCustomerEmail.text.toString()
            val name = etCustomerName.text.toString()
            val phone = etCustomerPhone.text.toString()
            val address = etCustomerAddress.text.toString()
            val note = etCustomerNote.text.toString()

            if (name == ""){
                etCustomerName.error = "Please Fill the Field"
                return@onClick
            }

            presenter.saveCustomer(Customer(name,email,"","",phone,address,note))
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

    // Select Image method
    private fun selectImage() { // Defining Implicit Intent to mobile gallery

        intent = Intent()
        val options = mutableListOf("Take a Photo", "Pick from Gallery")


        selector("select image",options) {
                dialogInterface, i ->
            when(i){
                0 -> {

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
                1 -> {
                    intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    startActivityForResult(
                        intent,
                        PICK_IMAGE_GALLERY
                    )
                }
            }
        }

    }

    // Override onActivityResult method
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_CAMERA && resultCode == Activity.RESULT_OK) {
            try {
                val bitmap = MediaStore.Images.Media
                    .getBitmap(
                        contentResolver,
                        filePath
                    )
                //val imageBitmap = data.extras?.get("data") as Bitmap
                //ivProductImage.setImageBitmap(bitmap)
                //galleryAddPic()
                ivProductImage.setImageBitmap(rotateImage(bitmap))
            } catch (e: Exception) {
                filePath = null
                showError(this,e.message.toString())
                e.printStackTrace()
            }
        }
        else if (requestCode == PICK_IMAGE_GALLERY && resultCode == Activity.RESULT_OK && data != null && data.data != null
        ) { // Get the Uri of data
            filePath = data.data
            currentPhotoPath = getRealPathFromURI(filePath!!)
            try { // Setting image on image view using Bitmap
                val bitmap = MediaStore.Images.Media
                    .getBitmap(
                        contentResolver,
                        filePath
                    )
                //currentPhotoPath = photoURI.toString()
                ivProductImage.setImageBitmap(rotateImage(bitmap))
            } catch (e: IOException) { // Log the exception
                showError(this,e.message.toString())
                e.printStackTrace()
            }
        }
    }

    private fun rotateImage(bitmapSource : Bitmap) : Bitmap {
        val ei = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ExifInterface(File(currentPhotoPath))
        } else {
            ExifInterface(filePath.toString())
        };
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
        return Bitmap.createBitmap(bitmapSource, 0, 0, bitmapSource.width, bitmapSource.height,
            matrix, true)
    }

    private fun getRealPathFromURI(uri: Uri): String {
        @SuppressWarnings("deprecation")
        val cursor = managedQuery(uri, arrayOf(MediaStore.Images.Media.DATA), null, null, null);
        val column_index = cursor
            .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    override fun loadData(dataSnapshot: DataSnapshot, response: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun response(message: String) {
        if (message == EMessageResult.SUCCESS.toString())
            finish()
        toast(message)
    }
}
