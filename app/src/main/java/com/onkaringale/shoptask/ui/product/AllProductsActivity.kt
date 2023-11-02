package com.onkaringale.shoptask.ui.product

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.paging.PagingConfig
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.paging.FirestorePagingAdapter
import com.firebase.ui.firestore.paging.FirestorePagingOptions
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import com.onkaringale.shoptask.MainActivity
import com.onkaringale.shoptask.R
import com.onkaringale.shoptask.databinding.ActivityAllProductsBinding
import com.onkaringale.shoptask.glide.GlideApp
import com.onkaringale.shoptask.models.Product
import com.onkaringale.shoptask.ui.admin.CreateProductActivity
import com.onkaringale.shoptask.ui.customer.cart.CartActivity
import com.onkaringale.shoptask.ui.order.OrdersActivty
import com.onkaringale.shoptask.utils.SharedPref
import io.objectbox.BoxStore
import io.objectbox.BoxStoreBuilder
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.File


class AllProductsActivity : AppCompatActivity()
{
    lateinit var binding: ActivityAllProductsBinding


    val db = FirebaseFirestore.getInstance()
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        binding = ActivityAllProductsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.paginationProgressIndicator.isIndeterminate = true


        if (SharedPref.getdata(this, "isadmin") == "1")
        {
            binding.btnAddProduct.setOnClickListener {
                startActivity(Intent(this, CreateProductActivity::class.java))
            }
        }
        else
        {
            binding.btnAddProduct.visibility = View.GONE
        }
        binding.btnCart.setOnClickListener {
            startActivity(Intent(this, CartActivity::class.java))
        }
        binding.btnOrder.setOnClickListener {
            startActivity(Intent(this, OrdersActivty::class.java))
        }
        binding.btnLogout.setOnClickListener {
            val intentBaseactivity = Intent(this, MainActivity::class.java)
            intentBaseactivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            FirebaseAuth.getInstance().signOut()
            BoxStore.deleteAllFiles(File(BoxStoreBuilder.DEFAULT_NAME))
            SharedPref.deleteall(this)
            val ctx: Context = applicationContext
            val pm = ctx.packageManager
            val intent = pm.getLaunchIntentForPackage(ctx.packageName)
            val mainIntent = Intent.makeRestartActivityTask(intent!!.component)
            ctx.startActivity(mainIntent)
            Runtime.getRuntime().exit(0)
        }


        val productCollection = db.collection("/products")
        val baseQuery: Query = productCollection.orderBy("createdTime", Query.Direction.DESCENDING)
        val config =
            PagingConfig( /* page size */20,  /* prefetchDistance */10,  /* enablePlaceHolders */
                          false)
        val options: FirestorePagingOptions<Product> =
            FirestorePagingOptions.Builder<Product>().setLifecycleOwner(this)
                .setQuery(baseQuery, config, Product::class.java).build()

        val adapter = object : FirestorePagingAdapter<Product, ProductViewHolder>(options)
        {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder
            {
                val view = layoutInflater.inflate(R.layout.list_item_product, parent, false)
                return ProductViewHolder(view)
            }

            override fun onBindViewHolder(viewHolder: ProductViewHolder,
                                          position: Int,
                                          product: Product)
            {
                // Bind to ViewHolder
                viewHolder.bind(product, this@AllProductsActivity)
            }

        }
        binding.productsRecycler.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.productsRecycler.adapter = adapter
        // Activities can use lifecycleScope directly, but Fragments should instead use
// viewLifecycleOwner.lifecycleScope.
        lifecycleScope.launch {
            adapter.loadStateFlow.collectLatest { loadStates ->
                when (loadStates.refresh)
                {
                    is LoadState.Error      ->
                    {
                        // The initial load failed. Call the retry() method
                        // in order to retry the load operation.
                        // ...
                        binding.paginationProgressIndicator.visibility = View.GONE
                        Snackbar.make(binding.root, "Loading Products Failed", Snackbar.LENGTH_LONG)
                            .setAction("Retry") {
                                adapter.retry()
                            }.show()

                    }

                    is LoadState.Loading    ->
                    {
                        // The initial Load has begun
                        // ...
                        binding.paginationProgressIndicator.visibility = View.VISIBLE

                    }

                    is LoadState.NotLoading ->
                    {
                        binding.paginationProgressIndicator.visibility = View.GONE
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
                        binding.paginationProgressIndicator.visibility = View.GONE

                    }

                    is LoadState.Loading    ->
                    {
                        // The adapter has started to load an additional page
                        // ...
                        binding.paginationProgressIndicator.visibility = View.VISIBLE

                    }

                    is LoadState.NotLoading ->
                    {
                        if (loadStates.append.endOfPaginationReached)
                        {
                            // The adapter has finished loading all of the data set
                            // ...
                            binding.paginationProgressIndicator.visibility = View.GONE

                        }
                        if (loadStates.refresh is LoadState.NotLoading)
                        {
                            // The previous load (either initial or additional) completed
                            // ...
                            binding.paginationProgressIndicator.visibility = View.GONE

                        }
                    }
                }
            }
        }
    }

    class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
    {

        private var productTitle: TextView = itemView.findViewById(R.id.tv_title_productCard)


        private var productPrice: TextView = itemView.findViewById(R.id.tv_price_productCard)
        private var productMainImage: ImageView = itemView.findViewById(R.id.img_productCard)
        private var productCard: MaterialCardView = itemView.findViewById(R.id.card_product)
        private var productEdit: MaterialButton = itemView.findViewById(R.id.btn_edit)

        val storage: FirebaseStorage = FirebaseStorage.getInstance()
        val storageRef = storage.reference


        fun bind(product: Product, context: Context)
        {
            if (SharedPref.getdataBackground(context, "isadmin") == "1")
            {
                productEdit.setOnClickListener {
                    context.startActivity(Intent(context,
                                                 CreateProductActivity::class.java).putExtra("product",
                                                                                             product.productUid))
                }
            }
            else
            {
                productEdit.visibility = View.GONE
            }
            productTitle.text = product.productName

            productPrice.text = "₹" + product.price.toString()


            if (product.imageLinks.isNotEmpty())
            {
                val firstImageLink = storageRef.child(product.imageLinks[0])
                GlideApp.with(itemView.context).load(firstImageLink).into(productMainImage)
            }
            productCard.setOnClickListener {
                context.startActivity(Intent(context,
                                             ViewProductActivity::class.java).putExtra("product",
                                                                                       product.productUid))
            }
        }
    }
}