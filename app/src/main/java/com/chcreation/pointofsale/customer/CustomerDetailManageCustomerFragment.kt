package com.chcreation.pointofsale.customer

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.core.content.FileProvider
import com.bumptech.glide.Glide
import com.chcreation.pointofsale.EMessageResult

import com.chcreation.pointofsale.R
import com.chcreation.pointofsale.customer.CustomerDetailActivity.Companion.custCode
import com.chcreation.pointofsale.dateFormat
import com.chcreation.pointofsale.model.Customer
import com.chcreation.pointofsale.presenter.CustomerPresenter
import com.chcreation.pointofsale.presenter.ProductPresenter
import com.chcreation.pointofsale.product.ManageProductDetailActivity
import com.chcreation.pointofsale.product.ManageProductUpdateProductFragment
import com.chcreation.pointofsale.showError
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
import kotlinx.android.synthetic.main.activity_new_product.*
import kotlinx.android.synthetic.main.fragment_customer_detail_manage_customer.*
import kotlinx.android.synthetic.main.fragment_manage_product_update_product.*
import org.jetbrains.anko.noButton
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.selector
import org.jetbrains.anko.support.v4.alert
import org.jetbrains.anko.support.v4.ctx
import org.jetbrains.anko.support.v4.selector
import org.jetbrains.anko.support.v4.toast
import org.jetbrains.anko.yesButton
import java.io.File
import java.io.IOException
import java.util.*

/**
 * A simple [Fragment] subclass.
 */
class CustomerDetailManageCustomerFragment : Fragment(), MainView {

    private lateinit var presenter: CustomerPresenter
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDatabase : DatabaseReference
    private lateinit var storage: StorageReference
    private var manageStock = true
    private var PICK_IMAGE_CAMERA  = 111
    private var PICK_IMAGE_GALLERY = 222
    private var currentPhotoPath = ""
    private var filePath:Uri? = null
    private lateinit var customer : Customer

    companion object{
        var custKey = 0
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_customer_detail_manage_customer, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        mAuth = FirebaseAuth.getInstance()
        mDatabase = FirebaseDatabase.getInstance().reference
        storage = FirebaseStorage.getInstance().reference
        presenter = CustomerPresenter(this,mAuth,mDatabase,ctx)

        presenter.retrieveCustomerByCustCode(custCode)

        btnManageCustomerSave.onClick {
            alert ("Are You Sure Want to Update?"){

                title = "Update"
                yesButton {
                    if (filePath == null)
                        updateCustomer(null)
                    else
                       uploadImage()
                }
                noButton {

                }
            }.show()
        }

        layoutManageCustomerDefaultImage.onClick {
            selectImage()
        }

        ivManageCustomerImage.onClick {
            selectImage()
        }
    }

