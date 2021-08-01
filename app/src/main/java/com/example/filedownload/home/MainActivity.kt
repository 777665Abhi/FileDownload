package com.example.filedownload.home

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import com.example.filedownload.R


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        init()
    }
    private fun init()
    {
        supportFragmentManager.commit {
            replace<MainFragment>(R.id.main_wrapper)
            setReorderingAllowed(true)
        }
    }
}