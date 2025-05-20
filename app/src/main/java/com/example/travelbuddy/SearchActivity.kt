package com.example.travelbuddy

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.travelbuddy.adapters.DestinationAdapter
import com.example.travelbuddy.database.AppDatabase
import com.example.travelbuddy.database.Destination
import com.example.travelbuddy.databinding.ActivitySearchBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SearchActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySearchBinding
    private lateinit var adapter: DestinationAdapter
    private var allDestinations: List<Destination> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Setup RecyclerView
        adapter = DestinationAdapter(emptyList()) { destination ->
            val intent = Intent(this, DestinationDetailActivity::class.java)
            intent.putExtra("DESTINATION_ID", destination.id)
            startActivity(intent)
        }

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        // Load all destinations
        loadDestinations()

        // Setup search functionality
        binding.editSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterDestinations(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun loadDestinations() {
        binding.progressBar.visibility = View.VISIBLE
        binding.textNoResults.visibility = View.GONE

        CoroutineScope(Dispatchers.IO).launch {
            allDestinations = AppDatabase.getInstance(applicationContext)
                .destinationDao()
                .getAllDestinations()

            withContext(Dispatchers.Main) {
                binding.progressBar.visibility = View.GONE
                if (allDestinations.isEmpty()) {
                    binding.textNoResults.visibility = View.VISIBLE
                    binding.textNoResults.text = getString(R.string.no_destinations)
                } else {
                    adapter.updateDestinations(allDestinations)
                }
            }
        }
    }

    private fun filterDestinations(query: String) {
        if (query.isEmpty()) {
            adapter.updateDestinations(allDestinations)
            binding.textNoResults.visibility = View.GONE
            return
        }

        val filteredList = allDestinations.filter {
            it.name.contains(query, ignoreCase = true) ||
                    it.country.contains(query, ignoreCase = true) ||
                    it.city.contains(query, ignoreCase = true) ||
                    it.description.contains(query, ignoreCase = true)
        }

        adapter.updateDestinations(filteredList)

        if (filteredList.isEmpty()) {
            binding.textNoResults.visibility = View.VISIBLE
            binding.textNoResults.text = getString(R.string.no_search_results)
        } else {
            binding.textNoResults.visibility = View.GONE
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
