package com.bodanov.recordkeeper.running

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.bodanov.recordkeeper.running.EditRunningRecordActivity
import com.bodanov.recordkeeper.databinding.FragmentRunningBinding
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.io.IOException

class RunningFragment : Fragment() {

    private var _binding : FragmentRunningBinding? = null
    private val binding get() = _binding!!

    private lateinit var dateKey5km: Preferences.Key<String>
    private lateinit var recordKey5km: Preferences.Key<String>
    private lateinit var dateKey10km: Preferences.Key<String>
    private lateinit var recordKey10km: Preferences.Key<String>
    private lateinit var dateKeyHM: Preferences.Key<String>
    private lateinit var recordKeyHM: Preferences.Key<String>
    private lateinit var dateKeyM: Preferences.Key<String>
    private lateinit var recordKeyM: Preferences.Key<String>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRunningBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpClickListeners()
        initPreferences()
        setScreenData()
    }

    private fun initPreferences() {
        dateKey5km = stringPreferencesKey("5 km_date")
        recordKey5km = stringPreferencesKey("5 km_record")
        dateKey10km = stringPreferencesKey("10 km_date")
        recordKey10km = stringPreferencesKey("10 km_record")
        dateKeyHM = stringPreferencesKey("Half Marathon_date")
        recordKeyHM = stringPreferencesKey("Half Marathon_record")
        dateKeyM = stringPreferencesKey("Marathon_date")
        recordKeyM = stringPreferencesKey("Marathon_record")
    }

    private fun setUpClickListeners(){
        binding.container5km.setOnClickListener { launchRunningRecordScreen("5 km") }
        binding.container10km.setOnClickListener { launchRunningRecordScreen("10 km") }
        binding.containerHalfMarathon.setOnClickListener { launchRunningRecordScreen("Half Marathon") }
        binding.containerMarathon.setOnClickListener { launchRunningRecordScreen("Marathon") }
    }

    private fun launchRunningRecordScreen(distance: String) {
        val intent = Intent(context, EditRunningRecordActivity::class.java)
        intent.putExtra("distance", distance)
        startActivity(intent)
    }

    private fun setScreenData() {
        val data = requireContext().running
            .data
            .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                data.collect { dict ->
                    binding.textView5kmDate.text             = dict[dateKey5km]  ?: ""
                    binding.textView5kmValue.text            = dict[recordKey5km] ?: ""
                    binding.textView10kmDate.text            = dict[dateKey10km]  ?: ""
                    binding.textView10kmValue.text           = dict[recordKey10km] ?: ""
                    binding.textViewHalfMarathonDate.text    = dict[dateKeyHM]    ?: ""
                    binding.textViewHalfMarathonValue.text   = dict[recordKeyHM]  ?: ""
                    binding.textViewMarathonDate.text        = dict[dateKeyM]     ?: ""
                    binding.textViewMarathonValue.text       = dict[recordKeyM]   ?: ""
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}