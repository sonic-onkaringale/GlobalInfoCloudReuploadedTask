package com.onkaringale.shoptask.ui.payments

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.onkaringale.shoptask.databinding.ActivityDemoPaymentBinding

class DemoPaymentActivity : AppCompatActivity()
{
    lateinit var binding: ActivityDemoPaymentBinding
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        binding = ActivityDemoPaymentBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val paymentAmount = intent.getDoubleExtra("paymentAmount", 0.0)

        binding.tvPriceDemo.text = "₹" + paymentAmount.toString()
        val resultIntent = Intent()
        binding.btnApprovePayment.setOnClickListener {
            resultIntent.putExtra("paymentResult", true)
            setResult(Activity.RESULT_OK, resultIntent)

            finish()
        }
        binding.btnDeclinePayment.setOnClickListener {
            resultIntent.putExtra("paymentResult", false)
            setResult(Activity.RESULT_OK, resultIntent)

            finish()
        }

    }

}