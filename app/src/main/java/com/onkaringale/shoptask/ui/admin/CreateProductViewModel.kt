package com.onkaringale.shoptask.ui.admin

import android.app.Application
import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.core.net.toUri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import com.onkaringale.shoptask.models.Product
import com.onkaringale.shoptask.utils.FileUtils
import id.zelory.compressor.Compressor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.UUID

class CreateProductViewModel(application: Application) : AndroidViewModel(application)
{
    var productName = ""
    var productDescription = ""
    var price = 0.0
    val imageUris = MutableLiveData<ArrayList<Uri>>()
    var imageURLs = ArrayList<String>()

    val isProcessing = MutableLiveData<Boolean>()
    val isFinished = MutableLiveData<Boolean>()

    var productUid = UUID.randomUUID().toString()

    val isUpdateSignal = MutableLiveData<Boolean>(false)

    init
    {
        imageUris.postValue(ArrayList())
        isProcessing.postValue(false)
        isFinished.postValue(false)
    }

    val storage: FirebaseStorage = FirebaseStorage.getInstance()
    val storageRef = storage.reference

    val db = FirebaseFirestore.getInstance()

    fun isUpdate(productRef: String)
    {
        isProcessing.postValue(true)
        viewModelScope.launch {
            val task = db.collection("products")
                .document(productRef).get()
            task.addOnSuccessListener {
                val product = Product(it.data)
                productUid = product.productUid
                productName = product.productName
                productDescription = product.productDescription
                price = product.price
                imageURLs = ArrayList(product.imageLinks)
                isUpdateSignal.postValue(true)

            }
            task.await()
            isProcessing.postValue(false)
        }
    }


    fun uploadProduct()
    {
        isProcessing.postValue(true)
        viewModelScope.launch(Dispatchers.IO) {
            val images = imageUris.value
            if (images == null) return@launch
            val imageLinks = ArrayList<String>()
            for (i in 0 until images.size)
            {
                val fileUid = UUID.randomUUID().toString()
                val filePath = "$productUid/$fileUid"
                val fileRef = storageRef.child(filePath)
                val uploadTask = fileRef.putFile(images[i])
                val awaitedTask = Tasks.await(uploadTask)
                if (awaitedTask.task.isSuccessful)
                {
                    awaitedTask.metadata?.path?.let { imageLinks.add(it) }
                }
                else
                {
                    withContext(Dispatchers.Main)
                    {
                        Toast.makeText(getApplication(),
                                       "Upload Failed,Product Didn't Save!",
                                       Toast.LENGTH_LONG).show()
                    }
                    isProcessing.postValue(false)
                    return@launch
                }
            }

            val product = Product(productUid,
                                  productName,
                                  productDescription,
                                  price,
                                  imageLinks.toList())
            val documentTask = db.collection("products")
                .document(product.productUid)
                .set(product.toMap(), SetOptions.merge())
            documentTask.addOnFailureListener {
                Toast.makeText(getApplication(),
                               "Upload Failed,Product Didn't Save!",
                               Toast.LENGTH_LONG).show()
                isProcessing.postValue(false)
            }
            documentTask.addOnSuccessListener {
                isFinished.postValue(true)
            }
        }
    }

    fun updateProduct()
    {
        isProcessing.postValue(true)
        viewModelScope.launch(Dispatchers.IO) {

            val product = Product(productUid,
                                  productName,
                                  productDescription,
                                  price,
                                  imageURLs)
            val documentTask = db.collection("products")
                .document(product.productUid)
                .update(product.toMap())
            documentTask.addOnFailureListener {
                Toast.makeText(getApplication(),
                               "Upate Failed,Product Didn't Save!",
                               Toast.LENGTH_LONG).show()
                isProcessing.postValue(false)
            }
            documentTask.addOnSuccessListener {
                isFinished.postValue(true)
            }
        }
    }

    fun deleteImage(postion: Int)
    {
        val images = imageUris.value!!
        images.removeAt(postion)
        imageUris.postValue(images)
    }

    fun addImages(list: ArrayList<Uri>)
    {
        isProcessing.postValue(true)
        viewModelScope.launch(Dispatchers.IO) {
            val existingImages: ArrayList<Uri> =
                imageUris.value ?: throw RuntimeException("Existing Image Uri Array is Null ")
            val appContext: Context = getApplication()
            val fileUtils = FileUtils(appContext)
            for (i in 0 until list.size)
            {

                val compressedImageFile = Compressor.compress(appContext,
                                                              fileUtils.getFileFromContentUri(
                                                                      appContext,
                                                                      list[i]))
                existingImages.add(compressedImageFile.toUri())


            }
            imageUris.postValue(existingImages)
            Log.d("DEBUG", existingImages.toString())

            isProcessing.postValue(false)
        }
    }
}