package com.chcreation.pointofsale.product

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.chcreation.pointofsale.*
import com.chcreation.pointofsale.model.ActivityLogs
import com.chcreation.pointofsale.model.Cat
import com.chcreation.pointofsale.model.Product
import com.chcreation.pointofsale.model.WholeSale
import com.chcreation.pointofsale.presenter.ProductPresenter
import com.chcreation.pointofsale.product.ProductWholeSaleActivity.Companion.wholeSaleItems
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
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.google.zxing.Result
import kotlinx.android.synthetic.main.activity_new_product.*
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.fragment_manage_product_update_product.*
import me.dm7.barcodescanner.zxing.ZXingScannerView
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk27.coroutines.onCheckedChange
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.support.v4.ctx
import java.io.File
import java.io.IOException
import java.util.*


class NewProductActivity : AppCompatActivity(), MainView, AdapterView.OnItemSelectedListener,
    ZXingScannerView.ResultHandler {

    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDatabase : DatabaseReference
    private lateinit var presenter : ProductPresenter
    private lateinit var storage: StorageReference
    private lateinit var sharedPreference: SharedPreferences
    private var PICK_IMAGE_CAMERA  = 111
    private var CAMERA_PERMISSION  = 101
    private var CAMERA_PERMISSION_SCAN  = 102
    private var PICK_IMAGE_GALLERY = 222
    private var READ_PERMISION = 202
    private var filePath: Uri? = null
    private var prodCode = ""
    private var merchant = ""
    private var categoryItems : MutableList<String> = mutableListOf()
    private var selectedCategory = ""
    private var positionSpinner = 0
    private lateinit var spAdapter: ArrayAdapter<String>
    private var manageStock = false
    private lateinit var mScannerView : ZXingScannerView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_product)

        supportActionBar!!.title = "Set Up Product"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        mAuth = FirebaseAuth.getInstance()
        mDatabase = FirebaseDatabase.getInstance().reference
        storage = FirebaseStorage.getInstance().reference
        presenter = ProductPresenter(this,mAuth,mDatabase,this)
        sharedPreference =  this.getSharedPreferences("LOCAL_DATA", Context.MODE_PRIVATE)
        mScannerView = ZXingScannerView(this)

        merchant = sharedPreference.getString("merchant","").toString()
        prodCode = generateProdCode()

        spAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item,categoryItems)
        spAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        spProduct.adapter = spAdapter
        spProduct.onItemSelectedListener = this
        spProduct.gravity = Gravity.CENTER

        layoutProductWholeSale.onClick {
            layoutProductWholeSale.startAnimation(normalClickAnimation())

            startActivity<ProductWholeSaleActivity>()
        }

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onStart() {
        super.onStart()

        spProduct.setSelection(0)

        if (wholeSaleItems.size == 0)
            tvProductWholeSaleTitle.text = "Set Wholesale"
        else
            tvProductWholeSaleTitle.text = "Whole Sale Detail (${wholeSaleItems.size} Items)"

        btnProductSave.onClick {
            btnProductSave.isEnabled = false
            loading()
            btnProductSave.startAnimation(normalClickAnimation())

            presenter.checkProductSize(){
                if (it > 8 && getMerchantMemberStatus(this@NewProductActivity) == EMerchantMemberStatus.FREE_TRIAL.toString()){
                    alert ("Upgrade to Premium for Unlimited Product"){
                        title = "Oops!"
                        yesButton {
                            sendEmail("Upgrade Premium",
                                "Merchant: ${getMerchantName(this@NewProductActivity)}",this@NewProductActivity)
                        }

                        noButton {  }
                    }.show()
                    endLoading()
                }else{
                    if (positionSpinner == 0){
                        endLoading()
                        toast("Please Select Category")
                    }
                    else if (etProductName.text.toString() == ""){
                        endLoading()
                        etProductName.error = "Please Fill Product Name!"
                    }
                    else{
                        if (filePath == null)
                            saveProduct("")
                        else
                            uploadImage()
                    }
                }
            }

        }

        ivProductImage.onClick{

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

        ivProductScan.onClick { cs->
            ivProductScan.startAnimation(normalClickAnimation())

            if (ContextCompat.checkSelfPermission(this@NewProductActivity, android.Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED)
                ActivityCompat.requestPermissions(this@NewProductActivity,
                    arrayOf(android.Manifest.permission.CAMERA),CAMERA_PERMISSION_SCAN
                )
            else
                openScanBarcode()
        }

        btnProductScanCancel.onClick {
            btnProductScanCancel.startAnimation(normalClickAnimation())
            cancelScan()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        wholeSaleItems.clear()
    }

    private fun openScanBarcode(){
        cancelScan()
        layoutProductScan.visibility = View.VISIBLE
        mScannerView.setAutoFocus(true)
        mScannerView.setResultHandler(this)
        layoutProductScanContent.addView(mScannerView)
        mScannerView.startCamera()
    }

    private fun cancelScan(){
        mScannerView.stopCamera();
        mScannerView.removeAllViewsInLayout()
        layoutProductScanContent.removeAllViews()
        layoutProductScan.visibility = View.GONE
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

        if (!swProduct.isChecked)
            stock = 0

        val code = etProductCode.text.toString()
        val uomCode = "Unit"
        var cat = ""
        cat = if (selectedCategory == categoryItems[0])
            "ALL"
        else
            selectedCategory

        val gson = Gson()
        val wholeSaleItems = gson.toJson(wholeSaleItems)

        if (getMerchantCode(this) == ""){
            endLoading()
            toast("You Haven't Set Up Your Merchant")
        }
        else{
            presenter.saveProduct(Product(name,price,desc,cost,manageStock,stock,imageUrl,prodCode,uomCode,selectedCategory,code,EStatusCode.ACTIVE.toString(),
                dateFormat().format(Date()),dateFormat().format(Date()),
                mAuth.currentUser!!.uid,mAuth.currentUser!!.uid,wholeSaleItems))

            presenter.saveActivityLogs(ActivityLogs("Create Product $name",mAuth.currentUser!!.uid,dateFormat().format(Date())))
        }
    }

    override fun onResume() {
        super.onResume()

        presenter.retrieveCategories()
    }
    private fun uploadImage(){
        if(filePath != null){
            layoutProduct.alpha = 0.3F
            pbProduct.visibility = View.VISIBLE

            val ref = storage.child(ETable.PRODUCT.toString())
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
                    if (ContextCompat.checkSelfPermission(this@NewProductActivity, android.Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED)
                        ActivityCompat.requestPermissions(this@NewProductActivity,
                            arrayOf(android.Manifest.permission.CAMERA),CAMERA_PERMISSION
                        )
                    else
                        openCamera()

                }
                1 -> {
                    if (ContextCompat.checkSelfPermission(this@NewProductActivity, android.Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED)
                        ActivityCompat.requestPermissions(this@NewProductActivity,
                            arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE,android.Manifest.permission.WRITE_EXTERNAL_STORAGE),READ_PERMISION
                        )
                    else
                        openGallery()
                }
            }
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
        else if (requestCode == CAMERA_PERMISSION_SCAN){
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                openScanBarcode()
            }
            else
                toast("Permission Denied")
        }
    }

    private fun rotateImage(bitmapSource : Bitmap) : Bitmap{
        val ei = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ExifInterface(File(currentPhotoPath))
        } else {
            ExifInterface(currentPhotoPath)
        };
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

    private fun decodePicture() {
        // Get the dimensions of the View
        val targetW: Int = ivProductImage.width
        val targetH: Int = ivProductImage.height

        val bmOptions = BitmapFactory.Options().apply {
            // Get the dimensions of the bitmap
            inJustDecodeBounds = true

            val photoW: Int = outWidth
            val photoH: Int = outHeight

            // Determine how much to scale down the image
            val scaleFactor: Int = (photoW / targetW).coerceAtMost(photoH / targetH)

            // Decode the image file into a Bitmap sized to fill the View
            inJustDecodeBounds = false
            inSampleSize = scaleFactor
            inPurgeable = true
        }
        BitmapFactory.decodeFile(currentPhotoPath, bmOptions)?.also { bitmap ->
            ivProductImage.setImageBitmap(bitmap)
        }
    }

    fun getRealPathFromURI(uri: Uri): String {
        @SuppressWarnings("deprecation")
        val cursor = managedQuery(uri, arrayOf(MediaStore.Images.Media.DATA), null, null, null);
        val column_index = cursor
                .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    private fun loading(){
        pbProduct.visibility = View.VISIBLE
        btnProductSave.isEnabled = false
    }

    private fun endLoading(){
        pbProduct.visibility = View.GONE
        btnProductSave.isEnabled = true
    }

    override fun loadData(dataSnapshot: DataSnapshot, response: String) {
        if (response == EMessageResult.FETCH_CATEGORY_SUCCESS.toString()) {
            categoryItems.clear()
            categoryItems.add("Select Category")
            categoryItems.add("Create Category")

            if (dataSnapshot.exists()  && dataSnapshot.value != ""){
                val gson = Gson()
                val arrayCartType = object : TypeToken<MutableList<Cat>>() {}.type
                val items : MutableList<Cat> = gson.fromJson(dataSnapshot.value.toString(),arrayCartType)

                for (data in items) {
                    val item = data.CAT
                    if (item != "" && data.STATUS_CODE == EStatusCode.ACTIVE.toString())
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
            finish()
        }else
            toast("" + message)
        endLoading()
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {

    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        selectedCategory = categoryItems[position]
        positionSpinner = position
        if (position == 1)
            startActivity<ListCategoryActivity>()
    }
    override fun handleResult(p0: Result?) {
        etProductCode.setText(p0.toString())
        cancelScan()
    }
}
