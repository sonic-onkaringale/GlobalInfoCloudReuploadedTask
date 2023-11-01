package com.onkaringale.shoptask.ui.customer.registration

import android.content.Intent
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.core.widget.addTextChangedListener
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.ErrorCodes
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.installations.FirebaseInstallations
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.onkaringale.shoptask.R
import com.onkaringale.shoptask.utils.FileUtils
import com.onkaringale.shoptask.databinding.ActivityRegistrationBinding
import com.onkaringale.shoptask.ui.product.AllProductsActivity
import com.onkaringale.shoptask.ui.customer.login.LoginActivity
import com.onkaringale.shoptask.utils.SharedPref
import com.sangcomz.fishbun.FishBun
import com.sangcomz.fishbun.adapter.image.impl.GlideAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID

class RegistrationActivity : AppCompatActivity()
{
    private val GALLERY_CODE = 378
    val db = FirebaseFirestore.getInstance()


    lateinit var binding: ActivityRegistrationBinding
    var imageFile: File? = null

    val viewModel: RegistrationViewModel by viewModels()
    private lateinit var signInLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        binding = ActivityRegistrationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        viewModel.initialize()
        when (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK)
        {
            Configuration.UI_MODE_NIGHT_YES ->
            {
                binding.imagebtnAddimage.imageTintList = ColorStateList.valueOf(Color.WHITE)
                binding.btnDeleteImage.imageTintList = ColorStateList.valueOf(Color.WHITE)
            }

            Configuration.UI_MODE_NIGHT_NO  ->
            {
            }
        }
        binding.cardviewImageContainer.visibility = View.GONE
        binding.addImage.setOnClickListener { v ->
            FishBun.with(this).setImageAdapter(GlideAdapter()).setMinCount(1).setMaxCount(1)
                .startAlbumWithOnActivityResult(GALLERY_CODE)
        }
        binding.edtName.editText?.setText(viewModel.name)
        binding.edtMobile.editText?.setText(viewModel.mobileNo)
        binding.edtEmail.editText?.setText(viewModel.emailId)
        binding.edtAddress.editText?.setText(viewModel.address)
        binding.edtCity.editText?.setText(viewModel.city)
        binding.edtPincode.editText?.setText(viewModel.pincode)
        binding.edtPassword.editText?.setText(viewModel.password)

        binding.edtName.editText?.addTextChangedListener {
            viewModel.name = it.toString()
        }
        binding.edtMobile.editText?.addTextChangedListener {
            viewModel.mobileNo = it.toString()
        }
        binding.edtEmail.editText?.addTextChangedListener {
            viewModel.emailId = it.toString()
        }
        binding.edtAddress.editText?.addTextChangedListener {
            viewModel.address = it.toString()
        }
        binding.edtCity.editText?.addTextChangedListener {
            viewModel.city = it.toString()
        }
        binding.edtPincode.editText?.addTextChangedListener {
            viewModel.pincode = it.toString()
        }
        binding.edtPassword.editText?.addTextChangedListener {
            viewModel.password = it.toString()
        }

