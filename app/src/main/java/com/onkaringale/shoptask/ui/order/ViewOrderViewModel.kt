package com.onkaringale.shoptask.ui.order

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.onkaringale.shoptask.Constants
import com.onkaringale.shoptask.models.Order
import com.onkaringale.shoptask.models.Product
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ViewOrderViewModel(application: Application) : AndroidViewModel(application)
{
    var isInitialized = false
    val db = FirebaseFirestore.getInstance()
    val storage: FirebaseStorage = FirebaseStorage.getInstance()
    val storageRef = storage.reference
    var products = MutableLiveData<ArrayList<Pair<Product, Int>>>()

    val order = MutableLiveData<Order>()

    fun setup(orderRef: String)
    {
        if (isInitialized) return
        viewModelScope.launch(Dispatchers.IO) {
            isInitialized = true
            var innerOrder: Order
            val orderCollections = db.collection("orders")
            val documentTask = orderCollections.document(orderRef).get()
            documentTask.addOnSuccessListener {
                viewModelScope.launch(Dispatchers.IO) {
                    innerOrder = Order(it.data)
                    val fetchedProducts = ArrayList<Pair<Product, Int>>()
                    val productCollection = db.collection("products")
                    for (i in 0 until innerOrder.products.size)
                    {
                        val task = productCollection.document(innerOrder.products[i]).get()
                            .addOnSuccessListener { result ->
                                val product = Product(result.data)
                                fetchedProducts.add(Pair(product, innerOrder.productQuantity[i]))
                            }.addOnFailureListener {
                                Toast.makeText(getApplication(),
                                               "Network Error",
                                               Toast.LENGTH_SHORT)
                                    .show()
                            }
                        Tasks.await(task)
                    }
                    products.postValue(fetchedProducts)
                    order.postValue(innerOrder)
                }


            }


        }

    }

    fun accept(order: Order): Task<Void>
    {
        val orderCollections = db.collection("orders")
        order.orderStatus = Constants.APPROVED
        val documentTask = orderCollections.document(order.orderUid).update(
                order.toMap())
        return documentTask
    }

    fun reject(order: Order, rejectMessage: String): Task<Void>
    {

        val orderCollections = db.collection("orders")
        order.orderStatus = Constants.REJECTED
        order.rejectionReason = rejectMessage
        val documentTask = orderCollections.document(order.orderUid).update(
                order.toMap())
        return documentTask

    }
}