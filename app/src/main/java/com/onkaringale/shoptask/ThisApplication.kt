package com.onkaringale.shoptask

import android.app.Application
import com.google.firebase.Firebase
import com.google.firebase.initialize
import com.onkaringale.shoptask.db.ObjectBox

class ThisApplication:Application()
{
    override fun onCreate()
    {
        super.onCreate()
        Firebase.initialize(this)

        ObjectBox.init(this)

    }
}