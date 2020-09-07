package com.chcreation.pointofsale.product

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Rect
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.view.PixelCopy
import android.view.View
import android.widget.Toast
import androidx.core.content.FileProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.GlideDrawable
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.chcreation.pointofsale.*
import com.chcreation.pointofsale.checkout.DiscountActivity
import com.chcreation.pointofsale.checkout.NoteActivity
import com.chcreation.pointofsale.checkout.PostCheckOutActivity
import com.chcreation.pointofsale.home.HomeFragment
import com.chcreation.pointofsale.model.Product
import com.chcreation.pointofsale.presenter.Homepresenter
import com.chcreation.pointofsale.presenter.ProductPresenter
import com.chcreation.pointofsale.view.MainView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_product_detail.*
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk27.coroutines.onClick
import java.io.File
import java.io.FileOutputStream

class ProductDetailActivity : AppCompatActivity(), MainView {

    private lateinit var presenter: ProductPresenter
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDatabase : DatabaseReference
    private lateinit var product: Product
    private var prodCode = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_detail)

        supportActionBar?.hide()
        mAuth = FirebaseAuth.getInstance()
        mDatabase = FirebaseDatabase.getInstance().reference
        presenter = ProductPresenter(this,mAuth,mDatabase,this)

        prodCode = intent.extras!!.getString("prodCode","")
    }

    override fun onStart() {
        super.onStart()

        presenter.retrieveProducts()

        btnProductDetail.onClick {
            btnProductDetail.startAnimation(normalClickAnimation())

            alert("Are You Want to Share?"){
                title = "Share"
                yesButton {
                    getBitmapFromView(layoutProductDetail,this@ProductDetailActivity){

                    }
                }
                noButton {

                }
            }.show()
        }

        ivProductDetailMoreOptions.onClick {
            val options = mutableListOf("Installment Plan")


            selector("More Options",options) { dialogInterface, i ->
                when(i) {
                    0 ->{
                        startActivity(intentFor<InstallmentPlanActivity>("price" to product.PRICE))
                    }
                }
            }
        }
    }

    private fun fetchData(){
        if (product.IMAGE.toString() != ""){
            Glide.with(this).load(product.IMAGE.toString()).listener(object :
                RequestListener<String, GlideDrawable> {
                override fun onException(
                    e: java.lang.Exception?,
                    model: String?,
                    target: Target<GlideDrawable>?,
                    isFirstResource: Boolean
                ): Boolean {
                    pbProductDetail.visibility = View.GONE
                    return false
                }

                override fun onResourceReady(
                    resource: GlideDrawable?,
                    model: String?,
                    target: Target<GlideDrawable>?,
                    isFromMemoryCache: Boolean,
                    isFirstResource: Boolean
                ): Boolean {
                    pbProductDetail.visibility = View.GONE
                    return false
                }

            }).into(ivProductDetailImage)
        }


        tvProductDetailName.text = product.NAME.toString()
        tvProductDetailDesc.text = product.DESC.toString()
        tvProductDetailPrice.text = indonesiaCurrencyFormat().format(product.PRICE)
    }

    private fun getBitmapFromView(view: View, activity: Activity, callback: (Bitmap) -> Unit) {
        activity.window?.let { window ->
            val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
            val locationOfViewInWindow = IntArray(2)
            view.getLocationInWindow(locationOfViewInWindow)
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    PixelCopy.request(window,
                        Rect(
                            locationOfViewInWindow[0],
                            locationOfViewInWindow[1],
                            locationOfViewInWindow[0] + view.width,
                            locationOfViewInWindow[1] + view.height
                        ), bitmap, { copyResult ->
                            if (copyResult == PixelCopy.SUCCESS) {
                                val photoURI: Uri = FileProvider.getUriForFile(
                                    this,
                                    "com.example.android.fileprovider",
                                    store(bitmap,"share_catalog")
                                )

                                shareImage(photoURI)
                                callback(bitmap)
                            }
                            // possible to handle other result codes ...
                        },
                        Handler()
                    )
                }else
                    toast("Screenshot is not support for this device")
            } catch (e: IllegalArgumentException) {
                // PixelCopy may throw IllegalArgumentException, make sure to handle it
                toast(e.message.toString())
                e.printStackTrace()
            }
        }
    }

    private fun store(bm: Bitmap, fileName: String?): File {
        val dirPath: String =
            getExternalFilesDir(Environment.DIRECTORY_PICTURES)?.absolutePath.toString()
        val dir = File(dirPath)
        if (!dir.exists()) dir.mkdirs()
        val file = File(dirPath, "${fileName}.jpg")
        try {
            val fOut = FileOutputStream(file)
            bm.compress(Bitmap.CompressFormat.PNG, 85, fOut)
            fOut.flush()
            fOut.close()
//
//            val photoURI: Uri = FileProvider.getUriForFile(
//                this,
//                "com.example.android.fileprovider",
//                file
//            )

        } catch (e: Exception) {
            ErrorActivity.errorMessage = e.message.toString()
            startActivity<ErrorActivity>()
            e.printStackTrace()
        }
        return file
    }

    private fun shareImage(uri: Uri) {
        val intent = Intent()
        intent.action = Intent.ACTION_SEND
        intent.type = "image/*"
        intent.putExtra(Intent.EXTRA_SUBJECT, "")
        intent.putExtra(Intent.EXTRA_TEXT, "")
        intent.putExtra(Intent.EXTRA_STREAM, uri)
        try {
            startActivity(Intent.createChooser(intent, "Share Screenshot"))
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, "No App Available", Toast.LENGTH_SHORT).show()
        }
    }

    override fun loadData(dataSnapshot: DataSnapshot, response: String) {
        if (response == EMessageResult.FETCH_PROD_SUCCESS.toString()){
            if (dataSnapshot.exists()){
                for (data in dataSnapshot.children){
                    val item = data.getValue(Product::class.java)

                    if (item!!.PROD_CODE.toString() == prodCode){
                        product = item
                        fetchData()
                        return
                    }
                }
            }
        }
    }

    override fun response(message: String) {
        toast(message)
    }
}
