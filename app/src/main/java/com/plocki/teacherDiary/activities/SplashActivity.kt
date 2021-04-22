package com.plocki.teacherDiary.activities


import android.animation.ObjectAnimator
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.app.ActivityOptionsCompat
import com.plocki.teacherDiary.R
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class SplashActivity : Activity() {

    private lateinit var logo: ImageView
    private lateinit var text: TextView
    private var width : Float = 0.0f
    private var height : Float = 0.0f
    private var animationDuration = 1000.toLong()
    private lateinit var progres : View


    override fun onCreate(icicle: Bundle?) {
        setContentView(R.layout.activity_splash)
        super.onCreate(icicle)
        window.sharedElementsUseOverlay = false;
        progres = findViewById(R.id.progres)
        initVariables()
        GlobalScope.launch(Main) {
            setIntroAnimation()
            delay(1000)
            setOutroAnimation()
        }

    }

    private fun initVariables(){
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)

        logo = findViewById(R.id.splash_logo)
        text = findViewById(R.id.iv_logo_sd_text)
        width = displayMetrics.widthPixels.toFloat()
        height = displayMetrics.heightPixels.toFloat()
    }

    private fun startMainActivity(){
        val mainIntent = Intent(this, LoginActivity::class.java)
        val compat = ActivityOptionsCompat.makeSceneTransitionAnimation(
            this, findViewById(R.id.splash_logo),
            "trans1"
        )

        this.startActivity(mainIntent, compat.toBundle())
        this.overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out)
        this.finish()
    }



    private fun setIntroAnimation(){
        val objectAnimatorLogo = ObjectAnimator.ofFloat(logo, "translationY", -height, 0f)
        val objectAnimatorText = ObjectAnimator.ofFloat(text, "translationY", -height, 0f)

        objectAnimatorLogo.duration = animationDuration
        objectAnimatorText.duration = animationDuration

        objectAnimatorLogo.start()
        objectAnimatorText.start()

    }

    private fun setOutroAnimation(){
        progres.visibility = View.GONE

//        val objectAnimatorLogo = ObjectAnimator.ofFloat(logo,"translationY",0f,height)
//        val objectAnimatorText = ObjectAnimator.ofFloat(text,"translationY",0f,height)
//
//        objectAnimatorLogo.duration = animationDuration
//        objectAnimatorText.duration = animationDuration
//
//        objectAnimatorLogo.start()
//        objectAnimatorText.start()

        GlobalScope.launch(Main) {
            delay(animationDuration)
            startMainActivity()
        }
    }


}
