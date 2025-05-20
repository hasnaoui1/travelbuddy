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
import com.example.travelbuddy.databinding.ActivityAddDestinationBinding
import com.example.travelbuddy.api.WeatherService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AddDestinationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddDestinationBinding
    private var currentPhotoPath: String? = null
    
    private val takePictureLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            binding.imagePreview.setImageURI(Uri.parse(currentPhotoPath))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddDestinationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        
        binding.btnTakePhoto.setOnClickListener {
            dispatchTakePictureIntent()
        }
        
        binding.btnCheckWeather.setOnClickListener {
            val city = binding.editCity.text.toString()
            if (city.isNotEmpty()) {
                checkWeather(city)
            }
        }
        
        binding.btnSave.setOnClickListener {
            saveDestination()
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
    
    private fun checkWeather(city: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val retrofit = Retrofit.Builder()
                    .baseUrl("https://api.openweathermap.org/data/2.5/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                
                val service = retrofit.create(WeatherService::class.java)
                val response = service.getWeather(city, "metric", "34e8e88f9a1395a7eef0576361c23aa0")
                
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        val weather = response.body()
                        weather?.let {
                            binding.textWeather.text = getString(
                                R.string.weather_info,
                                it.main.temp,
                                it.weather[0].description
                            )
                        }
                    } else {
                        binding.textWeather.text = getString(R.string.weather_error)
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    binding.textWeather.text = getString(R.string.weather_error)
                }
            }
        }
    }
    
    private fun saveDestination() {
        val name = binding.editName.text.toString()
        val country = binding.editCountry.text.toString()
        val city = binding.editCity.text.toString()
        val description = binding.editDescription.text.toString()
        
        if (name.isEmpty() || country.isEmpty() || city.isEmpty()) {
            binding.textError.text = getString(R.string.error_empty_fields)
            return
        }
        
        val destination = Destination(
            name = name,
            country = country,
            city = city,
            description = description,
            imagePath = currentPhotoPath
        )
        
        CoroutineScope(Dispatchers.IO).launch {
            AppDatabase.getInstance(applicationContext)
                .destinationDao()
                .insertDestination(destination)
                
            withContext(Dispatchers.Main) {
                setResult(RESULT_OK)
                finish()
            }
        }
    }
    
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