        binding.btnRegister.setOnClickListener {
            if (viewModel.name.isBlank() || viewModel.mobileNo.isBlank() || viewModel.emailId.isBlank() || viewModel.address.isBlank() || viewModel.city.isBlank() || viewModel.pincode.isBlank() || viewModel.password.isBlank())
            {
                Toast.makeText(this, "Fill Every Field", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (viewModel.mobileNo.length != 10)
            {
                Toast.makeText(this, "Enter a Valid Mobile Number", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!viewModel.emailId.isValidEmail())
            {
                Toast.makeText(this, "Enter a Valid Email Address", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (viewModel.pincode.length != 6)
            {
                Toast.makeText(this, "Enter a Valid Pincode", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (viewModel.password.length < 4)
            {
                Toast.makeText(this, "Enter password least length of 4", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (imageFile == null)
            {
                Toast.makeText(this, "Click Photo of Address Proof", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            db.collection("users")
                .whereEqualTo("mobileNo",viewModel.mobileNo)
                .get().addOnSuccessListener {
                    if (it.size()==0)
                        verifyAndLogin()
                    else
                    {
                        Toast.makeText(this, "User Already Exists,Redireting to Login", Toast.LENGTH_LONG).show()
                        startAct(Intent(this, LoginActivity::class.java))
                    }

                }



        }

        signInLauncher = registerForActivityResult<Intent, FirebaseAuthUIAuthenticationResult>(
                FirebaseAuthUIActivityResultContract()) { result: FirebaseAuthUIAuthenticationResult ->
            // Handle the FirebaseAuthUIAuthenticationResult
            // ...
            val response = result.idpResponse
            if (result.resultCode == AppCompatActivity.RESULT_OK)
            {
                // Successfully signed in
                SharedPref.setdataBackground(this, "isverified", "1")
                SharedPref.setdataBackground(this,
                                             "number",
                                             deleteCountry(result.idpResponse!!.phoneNumber!!))
                FirebaseInstallations.getInstance().getToken(true).addOnCompleteListener {

                    CoroutineScope(Dispatchers.IO).launch {
                        val token = it.result.token

                        val uid = UUID.randomUUID().toString()
                        val documentTask = viewModel.register(token, uid)
                        documentTask.addOnSuccessListener {
                            Toast.makeText(applicationContext,
                                           "Login Successful",
                                           Toast.LENGTH_SHORT).show()
                            SharedPref.setdataBackground(applicationContext, "uid", uid)
                            SharedPref.setdataBackground(applicationContext, "name", viewModel.name)
                            SharedPref.setdataBackground(applicationContext,
                                                         "emailId",
                                                         viewModel.emailId)
                            SharedPref.setdataBackground(applicationContext, "city", viewModel.city)
                            SharedPref.setdataBackground(applicationContext,
                                                         "address",
                                                         viewModel.address)
                            SharedPref.setdataBackground(applicationContext,
                                                         "pincode",
                                                         viewModel.pincode)
                            SharedPref.setdataBackground(applicationContext, "firebasetoken", token)
                            SharedPref.setdataBackground(applicationContext, "isLoggedIn", "1");
                            SharedPref.setdataBackground(applicationContext, "isadmin", "0");

                            startAct(Intent(this@RegistrationActivity, AllProductsActivity::class.java))
                        }


                    }

                }
            }
            else
            {
                // Sign in failed
                if (response == null)
                {
                    // User pressed back button
                    Toast.makeText(this, "Login Canceled", Toast.LENGTH_SHORT).show()
                    return@registerForActivityResult
                }
                if (response.error!!.errorCode == ErrorCodes.NO_NETWORK)
                {
                    Toast.makeText(this, "Check Your Network Connection", Toast.LENGTH_SHORT).show()
                    return@registerForActivityResult
                }
                Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show()
                Log.e("DEBUG", "Sign-in error: ", response.error)
            }
        }
    }

    private fun verifyAndLogin()
    {
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        try
        {
            val img = InputImage.fromFilePath(this, imageFile!!.toUri())
            val result = recognizer.process(img).addOnSuccessListener { visionText ->
                    // Task completed successfully
                    // ...
                    if (visionText.text.contains(viewModel.city, true) && visionText.text.contains(
                                viewModel.pincode)
                    )
                    {
                        Toast.makeText(this, "Verified", Toast.LENGTH_SHORT).show()
                        startLoginActivity()
                    }
                    else
                    {
                        Toast.makeText(this,
                                       "Address Proof must contain both pincode and city",
                                       Toast.LENGTH_LONG).show()
                    }


                }.addOnFailureListener { e ->
                    // Task failed with an exception
                    // ...
                    Toast.makeText(this,
                                   "Text recognition failed mitigating you to login",
                                   Toast.LENGTH_LONG).show()
                    startLoginActivity()
                }
        } catch (e: Exception)
        {
            Toast.makeText(this,
                           "Text recognition failed mitigating you to login",
                           Toast.LENGTH_LONG).show()
            startLoginActivity()
        }
    }

    fun SetImage(image: File)
    {
        binding.addImage.visibility = View.GONE
        binding.cardviewImageContainer.visibility = View.VISIBLE
        Glide.with(this).load(image).skipMemoryCache(true).diskCacheStrategy(DiskCacheStrategy.NONE)
            .into(binding.imagePreview)
        imageFile = image
        binding.btnDeleteImage.setOnClickListener { v -> DeleteImage() }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)
    {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == GALLERY_CODE && resultCode == RESULT_OK && data != null)
        {
            val uris = data.getParcelableArrayListExtra<Uri>(FishBun.INTENT_PATH)
            if (!uris!!.isEmpty())
            {
                val fileUtils = FileUtils(this)
                fileUtils.compress(this, fileUtils.getFileFromContentUri(this, uris[0]))
            }
        }
    }

    fun DeleteImage()
    {
        binding.addImage.visibility = View.VISIBLE
        binding.cardviewImageContainer.visibility = View.GONE
        if (imageFile != null) imageFile = null
    }

    fun startAct(intent: Intent)
    {
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
    }

    private fun startLoginActivity()
    {

        val signInIntent = AuthUI.getInstance().createSignInIntentBuilder() // ... options ...
            .setAvailableProviders(java.util.List.of(AuthUI.IdpConfig.PhoneBuilder()
                                                         .setDefaultNumber("IN", viewModel.mobileNo)
                                                         .setAllowedCountries(listOf("+91"))
                                                         .build())).setTheme(R.style.Theme_ShopTask)
            .setIsSmartLockEnabled(false, false).setLockOrientation(true)
            .setLogo(R.drawable.ic_launcher_foreground).build()

        signInLauncher.launch(signInIntent)
    }

    fun deleteCountry(phone: String): String
    {
        val phoneInstance = PhoneNumberUtil.getInstance()
        try
        {
            val phoneNumber = phoneInstance.parse(phone, null)
            return phoneNumber?.nationalNumber?.toString() ?: phone
        } catch (_: Exception)
        {
        }
        if (phone[0] == '0')
        {
            return phone.substring(1);
        }
        return phone
    }

    fun CharSequence?.isValidEmail() =
        !isNullOrEmpty() && Patterns.EMAIL_ADDRESS.matcher(this).matches()
}