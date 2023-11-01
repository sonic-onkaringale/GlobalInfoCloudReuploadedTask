package com.onkaringale.shoptask.ui.customer.login

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import com.google.firebase.firestore.FirebaseFirestore
import com.onkaringale.shoptask.databinding.ActivityLoginBinding
import com.onkaringale.shoptask.models.User
import com.onkaringale.shoptask.ui.product.AllProductsActivity
import com.onkaringale.shoptask.ui.customer.registration.RegistrationActivity
import com.onkaringale.shoptask.utils.SharedPref

class LoginActivity : AppCompatActivity()
{
    lateinit var binding: ActivityLoginBinding
    val viewModel: LoginViewModel by viewModels()
    val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.edtMobile.editText?.setText(viewModel.mobileNo)
        binding.edtPassword.editText?.setText(viewModel.password)
        binding.edtMobile.editText?.addTextChangedListener {
            viewModel.mobileNo = it.toString()
        }
        binding.edtPassword.editText?.addTextChangedListener {
            viewModel.password = it.toString()
        }
        binding.btnLogin.setOnClickListener {
            if (viewModel.mobileNo.isBlank() || viewModel.password.isBlank())
            {
                Toast.makeText(this, "Fill Every Field", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (viewModel.mobileNo.length != 10)
            {
                Toast.makeText(this, "Enter a Valid Mobile Number", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (viewModel.password.length < 4)
            {
                Toast.makeText(this, "Enter password least length of 4", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            binding.edtPassword.isEnabled=false
            binding.edtMobile.isEnabled=false

            val collection = db.collection("users")
            collection
                .whereEqualTo("mobileNo", viewModel.mobileNo)
                .get().addOnSuccessListener {
                    if (it.documents.size==0)
                    {
                        Toast.makeText(this, "User not registered", Toast.LENGTH_LONG).show()
                        startAct(Intent(this@LoginActivity, RegistrationActivity::class.java))
                        return@addOnSuccessListener
                    }
                    val password = it.documents[0].get("password")as String
                    if (password == viewModel.password)
                    {
                        val user = User(it.documents[0].data)
                        SharedPref.setdataBackground(this, "isverified", "1")
                        SharedPref.setdataBackground(this,
                                                     "number",
                                                     user.mobileNo)

                        SharedPref.setdataBackground(applicationContext, "uid", user.userUid)
                        SharedPref.setdataBackground(applicationContext, "name", user.name)
                        SharedPref.setdataBackground(applicationContext,
                                                     "emailId",
                                                     user.emailId)
                        SharedPref.setdataBackground(applicationContext, "city", user.city)
                        SharedPref.setdataBackground(applicationContext,
                                                     "address",
                                                     user.address)
                        SharedPref.setdataBackground(applicationContext,
                                                     "pincode",
                                                     user.pincode)
                        SharedPref.setdataBackground(applicationContext, "firebasetoken", user.firebaseToken)
                        SharedPref.setdataBackground(applicationContext, "isLoggedIn", "1");
                        SharedPref.setdataBackground(applicationContext, "isadmin", "0");

                        Toast.makeText(applicationContext,
                                       "Login Successful",
                                       Toast.LENGTH_SHORT).show()
                        startAct(Intent(this@LoginActivity, AllProductsActivity::class.java))

                    }
                    else
                    {
                        Toast.makeText(this, "Passwords is wrong,enter again", Toast.LENGTH_SHORT).show()
                    }
                }.addOnCompleteListener {
                    binding.edtPassword.isEnabled=true
                    binding.edtMobile.isEnabled=true
                }

        }


    }
    fun startAct(intent: Intent)
    {
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
    }
}