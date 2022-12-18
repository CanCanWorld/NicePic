package com.zrq.nicepicture

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.zrq.nicepicture.util.StatusBarUtil

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        StatusBarUtil.transparencyBar(this)
    }
}