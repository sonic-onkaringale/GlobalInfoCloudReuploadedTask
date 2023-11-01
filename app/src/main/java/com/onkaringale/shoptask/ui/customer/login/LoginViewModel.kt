package com.onkaringale.shoptask.ui.customer.login

import android.app.Application
import androidx.lifecycle.AndroidViewModel

class LoginViewModel(application: Application) : AndroidViewModel(application)
{
    var mobileNo = ""
    var password = ""
}