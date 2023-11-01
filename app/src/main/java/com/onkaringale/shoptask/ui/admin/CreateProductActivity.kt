package com.onkaringale.shoptask.ui.admin

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.onkaringale.shoptask.R
import com.onkaringale.shoptask.databinding.ActivityCreateProductBinding
import com.sangcomz.fishbun.FishBun
import com.sangcomz.fishbun.adapter.image.impl.GlideAdapter

class CreateProductActivity : AppCompatActivity()
{
    val galleryRequestCode = 10

    lateinit var binding: ActivityCreateProductBinding
    val viewModel: CreateProductViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateProductBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.progressBar.isIndeterminate = true
        if (intent.hasExtra("product"))
        {
            viewModel.isUpdate(intent.getStringExtra("product")!!)
        }
        binding.edtProductName.editText?.setText(viewModel.productName)
        binding.edtProductDescription.editText?.setText(viewModel.productDescription)
        binding.edtProductPrice.editText?.setText(viewModel.price.toString())
        binding.edtProductName.editText?.addTextChangedListener {
            viewModel.productName = it.toString()
        }
        binding.edtProductDescription.editText?.addTextChangedListener {
            viewModel.productDescription = it.toString()
        }
        binding.edtProductPrice.editText?.addTextChangedListener {
            if (it.toString().isNotBlank()) viewModel.price = it.toString().toDouble()
            else viewModel.price = 0.0
        }
        viewModel.isProcessing.observe(this) {
            setEnabled(it)
        }
        binding.btnSave.setOnClickListener {
            if (intent.hasExtra("product")) viewModel.updateProduct()
            else viewModel.uploadProduct()
        }
        binding.addImage.setOnClickListener {
            FishBun.with(this).setImageAdapter(GlideAdapter()).setMinCount(1).setMaxCount(5)
                .startAlbumWithOnActivityResult(galleryRequestCode)
        }


        viewModel.imageUris.observe(this) {
            val adapter = CustomAdapter(it, viewModel)
            binding.addImageRecycler.layoutManager =
                LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
            binding.addImageRecycler.adapter = adapter
        }
        viewModel.isFinished.observe(this) {
            if (it)
            {
                Toast.makeText(applicationContext,
                               "Product Uploaded Successfully ",
                               Toast.LENGTH_SHORT).show()
                finish()
            }
        }
        viewModel.isUpdateSignal.observe(this) {
            if (it)
            {
                binding.edtProductName.editText?.setText(viewModel.productName)
                binding.edtProductDescription.editText?.setText(viewModel.productDescription)
                binding.edtProductPrice.editText?.setText(viewModel.price.toString())

                binding.addImage.visibility = View.GONE
                binding.addImageRecycler.visibility = View.GONE
            }
        }

    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)
    {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode)
        {
            galleryRequestCode -> if (resultCode == RESULT_OK)
            {

                val path: ArrayList<Uri> =
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                    {
                        data!!.getParcelableArrayListExtra(FishBun.INTENT_PATH, Uri::class.java)!!
                    }
                    else
                    {
                        data!!.getParcelableArrayListExtra(FishBun.INTENT_PATH)!!

                    }
                Log.d("DEBUG", path.toString())
                viewModel.addImages(path)
            }
        }

    }

    private fun setEnabled(it: Boolean)
    {
        binding.btnSave.isEnabled = !it
        binding.edtProductName.isEnabled = !it
        binding.edtProductDescription.isEnabled = !it
        binding.edtProductPrice.isEnabled = !it
        binding.addImage.isEnabled = !it
        binding.addImageRecycler.isEnabled = !it
        if (it) binding.progressBar.visibility = View.VISIBLE
        else binding.progressBar.visibility = View.GONE

    }

    class CustomAdapter(private val dataSet: ArrayList<Uri>,
                        val viewModel: CreateProductViewModel) :
        RecyclerView.Adapter<CustomAdapter.ViewHolder>()
    {


        class ViewHolder(view: View) : RecyclerView.ViewHolder(view)
        {
            val imagePreview: ImageView
            val deleteImage: ImageButton

            init
            {
                imagePreview = view.findViewById(R.id.image_preview)
                deleteImage = view.findViewById(R.id.btn_delete_image)
            }
        }


        override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder
        {

            val view = LayoutInflater.from(viewGroup.context)
                .inflate(R.layout.list_item_image_create_product, viewGroup, false)

            return ViewHolder(view)
        }


        override fun onBindViewHolder(viewHolder: ViewHolder, position: Int)
        {


            viewHolder.deleteImage.setOnClickListener {
                viewModel.deleteImage(position)
            }

            viewHolder.imagePreview.setImageURI(dataSet[position])
        }


        override fun getItemCount() = dataSet.size

    }

}