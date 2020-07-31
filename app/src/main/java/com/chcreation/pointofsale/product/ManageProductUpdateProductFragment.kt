package com.chcreation.pointofsale.product

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
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.bumptech.glide.Glide
import com.chcreation.pointofsale.EMessageResult

import com.chcreation.pointofsale.R
import com.chcreation.pointofsale.dateFormat
import com.chcreation.pointofsale.model.Product
import com.chcreation.pointofsale.presenter.Homepresenter
import com.chcreation.pointofsale.presenter.ProductPresenter
import com.chcreation.pointofsale.product.ManageProductDetailActivity.Companion.prodCode
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
import kotlinx.android.synthetic.main.fragment_manage_product_update_product.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk27.coroutines.onCheckedChange
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.support.v4.alert
import org.jetbrains.anko.support.v4.ctx
import org.jetbrains.anko.support.v4.selector
import org.jetbrains.anko.support.v4.toast
import java.io.File
import java.io.IOException
import java.lang.Exception
import java.util.*


class ManageProductUpdateProductFragment : Fragment(), MainView, AdapterView.OnItemSelectedListener {

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
    private var PICK_IMAGE_GALLERY = 222

    companion object{
        var filePath: Uri? = null
        var bitmap: Bitmap? = null
        var productKey = 0
        var product: Product = Product()
        var currentPhotoPath = ""
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
    }

    override fun onStart() {
        super.onStart()

        btnManageProductSave.onClick {
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

        presenter.saveProduct(Product(name,price,desc,cost,manageStock,product.STOCK,image,
            product.PROD_CODE,product.UOM_CODE,categoryItems[positionSpinner],prodCode,product.CREATED_DATE,
            dateFormat().format(Date()), product.CREATED_BY,mAuth.currentUser!!.uid),productKey)
    }
    private fun uploadImage(){
        if(filePath != null){
            layoutManageProduct.alpha = 0.3F
            pbManageProduct.visibility = View.VISIBLE

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
                1 -> {
                    intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    this.startActivityForResult(
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

    private fun fetchData(){
        etManageProductCode.setText(product.CODE.toString())
        etManageProductCost.setText(product.COST.toString())
        etManageProductDescription.setText(product.DESC.toString())
        etManageProductName.setText(product.NAME.toString())
        etManageProductPrice.setText(product.PRICE.toString())
        (activity as AppCompatActivity).supportActionBar?.title = product.NAME.toString()

        if (bitmap != null)
            ivManageProductImage.setImageBitmap(rotateImage(bitmap!!))
        else if (product.IMAGE.toString() != "")
            Glide.with(ctx).load(product.IMAGE).into(ivManageProductImage)

        if (categoryItems.size != 0){
            val index = categoryItems.indexOf(product.CAT)
            spManageProduct.setSelection(index)
        }

        swManageProduct.isChecked = product.MANAGE_STOCK
    }


    override fun loadData(dataSnapshot: DataSnapshot, response: String) {
        if (response == EMessageResult.FETCH_PROD_SUCCESS.toString()){
            if (dataSnapshot.exists())
            {
                try{
                    for (data in dataSnapshot.children){
                        productKey = data.key!!.toInt()
                        val item = data.getValue(Product::class.java)
                        product = item!!
                    }
                    fetchData()
                }catch (e: Exception){
                    showError(ctx,e.message.toString())
                    e.printStackTrace()
                }
            }
        }else if (response == EMessageResult.FETCH_CATEGORY_SUCCESS.toString()) {
            categoryItems.clear()
            categoryItems.add("")
            categoryItems.add("Create Category")

            if (dataSnapshot.exists()){
                for (data in dataSnapshot.children) {
                    val item = data.key
                    if (item != "")
                        categoryItems.add(item.toString())
                }

                selectedCategory = categoryItems[0]
                presenter.retrieveProductByProdCode(prodCode)
                spAdapter.notifyDataSetChanged()
                spManageProduct.setSelection(0)
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
            ctx.startActivity<NewCategory>()
    }

}
