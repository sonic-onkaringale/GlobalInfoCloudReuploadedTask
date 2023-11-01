package com.onkaringale.shoptask.ui.customer.registration

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.onkaringale.shoptask.models.User

class RegistrationViewModel(application: Application) : AndroidViewModel(application)
{
    var isInitialized = false
    var name = ""
    var mobileNo = ""
    var emailId = ""
    var address = ""
    var city = ""
    var pincode = ""
    var password = ""


    val db = FirebaseFirestore.getInstance()
    fun initialize()
    {

        if (isInitialized) return
        isInitialized = true
    }

    fun register(token: String,uid:String): Task<Void>
    {
        val userCollection = db.collection("users")

        val user = User(
                uid,name,mobileNo,emailId,address,city,pincode,password,token
                       )
        val documentTask=userCollection.document(uid).set(user.toMap(), SetOptions.merge())
        return documentTask
    }

}