package com.onkaringale.shoptask.ui.product

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.storage.FirebaseStorage
import com.onkaringale.shoptask.R
import com.onkaringale.shoptask.databinding.ActivityViewProductBinding
import com.smarteist.autoimageslider.SliderViewAdapter
import com.stfalcon.imageviewer.StfalconImageViewer

class ViewProductActivity : AppCompatActivity()
{
    val viewModel: ViewProductViewModel by viewModels()
    lateinit var binding: ActivityViewProductBinding
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        binding = ActivityViewProductBinding.inflate(layoutInflater)
        setContentView(binding.root)
        if (intent.extras == null)
        {
            finish()
            return
        }
        if (!intent.hasExtra("product"))
        {
            finish()
            return
        }
        viewModel.initialize(intent.getStringExtra("product")!!)
        viewModel.product.observe(this) {
            binding.tvProductName.text = it.productName
            binding.tvProductDescription.text = it.productDescription
            binding.tvProductPrice.text = "₹" + it.price
            val adap = SliderAdapterExample(this@ViewProductActivity, it.imageLinks)
            binding.imageSlider.setSliderAdapter(adap)
            binding.imageSlider.setInfiniteAdapterEnabled(false)
        }
        viewModel.isProductPresentInCart.observe(this) {
            if (it) binding.btnAddToCart.visibility = View.GONE
            else binding.btnAddToCart.visibility = View.VISIBLE
            binding.btnAddToCart.setOnClickListener {
                viewModel.addToCart()
            }
        }

    }

    private class SliderAdapterExample(context: Context, val imageLinks: List<String>) :
        SliderViewAdapter<SliderAdapterExample.SliderAdapterVH>()
    {
        val storage: FirebaseStorage = FirebaseStorage.getInstance()
        val storageRef = storage.reference
        private val context: Context


        init
        {
            this.context = context
        }

        override fun onCreateViewHolder(parent: ViewGroup): SliderAdapterVH
        {

            val inflate: View = LayoutInflater.from(parent.context).inflate(R.layout.sliding, null)
            return SliderAdapterVH(inflate)
        }

        override fun onBindViewHolder(viewHolder: SliderAdapterVH, position: Int)
        {

            viewHolder.textViewDescription.text = ""
            viewHolder.textViewDescription.textSize = 16f
            viewHolder.textViewDescription.setTextColor(android.graphics.Color.WHITE)


            Glide.with(viewHolder.itemView).load(storageRef.child(imageLinks[position])).fitCenter()
                .into(viewHolder.imageViewBackground)
            viewHolder.imageViewBackground.setOnClickListener {
                StfalconImageViewer.Builder<String>(context, imageLinks) { view, imageLink ->
                    val imageRef = storageRef.child(imageLink)
                    Glide.with(viewHolder.itemView).load(imageRef).fitCenter().into(view)
                }.withTransitionFrom(viewHolder.imageViewBackground).withHiddenStatusBar(true)
                    .allowZooming(true).allowSwipeToDismiss(true).withStartPosition(position).show()
            }
            viewHolder.itemView.setOnClickListener {

            }
        }

        override fun getCount(): Int
        {
            //slider view count could be dynamic size
            return imageLinks.size
        }

        inner class SliderAdapterVH(itemView: View) : ViewHolder(itemView)
        {
            var imageViewBackground: ImageView
            var imageGifContainer: ImageView
            var textViewDescription: TextView

            init
            {
                imageViewBackground = itemView.findViewById(R.id.iv_auto_image_slider)
                imageGifContainer = itemView.findViewById(R.id.iv_gif_container)
                textViewDescription = itemView.findViewById(R.id.tv_auto_image_slider)
            }
        }
    }
}