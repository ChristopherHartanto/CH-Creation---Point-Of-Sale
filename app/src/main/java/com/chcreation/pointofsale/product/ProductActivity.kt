package com.chcreation.pointofsale.product

import android.R.attr.bitmap
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.chcreation.pointofsale.EMessageResult
import com.chcreation.pointofsale.HomeActivity
import com.chcreation.pointofsale.R
import com.chcreation.pointofsale.model.Product
import com.chcreation.pointofsale.presenter.ProductPresenter
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
import kotlinx.android.synthetic.main.activity_product.*
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.selector
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.toast
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.nio.file.Files.createFile


class ProductActivity : AppCompatActivity(), MainView {

    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDatabase : DatabaseReference
    private lateinit var presenter : ProductPresenter
    private lateinit var storage: StorageReference
    private var PICK_IMAGE_CAMERA  = 1
    private var PICK_IMAGE_GALLERY = 2
    private var filePath: Uri? = null
    private var prodCode = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product)

        mAuth = FirebaseAuth.getInstance()
        mDatabase = FirebaseDatabase.getInstance().reference
        storage = FirebaseStorage.getInstance().reference
        presenter = ProductPresenter(this,mAuth,mDatabase)

        prodCode = generateProdCode()
    }

    override fun onStart() {
        super.onStart()

        btnProductSave.onClick {
            toast("filepath: ${filePath}")

            if (filePath == null)
                saveProduct("")
            else
                uploadImage()
        }

        ivProductImage.onClick {
            selectImage()
        }
    }

    private fun saveProduct(imageUrl: String){
        val name = etProductName.text.toString()
        val desc = etProducDescription.text.toString()
        val price = etProductPrice.text.toString().toInt()
        val cost = etProductCost.text.toString().toInt()
        val stock = etProductStock.text.toString().toInt()
        val uomCode = "Unit"

        presenter.saveProduct(Product(name,price,desc,cost,stock,imageUrl,prodCode,uomCode))
    }

    private fun uploadImage(){
        if(filePath != null){
            val ref = storage.child("product")
                .child(mAuth.currentUser!!.uid)
                .child(prodCode)

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
                    val downloadUri = task.result
                    saveProduct(downloadUri.toString())
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

    private fun generateProdCode() : String{
        return "P${mDatabase.push().key.toString()}"
    }

    // Select Image method
    private fun selectImage() { // Defining Implicit Intent to mobile gallery

        intent = Intent()
        var options = mutableListOf("Take a Photo", "Pick from Gallery")

        selector("select image",options) {
                dialogInterface, i ->
            when(i){
                0 -> {
                    intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)

                    startActivityForResult(
                        intent,
                        PICK_IMAGE_CAMERA
                    )
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
//
//        intent.type = "image/*"
//        intent.action = Intent.ACTION_GET_CONTENT

    }

    // Override onActivityResult method
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_CAMERA && data != null && data.data != null) {
            try {
                filePath = data.data
                val bitmap = MediaStore.Images.Media
                    .getBitmap(
                        contentResolver,
                        filePath
                    )
                ivProductImage.setImageBitmap(bitmap)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        if (requestCode == PICK_IMAGE_GALLERY && resultCode == Activity.RESULT_OK && data != null && data.data != null
        ) { // Get the Uri of data
            filePath = data.data
            try { // Setting image on image view using Bitmap
                val bitmap = MediaStore.Images.Media
                    .getBitmap(
                        contentResolver,
                        filePath
                    )
                ivProductImage.setImageBitmap(bitmap)
            } catch (e: IOException) { // Log the exception
                e.printStackTrace()
            }
        }
    }

    override fun loadData(dataSnapshot: DataSnapshot, response: String) {
    }

    override fun response(message: String) {
        if (message == EMessageResult.SUCCESS.toString()) {
            toast("success")

            finish()
        }else
            toast("" + message)
    }
}
