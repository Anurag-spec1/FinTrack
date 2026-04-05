package com.hustlers.fintrack.activities

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.BounceInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.animation.doOnEnd
import com.hustlers.fintrack.MainActivity
import com.hustlers.fintrack.R
import com.hustlers.fintrack.utils.BiometricLockManager

class SplashScreenActivity : AppCompatActivity() {

    private lateinit var logoContainer: View
    private lateinit var ivLogo: ImageView
    private lateinit var tvAppName: TextView
    private lateinit var tvTagline: TextView
    private lateinit var tvVersion: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var tvInitializing: TextView
    private lateinit var bottomFeatures: View
    private lateinit var glowEffect: View

    private val handler = Handler(Looper.getMainLooper())
    private var isAnimationCompleted = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.decorView.systemUiVisibility = android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                android.view.View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN

        initViews()
        startSplashAnimation()
    }

    private fun initViews() {
        logoContainer = findViewById(R.id.logo_container)
        ivLogo = findViewById(R.id.iv_logo)
        tvAppName = findViewById(R.id.tv_app_name)
        tvTagline = findViewById(R.id.tv_tagline)
        tvVersion = findViewById(R.id.tv_version)
        progressBar = findViewById(R.id.progress_bar)
        tvInitializing = findViewById(R.id.tv_initializing)
        bottomFeatures = findViewById(R.id.bottom_features)
        glowEffect = findViewById(R.id.glow_effect)
    }

    private fun startSplashAnimation() {
        ivLogo.alpha = 0f
        ivLogo.scaleX = 0f
        ivLogo.scaleY = 0f
        glowEffect.alpha = 0f
        glowEffect.scaleX = 0f
        glowEffect.scaleY = 0f
        tvAppName.alpha = 0f
        tvAppName.translationY = 50f
        tvTagline.alpha = 0f
        tvTagline.translationY = 50f
        tvVersion.alpha = 0f
        tvVersion.translationY = 30f
        progressBar.alpha = 0f
        tvInitializing.alpha = 0f
        bottomFeatures.alpha = 0f
        bottomFeatures.translationY = 30f

        val logoScaleX = ObjectAnimator.ofFloat(ivLogo, "scaleX", 0f, 1f).apply {
            duration = 800
            interpolator = BounceInterpolator()
        }

        val logoScaleY = ObjectAnimator.ofFloat(ivLogo, "scaleY", 0f, 1f).apply {
            duration = 800
            interpolator = BounceInterpolator()
        }

        val logoFadeIn = ObjectAnimator.ofFloat(ivLogo, "alpha", 0f, 1f).apply {
            duration = 600
        }

        val logoRotation = ObjectAnimator.ofFloat(ivLogo, "rotation", -180f, 0f, 360f).apply {
            duration = 1200
            interpolator = AccelerateDecelerateInterpolator()
        }

        val glowScaleX = ObjectAnimator.ofFloat(glowEffect, "scaleX", 0f, 1.5f).apply {
            duration = 800
            interpolator = AccelerateDecelerateInterpolator()
        }

        val glowScaleY = ObjectAnimator.ofFloat(glowEffect, "scaleY", 0f, 1.5f).apply {
            duration = 800
            interpolator = AccelerateDecelerateInterpolator()
        }

        val glowFadeIn = ObjectAnimator.ofFloat(glowEffect, "alpha", 0f, 0.3f).apply {
            duration = 600
        }

        val appNameFadeIn = ObjectAnimator.ofFloat(tvAppName, "alpha", 0f, 1f).apply {
            duration = 600
            startDelay = 400
        }

        val appNameSlideUp = ObjectAnimator.ofFloat(tvAppName, "translationY", 50f, 0f).apply {
            duration = 600
            startDelay = 400
            interpolator = DecelerateInterpolator()
        }

        val taglineFadeIn = ObjectAnimator.ofFloat(tvTagline, "alpha", 0f, 1f).apply {
            duration = 600
            startDelay = 800
        }

        val taglineSlideUp = ObjectAnimator.ofFloat(tvTagline, "translationY", 50f, 0f).apply {
            duration = 600
            startDelay = 800
            interpolator = DecelerateInterpolator()
        }

        val versionFadeIn = ObjectAnimator.ofFloat(tvVersion, "alpha", 0f, 1f).apply {
            duration = 500
            startDelay = 1200
        }

        val versionSlideUp = ObjectAnimator.ofFloat(tvVersion, "translationY", 30f, 0f).apply {
            duration = 500
            startDelay = 1200
            interpolator = BounceInterpolator()
        }

        val progressFadeIn = ObjectAnimator.ofFloat(progressBar, "alpha", 0f, 1f).apply {
            duration = 400
            startDelay = 1600
        }

        val initializingFadeIn = ObjectAnimator.ofFloat(tvInitializing, "alpha", 0f, 1f).apply {
            duration = 400
            startDelay = 1600
        }

        val bottomFadeIn = ObjectAnimator.ofFloat(bottomFeatures, "alpha", 0f, 1f).apply {
            duration = 600
            startDelay = 2000
        }

        val bottomSlideUp = ObjectAnimator.ofFloat(bottomFeatures, "translationY", 30f, 0f).apply {
            duration = 600
            startDelay = 2000
            interpolator = DecelerateInterpolator()
        }

        AnimatorSet().apply {
            playTogether(logoScaleX, logoScaleY, logoFadeIn, logoRotation, glowScaleX, glowScaleY, glowFadeIn)
            start()
        }

        AnimatorSet().apply {
            playTogether(appNameFadeIn, appNameSlideUp)
            start()
        }

        AnimatorSet().apply {
            playTogether(taglineFadeIn, taglineSlideUp)
            start()
        }

        AnimatorSet().apply {
            playTogether(versionFadeIn, versionSlideUp)
            start()
        }

        AnimatorSet().apply {
            playTogether(progressFadeIn, initializingFadeIn)
            start()
            doOnEnd {
                animateProgress()
            }
        }

        AnimatorSet().apply {
            playTogether(bottomFadeIn, bottomSlideUp)
            start()
        }

        handler.postDelayed({
            isAnimationCompleted = true
            navigateToMain()
        }, 3500)
    }

    private fun animateProgress() {
        val progressAnimator = ValueAnimator.ofInt(0, 100).apply {
            duration = 2000
            interpolator = DecelerateInterpolator()
            addUpdateListener { animator ->
                val progress = animator.animatedValue as Int
                progressBar.progress = progress

                when (progress) {
                    in 0..20 -> tvInitializing.text = "Setting up secure vault..."
                    in 21..40 -> tvInitializing.text = "Loading your finances..."
                    in 41..60 -> tvInitializing.text = "Syncing transactions..."
                    in 61..80 -> tvInitializing.text = "Preparing insights..."
                    in 81..100 -> tvInitializing.text = "Ready to track!"
                }
            }
        }
        progressAnimator.start()
    }

    private fun navigateToMain() {
        val fadeOut = ObjectAnimator.ofFloat(findViewById<View>(android.R.id.content), "alpha", 1f, 0f).apply {
            duration = 400
            interpolator = AccelerateDecelerateInterpolator()
        }

        fadeOut.doOnEnd {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            finish()
        }

        fadeOut.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
    }
}