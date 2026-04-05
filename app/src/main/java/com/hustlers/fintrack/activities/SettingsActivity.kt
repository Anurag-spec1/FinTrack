package com.hustlers.fintrack.activities

import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import com.hustlers.fintrack.MainActivity
import com.hustlers.fintrack.R
import com.hustlers.fintrack.dataclass.ExportData
import com.hustlers.fintrack.storage.FinTrackPreferences
import com.hustlers.fintrack.storage.GoalPreferences
import com.hustlers.fintrack.utils.BiometricLockManager
import com.hustlers.fintrack.utils.CurrencyManager
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*

class SettingsActivity : AppCompatActivity() {

    private lateinit var prefs: FinTrackPreferences
    private lateinit var goalPrefs: GoalPreferences
    private lateinit var currencyManager: CurrencyManager
    private lateinit var biometricManager: BiometricLockManager

    private lateinit var btnBack: TextView
    private lateinit var tvCurrency: TextView

    private lateinit var btnCurrency: LinearLayout
    private lateinit var btnExportData: LinearLayout
    private lateinit var btnImportData: LinearLayout
    private lateinit var tvExportStatus: TextView
    private lateinit var tvVersion: TextView

    private lateinit var switchBiometric: SwitchCompat
    private lateinit var btnSetPin: LinearLayout
    private lateinit var tvPinStatus: TextView

    private val IMPORT_REQUEST_CODE = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        prefs = FinTrackPreferences.getInstance(this)
        goalPrefs = GoalPreferences.getInstance(this)
        currencyManager = CurrencyManager(this)
        biometricManager = BiometricLockManager(this)

