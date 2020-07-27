package com.chcreation.pointofsale.product

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.chcreation.pointofsale.EMessageResult
import com.chcreation.pointofsale.R
import com.chcreation.pointofsale.dateFormat
import com.chcreation.pointofsale.getMerchant
import com.chcreation.pointofsale.model.Merchant
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
import kotlinx.android.synthetic.main.activity_merchant.*
import kotlinx.android.synthetic.main.activity_new_product.*
import org.jetbrains.anko.sdk27.coroutines.onCheckedChange
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.selector
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.toast
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


class NewProductActivity : AppCompatActivity(), MainView, AdapterView.OnItemSelectedListener {

    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDatabase : DatabaseReference
    private lateinit var presenter : ProductPresenter
    private lateinit var storage: StorageReference
    private lateinit var sharedPreference: SharedPreferences
    private var PICK_IMAGE_CAMERA  = 111
    private var PICK_IMAGE_GALLERY = 222
    private var filePath: Uri? = null
    private var prodCode = ""
    private var merchant = ""
    private var categoryItems : MutableList<String> = mutableListOf()
    private var selectedCategory = ""
    private lateinit var spAdapter: ArrayAdapter<String>
    private var manageStock = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_product)

        mAuth = FirebaseAuth.getInstance()
        mDatabase = FirebaseDatabase.getInstance().reference
        storage = FirebaseStorage.getInstance().reference
        presenter = ProductPresenter(this,mAuth,mDatabase,this)
        sharedPreference =  this.getSharedPreferences("LOCAL_DATA", Context.MODE_PRIVATE)

        merchant = sharedPreference.getString("merchant","").toString()
        prodCode = generateProdCode()

        spAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item,categoryItems)
        spAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        spProduct.adapter = spAdapter
        spProduct.onItemSelectedListener = this
        spProduct.gravity = Gravity.CENTER

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

        swProduct.onCheckedChange { buttonView, isChecked ->
            if (swProduct.isChecked){
                manageStock = true
                layoutProductStock.visibility = View.VISIBLE
            }else{
                manageStock = false
                layoutProductStock.visibility = View.GONE
            }
        }

        swProduct.isChecked = !swProduct.isChecked

    }

    private fun saveProduct(imageUrl: String){
        val name = etProductName.text.toString()
        val desc = etProductDescription.text.toString()
        var price = 0
        if (etProductPrice.text.toString() != "")
            price = etProductPrice.text.toString().toInt()

        var cost = 0
        if (etProductCost.text.toString() != "")
            cost = etProductCost.text.toString().toInt()

        var stock = 0
        if (etProductStock.text.toString() != "")
            stock = etProductStock.text.toString().toInt()

        val code = etProductCode.text.toString()
        val uomCode = "Unit"
        var cat = ""
        cat = if (selectedCategory == categoryItems[0])
            "ALL"
        else
            selectedCategory

        if (getMerchant(this) == "")
            toast("You Haven't Set Up Your Merchant")
        else
            presenter.saveProduct(Product(name,price,desc,cost,manageStock,stock,imageUrl,prodCode,uomCode,selectedCategory,code,
                dateFormat().format(Date()),dateFormat().format(Date()),
                mAuth.currentUser!!.uid,mAuth.currentUser!!.uid))
    }

    override fun onResume() {
        super.onResume()

        presenter.retrieveCategories()
    }
    private fun uploadImage(){
        if(filePath != null){
            layoutProduct.alpha = 0.3F
            pbProduct.visibility = View.VISIBLE

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
                    layoutProduct.alpha = 1F
                    pbProduct.visibility = View.GONE
                }
            }.addOnFailureListener{
                toast("Error : ${it.message}")
                layoutProduct.alpha = 1F
                pbProduct.visibility = View.GONE
            }
        }else{
            Toast.makeText(this, "Please Upload an Image", Toast.LENGTH_SHORT).show()
        }
    }

    private fun generateProdCode() : String{
        return "P${mDatabase.push().key.toString()}"
    }

    lateinit var currentPhotoPath: String

    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            currentPhotoPath = "File = $absolutePath"

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
//                    val values = ContentValues()
//                    values.put(MediaStore.Images.Media.TITLE, "New Picture")
//                    values.put(MediaStore.Images.Media.DESCRIPTION, "From the Camera")
//                    val image_uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
//                    //camera intent
//                    intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
//                    //intent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri)
//
//                    startActivityForResult(
//                        intent,
//                        PICK_IMAGE_CAMERA
//                    )

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
//
//                    Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
//                        takePictureIntent.resolveActivity(packageManager)?.also {
//                            startActivityForResult(takePictureIntent, PICK_IMAGE_CAMERA)
//                        }
//                    }
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
                ivProductImage.setImageBitmap(bitmap)
                //ivProductImage.setImageBitmap(rotateImage(bitmap))
            } catch (e: Exception) {
                filePath = null
                e.printStackTrace()
            }
        }
        else if (requestCode == PICK_IMAGE_GALLERY && resultCode == Activity.RESULT_OK && data != null && data.data != null
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

    fun rotateImage(bitmapSource : Bitmap) : Bitmap{
        val ei = ExifInterface(filePath.toString());
        val orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION,
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

    override fun loadData(dataSnapshot: DataSnapshot, response: String) {
        if (response == EMessageResult.FETCH_CATEGORY_SUCCESS.toString()) {
            categoryItems.clear()
            categoryItems.add("ALL")
            categoryItems.add("Create Category")

            if (dataSnapshot.exists()){
                for (data in dataSnapshot.children) {
                    val item = data.key
                    if (item != "")
                        categoryItems.add(item.toString())
                }
                spProduct.setSelection(0)
                selectedCategory = categoryItems[0]
            }
            spAdapter.notifyDataSetChanged()
        }
    }

    override fun response(message: String) {
        if (message == EMessageResult.SUCCESS.toString()) {
            toast("success")
            layoutProduct.alpha = 1F
            pbProduct.visibility = View.GONE
            finish()
        }else
            toast("" + message)
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {

    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        selectedCategory = categoryItems[position]

        if (position == 1)
            startActivity<NewCategory>()
    }
}
