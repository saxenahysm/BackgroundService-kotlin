package com.shyam.roomdbexample.coroutinesExamples

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.shyam.roomdbexample.R

class ActivityCoroutineExample : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_coroutine_example)
        Toast.makeText(applicationContext,intent.getStringExtra("---"),Toast.LENGTH_LONG).show()
    }
}