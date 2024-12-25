package com.example.jobfinder.Utils

import android.app.Activity
import android.net.Uri
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.example.jobfinder.R
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageException
import com.google.firebase.storage.StorageReference

object RetriveImg {
    fun retrieveImage(userId: String, imgView: ImageView) {
        val context = imgView.context
        if (context is Activity && !context.isDestroyed) {
            // Tiếp tục tải ảnh nếu activity vẫn chưa bị phá hủy
            // Còn không thì không làm gì cả
            val storageReference: StorageReference = FirebaseStorage.getInstance().reference
            val imageRef: StorageReference = storageReference.child(userId)

            // Trước tiên tải URI của hình ảnh
            imageRef.downloadUrl
                .addOnSuccessListener { uri: Uri ->
                    imgView.setBackgroundResource(R.drawable.image_loading_80px)
                    // Sử dụng Glide để tải ảnh từ URI
                    Glide.with(context)
                        .load(uri)
                        .apply(RequestOptions.bitmapTransform(CircleCrop()))
                        .into(imgView)
                }
                .addOnFailureListener { exception ->
                    if (exception is StorageException &&
                        (exception.errorCode == StorageException.ERROR_OBJECT_NOT_FOUND ||
                                exception.errorCode == StorageException.ERROR_BUCKET_NOT_FOUND)
                    ) {
                        retrieveImage("default_user_avt.png", imgView)
                    }
                }
        }
    }
}
