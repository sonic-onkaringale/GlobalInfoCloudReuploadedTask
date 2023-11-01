package com.onkaringale.shoptask.ui.customer.cart

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.android.gms.tasks.Tasks
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import com.onkaringale.shoptask.Constants
import com.onkaringale.shoptask.db.ObjectBox
import com.onkaringale.shoptask.db.models.CartModel
import com.onkaringale.shoptask.models.Order
import com.onkaringale.shoptask.models.Product
import com.onkaringale.shoptask.utils.SharedPref
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Instant
import java.util.Date
import java.util.UUID

class CartViewModel(application: Application) : AndroidViewModel(application)
{
    val cartModelBox = ObjectBox.get().boxFor(CartModel::class.java)
    val allProductsInCart = cartModelBox.all

    val storage: FirebaseStorage = FirebaseStorage.getInstance()
    val storageRef = storage.reference

    val db = FirebaseFirestore.getInstance()
    val currentUser = FirebaseAuth.getInstance().currentUser
    var products = MutableLiveData<ArrayList<Pair<Product, Int>>>()
    var isProcessing = false

    var isTransactionFinished = MutableLiveData<Boolean>(false)
    val isInitialized = MutableLiveData<Boolean>(false)
    var shippingAddress = ""
    var mobileNumberContact = ""
    var dateTime = ""

    init
    {
//        products.postValue(ArrayList())
        getAllProduct()
    }

    fun getAllProduct()
    {
        viewModelScope.launch(Dispatchers.IO) {

            val fetchedProducts = ArrayList<Pair<Product, Int>>()
            val productCollection = db.collection("products")
            for (i in 0 until allProductsInCart.size)
            {
                val task = productCollection.document(allProductsInCart[i].product).get()
                    .addOnSuccessListener { result ->
                        val product = Product(result.data)
                        fetchedProducts.add(Pair(product, allProductsInCart[i].quantity))
                    }.addOnFailureListener {
                        Toast.makeText(getApplication(), "Network Error", Toast.LENGTH_SHORT).show()
                    }
                Tasks.await(task)
            }
            products.postValue(fetchedProducts)
            if (allProductsInCart.isNotEmpty())
                isInitialized.postValue(true)
            else
            {
                withContext(Dispatchers.Main) {
                    Toast.makeText(getApplication(), "Empty Cart", Toast.LENGTH_SHORT).show()
                }

            }
        }
    }

    fun minusOne(position: Int)
    {
        viewModelScope.launch {
            if (isProcessing) return@launch
            isProcessing = true
            val pairArrayList = products.value
            val pair = pairArrayList!![position]
            if (pair.second == 1) return@launch
            pairArrayList[position] = pair.copy(second = pair.second - 1)
            val cartModel = allProductsInCart[position]
            --cartModel.quantity
            cartModelBox.put(cartModel)
            allProductsInCart[position] = cartModel
            products.postValue(pairArrayList!!)
            isProcessing = false
        }
    }

    fun plusOne(position: Int)
    {
        viewModelScope.launch {
            if (isProcessing) return@launch
            isProcessing = true
            val pairArrayList = products.value
            val pair = pairArrayList!![position]
            pairArrayList[position] = pair.copy(second = pair.second + 1)
            val cartModel = allProductsInCart[position]
            ++cartModel.quantity
            cartModelBox.put(cartModel)
            allProductsInCart[position] = cartModel
            products.postValue(pairArrayList!!)
            isProcessing = false

        }
    }

    fun delete(position: Int)
    {
        viewModelScope.launch {
            if (isProcessing) return@launch
            isProcessing = true
            val pairArrayList = products.value
            pairArrayList?.removeAt(position)
            val cartModel = allProductsInCart[position]
            cartModelBox.remove(cartModel)
            allProductsInCart.removeAt(position)
            products.postValue(pairArrayList!!)
            isProcessing = false

        }

    }

    fun uploadOrder()
    {
        viewModelScope.launch(Dispatchers.IO) {
            val uid = UUID.randomUUID().toString();
            val allProductUid = ArrayList<String>()
            val allProductQuantity = ArrayList<Int>()
            withContext(Dispatchers.Main)
            {
                val temp = products.value!!
                for (i in temp.indices)
                {
                    allProductUid.add(temp[i].first.productUid)
                    allProductQuantity.add(temp[i].second)
                }
            }

            val order = Order(uid,
                              SharedPref.getdataBackground(getApplication(), "uid"),
                              allProductUid,
                              allProductQuantity,
                              SharedPref.getdataBackground(getApplication(), "name"),
                              shippingAddress,
                              mobileNumberContact,
                              Timestamp.now(),
                              Constants.PENDING,
                              Timestamp(Date.from(Instant.ofEpochMilli(dateTime.toLong()))),
                              ""
                             )
            val orderCollection = db.collection("orders")
            orderCollection.document(uid).set(order.toMap(), SetOptions.merge())
                .addOnSuccessListener {
                    cartModelBox.removeAll()
                    isTransactionFinished.postValue(true)
                }
        }

    }


}