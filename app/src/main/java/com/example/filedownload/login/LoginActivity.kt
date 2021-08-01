package com.example.filedownload.login

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.filedownload.R
import com.example.filedownload.databinding.ActivityLoginBinding
import com.example.filedownload.home.MainActivity
import com.example.loginviaotp.Utils

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //  setContentView(R.layout.activity_login)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login)
        init()
        initControl()
    }

    fun init() {
        setSupportActionBar(binding.toolbar)
    }

    fun initControl() {
        binding.contentMain.btnGo.setOnClickListener {
            if (binding.contentMain.etMobileNumber.text?.length != 10) {
                Utils.showToast(this, "Please enter a valid mobile number")
                return@setOnClickListener
            }
            DetectingOtpDialogFragment
                .newInstance(binding.contentMain.etMobileNumber.text.toString())
                .show(supportFragmentManager, DetectingOtpDialogFragment.TAG)
        }
    }

    fun moveToHome() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

}