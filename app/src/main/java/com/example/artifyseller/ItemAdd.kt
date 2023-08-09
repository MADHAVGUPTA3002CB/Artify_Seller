package com.example.artifyseller

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.ViewModelProvider
import com.example.artifyseller.data.User
import com.example.artifyseller.data.UserItemAdd
import com.example.artifyseller.databinding.ActivityItemAddBinding
import com.example.artifyseller.viewmodel.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream
import java.util.Date

class ItemAdd : AppCompatActivity() {

    private lateinit var binding : ActivityItemAddBinding
    private lateinit var imageBitmap : Bitmap
    private lateinit var auth : FirebaseAuth
    private lateinit var storage: FirebaseStorage
    private var selectedImg: Uri? = null
    private lateinit var vm : ViewModel
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityItemAddBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val backImage : ImageView = findViewById(R.id.toolbarBackImage)

        backImage.setOnClickListener {
            val intent = Intent(this, ItemFragment::class.java)
            startActivity(intent)
        }

        binding.saveItemButton.setOnClickListener {
            // this function also include the code to store data in fire store
            uploadImageToStorage(imageBitmap)

            val intent = Intent(this , ItemFragment::class.java)
            startActivity(intent)
        }

        binding.productImage.setOnClickListener {
            imageOptionDialogue()
        }
    }
    // code to send image to storage and the getting uri
    fun uploadImageToStorage(imageBitmap : Bitmap){
        auth = FirebaseAuth.getInstance()
        vm = ViewModelProvider(this).get(ViewModel::class.java)
        val baos = ByteArrayOutputStream()
        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()

        storage = FirebaseStorage.getInstance()
        val storageRef = storage.reference

        val storagePath = storageRef.child("Photos/${Date().time.toString()}.jpg")
        val uploadTask = storagePath.putBytes(data)

        uploadTask.addOnSuccessListener { it ->
            val task = it.metadata?.reference?.downloadUrl
            task?.addOnSuccessListener {
                selectedImg = it
                val user_item_data = UserItemAdd(binding.productNameTextView.text.toString() ,
                    binding.priceTextView.text.toString().toInt() ,
                    binding.availableQuantityTextView.text.toString() ,
                    binding.productDescription.text.toString(),
                    selectedImg.toString())

                vm.upload_item_data(user_item_data)
                Toast.makeText(this, "$it", Toast.LENGTH_SHORT).show()
            }
                ?.addOnFailureListener {
                    Toast.makeText(this, "task failed", Toast.LENGTH_SHORT).show()
                }
        }
    }

    // code for choosing btw camera and media
    private fun imageOptionDialogue() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.custom_dialog_select_image_options)
        dialog.window!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        dialog.findViewById<LinearLayout>(R.id.layoutTakePicture).setOnClickListener {
            fromcamera()
            dialog.dismiss()
        }

        dialog.findViewById<LinearLayout>(R.id.layoutSelectFromGallery).setOnClickListener {
            pickFromGallery()
            dialog.dismiss()
        }

        dialog.setOnDismissListener {
            //See description at declaration
        }

        dialog.show()
    }
    @SuppressLint("QueryPermissionsNeeded")
    private fun pickFromGallery() {
        val pickPictureIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        if (pickPictureIntent.resolveActivity(this.packageManager) != null) {
            startActivityForResult(pickPictureIntent, 2)
        }
    }

    @SuppressLint("QueryPermissionsNeeded")
    private fun fromcamera() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(takePictureIntent, 1)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                1 -> {
                    imageBitmap = data?.extras?.get("data") as Bitmap
                    try {
                        binding.productImage.setImageBitmap(imageBitmap)
                    }catch (e: Exception){}
                }
                2 -> {
                    val imageUri = data?.data
                    imageBitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, imageUri)
                    try {
                        binding.productImage.setImageBitmap(imageBitmap)
                    }catch (e :Exception){
                    }
                }
            }
        }
    }
}