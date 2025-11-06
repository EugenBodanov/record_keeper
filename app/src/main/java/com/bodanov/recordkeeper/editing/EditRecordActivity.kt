package com.bodanov.recordkeeper.editing

import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.bodanov.recordkeeper.R
import com.bodanov.recordkeeper.databinding.ActivityEditRecordBinding
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import java.io.IOException
import com.bodanov.recordkeeper.DataStoreC
import com.bodanov.recordkeeper.DataStoreKeys
import com.bodanov.recordkeeper.INTENT_EXTRA_SCREEN_DATA
import java.io.Serializable

class EditRecordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditRecordBinding

    private val screenData: ScreenData by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getSerializableExtra(
                INTENT_EXTRA_SCREEN_DATA,
                ScreenData::class.java
            ) as ScreenData
        } else {
            intent.getSerializableExtra(INTENT_EXTRA_SCREEN_DATA) as ScreenData
        }
    }

    private val dataDict by lazy {
        DataStoreC.of(this, screenData.dataStoreFileName)
    }
    private val dateKey: Preferences.Key<String> by lazy {
        stringPreferencesKey("${screenData.record}_${DataStoreKeys.DATE_KEY}")
    }
    private val recordKey: Preferences.Key<String> by lazy {
        stringPreferencesKey("${screenData.record}_${DataStoreKeys.RECORD_KEY}")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityEditRecordBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupUI()
        setStoredValue()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressedDispatcher.onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setupUI() {
        title = "${screenData.record} Record"
        binding.textInputRecord.hint = screenData.recordFieldHint
        binding.buttonSave.setOnClickListener {
            saveData()
            finish()
        }
        binding.buttonDelete.setOnClickListener { deleteData() }
    }

    private fun deleteData() {
        AlertDialog.Builder(this).setTitle("Delete Data")
            .setMessage("Delete saved Record and Date?")
            .setPositiveButton("Yes") { _, _ ->
                lifecycleScope.launch {
                    try {
                        dataDict.edit { dict ->
                            dict.remove(recordKey)
                            dict.remove(dateKey)
                        }
                        binding.editTextRecord.setText(null)
                        binding.editTextDate.setText(null)
                        Toast.makeText(
                            this@EditRecordActivity,
                            "Data is deleted",
                            Toast.LENGTH_SHORT
                        ).show()
                    } catch (e: IOException) {
                        Toast.makeText(
                            this@EditRecordActivity,
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
        val data = dataDict.data
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
            dataDict.edit { dict ->
                dict[recordKey] = binding.editTextRecord.text.toString()
                dict[dateKey] = binding.editTextDate.text.toString()
            }
            Toast.makeText(this@EditRecordActivity, "Data is saved", Toast.LENGTH_SHORT)
                .show()
        }
    }

    data class ScreenData(
        val record: String,
        val dataStoreFileName: String,
        val recordFieldHint: String
    ) : Serializable

}