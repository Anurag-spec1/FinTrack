package com.hustlers.fintrack.activities

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import com.hustlers.fintrack.MainActivity
import com.hustlers.fintrack.R
import com.hustlers.fintrack.utils.BiometricLockManager
import com.google.android.material.button.MaterialButton
import java.util.concurrent.Executor

class LockActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "LockActivity"
    }

    private lateinit var biometricManager: BiometricLockManager
    private var pinInput = ""
    private lateinit var tvPinDisplay: TextView
    private lateinit var tvStatus: TextView
    private lateinit var btnBiometric: MaterialButton
    private var hasUnlocked = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate: LockActivity created")
        setContentView(R.layout.activity_lock)

        biometricManager = BiometricLockManager(this)

        tvPinDisplay = findViewById(R.id.tvPinDisplay)
        tvStatus = findViewById(R.id.tvStatus)
        btnBiometric = findViewById(R.id.btnBiometric)

        setupPinButtons()
        setupBiometric()

        val isBiometricAvailable = biometricManager.isBiometricAvailable()
        val isBiometricEnabled = biometricManager.isBiometricEnabled()

        if (isBiometricEnabled && isBiometricAvailable) {
            Log.d(TAG, "onCreate: Auto-showing biometric prompt")
            tvPinDisplay.postDelayed({
                if (!hasUnlocked) {
                    authenticateWithBiometrics()
                }
            }, 500)
        }
    }

    private fun setupPinButtons() {
        val pinButtons = listOf(
            R.id.btn0, R.id.btn1, R.id.btn2, R.id.btn3, R.id.btn4,
            R.id.btn5, R.id.btn6, R.id.btn7, R.id.btn8, R.id.btn9,
            R.id.btnDelete, R.id.btnClear
        )

        pinButtons.forEach { id ->
            findViewById<MaterialButton>(id).setOnClickListener {
                if (!hasUnlocked) {
                    handleButtonClick(it)
                }
            }
        }
    }

    private fun handleButtonClick(button: android.view.View) {
        when (button.id) {
            R.id.btnDelete -> {
                if (pinInput.isNotEmpty()) {
                    pinInput = pinInput.dropLast(1)
                    vibrate()
                }
            }
            R.id.btnClear -> {
                pinInput = ""
                vibrate()
            }
            else -> {
                if (pinInput.length < 6) {
                    pinInput += (button as MaterialButton).text
                    vibrate()
                }
            }
        }
        updatePinDisplay()

        if (pinInput.length == 6) {
            verifyPin()
        }
    }

    private fun setupBiometric() {
        btnBiometric.setOnClickListener {
            if (!hasUnlocked) {
                authenticateWithBiometrics()
            }
        }

        if (!biometricManager.isBiometricAvailable()) {
            btnBiometric.visibility = android.view.View.GONE
        }
    }

    private fun authenticateWithBiometrics() {
        if (hasUnlocked) return

        val biometricManagerSystem = BiometricManager.from(this)
        when (biometricManagerSystem.canAuthenticate()) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                Log.d(TAG, "Biometric hardware available and enrolled")
            }
            else -> {
                Log.e(TAG, "Biometric not available")
                return
            }
        }

        val executor: Executor = ContextCompat.getMainExecutor(this)

        val biometricPrompt = BiometricPrompt(this, executor, object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                Log.d(TAG, "onAuthenticationSucceeded: Authentication successful!")
                if (!hasUnlocked) {
                    runOnUiThread {
                        Toast.makeText(this@LockActivity, "Authentication successful!", Toast.LENGTH_SHORT).show()
                        unlockSuccess()
                    }
                }
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                Log.w(TAG, "onAuthenticationFailed: Authentication failed")
                runOnUiThread {
                    tvStatus.text = "Authentication failed. Try again or use PIN."
                    tvStatus.setTextColor(resources.getColor(android.R.color.holo_red_dark))
                    vibrate()
                }
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                Log.e(TAG, "onAuthenticationError: errorCode=$errorCode, errString=$errString")
                runOnUiThread {
                    tvStatus.text = "Error: $errString"
                    tvStatus.setTextColor(resources.getColor(android.R.color.holo_orange_dark))
                }
            }
        })

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Unlock FinTrack")
            .setSubtitle("Verify your identity")
            .setDescription("Use fingerprint or face recognition to unlock")
            .setNegativeButtonText("Use PIN")
            .build()

        try {
            biometricPrompt.authenticate(promptInfo)
            Log.d(TAG, "Biometric prompt displayed")
        } catch (e: Exception) {
            Log.e(TAG, "Error showing biometric prompt: ${e.message}", e)
        }
    }

    private fun updatePinDisplay() {
        val display = pinInput.map { "●" }.joinToString(" ")
        tvPinDisplay.text = if (pinInput.isEmpty()) "• • • • • •" else display
    }

    private fun verifyPin() {
        if (hasUnlocked) return

        if (biometricManager.verifyPin(pinInput)) {
            Log.d(TAG, "verifyPin: PIN correct, unlocking...")
            unlockSuccess()
        } else {
            Log.w(TAG, "verifyPin: PIN incorrect")
            tvStatus.text = "Wrong PIN. Try again."
            tvStatus.setTextColor(resources.getColor(android.R.color.holo_red_dark))
            pinInput = ""
            updatePinDisplay()
            vibrate()

            tvPinDisplay.postDelayed({
                tvStatus.text = "Enter your PIN"
                tvStatus.setTextColor(resources.getColor(android.R.color.white))
            }, 2000)
        }
    }

    private fun unlockSuccess() {
        if (hasUnlocked) return
        hasUnlocked = true

        Log.d(TAG, "unlockSuccess: Starting unlock process")
        try {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            intent.putExtra("from_unlock", true)
            startActivity(intent)
            finish()
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            Log.d(TAG, "unlockSuccess: Unlock completed successfully")
        } catch (e: Exception) {
            Log.e(TAG, "unlockSuccess: Error unlocking: ${e.message}", e)
            Toast.makeText(this, "Error unlocking: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun vibrate() {
        try {
            val vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                vibrator.vibrate(50)
            }
        } catch (e: Exception) {
        }
    }

    override fun onBackPressed() {
        finishAffinity()
    }
}