        bindViews()
        setupClickListeners()
        loadSettings()
        animateViews()
    }

    private fun bindViews() {
        btnBack = findViewById(R.id.btnBack)
        tvCurrency = findViewById(R.id.tvCurrency)
        btnExportData = findViewById(R.id.btnExportData)
        btnImportData = findViewById(R.id.btnImportData)
        btnCurrency = findViewById(R.id.btnCurrency)
        tvExportStatus = findViewById(R.id.tvExportStatus)
        tvVersion = findViewById(R.id.tvVersion)

        switchBiometric = findViewById(R.id.switchBiometric)
        btnSetPin = findViewById(R.id.btnSetPin)
        tvPinStatus = findViewById(R.id.tvPinStatus)
    }

    private fun setupClickListeners() {
        btnBack.setOnClickListener { finish() }

        btnCurrency.setOnClickListener {
            startActivity(Intent(this, CurrencySettingsActivity::class.java))
        }

        btnExportData.setOnClickListener {
            exportData()
        }

        btnImportData.setOnClickListener {
            importData()
        }

        switchBiometric.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                if (biometricManager.isBiometricAvailable()) {
                    if (!biometricManager.hasPin()) {
                        AlertDialog.Builder(this, R.style.DarkDialogTheme)
                            .setTitle("Set PIN First")
                            .setMessage("Please set a backup PIN before enabling biometric lock.")
                            .setPositiveButton("Set PIN") { _, _ ->
                                showPinSetupDialog()
                                switchBiometric.isChecked = false
                            }
                            .setNegativeButton("Cancel") { _, _ ->
                                switchBiometric.isChecked = false
                            }
                            .show()
                    } else {
                        biometricManager.setBiometricEnabled(true)
                        Toast.makeText(this, "Biometric lock enabled", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Biometric not available on this device", Toast.LENGTH_SHORT).show()
                    switchBiometric.isChecked = false
                }
            } else {
                biometricManager.setBiometricEnabled(false)
                Toast.makeText(this, "Biometric lock disabled", Toast.LENGTH_SHORT).show()
            }
        }

        btnSetPin.setOnClickListener {
            showPinSetupDialog()
        }
    }

    private fun loadSettings() {
        val currentCurrency = currencyManager.currentCurrency
        tvCurrency.text = "${currentCurrency.flag} ${currentCurrency.code} - ${currentCurrency.displayName}"

        tvVersion.text = "Version ${getAppVersion()}"

        try {
            switchBiometric.isChecked = biometricManager.isBiometricEnabled()
            if (biometricManager.hasPin()) {
                tvPinStatus.text = "PIN is set"
                tvPinStatus.setTextColor(resources.getColor(android.R.color.holo_green_dark))
            } else {
                tvPinStatus.text = "Set backup PIN for biometric"
                tvPinStatus.setTextColor(resources.getColor(android.R.color.white))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        updateExportStatus()
    }

    private fun showPinSetupDialog() {
        val editText = EditText(this).apply {
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
            hint = "Enter 6-digit PIN"
            maxLines = 1
            setPadding(50, 30, 50, 30)
        }

        val hasPin = biometricManager.hasPin()

        AlertDialog.Builder(this, R.style.DarkDialogTheme)
            .setTitle(if (hasPin) "Change/Remove PIN" else "Set Backup PIN")
            .setMessage(if (hasPin) "Enter new 6-digit PIN or leave empty to remove" else "This PIN will be used to unlock the app if biometric fails")
            .setView(editText)
            .setPositiveButton(if (hasPin) "Update" else "Set PIN") { _, _ ->
                val pin = editText.text.toString()
                if (pin.length == 6) {
                    biometricManager.setPin(pin)
                    tvPinStatus.text = "PIN is set"
                    tvPinStatus.setTextColor(resources.getColor(android.R.color.holo_green_dark))
                    Toast.makeText(this, "PIN set successfully!", Toast.LENGTH_SHORT).show()
                } else if (pin.isEmpty() && hasPin) {
                    biometricManager.setPin("")
                    tvPinStatus.text = "Set backup PIN for biometric"
                    tvPinStatus.setTextColor(resources.getColor(android.R.color.white))
                    Toast.makeText(this, "PIN removed", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "PIN must be 6 digits", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun exportData() {
        try {
            val transactions = prefs.getTransactions()
            val goals = goalPrefs.getGoals()

            val exportData = ExportData(
                version = 1,
                exportDate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date()),
                userName = prefs.userName,
                userEmail = prefs.userEmail,
                userBio = prefs.userBio,
                goalTarget = prefs.goalTarget,
                currency = currencyManager.currentCurrency.code,
                transactions = transactions,
                goals = goals
            )

            val gson = GsonBuilder().setPrettyPrinting().create()
            val jsonData = gson.toJson(exportData)

            val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
            val fileName = "FinTrack_Backup_${dateFormat.format(Date())}.json"

            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            if (!downloadsDir.exists()) {
                downloadsDir.mkdirs()
            }

            val file = File(downloadsDir, fileName)
            FileWriter(file).use { it.write(jsonData) }

            tvExportStatus.text = "Last backup: ${SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date())}"
            tvExportStatus.visibility = android.view.View.VISIBLE

            AlertDialog.Builder(this, R.style.DarkDialogTheme)
                .setTitle("Export Successful ✓")
                .setMessage("File saved to:\n${file.absolutePath}\n\nSize: ${String.format("%.2f", file.length() / 1024.0)} KB")
                .setPositiveButton("OK") { _, _ -> }
                .setNegativeButton("Share") { _, _ ->
                    shareFile(file)
                }
                .show()

        } catch (e: Exception) {
            e.printStackTrace()
            AlertDialog.Builder(this, R.style.DarkDialogTheme)
                .setTitle("Export Failed")
                .setMessage("Error: ${e.message}")
                .setPositiveButton("OK", null)
                .show()
        }
    }

    private fun shareFile(file: File) {
        try {
            val uri = androidx.core.content.FileProvider.getUriForFile(
                this,
                "${packageName}.fileprovider",
                file
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/json"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            startActivity(Intent.createChooser(shareIntent, "Share Backup File"))
        } catch (e: Exception) {
            Toast.makeText(this, "Failed to share file", Toast.LENGTH_SHORT).show()
        }
    }

    private fun importData() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "application/json"
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        startActivityForResult(intent, IMPORT_REQUEST_CODE)
    }

    private fun performImport(fileUri: android.net.Uri) {
        try {
            val inputStream = contentResolver.openInputStream(fileUri)
            val jsonData = inputStream?.bufferedReader()?.use { it.readText() }

            if (jsonData.isNullOrEmpty()) {
                Toast.makeText(this, "Invalid backup file", Toast.LENGTH_SHORT).show()
                return
            }

            val gson = Gson()
            val type = object : TypeToken<ExportData>() {}.type
            val importData: ExportData = gson.fromJson(jsonData, type)

            val message = buildString {
                appendln("📋 Backup Details:")
                appendln("Date: ${importData.exportDate}")
                appendln("User: ${importData.userName}")
                appendln("Transactions: ${importData.transactions.size}")
                appendln("Goals: ${importData.goals.size}")
                appendln("Currency: ${importData.currency}")
                appendln("\n⚠️ This will REPLACE all existing data!")
            }

            AlertDialog.Builder(this, R.style.DarkDialogTheme)
                .setTitle("Import Data")
                .setMessage(message)
                .setPositiveButton("Import Now") { _, _ ->
                    performDataImport(importData)
                }
                .setNegativeButton("Cancel", null)
                .show()

        } catch (e: Exception) {
            e.printStackTrace()
            AlertDialog.Builder(this, R.style.DarkDialogTheme)
                .setTitle("Import Failed")
                .setMessage("The file is corrupted or invalid.\nError: ${e.message}")
                .setPositiveButton("OK", null)
                .show()
        }
    }

    private fun performDataImport(importData: ExportData) {
        try {
            prefs.clearAll()
            goalPrefs.getGoals().clear()

            prefs.userName = importData.userName
            prefs.userEmail = importData.userEmail
            prefs.userBio = importData.userBio
            prefs.goalTarget = importData.goalTarget

            importData.transactions.forEach { transaction ->
                prefs.addTransaction(transaction)
            }

            importData.goals.forEach { goal ->
                goalPrefs.addGoal(goal)
            }

            val currentCurrency = currencyManager.currentCurrency
            if (currentCurrency.code != importData.currency) {
                AlertDialog.Builder(this, R.style.DarkDialogTheme)
                    .setTitle("Currency Mismatch")
                    .setMessage("Backup was in ${importData.currency} but your app uses ${currentCurrency.code}. You can change currency in settings.")
                    .setPositiveButton("OK", null)
                    .show()
            }

            AlertDialog.Builder(this, R.style.DarkDialogTheme)
                .setTitle("Import Successful ✓")
                .setMessage("Successfully imported:\n• ${importData.transactions.size} transactions\n• ${importData.goals.size} goals\n• User profile data")
                .setPositiveButton("Restart App") { _, _ ->
                    val intent = Intent(this, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                }
                .setNegativeButton("Later") { _, _ ->
                    finish()
                }
                .show()

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Import failed: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun updateExportStatus() {
        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val files = downloadsDir.listFiles { file ->
            file.name.startsWith("FinTrack_Backup_") && file.name.endsWith(".json")
        }

        val latestFile = files?.maxByOrNull { it.lastModified() }

        if (latestFile != null) {
            tvExportStatus.text = "Last backup: ${SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date(latestFile.lastModified()))}"
            tvExportStatus.visibility = android.view.View.VISIBLE
        } else {
            tvExportStatus.text = "No backup found"
            tvExportStatus.visibility = android.view.View.VISIBLE
        }
    }

    private fun getAppVersion(): String {
        return try {
            packageManager.getPackageInfo(packageName, 0).versionName ?: "1.0"
        } catch (e: Exception) {
            "1.0"
        }
    }

    private fun animateViews() {
        val views = listOf(
            findViewById<LinearLayout>(R.id.currencyCard),
            findViewById<LinearLayout>(R.id.securityCard),
            findViewById<LinearLayout>(R.id.dataCard),
            findViewById<LinearLayout>(R.id.aboutCard)
        )

        views.forEachIndexed { index, view ->
            view.alpha = 0f
            view.translationY = 30f
            view.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(400)
                .setStartDelay((index * 100).toLong())
                .start()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == IMPORT_REQUEST_CODE && resultCode == RESULT_OK) {
            data?.data?.let { uri ->
                performImport(uri)
            }
        }
    }
}