package com.onkaringale.shoptask.ui.product

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.FirebaseFirestore
import com.onkaringale.shoptask.db.ObjectBox
import com.onkaringale.shoptask.db.models.CartModel
import com.onkaringale.shoptask.db.models.CartModel_
import com.onkaringale.shoptask.models.Product
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ViewProductViewModel(application: Application) : AndroidViewModel(application)
{
    var isInitialized = false
    val db = FirebaseFirestore.getInstance()
    val product = MutableLiveData<Product>()
    val cartModelBox = ObjectBox.get().boxFor(CartModel::class.java)


    val isProductPresentInCart = MutableLiveData<Boolean>(true)

    fun initialize(productReference: String)
    {
        if (isInitialized) return
        viewModelScope.launch(Dispatchers.IO) {
            isInitialized = true
            val productCollection = db.collection("products")
            val task = productCollection.document(productReference).get()
                .addOnSuccessListener { result ->
                    viewModelScope.launch(Dispatchers.IO){
                        val productResult = Product(result.data)
                        product.postValue(productResult)
                        val count =
                            cartModelBox.query(CartModel_.product.equal(productResult.productUid))
                                .build()
                                .count()
                        if (count.toInt() == 0)
                            isProductPresentInCart.postValue(false)
                        else
                            isProductPresentInCart.postValue(true)
                    }
                }.addOnFailureListener {
                    Toast.makeText(getApplication(), "Network Error", Toast.LENGTH_SHORT).show()
                }
            Tasks.await(task)
        }
    }

    fun addToCart()
    {
        viewModelScope.launch(Dispatchers.IO) {
            val count = cartModelBox.query(CartModel_.product.equal(product.value!!.productUid))
                .build().count()
            if (count.toInt() == 0)
            {
                cartModelBox.put(CartModel(0L, product.value!!.productUid, 1))
                isProductPresentInCart.postValue(true)
                withContext(Dispatchers.Main){
                    Toast.makeText(getApplication(),"Added to Cart",Toast.LENGTH_SHORT).show()
                }
            }
            else
                isProductPresentInCart.postValue(true)
        }


    }
}