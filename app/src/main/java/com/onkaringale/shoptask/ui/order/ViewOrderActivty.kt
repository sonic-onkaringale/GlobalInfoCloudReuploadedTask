package com.onkaringale.shoptask.ui.order

import android.annotation.SuppressLint
import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.storage.FirebaseStorage
import com.onkaringale.shoptask.Constants
import com.onkaringale.shoptask.R
import com.onkaringale.shoptask.databinding.ActivityViewOrderActivtyBinding
import com.onkaringale.shoptask.glide.GlideApp
import com.onkaringale.shoptask.models.Product
import com.onkaringale.shoptask.utils.SharedPref
import java.util.Locale

class ViewOrderActivty : AppCompatActivity()
{
    val viewModel: ViewOrderViewModel by viewModels()
    lateinit var binding: ActivityViewOrderActivtyBinding
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        binding = ActivityViewOrderActivtyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (!intent.hasExtra("order"))
        {
            finish()
            return
        }
        binding.progressBarCart.isIndeterminate=true
        viewModel.setup(intent.getStringExtra("order")!!)
        binding.RejectedReason.visibility=View.GONE
        binding.approvalsLinearLayout.visibility=View.GONE
        viewModel.products.observe(this) {
            binding.checkoutRecycler.layoutManager =
                LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
            binding.checkoutRecycler.adapter = OrderAdapter(it, viewModel)
            var paymentAmount = 1.0
            for (i in it.indices)
            {
                paymentAmount = it[i].first.price * it[i].second
            }
            binding.TotalPriceWholeCart.text = "Total Payment : ₹" + paymentAmount
        }
        viewModel.order.observe(this){
            binding.progressBarCart.visibility=View.GONE
            binding.CustomerName.text = "Customer Name : "+it.customerName
            binding.CustomerContactNumber.text = "Customer Contact Number : "+it.customerMobileNo
            binding.ShippingAddress.text = "Customer Name : "+it.customerAddress
            when(it.orderStatus)
            {
                Constants.PENDING  ->
                {
                    binding.OrderStatus.text = "Order Status : Pending Approval"
                    if (SharedPref.getdata(this, "isadmin")=="1")
                    {
                        binding.edtReasonToReject.visibility=View.VISIBLE
                        binding.approvalsLinearLayout.visibility=View.VISIBLE
                        binding.accept.setOnClickListener {view->
                            binding.approvalsLinearLayout.visibility=View.GONE
                            viewModel.accept(it).addOnSuccessListener {
                                Toast.makeText(this,"Approved",Toast.LENGTH_SHORT).show()
                                binding.RejectedReason.visibility=View.GONE
                                binding.edtReasonToReject.visibility=View.GONE
                                binding.OrderStatus.text = "Order Status :Approved"


                            }.addOnFailureListener {
                                Toast.makeText(this,"Network Error",Toast.LENGTH_SHORT).show()
                                binding.approvalsLinearLayout.visibility=View.VISIBLE

                            }
                        }
                        binding.reject.setOnClickListener {view->
                            if (!binding.edtReasonToReject.editText!!.text.isBlank())
                            {
                                binding.approvalsLinearLayout.visibility=View.GONE
                                viewModel.reject(it,binding.edtReasonToReject.editText!!.text.toString())
                                    .addOnSuccessListener {
                                        binding.OrderStatus.text = "Order Status : Rejected"

                                        binding.edtReasonToReject.visibility=View.GONE
                                        Toast.makeText(this,"Rejected",Toast.LENGTH_SHORT).show()
                                    }
                                    .addOnFailureListener {
                                        Toast.makeText(this,"Network Error",Toast.LENGTH_SHORT).show()
                                        binding.approvalsLinearLayout.visibility=View.VISIBLE

                                    }
                            }
                            else
                            {
                                Toast.makeText(this,"Enter the reason to reject",Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                    else{
                        binding.edtReasonToReject.visibility=View.GONE
                        binding.approvalsLinearLayout.visibility=View.GONE
                    }


                }
                Constants.APPROVED ->
                {
                    binding.OrderStatus.text = "Order Status :Approved"
                    binding.RejectedReason.visibility=View.GONE
                    binding.edtReasonToReject.visibility=View.GONE


                }
                Constants.REJECTED ->
                {
                    binding.OrderStatus.text = "Order Status : Rejected"
                    binding.RejectedReason.visibility=View.VISIBLE
                    binding.RejectedReason.text="Rejected Reason : "+it.rejectionReason
                    binding.edtReasonToReject.visibility=View.GONE


                }
            }
            val pattern = "dd-MMM-yyyy"
            val simpleDateFormat = SimpleDateFormat(pattern, Locale.getDefault())
            binding.OrderPlacedOn.text = "Order Placed On : "+simpleDateFormat.format(it.placedOrderTime.toDate())
            binding.dateOfDelivery.text = "Order Delivery Date : "+simpleDateFormat.format(it.deliveryDate.toDate())



        }

    }

    class OrderAdapter(val dataset: ArrayList<Pair<Product, Int>>,
                       val viewModel: ViewOrderViewModel) :
        RecyclerView.Adapter<OrderAdapter.ViewHolder>()
    {
        val storage: FirebaseStorage = FirebaseStorage.getInstance()
        val storageRef = storage.reference

        class ViewHolder(view: View) : RecyclerView.ViewHolder(view)
        {
            val title: TextView
            val price: TextView
            val totalPrice: TextView
            val image: ImageView


            init
            {
                // Define click listener for the ViewHolder's View
                title = view.findViewById(R.id.title_cart_item)
                price = view.findViewById(R.id.price_cart_item)
                totalPrice = view.findViewById(R.id.total_price_cart_item)
                image = view.findViewById(R.id.imageView_cart_item)


            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder
        {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.list_item_order_cart_like, parent, false)

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


        }
    }
}