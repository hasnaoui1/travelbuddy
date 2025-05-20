package com.example.travelbuddy

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.travelbuddy.database.AppDatabase
import com.example.travelbuddy.database.Destination
import com.example.travelbuddy.databinding.ActivityDestinationDetailBinding
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File

class DestinationDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDestinationDetailBinding
    private var destinationId: Long = -1
    private var destination: Destination? = null
    private val db = FirebaseFirestore.getInstance()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDestinationDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        
        destinationId = intent.getLongExtra("DESTINATION_ID", -1)
        if (destinationId == -1L) {
            finish()
            return
        }
        
        loadDestination()
        
        binding.btnCall.setOnClickListener {
            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:+123456789")
            }
            startActivity(intent)
        }
        
        binding.btnSms.setOnClickListener {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("smsto:+123456789")
                putExtra("sms_body", getString(R.string.sms_template, destination?.name ?: ""))
            }
            startActivity(intent)
        }
        
        binding.btnSearch.setOnClickListener {
            val intent = Intent(Intent.ACTION_WEB_SEARCH).apply {
                putExtra("query", "${destination?.name} ${destination?.country} travel")
            }
            startActivity(intent)
        }
        
        binding.btnSaveToCloud.setOnClickListener {
            destination?.let { dest ->
                saveToFirebase(dest)
            }
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
        binding.textName.text = destination.name
        binding.textLocation.text = getString(R.string.location_format, destination.city, destination.country)
        binding.textDescription.text = destination.description
        
        destination.imagePath?.let { path ->
            binding.imageDestination.setImageURI(Uri.fromFile(File(path)))
        } ?: run {
            binding.imageDestination.visibility = View.GONE
        }
    }
    
    private fun saveToFirebase(destination: Destination) {
        binding.progressBar.visibility = View.VISIBLE
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val destMap = hashMapOf(
                    "name" to destination.name,
                    "country" to destination.country,
                    "city" to destination.city,
                    "description" to destination.description
                )
                
                db.collection("destinations")
                    .add(destMap)
                    .await()
                
                withContext(Dispatchers.Main) {
                    binding.progressBar.visibility = View.GONE
                    binding.textSyncStatus.text = getString(R.string.sync_success)
                    binding.textSyncStatus.visibility = View.VISIBLE
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    binding.progressBar.visibility = View.GONE
                    binding.textSyncStatus.text = getString(R.string.sync_error)
                    binding.textSyncStatus.visibility = View.VISIBLE
                }
            }
        }
    }
    
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.detail_menu, menu)
        return true
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_share -> {
                shareDestination()
                true
            }
            R.id.action_edit -> {
                editDestination()
                true
            }
            R.id.action_delete -> {
                deleteDestination()
                true
            }
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    
    private fun shareDestination() {
        destination?.let { dest ->
            val shareText = getString(
                R.string.share_destination_text,
                dest.name,
                dest.city,
                dest.country,
                dest.description
            )
            
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, shareText)
            }
            startActivity(Intent.createChooser(intent, getString(R.string.share_via)))
        }
    }
    
    private fun editDestination() {
        val intent = Intent(this, EditDestinationActivity::class.java).apply {
            putExtra("DESTINATION_ID", destinationId)
        }
        startActivity(intent)
    }
    
    private fun deleteDestination() {
        destination?.let { dest ->
            CoroutineScope(Dispatchers.IO).launch {
                AppDatabase.getInstance(applicationContext)
                    .destinationDao()
                    .deleteDestination(dest)
                
                withContext(Dispatchers.Main) {
                    finish()
                }
            }
        }
    }
}
