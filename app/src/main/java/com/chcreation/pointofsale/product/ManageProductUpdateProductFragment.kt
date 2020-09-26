package com.chcreation.pointofsale.product

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.GlideDrawable
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.chcreation.pointofsale.*
import com.chcreation.pointofsale.model.ActivityLogs
import com.chcreation.pointofsale.model.Cat

import com.chcreation.pointofsale.model.Product
import com.chcreation.pointofsale.model.WholeSale
import com.chcreation.pointofsale.presenter.Homepresenter
import com.chcreation.pointofsale.presenter.ProductPresenter
import com.chcreation.pointofsale.product.ManageProductDetailActivity.Companion.prodCode
import com.chcreation.pointofsale.product.ManageProductDetailActivity.Companion.prodName
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import me.dm7.barcodescanner.zxing.ZXingScannerView
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk27.coroutines.onCheckedChange
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.support.v4.*
import java.io.File
import java.io.IOException
import java.lang.Exception
import java.util.*


class ManageProductUpdateProductFragment : Fragment(), MainView, AdapterView.OnItemSelectedListener,
    ZXingScannerView.ResultHandler {

    private lateinit var presenter: ProductPresenter
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDatabase : DatabaseReference
    private lateinit var storage: StorageReference
    private var categoryItems : MutableList<String> = mutableListOf()
    private var selectedCategory = ""
    private var positionSpinner = 0
    private lateinit var spAdapter: ArrayAdapter<String>
    private var manageStock = true
    private var PICK_IMAGE_CAMERA  = 111
    private var CAMERA_PERMISSION  = 101
    private var CAMERA_PERMISSION_SCAN  = 102
    private var PICK_IMAGE_GALLERY = 222
    private var READ_PERMISION = 202
    private lateinit var mScannerView : ZXingScannerView

    companion object{
        var filePath: Uri? = null
        var bitmap: Bitmap? = null
        var productKey = 0
        var product: Product = Product()
        var currentPhotoPath = ""
        var saveWholeSale = false
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_manage_product_update_product, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mAuth = FirebaseAuth.getInstance()
        mDatabase = FirebaseDatabase.getInstance().reference
        storage = FirebaseStorage.getInstance().reference
        presenter = ProductPresenter(this,mAuth,mDatabase,ctx)
        mScannerView = ZXingScannerView(ctx)

        spAdapter = ArrayAdapter(ctx, android.R.layout.simple_spinner_item,categoryItems)
        spAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        spManageProduct.adapter = spAdapter
        spManageProduct.onItemSelectedListener = this
        spManageProduct.gravity = Gravity.CENTER

        swManageProduct.onCheckedChange { buttonView, isChecked ->
            manageStock = swManageProduct.isChecked
        }

        swManageProduct.isChecked = !swManageProduct.isChecked
//
//        GlobalScope.launch (Dispatchers.Main){
//            presenter.retrieveCategories()
//            presenter.retrieveProductByProdCode(prodCode)
//        }
        presenter.retrieveCategories()
        ivManageProductImage.onClick {
            selectImage()
        }

        ivManageProductScan.onClick { cs->
            ivManageProductScan.startAnimation(normalClickAnimation())

            if (ContextCompat.checkSelfPermission(ctx, android.Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED)
                ActivityCompat.requestPermissions(requireActivity(),
                    arrayOf(android.Manifest.permission.CAMERA),CAMERA_PERMISSION
                )
            else
                openScanBarcode()
        }

        btnManageProductScanCancel.onClick {
            btnManageProductScanCancel.startAnimation(normalClickAnimation())
            cancelScan()
        }

        layoutManageProductWholeSale.onClick {
            layoutManageProductWholeSale.startAnimation(normalClickAnimation())
            ctx.startActivity<ProductWholeSaleActivity>()
        }
    }

    override fun onStart() {
        super.onStart()

        btnManageProductSave.onClick {
            if (getMerchantUserGroup(ctx) == EUserGroup.WAITER.toString())
                toast("Only Manager Can Update Product")
            else{
                alert ("Are You Sure want to Update?"){
                    title = "Update"
                    yesButton {
                        if (positionSpinner == 0 || positionSpinner == 1)
                            toast("Please Select Category")
                        else if (etManageProductName.text.toString() == "")
                            toast("Name Must be Fill !!")
                        else{
                            pbManageProduct.visibility = View.VISIBLE

                            if (filePath == null)
                                updateProduct(null)
                            else
                                uploadImage()
                        }
                    }
                    noButton {

                    }
                }.show()
            }
        }

        if (wholeSaleItems.size == 0)
            tvManageProductWholeSaleTitle.text = "Set Wholesale"
        else
            tvManageProductWholeSaleTitle.text = "Wholesale (${wholeSaleItems.size} Items)"
    }

    private fun updateProduct(imageUri: String?){
        val name = etManageProductName.text.toString()
        val desc = etManageProductDescription.text.toString()
        val prodCode = etManageProductCode.text.toString()

        var price = 0
        if (etManageProductPrice.text.toString() != "")
            price = etManageProductPrice.text.toString().toInt()

        var cost = 0
        if (etManageProductCost.text.toString() != "")
            cost =  etManageProductCost.text.toString().toInt()

        var image = product.IMAGE
        if (imageUri != null)
            image = imageUri

        prodName = name

        val gson = Gson()
        val wholeSaleItems = gson.toJson(wholeSaleItems)

        presenter.saveProduct(Product(name,price,desc,cost,manageStock,product.STOCK,image,
            product.PROD_CODE,product.UOM_CODE,categoryItems[positionSpinner],prodCode,
            EStatusCode.ACTIVE.toString(),product.CREATED_DATE,
            dateFormat().format(Date()), product.CREATED_BY,mAuth.currentUser!!.uid, wholeSaleItems),productKey)

        val log = "Update Product $name"
        presenter.saveActivityLogs(ActivityLogs(log,mAuth.currentUser!!.uid,dateFormat().format(Date())))
    }
    private fun uploadImage(){
        if(filePath != null){
            layoutManageProduct.alpha = 0.3F
            pbManageProduct.visibility = View.VISIBLE

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
                    updateProduct(downloadUri.toString())
                } else {
                    toast("Failed to Save Image")
                    layoutManageProduct.alpha = 1F
                    pbManageProduct.visibility = View.GONE
                }
            }.addOnFailureListener{
                toast("Error : ${it.message}")
                layoutManageProduct.alpha = 1F
                pbManageProduct.visibility = View.GONE
            }
        }else{
            toast("Please Upload an Image")
            layoutManageProduct.alpha = 1F
            pbManageProduct.visibility = View.GONE
        }
    }

    private fun openScanBarcode(){
        cancelScan()
        layoutManageProductScan.visibility = View.VISIBLE
        mScannerView.setAutoFocus(true)
        mScannerView.setResultHandler(this)
        layoutManageProductScanContent.addView(mScannerView)
        mScannerView.startCamera()
    }

    private fun cancelScan(){
        mScannerView.stopCamera();
        mScannerView.removeAllViewsInLayout()
        layoutManageProductScanContent.removeAllViews()
        layoutManageProductScan.visibility = View.GONE
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

    private fun selectImage() { // Defining Implicit Intent to mobile gallery

        var intent = Intent()
        val options = mutableListOf("Take a Photo", "Pick from Gallery")


        selector("select image",options) {
                dialogInterface, i ->
            when(i){
                0 -> {
                    if (ContextCompat.checkSelfPermission(ctx, android.Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED)
                        ActivityCompat.requestPermissions(requireActivity(),
                            arrayOf(android.Manifest.permission.CAMERA),CAMERA_PERMISSION
                        )
                    else
                        openCamera()

                }
                1 -> {
                    if (ContextCompat.checkSelfPermission(ctx, android.Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED)
                        ActivityCompat.requestPermissions(requireActivity(),
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
            takePictureIntent.resolveActivity(ctx.packageManager)?.also {
                // Create the File where the photo should go
                val photoFile: File? = try {
                    createImageFile()
                } catch (ex: IOException) {
                    null
                }
                // Continue only if the File was successfully created
                photoFile?.also {
                    val photoURI: Uri = FileProvider.getUriForFile(
                        ctx,
                        "com.example.android.fileprovider",
                        it
                    )
                    Log.d("uri: ",photoURI.toString())
                    filePath = photoURI
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    this.startActivityForResult(takePictureIntent, PICK_IMAGE_CAMERA)
                }
            }
        }
    }

    private fun openGallery(){
        var intent = Intent()
        intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        this.startActivityForResult(
            intent,
            PICK_IMAGE_GALLERY
        )
    }

    // Override onActivityResult method
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_CAMERA && resultCode == Activity.RESULT_OK) {
            try {
                bitmap = MediaStore.Images.Media
                    .getBitmap(
                        ctx.contentResolver,
                        filePath
                    )
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
                bitmap = MediaStore.Images.Media
                    .getBitmap(
                        ctx.contentResolver,
                        filePath
                    )
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
            ExifInterface(currentPhotoPath)
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

    private fun getRealPathFromURI(uri: Uri): String {
        @SuppressWarnings("deprecation")
        val cursor = requireActivity().managedQuery(uri, arrayOf(MediaStore.Images.Media.DATA), null, null, null);
        val column_index = cursor
            .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    private fun fetchData(){
        etManageProductCode.setText(product.CODE.toString())

        if (getMerchantUserGroup(ctx) == EUserGroup.WAITER.toString()){
            etManageProductCost.setText("*********")
        }else
            etManageProductCost.setText(product.COST.toString())

        etManageProductDescription.setText(product.DESC.toString())
        etManageProductName.setText(product.NAME.toString())
        etManageProductPrice.setText(product.PRICE.toString())
        etManageProductCreatedDate.setText(parseDateFormatFull(product.CREATED_DATE.toString()))
        etManageProductUpdatedDate.setText(parseDateFormatFull(product.UPDATED_DATE.toString()))

        etManageProductCreatedDate.isEnabled = false
        etManageProductUpdatedDate.isEnabled = false
        etManageProductUpdatedBy.isEnabled = false

        if (wholeSaleItems.size == 0)
            tvManageProductWholeSaleTitle.text = "Set Wholesale"
        else
            tvManageProductWholeSaleTitle.text = "Wholesale (${wholeSaleItems.size} Items)"

        presenter.getUserName(product.UPDATED_BY.toString()){
            etManageProductUpdatedBy.setText(it)
        }

        (activity as AppCompatActivity).supportActionBar?.title = product.NAME.toString()

        if (bitmap != null){
            ivManageProductImage.setImageBitmap(rotateImage(bitmap!!))
            pbManageProductLoadImage.visibility = View.GONE
        }
        else if (product.IMAGE.toString() != ""){
            Glide.with(ctx).load(product.IMAGE).listener(object :
                RequestListener<String, GlideDrawable> {
                override fun onException(
                    e: Exception?,
                    model: String?,
                    target: Target<GlideDrawable>?,
                    isFirstResource: Boolean
                ): Boolean {
                    pbManageProductLoadImage.visibility = View.GONE
                    return false
                }

                override fun onResourceReady(
                    resource: GlideDrawable?,
                    model: String?,
                    target: Target<GlideDrawable>?,
                    isFromMemoryCache: Boolean,
                    isFirstResource: Boolean
                ): Boolean {
                    pbManageProductLoadImage.visibility = View.GONE
                    return false
                }

            }).into(ivManageProductImage)
        }else
            pbManageProductLoadImage.visibility = View.GONE

        if (categoryItems.size != 0){
            if (categoryItems.contains(product.CAT)){
                val index = categoryItems.indexOf(product.CAT)
                spManageProduct.setSelection(index)
            }
        }

        swManageProduct.isChecked = product.MANAGE_STOCK

    }


    override fun loadData(dataSnapshot: DataSnapshot, response: String) {
        if (isVisible && isResumed){
            if (response == EMessageResult.FETCH_PROD_SUCCESS.toString()){
                if (dataSnapshot.exists())
                {
                    try{
                        for (data in dataSnapshot.children){
                            productKey = data.key!!.toInt()
                            val item = data.getValue(Product::class.java)
                            product = item!!


                            if (item.WHOLE_SALE != "" && wholeSaleItems.size == 0 && !saveWholeSale){
                                val gson = Gson()
                                val arrayWholeSaleType = object : TypeToken<MutableList<WholeSale>>() {}.type
                                wholeSaleItems = gson.fromJson(item.WHOLE_SALE,arrayWholeSaleType)
                            }
                        }
                        fetchData()
                    }catch (e: Exception){
                        showError(ctx,e.message.toString())
                        e.printStackTrace()
                    }
                }
                pbManageProduct.visibility = View.GONE
            }else if (response == EMessageResult.FETCH_CATEGORY_SUCCESS.toString()) {
                categoryItems.clear()
                categoryItems.add("")
                categoryItems.add("Create Category")

                if (dataSnapshot.exists() && dataSnapshot.value != ""){
                    val gson = Gson()
                    val arrayCartType = object : TypeToken<MutableList<Cat>>() {}.type
                    val items : MutableList<Cat> = gson.fromJson(dataSnapshot.value.toString(),arrayCartType)

                    for (data in items) {
                        val item = data.CAT
                        if (item != ""){
                            if (data.STATUS_CODE == EStatusCode.ACTIVE.toString()){
                                categoryItems.add(item.toString())
                            }
                        }
                    }

                    selectedCategory = categoryItems[0]
                    presenter.retrieveProductByProdCode(prodCode)
                    spAdapter.notifyDataSetChanged()
                    spManageProduct.setSelection(0)
                }
            }
        }

    }

    fun clearData(){
        filePath = null
        bitmap = null
        currentPhotoPath = ""
        product = Product()
        productKey = 0
    }

    override fun response(message: String) {
        if (message == EMessageResult.SUCCESS.toString()){
            clearData()
            presenter.retrieveProductByProdCode(prodCode)
            toast("Update Success")
            if (swManageProduct.isChecked)
                product.MANAGE_STOCK = true
        }
        layoutManageProduct.alpha = 1F
        pbManageProduct.visibility = View.GONE
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {

    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        selectedCategory = categoryItems[position]
        positionSpinner = position
        if (position == 1)
            ctx.startActivity<ListCategoryActivity>()
    }

    override fun handleResult(p0: Result?) {
        etManageProductCode.setText(p0.toString())
        cancelScan()
    }

}
