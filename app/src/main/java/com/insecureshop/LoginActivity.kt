package com.insecureshop

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.insecureshop.databinding.ActivityLoginBinding
import com.insecureshop.util.Prefs
import com.insecureshop.util.Util

class LoginActivity : AppCompatActivity() {
    lateinit var mBinding: ActivityLoginBinding

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding =
            DataBindingUtil.setContentView<ActivityLoginBinding>(this, R.layout.activity_login)
        when {
            else -> {
                requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE), 100)
            }
        }
    }

    fun onLogin(view: View) {
        val username = mBinding.edtUserName.text.toString()
        val password = mBinding.edtPassword.text.toString()

        // Remove insecure password logging
        // Log.d("userName", username)
        // Log.d("password", password)

        // Use the new secure authentication method
        val auth = Util.verifyUserNamePassword(applicationContext, username, password)
        if (auth) {
            // Store only what's needed for session management
            Prefs.getInstance(applicationContext).username = username
            // Don't store the raw password anymore - use session token instead
            Prefs.getInstance(applicationContext).sessionToken = generateSessionToken()
            
            Util.saveProductList(this)
            val intent = Intent(this, ProductListActivity::class.java)
            startActivity(intent)
        } else {
            // Remove the dangerous code execution - just show the error message
            Toast.makeText(applicationContext, "Invalid username and password", Toast.LENGTH_LONG)
                .show()
        }
    }
    
    // Generate a simple session token - in production, make this more robust
    private fun generateSessionToken(): String {
        val allowedChars = ('A'..'Z') + ('a'..'z') + ('0'..'9')
        return (1..30)
            .map { allowedChars.random() }
            .joinToString("")
    }
}