    private fun uploadImage(){
        if(ManageProductUpdateProductFragment.filePath != null){
            pbManageCustomer.visibility = View.VISIBLE

            val ref = storage.child("product")
                .child(mAuth.currentUser!!.uid)
                .child(ManageProductDetailActivity.prodCode)

            val uploadTask = ref.putFile(ManageProductUpdateProductFragment.filePath!!)

            val urlTask = uploadTask.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
                if (!task.isSuccessful) {
                    task.exception?.let {
                        throw it
                    }
                }
                return@Continuation ref.downloadUrl
            }).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val downloadUri = task.result
                    updateCustomer(downloadUri.toString())
                } else {
                    toast("Failed to Save Image")
                    pbManageCustomer.visibility = View.GONE
                }
            }.addOnFailureListener{
                toast("Error : ${it.message}")
                pbManageCustomer.visibility = View.GONE
            }
        }else{
            toast("Please Upload an Image")
            pbManageCustomer.visibility = View.GONE
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp: String = dateFormat().format(Date(1000))
        val storageDir: File? = ctx.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
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

        var intent = Intent()
        val options = mutableListOf("Take a Photo", "Pick from Gallery")


        selector("select image",options) {
                dialogInterface, i ->
            when(i){
                0 -> {

                    Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
                        // Ensure that there's a camera activity to handle the intent
                        takePictureIntent.resolveActivity(ctx.packageManager)?.also {
                            // Create the File where the photo should go
                            val photoFile: File? = try {
                                createImageFile()
                            } catch (ex: IOException) {
                                null
                            }
                            // Continue only if the File was successfully created

                            try {
                                photoFile?.also {
                                    val photoURI: Uri = FileProvider.getUriForFile(
                                        ctx,
                                        "com.example.android.fileprovider",
                                        it
                                    )
                                    Log.d("uri: ",photoURI.toString())
                                    filePath = photoURI

                                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                                    startActivityForResult(takePictureIntent, PICK_IMAGE_CAMERA)
                                }
                            }catch (e: java.lang.Exception){
                                showError(ctx,e.message.toString())
                                e.printStackTrace()
                            }
                        }
                    }
                }
                1 -> {
                    try {
                        intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                        startActivityForResult(
                            intent,
                            PICK_IMAGE_GALLERY
                        )

                    }catch (e: java.lang.Exception){
                        showError(ctx,e.message.toString())
                        e.printStackTrace()
                    }
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
                        ctx.contentResolver,
                        filePath
                    )
                //val imageBitmap = data.extras?.get("data") as Bitmap
                //ivProductImage.setImageBitmap(bitmap)
                //galleryAddPic()
                ivManageCustomerImage.setImageBitmap(rotateImage(bitmap))
            } catch (e: Exception) {
                filePath = null
                showError(ctx,e.message.toString())
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
                        ctx.contentResolver,
                        filePath
                    )
                //currentPhotoPath = photoURI.toString()
                ivManageCustomerImage.setImageBitmap(rotateImage(bitmap))
            } catch (e: IOException) { // Log the exception
                showError(ctx,e.message.toString())
                e.printStackTrace()
            }
        }
    }

    fun rotateImage(bitmapSource : Bitmap) : Bitmap {
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

    fun getRealPathFromURI(uri: Uri): String {
        @SuppressWarnings("deprecation")
        val cursor = requireActivity().managedQuery(uri, arrayOf(MediaStore.Images.Media.DATA), null, null, null);
        val column_index = cursor
            .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    fun updateCustomer(imageUri: String?){
        val email = etManageCustomerEmail.text.toString()
        val address = etManageCustomerAddress.text.toString()
        val name = etManageCustomerName.text.toString()
        val note = etManageCustomerNote.text.toString()
        val phone = etManageCustomerPhone.text.toString()

        if (name == "")
            toast("Please Fill Customer Name !!")
        else{
            presenter.saveCustomer(Customer(name,email, customer.CREATED_DATE, customer.UPDATED_DATE,phone,
                address,note,customer.CODE,customer.IMAGE),custKey)
        }
    }

    fun fetchData(){
        etManageCustomerEmail.setText(customer.EMAIL)
        etManageCustomerAddress.setText(customer.ADDRESS)
        etManageCustomerName.setText(customer.NAME)
        etManageCustomerNote.setText(customer.NOTE)
        etManageCustomerPhone.setText(customer.PHONE)

        if (customer.IMAGE == ""){
            layoutManageCustomerDefaultImage.visibility = View.VISIBLE
            ivManageCustomerImage.visibility = View.GONE
            tvManageCustomerFirstName.text = customer.NAME!!.first().toString().toUpperCase(Locale.getDefault())
        }else{
            ivManageCustomerImage.visibility = View.VISIBLE
            layoutManageCustomerDefaultImage.visibility = View.GONE

            Glide.with(ctx).load(customer.IMAGE).into(ivManageCustomerImage)
        }
    }

    override fun loadData(dataSnapshot: DataSnapshot, response: String) {
       if (response == EMessageResult.FETCH_CUSTOMER_SUCCESS.toString()){
           if (dataSnapshot.exists()){
               for (data in dataSnapshot.children){
                   val item = data.getValue(Customer::class.java)
                   custKey = data.key!!.toInt()
                   customer = item!!
               }
               fetchData()
           }
       }
    }

    override fun response(message: String) {
       if (message == EMessageResult.SUCCESS.toString()){
           toast("Success Update")
       }
    }
}
