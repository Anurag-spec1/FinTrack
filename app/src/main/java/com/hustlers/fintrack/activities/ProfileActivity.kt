package com.hustlers.fintrack.activities

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import com.hustlers.fintrack.R
import com.hustlers.fintrack.storage.FinTrackPreferences
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ProfileActivity : AppCompatActivity() {

    private lateinit var prefs: FinTrackPreferences

    private lateinit var ivProfilePhoto: ImageView
    private lateinit var tvChangePhoto: TextView
    private lateinit var etName: EditText
    private lateinit var etEmail: EditText
    private lateinit var etBio: EditText
    private lateinit var tvMemberSince: TextView
    private lateinit var btnSave: TextView
    private lateinit var btnBack: TextView
    private lateinit var btnEditPhoto: LinearLayout

    private var selectedImageUri: Uri? = null
    private var selectedBitmap: Bitmap? = null

    private val PICK_IMAGE_REQUEST = 1
    private val CAMERA_REQUEST = 2
    private val PERMISSION_REQUEST = 3

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = android.graphics.Color.TRANSPARENT

        prefs = FinTrackPreferences.getInstance(this)

        bindViews()
        loadUserData()
        setupClickListeners()
        animateEntrance()
    }

    private fun bindViews() {
        ivProfilePhoto = findViewById(R.id.ivProfilePhoto)
        tvChangePhoto = findViewById(R.id.tvChangePhoto)
        etName = findViewById(R.id.etName)
        etEmail = findViewById(R.id.etEmail)
        etBio = findViewById(R.id.etBio)
        tvMemberSince = findViewById(R.id.tvMemberSince)
        btnSave = findViewById(R.id.btnSave)
        btnBack = findViewById(R.id.btnBack)
        btnEditPhoto = findViewById(R.id.btnEditPhoto)
    }

    private fun loadUserData() {
        etName.setText(prefs.userName)
        etEmail.setText(prefs.userEmail)
        etBio.setText(prefs.userBio)

        if (prefs.memberSince.isEmpty()) {
            val dateFormat = SimpleDateFormat("MMM yyyy", Locale.getDefault())
            prefs.memberSince = dateFormat.format(Date())
        }
        tvMemberSince.text = "Member since ${prefs.memberSince}"

        val photoBitmap = prefs.getUserPhotoBitmap()
        if (photoBitmap != null) {
            ivProfilePhoto.setImageBitmap(photoBitmap)
            selectedBitmap = photoBitmap
        } else {
            ivProfilePhoto.setImageResource(R.mipmap.ic_launcher)
        }
    }

    private fun setupClickListeners() {
        btnBack.setOnClickListener {
            finish()
        }

        btnEditPhoto.setOnClickListener {
            showPhotoPickerDialog()
        }

        tvChangePhoto.setOnClickListener {
            showPhotoPickerDialog()
        }

        btnSave.setOnClickListener {
            saveProfile()
        }
    }

    private fun showPhotoPickerDialog() {
        val options = arrayOf("Take Photo", "Choose from Gallery", "Remove Photo")
        AlertDialog.Builder(this, R.style.DarkDialogTheme)
            .setTitle("Profile Photo")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> checkCameraPermission()
                    1 -> openGallery()
                    2 -> removePhoto()
                }
            }
            .show()
    }

    private fun checkCameraPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.CAMERA),
                    PERMISSION_REQUEST
                )
            } else {
                openCamera()
            }
        } else {
            openCamera()
        }
    }

    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (intent.resolveActivity(packageManager) != null) {
            startActivityForResult(intent, CAMERA_REQUEST)
        } else {
            Toast.makeText(this, "Camera not available", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    private fun removePhoto() {
        selectedImageUri = null
        selectedBitmap = null
        ivProfilePhoto.setImageResource(R.drawable.default_avatar)
        prefs.userPhoto = ""
        animatePhotoChange()
        Toast.makeText(this, "Photo removed", Toast.LENGTH_SHORT).show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                PICK_IMAGE_REQUEST -> {
                    data?.data?.let { uri ->
                        selectedImageUri = uri
                        try {
                            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
                            val compressed = compressBitmap(bitmap)
                            selectedBitmap = compressed
                            ivProfilePhoto.setImageBitmap(compressed)
                            animatePhotoChange()
                        } catch (e: Exception) {
                            e.printStackTrace()
                            Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                CAMERA_REQUEST -> {
                    data?.extras?.get("data")?.let { obj ->
                        val bitmap = obj as Bitmap
                        val compressed = compressBitmap(bitmap)
                        selectedBitmap = compressed
                        ivProfilePhoto.setImageBitmap(compressed)
                        animatePhotoChange()
                    }
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera()
            } else {
                Toast.makeText(this, "Camera permission required", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun compressBitmap(bitmap: Bitmap): Bitmap {
        val maxSize = 500
        var width = bitmap.width
        var height = bitmap.height

        if (width > maxSize || height > maxSize) {
            val ratio = width.toFloat() / height.toFloat()
            if (width > height) {
                width = maxSize
                height = (maxSize / ratio).toInt()
            } else {
                height = maxSize
                width = (maxSize * ratio).toInt()
            }
            return Bitmap.createScaledBitmap(bitmap, width, height, true)
        }
        return bitmap
    }

    private fun saveProfile() {
        val name = etName.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val bio = etBio.text.toString().trim()

        if (name.isEmpty()) {
            etName.error = "Name is required"
            etName.requestFocus()
            return
        }

        prefs.userName = name
        prefs.userEmail = email
        prefs.userBio = bio

        selectedBitmap?.let {
            prefs.saveUserPhoto(it)
        }

        animateSaveSuccess()

        Toast.makeText(this, "Profile updated successfully!", Toast.LENGTH_SHORT).show()

        finish()
    }

    private fun animateEntrance() {
        val views = listOf(
            ivProfilePhoto,
            btnEditPhoto,
            findViewById<LinearLayout>(R.id.nameContainer),
            findViewById<LinearLayout>(R.id.emailContainer),
            findViewById<LinearLayout>(R.id.bioContainer),
            findViewById<LinearLayout>(R.id.memberContainer),
            btnSave
        )

        views.forEachIndexed { index, view ->
            view.alpha = 0f
            view.translationY = 30f
            AnimatorSet().apply {
                playTogether(
                    ObjectAnimator.ofFloat(view, "alpha", 0f, 1f),
                    ObjectAnimator.ofFloat(view, "translationY", 30f, 0f)
                )
                duration = 400
                startDelay = (index * 80).toLong()
                interpolator = DecelerateInterpolator(1.5f)
                start()
            }
        }
    }

    private fun animatePhotoChange() {
        ivProfilePhoto.animate()
            .scaleX(0.9f)
            .scaleY(0.9f)
            .setDuration(150)
            .withEndAction {
                ivProfilePhoto.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(200)
                    .start()
            }
            .start()
    }

    private fun animateSaveSuccess() {
        btnSave.animate()
            .scaleX(0.95f)
            .scaleY(0.95f)
            .setDuration(100)
            .withEndAction {
                btnSave.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(200)
                    .start()
            }
            .start()
    }
}