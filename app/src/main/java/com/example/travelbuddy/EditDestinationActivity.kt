package com.example.travelbuddy

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.example.travelbuddy.database.AppDatabase
import com.example.travelbuddy.database.Destination
import com.example.travelbuddy.databinding.ActivityEditDestinationBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class EditDestinationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditDestinationBinding
    private var destinationId: Long = -1
    private var currentPhotoPath: String? = null
    private var destination: Destination? = null

    private val takePictureLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            binding.imagePreview.setImageURI(Uri.parse(currentPhotoPath))
            binding.imagePreview.visibility = android.view.View.VISIBLE
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditDestinationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        destinationId = intent.getLongExtra("DESTINATION_ID", -1)
        if (destinationId == -1L) {
            finish()
            return
        }

        loadDestination()

        binding.btnTakePhoto.setOnClickListener {
            dispatchTakePictureIntent()
        }

        binding.btnSave.setOnClickListener {
            updateDestination()
        }
    }

    private fun loadDestination() {
        CoroutineScope(Dispatchers.IO).launch {
            val dest = AppDatabase.getInstance(applicationContext)
                .destinationDao()
                .getDestinationById(destinationId)

            withContext(Dispatchers.Main) {
                destination = dest
                displayDestination(dest)
            }
        }
    }

    private fun displayDestination(destination: Destination) {
        binding.editName.setText(destination.name)
        binding.editCountry.setText(destination.country)
        binding.editCity.setText(destination.city)
        binding.editDescription.setText(destination.description)

        currentPhotoPath = destination.imagePath

        destination.imagePath?.let { path ->
            binding.imagePreview.setImageURI(Uri.fromFile(File(path)))
            binding.imagePreview.visibility = android.view.View.VISIBLE
        }
    }

    private fun dispatchTakePictureIntent() {
        val photoFile = createImageFile()
        photoFile?.let {
            val photoURI = FileProvider.getUriForFile(
                this,
                "com.example.travelbuddy.fileprovider",
                it
            )
            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
            takePictureLauncher.launch(takePictureIntent)
        }
    }

    private fun createImageFile(): File? {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName = "JPEG_" + timeStamp + "_"
        val storageDir = getExternalFilesDir(null)
        val image = File.createTempFile(
            imageFileName,
            ".jpg",
            storageDir
        )
        currentPhotoPath = image.absolutePath
        return image
    }

    private fun updateDestination() {
        val name = binding.editName.text.toString()
        val country = binding.editCountry.text.toString()
        val city = binding.editCity.text.toString()
        val description = binding.editDescription.text.toString()

        if (name.isEmpty() || country.isEmpty() || city.isEmpty()) {
            binding.textError.text = getString(R.string.error_empty_fields)
            binding.textError.visibility = android.view.View.VISIBLE
            return
        }

        destination?.let { dest ->
            val updatedDestination = dest.copy(
                name = name,
                country = country,
                city = city,
                description = description,
                imagePath = currentPhotoPath
            )

            CoroutineScope(Dispatchers.IO).launch {
                AppDatabase.getInstance(applicationContext)
                    .destinationDao()
                    .updateDestination(updatedDestination)

                withContext(Dispatchers.Main) {
                    setResult(RESULT_OK)
                    finish()
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
