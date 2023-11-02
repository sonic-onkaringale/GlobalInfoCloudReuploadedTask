package com.onkaringale.shoptask.ui.customer.cart

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.firebase.storage.FirebaseStorage
import com.onkaringale.shoptask.R
import com.onkaringale.shoptask.databinding.ActivityCartBinding
import com.onkaringale.shoptask.glide.GlideApp
import com.onkaringale.shoptask.models.Product
import com.onkaringale.shoptask.ui.payments.DemoPaymentActivity
import java.util.Calendar
import kotlin.time.Duration.Companion.days
import kotlin.time.DurationUnit

class CartActivity : AppCompatActivity()
{
    companion object {
        const val PAYMENT_REQUEST_CODE = 123
    }
    val viewModel: CartViewModel by viewModels()
    private lateinit var binding: ActivityCartBinding
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        binding = ActivityCartBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.edtShippingAddressCart.visibility=View.GONE
        binding.edtMobileCart.visibility=View.GONE
        binding.btnCheckout.visibility=View.GONE
        binding.datePickButton.visibility=View.GONE
        binding.TotalPriceWholeCart.visibility=View.GONE
        binding.progressBarCart.visibility=View.GONE


        binding.edtShippingAddressCart.editText?.setText(viewModel.shippingAddress)
        binding.edtMobileCart.editText?.setText(viewModel.mobileNumberContact)
//        binding.edtDatetimeCart.editText?.setText(viewModel.dateTime)

        binding.edtShippingAddressCart.editText?.addTextChangedListener {
            viewModel.shippingAddress = it.toString()
        }
        binding.edtMobileCart.editText?.addTextChangedListener {
            viewModel.mobileNumberContact = it.toString()
        }
        binding.datePickButton.setOnClickListener {
            val calendarContraints = CalendarConstraints.Builder()
                .setValidator(DateValidatorPointForward.from(
                        Calendar.getInstance().timeInMillis
                                + 2.days.toLong(DurationUnit.MILLISECONDS)
                                                            )).build()
            val datePicker =
                MaterialDatePicker.Builder.datePicker()
                    .setTitleText("Select date")
                    .setCalendarConstraints(calendarContraints)
                    .build()
            datePicker.showNow(supportFragmentManager,"date")
            datePicker.addOnPositiveButtonClickListener {
                viewModel.dateTime = it.toString()
            }
        }

        viewModel.products.observe(this) {
            if (it.size==0)
            {
                binding.edtShippingAddressCart.visibility=View.GONE
                binding.edtMobileCart.visibility=View.GONE
                binding.btnCheckout.visibility=View.GONE
                binding.datePickButton.visibility=View.GONE
                binding.TotalPriceWholeCart.visibility=View.GONE
                Toast.makeText(this,"Cart Empty",Toast.LENGTH_LONG).show()
                finish()
                return@observe
            }
            binding.checkoutRecycler.layoutManager =
                LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
            binding.checkoutRecycler.adapter = CartAdapter(it, viewModel)
            var paymentAmount = 0.0 // Replace with the actual payment amount
            for (i in it.indices)
            {
                paymentAmount += it[i].first.price * it[i].second
            }
            binding.TotalPriceWholeCart.text="Total Payment : ₹"+paymentAmount
            binding.btnCheckout.setOnClickListener {view->
                if (viewModel.shippingAddress.isBlank() || viewModel.mobileNumberContact.isBlank() )
                {
                    Toast.makeText(this, "Fill Every Field", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                if (viewModel.mobileNumberContact.length != 10)
                {
                    Toast.makeText(this, "Enter a Valid Mobile Number", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                if (viewModel.dateTime.isBlank())
                {
                    Toast.makeText(this, "Pick a date of delivery", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }


                val intent = Intent(this, DemoPaymentActivity::class.java)
                intent.putExtra("paymentAmount", paymentAmount)
                startActivityForResult(intent, PAYMENT_REQUEST_CODE)
            }
        }
        viewModel.isInitialized.observe(this){
            if (it)
            {
                binding.edtShippingAddressCart.visibility=View.VISIBLE
                binding.edtMobileCart.visibility=View.VISIBLE
                binding.btnCheckout.visibility=View.VISIBLE
                binding.datePickButton.visibility=View.VISIBLE
                binding.TotalPriceWholeCart.visibility=View.VISIBLE
                binding.progressBarCart.isIndeterminate=true
                binding.progressBarCart.visibility=View.GONE

            }
            else
            {
                binding.edtShippingAddressCart.visibility=View.GONE
                binding.edtMobileCart.visibility=View.GONE
                binding.btnCheckout.visibility=View.GONE
                binding.datePickButton.visibility=View.GONE
                binding.TotalPriceWholeCart.visibility=View.GONE
                binding.progressBarCart.visibility=View.VISIBLE

            }
        }
    }
    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PAYMENT_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                val paymentResult = data?.getBooleanExtra("paymentResult", false)
                if (paymentResult == true) {
                    Toast.makeText(this, "Payment Approved", Toast.LENGTH_SHORT).show()
                    viewModel.uploadOrder()
                    viewModel.isTransactionFinished.observe(this){
                        if (it)
                            finish()
                    }
                } else {
                    // Payment failed
                    // Handle the failure
                    Toast.makeText(this, "Payment Rejected", Toast.LENGTH_SHORT).show()

                }
            } else {
                // Payment was canceled or failed
                // Handle the failure
                Toast.makeText(this, "Payment Canceled", Toast.LENGTH_SHORT).show()

            }
        }
    }


    class CartAdapter(val dataset: ArrayList<Pair<Product, Int>>, val viewModel: CartViewModel) :
        RecyclerView.Adapter<CartAdapter.ViewHolder>()
    {
        val storage: FirebaseStorage = FirebaseStorage.getInstance()
        val storageRef = storage.reference

        class ViewHolder(view: View) : RecyclerView.ViewHolder(view)
        {
            val title: TextView
            val price: TextView
            val totalPrice: TextView
            val image: ImageView
            val delete: Button
            val minusOne: Button
            val quantity: TextView
            val plusOne: Button

            init
            {
                // Define click listener for the ViewHolder's View
                title = view.findViewById(R.id.title_cart_item)
                price = view.findViewById(R.id.price_cart_item)
                totalPrice = view.findViewById(R.id.total_price_cart_item)
                image = view.findViewById(R.id.imageView_cart_item)
                delete = view.findViewById(R.id.btn_delete)
                minusOne = view.findViewById(R.id.btn_sub_quantity)
                plusOne = view.findViewById(R.id.btn_add_quantity)
                quantity = view.findViewById(R.id.quantity)


            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder
        {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.list_item_cart_product, parent, false)

            return ViewHolder(view)
        }

        override fun getItemCount(): Int
        {
            return dataset.size
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int)
        {
            val product = dataset[position].first
            val quantity = dataset[position].second
            holder.title.text = product.productName
            holder.price.text = "₹${product.price} x $quantity"
            val totalPrice = product.price * quantity
            holder.totalPrice.text = "₹$totalPrice"
            holder.image.clipToOutline = true
            val firstImageLink = storageRef.child(product.imageLinks[0])
            GlideApp.with(holder.image)
                .load(firstImageLink)
                .centerCrop()
                .into(holder.image)
            holder.quantity.text = quantity.toString()
            if (quantity == 1)
                holder.minusOne.visibility = View.GONE
            holder.minusOne.setOnClickListener {
                viewModel.minusOne(position)
            }
            holder.plusOne.setOnClickListener {
                viewModel.plusOne(position)
            }
            holder.delete.setOnClickListener {
                viewModel.delete(position)
            }
        }
    }

}