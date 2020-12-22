package com.example.flo

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.WindowManager
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.activity_splash.*

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        setContentView(R.layout.activity_splash)

        val timeout: Long = 2000

        Glide.with(this)
            .load("https://grepp-cloudfront.s3.ap-northeast-2.amazonaws.com/programmers_imgs/competition-imgs/2020-Flo-challenge/FLO_Splash-Img3x(1242x2688).png")
            .into(splash)

        Handler().postDelayed({
            kotlin.run {
                val intent = Intent(this, PlayActivity::class.java)
                startActivity(intent)
                finish()
            }
        }, timeout)
    }
}