package com.example.travelbuddy

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.travelbuddy.adapters.DestinationAdapter
import com.example.travelbuddy.database.AppDatabase
import com.example.travelbuddy.database.Destination
import com.example.travelbuddy.databinding.ActivityMainBinding
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: DestinationAdapter
    private lateinit var auth: FirebaseAuth
    private val ADD_DESTINATION_REQUEST = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        setSupportActionBar(binding.toolbar)


        val toggle = ActionBarDrawerToggle(
            this, binding.drawerLayout, binding.toolbar,
            R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        binding.navView.setNavigationItemSelectedListener(this)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Setup RecyclerView
        adapter = DestinationAdapter(emptyList()) { destination ->
            val intent = Intent(this, DestinationDetailActivity::class.java)
            intent.putExtra("DESTINATION_ID", destination.id)
            startActivity(intent)
        }

        binding.contentMain.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.contentMain.recyclerView.adapter = adapter

        // FAB to add new destination
        binding.contentMain.fab.setOnClickListener {
            val intent = Intent(this, AddDestinationActivity::class.java)
            startActivityForResult(intent, ADD_DESTINATION_REQUEST)
        }

        // Load destinations from database
        loadDestinations()
    }

    private fun loadDestinations() {
        CoroutineScope(Dispatchers.IO).launch {
            val destinations = AppDatabase.getInstance(applicationContext).destinationDao().getAllDestinations()
            withContext(Dispatchers.Main) {
                adapter.updateDestinations(destinations)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ADD_DESTINATION_REQUEST && resultCode == RESULT_OK) {
            loadDestinations()
            Toast.makeText(this, R.string.destination_added, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
                true
            }
            R.id.action_search -> {
                startActivity(Intent(this, SearchActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_home -> {
                // Already on home, do nothing
            }


            R.id.nav_weather -> {
                startActivity(Intent(this, WeatherActivity::class.java))
            }
            R.id.nav_share -> {
                val shareIntent = Intent(Intent.ACTION_SEND)
                shareIntent.type = "text/plain"
                shareIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_app_text))
                startActivity(Intent.createChooser(shareIntent, getString(R.string.share_via)))
            }
            R.id.nav_logout -> {
                auth.signOut()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
        }

        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}
