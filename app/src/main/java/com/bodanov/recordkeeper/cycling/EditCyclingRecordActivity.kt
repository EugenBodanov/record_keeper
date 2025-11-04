package com.bodanov.recordkeeper.cycling

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.bodanov.recordkeeper.R
import com.bodanov.recordkeeper.databinding.ActivityEditCyclingRecordBinding
import androidx.datastore.core.DataStore
import android.content.Context
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import java.io.IOException
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

val Context.cycling: DataStore<Preferences> by preferencesDataStore(name = "cycling")

class EditCyclingRecordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditCyclingRecordBinding
    private lateinit var prefix: String
    private lateinit var recordKey: Preferences.Key<String>
    private lateinit var dateKey: Preferences.Key<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityEditCyclingRecordBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        prefix = intent.getStringExtra("title") ?: ""
        title = "$prefix Record"
        recordKey = stringPreferencesKey("${prefix}_record")
        dateKey = stringPreferencesKey("${prefix}_date")

        setStoredValue()

        binding.buttonSave.setOnClickListener {
            saveData()
        }
        binding.buttonDelete.setOnClickListener {
            deleteData()
        }
    }

    private fun deleteData() {

        AlertDialog.Builder(this).setTitle("Delete Data")
            .setMessage("Delete saved Record and Date")
            .setPositiveButton("Yes") { _, _ ->
                lifecycleScope.launch {
                    try {
                        cycling.edit { dict ->
                            dict.remove(recordKey)
                            dict.remove(dateKey)
                        }
                        binding.editTextRecord.setText(null)
                        binding.editTextDate.setText(null)
                        Toast.makeText(
                            this@EditCyclingRecordActivity,
                            "Data is deleted",
                            Toast.LENGTH_SHORT
                        ).show()
                    } catch (e: IOException) {
                        Toast.makeText(
                            this@EditCyclingRecordActivity,
                            "Delete failed ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }.setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }.show()
    }

    private fun saveData() {

        lifecycleScope.launch {
            cycling.edit { dict ->
                dict[recordKey] = binding.editTextRecord.text.toString()
                dict[dateKey] = binding.editTextDate.text.toString()
            }
            Toast.makeText(this@EditCyclingRecordActivity, "Data is saved", Toast.LENGTH_SHORT)
                .show()
        }

    }

    private fun setStoredValue() {
        val data =
            cycling.data.catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                data.collect { prefs ->
                    binding.editTextRecord.setText(prefs[recordKey])
                    binding.editTextDate.setText(prefs[dateKey])
                }
            }
        }
    }


}