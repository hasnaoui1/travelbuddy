package com.example.travelbuddy

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.travelbuddy.api.WeatherService
import com.example.travelbuddy.databinding.ActivityWeatherBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class WeatherActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWeatherBinding
    private val apiKey = "34e8e88f9a1395a7eef0576361c23aa0"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWeatherBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Set current date
        val dateFormat = SimpleDateFormat("EEEE, MMMM d", Locale.getDefault())
        binding.textDate.text = dateFormat.format(Date())

        binding.btnCheckWeather.setOnClickListener {
            val city = binding.editCity.text.toString()
            if (city.isNotEmpty()) {
                checkWeather(city)
            } else {
                binding.textError.text = getString(R.string.enter_city_name)
                binding.textError.visibility = View.VISIBLE
                binding.weatherCard.visibility = View.GONE
            }
        }

        // Check if we have a city from intent (from a destination)
        intent.getStringExtra("CITY")?.let { city ->
            binding.editCity.setText(city)
            checkWeather(city)
        }
    }

    private fun checkWeather(city: String) {
        binding.progressBar.visibility = View.VISIBLE
        binding.textError.visibility = View.GONE
        binding.weatherCard.visibility = View.GONE

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val retrofit = Retrofit.Builder()
                    .baseUrl("https://api.openweathermap.org/data/2.5/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()

                val service = retrofit.create(WeatherService::class.java)
                val response = service.getWeather(city, "metric", apiKey)

                withContext(Dispatchers.Main) {
                    binding.progressBar.visibility = View.GONE

                    if (response.isSuccessful) {
                        val weather = response.body()
                        weather?.let {
                            binding.weatherCard.visibility = View.VISIBLE
                            binding.textCity.text = city
                            binding.textTemperature.text = getString(R.string.temperature_format, it.main.temp)
                            binding.textDescription.text = it.weather[0].description.capitalize(Locale.getDefault())
                            binding.textHumidity.text = getString(R.string.humidity_format, it.main.humidity)

                            // Set weather icon based on condition
                            val iconName = it.weather[0].icon
                            val weatherIconResId = getWeatherIconResource(iconName)
                            binding.imageWeather.setImageResource(weatherIconResId)
                        }
                    } else {
                        binding.textError.text = getString(R.string.city_not_found)
                        binding.textError.visibility = View.VISIBLE
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    binding.progressBar.visibility = View.GONE
                    binding.textError.text = getString(R.string.weather_error)
                    binding.textError.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun getWeatherIconResource(iconCode: String): Int {
        return when (iconCode) {
            "01d" -> R.drawable.ic_weather_sunny

            "03d", "03n", "04d", "04n" -> R.drawable.ic_weather_cloudy
            "09d", "09n" -> R.drawable.ic_weather_rainy
            "10d", "10n" -> R.drawable.ic_weather_rainy

            "50d", "50n" -> R.drawable.ic_weather_foggy
            else -> R.drawable.ic_weather
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
