package com.lg.screen

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import androidx.core.content.ContextCompat
import com.lg.R

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        window.statusBarColor = ContextCompat.getColor(this, R.color.color_secondary)

        val appLogoImage = findViewById<ImageView>(R.id.iv_app_logo)

        appLogoImage.animate().setDuration(1500).alpha(1f).withEndAction{
            val mainIntent = Intent(this,MainActivity::class.java)
            startActivity(mainIntent)
            overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out)
            finish()
        }
    }
}