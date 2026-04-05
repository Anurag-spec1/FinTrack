package com.hustlers.fintrack.utils

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.biometric.BiometricManager

class BiometricLockManager(private val context: Context) {

    companion object {
        private const val TAG = "BiometricLockManager"
    }

    private val prefs: SharedPreferences = context.getSharedPreferences("security_prefs", Context.MODE_PRIVATE)

    fun isBiometricAvailable(): Boolean {
        return try {
            val biometricManager = BiometricManager.from(context)
            val result = biometricManager.canAuthenticate()
            Log.d(TAG, "isBiometricAvailable: result=$result")
            when (result) {
                BiometricManager.BIOMETRIC_SUCCESS -> {
                    Log.d(TAG, "Biometric available")
                    true
                }
                BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                    Log.w(TAG, "No biometric hardware")
                    false
                }
                BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                    Log.w(TAG, "Biometric hardware unavailable")
                    false
                }
                BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                    Log.w(TAG, "No biometric enrolled")
                    false
                }
                else -> {
                    Log.w(TAG, "Unknown biometric status: $result")
                    false
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking biometric availability: ${e.message}", e)
            false
        }
    }

    fun isBiometricEnabled(): Boolean {
        val enabled = prefs.getBoolean("biometric_enabled", false)
        Log.d(TAG, "isBiometricEnabled: $enabled")
        return enabled
    }

    fun setBiometricEnabled(enabled: Boolean) {
        Log.d(TAG, "setBiometricEnabled: $enabled")
        prefs.edit().putBoolean("biometric_enabled", enabled).apply()
    }

    fun hasPin(): Boolean {
        val hasPin = getPin().isNotEmpty()
        Log.d(TAG, "hasPin: $hasPin")
        return hasPin
    }

    fun getPin(): String {
        val pin = prefs.getString("user_pin", "") ?: ""
        Log.d(TAG, "getPin: length=${pin.length}")
        return pin
    }

    fun setPin(pin: String) {
        Log.d(TAG, "setPin: length=${pin.length}")
        prefs.edit().putString("user_pin", pin).apply()
    }

    fun verifyPin(pin: String): Boolean {
        val savedPin = getPin()
        val isValid = pin == savedPin
        Log.d(TAG, "verifyPin: isValid=$isValid")
        return isValid
    }
}