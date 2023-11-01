package com.onkaringale.shoptask

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.onkaringale.shoptask.databinding.ActivityMainBinding
import com.onkaringale.shoptask.ui.product.AllProductsActivity
import com.onkaringale.shoptask.ui.customer.registration.RegistrationActivity
import com.onkaringale.shoptask.ui.customer.login.LoginActivity
import com.onkaringale.shoptask.utils.SharedPref


class MainActivity : AppCompatActivity()
{
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        if (SharedPref.getdata(this, "isLoggedIn") == "1")
        {
            startAct(Intent(this, AllProductsActivity::class.java))
            return
        }


        binding.Register.setOnClickListener {
            startActivity(Intent(this, RegistrationActivity::class.java))
        }


        binding.Login.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))

        }

        binding.AdminLogin.setOnClickListener {
            SharedPref.setdataBackground(applicationContext, "isLoggedIn", "1");
            SharedPref.setdataBackground(applicationContext, "isadmin", "1");
            SharedPref.setdataBackground(applicationContext, "uid", "admin")
            SharedPref.setdataBackground(applicationContext, "name", "admin")

            startAct(Intent(this, AllProductsActivity::class.java))
        }
    }

    fun startAct(intent: Intent)
    {
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
    }

}