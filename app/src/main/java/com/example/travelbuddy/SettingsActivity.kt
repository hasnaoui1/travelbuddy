package com.example.travelbuddy

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.travelbuddy.databinding.ActivitySettingsBinding
import java.util.Locale

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Set the current language selection
        val currentLocale = resources.configuration.locales[0]
        binding.radioEnglish.isChecked = currentLocale.language == "en"
        binding.radioFrench.isChecked = currentLocale.language == "fr"

        binding.btnApplyLanguage.setOnClickListener {
            val selectedLanguage = when {
                binding.radioEnglish.isChecked -> "en"
                binding.radioFrench.isChecked -> "fr"
                else -> "en" // Default to English
            }
            
            setLocale(selectedLanguage)
            Toast.makeText(this, "Language changed", Toast.LENGTH_SHORT).show()
            
            // Restart the app to apply changes
            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }
    }

    private fun setLocale(languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        
        val config = Configuration()
        config.setLocale(locale)
        
        resources.updateConfiguration(config, resources.displayMetrics)
        
        // Save the selected language preference
        val sharedPreferences = getSharedPreferences("Settings", Context.MODE_PRIVATE)
        sharedPreferences.edit().putString("language", languageCode).apply()
    }
    
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
