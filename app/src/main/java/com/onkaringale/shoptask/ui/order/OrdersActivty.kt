package com.onkaringale.shoptask.ui.order

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.paging.PagingConfig
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.paging.FirestorePagingAdapter
import com.firebase.ui.firestore.paging.FirestorePagingOptions
import com.google.android.material.card.MaterialCardView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import com.onkaringale.shoptask.Constants
import com.onkaringale.shoptask.R
import com.onkaringale.shoptask.databinding.ActivityOrdersActivtyBinding
import com.onkaringale.shoptask.models.Order
import com.onkaringale.shoptask.utils.SharedPref
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Locale


class OrdersActivty : AppCompatActivity()
{
    lateinit var binding: ActivityOrdersActivtyBinding
    val storage: FirebaseStorage = FirebaseStorage.getInstance()
    val storageRef = storage.reference
    val db = FirebaseFirestore.getInstance()
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        binding = ActivityOrdersActivtyBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.paginationProgressIndicatorOrders.isIndeterminate = true


        val config =
            PagingConfig( /* page size */20,  /* prefetchDistance */10,  /* enablePlaceHolders */
                          false)
        val ordersCollection = db.collection("/orders")

        val options: FirestorePagingOptions<Order> = if (SharedPref.getdata(this, "isadmin") == "1")
        {
            val baseQuery: Query =
                ordersCollection.orderBy("placedOrderTime", Query.Direction.ASCENDING)
            FirestorePagingOptions.Builder<Order>().setLifecycleOwner(this)
                .setQuery(baseQuery, config, Order::class.java).build()
        }
        else
        {
            val baseQuery: Query =
                ordersCollection.whereEqualTo("userId", SharedPref.getdata(this, "uid"))
            FirestorePagingOptions.Builder<Order>().setLifecycleOwner(this)
                .setQuery(baseQuery, config, Order::class.java).build()
        }

        val adapter = object : FirestorePagingAdapter<Order, OrderViewHolder>(options)
        {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder
            {
                val view = layoutInflater.inflate(R.layout.list_item_order, parent, false)
                return OrderViewHolder(view)
            }

            override fun onBindViewHolder(viewHolder: OrderViewHolder, position: Int, order: Order)
            {
                // Bind to ViewHolder
                viewHolder.bind(order, this@OrdersActivty)
            }

        }

        binding.ordersRecycler.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.ordersRecycler.adapter = adapter

        lifecycleScope.launch {
            adapter.loadStateFlow.collectLatest { loadStates ->
                when (loadStates.refresh)
                {
                    is LoadState.Error      ->
                    {
                        // The initial load failed. Call the retry() method
                        // in order to retry the load operation.
                        // ...
                        binding.paginationProgressIndicatorOrders.visibility = View.GONE
                        Snackbar.make(binding.root, "Loading Products Failed", Snackbar.LENGTH_LONG)
                            .setAction("Retry") {
                                adapter.retry()
                            }.show()

                    }

                    is LoadState.Loading    ->
                    {
                        // The initial Load has begun
                        // ...
                        binding.paginationProgressIndicatorOrders.visibility = View.VISIBLE

                    }

                    is LoadState.NotLoading ->
                    {
                        binding.paginationProgressIndicatorOrders.visibility = View.GONE
                    }
                }

                when (loadStates.append)
                {
                    is LoadState.Error      ->
                    {
                        // The additional load failed. Call the retry() method
                        // in order to retry the load operation.
                        // ...
                        Snackbar.make(binding.root,
                                      "Loading More Products Failed",
                                      Snackbar.LENGTH_LONG).setAction("Retry") {
                            adapter.retry()
                        }.show()
                        binding.paginationProgressIndicatorOrders.visibility = View.GONE

                    }

                    is LoadState.Loading    ->
                    {
                        // The adapter has started to load an additional page
                        // ...
                        binding.paginationProgressIndicatorOrders.visibility = View.VISIBLE

                    }

                    is LoadState.NotLoading ->
                    {
                        if (loadStates.append.endOfPaginationReached)
                        {
                            // The adapter has finished loading all of the data set
                            // ...
                            binding.paginationProgressIndicatorOrders.visibility = View.GONE

                        }
                        if (loadStates.refresh is LoadState.NotLoading)
                        {
                            // The previous load (either initial or additional) completed
                            // ...
                            binding.paginationProgressIndicatorOrders.visibility = View.GONE

                        }
                    }
                }
            }
        }
    }

    class OrderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
    {

        private var orderTitle: TextView = itemView.findViewById(R.id.tv_title_productCard)

        private var orderDeliveryDate: TextView = itemView.findViewById(R.id.tv_price_productCard)

        private var orderCard: MaterialCardView = itemView.findViewById(R.id.card_productOrder)

        private var orderStatus: TextView = itemView.findViewById(R.id.tv_status_orderCard)


        val storage: FirebaseStorage = FirebaseStorage.getInstance()


        @SuppressLint("SetTextI18n")
        fun bind(order: Order, context: Activity)
        {
            val pattern = "dd-MMM-yyyy"
            val simpleDateFormat = SimpleDateFormat(pattern, Locale.getDefault())
            if (SharedPref.getdata(context, "isadmin") == "1") orderTitle.text =
                order.products.size.toString() + " Orders by " + order.customerName
            else
            {
                orderTitle.text =
                    order.products.size.toString() + " Ordered on " + simpleDateFormat.format(order.placedOrderTime.toDate())
            }



            orderDeliveryDate.text =
                "Arriving on " + simpleDateFormat.format(order.deliveryDate.toDate())

            when (order.orderStatus)
            {
                Constants.PENDING  -> orderStatus.text = "Order Status : Pending Approval"

                Constants.APPROVED -> orderStatus.text = "Order Status : Approved"

                Constants.REJECTED ->
                {
                    orderStatus.text = "Order Status : Rejected"
                    orderDeliveryDate.visibility = View.GONE
                }

            }
            orderCard.setOnClickListener {
                val intent =
                    Intent(context, ViewOrderActivty::class.java).putExtra("order", order.orderUid)
                context.startActivity(intent)
            }
        }
    }
}