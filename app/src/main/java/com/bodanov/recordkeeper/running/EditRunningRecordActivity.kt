package com.bodanov.recordkeeper.running

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.bodanov.recordkeeper.R
import com.bodanov.recordkeeper.databinding.ActivityEditRunningRecordBinding
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.io.IOException

val Context.running: DataStore<Preferences> by preferencesDataStore(name = "running")

class EditRunningRecordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditRunningRecordBinding
    private var distance: String? = null
    private lateinit var dateKey: Preferences.Key<String>
    private lateinit var recordKey: Preferences.Key<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityEditRunningRecordBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        distance = intent.getStringExtra("distance")
        dateKey = stringPreferencesKey("${distance}_date")
        recordKey = stringPreferencesKey("${distance}_record")

        title = "$distance Record"

        setStoredValue()

        binding.buttonSave.setOnClickListener { saveData() }
        binding.buttonDelete.setOnClickListener { deleteData() }

//        val recordKey = stringPreferencesKey("${distance}_record")
//        val recordFlow: Flow<String> = dataStore.data
//                .catch { e ->
//                    if (e is IOException) emit(emptyPreferences()) else throw e
//                }
//                .map { prefs -> prefs[recordKey] ?: "empty" }


    }

    private fun deleteData() {
        AlertDialog.Builder(this).setTitle("Delete Data")
            .setMessage("Delete saved Record and Date?")
            .setPositiveButton("Yes") { _, _ ->
                lifecycleScope.launch {
                    try {
                        running.edit { dict ->
                            dict.remove(recordKey)
                            dict.remove(dateKey)
                        }
                        binding.editTextRecord.setText(null)
                        binding.editTextDate.setText(null)
                        Toast.makeText(
                            this@EditRunningRecordActivity,
                            "Data is deleted",
                            Toast.LENGTH_SHORT
                        ).show()
                    } catch (e: IOException) {
                        Toast.makeText(
                            this@EditRunningRecordActivity,
                            "Delete failed: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
            .setNegativeButton("No") { dialog, button ->
                dialog.dismiss()
            }
            .show()
    }

    private fun setStoredValue() {
        val data = running.data
            .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                data.collect { prefs ->
                    binding.editTextRecord.setText(prefs[recordKey] ?: "")
                    binding.editTextDate.setText(prefs[dateKey] ?: "")
                }
            }
        }
    }

    private fun saveData() {
        lifecycleScope.launch {
            running.edit { dict ->
                dict[recordKey] = binding.editTextRecord.text.toString()
                dict[dateKey] = binding.editTextDate.text.toString()
            }
            Toast.makeText(this@EditRunningRecordActivity, "Data is saved", Toast.LENGTH_SHORT)
                .show()
        }
    }
}