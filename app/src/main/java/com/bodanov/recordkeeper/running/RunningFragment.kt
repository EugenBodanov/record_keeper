package com.bodanov.recordkeeper.running

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.bodanov.recordkeeper.DataStoreC
import com.bodanov.recordkeeper.DataStoreFileName
import com.bodanov.recordkeeper.DataStoreKeys
import com.bodanov.recordkeeper.INTENT_EXTRA_SCREEN_DATA
import com.bodanov.recordkeeper.databinding.FragmentRunningBinding
import com.bodanov.recordkeeper.editing.EditRecordActivity
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import java.io.IOException

class RunningFragment : Fragment() {

    private var _binding: FragmentRunningBinding? = null
    private val binding get() = _binding!!

    private val dateKey5km = stringPreferencesKey("5 km_${DataStoreKeys.DATE_KEY}")
    private val recordKey5km = stringPreferencesKey("5 km_${DataStoreKeys.RECORD_KEY}")
    private val dateKey10km = stringPreferencesKey("10 km_${DataStoreKeys.DATE_KEY}")
    private val recordKey10km = stringPreferencesKey("10 km_${DataStoreKeys.RECORD_KEY}")
    private val dateKeyHM = stringPreferencesKey("Half Marathon_${DataStoreKeys.DATE_KEY}")
    private val recordKeyHM = stringPreferencesKey("Half Marathon_${DataStoreKeys.RECORD_KEY}")
    private val dateKeyM = stringPreferencesKey("Marathon_${DataStoreKeys.DATE_KEY}")
    private val recordKeyM = stringPreferencesKey("Marathon_${DataStoreKeys.RECORD_KEY}")

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
        setScreenData()
    }

    private fun setUpClickListeners() {
        binding.container5km.setOnClickListener { launchRunningRecordScreen("5 km") }
        binding.container10km.setOnClickListener { launchRunningRecordScreen("10 km") }
        binding.containerHalfMarathon.setOnClickListener { launchRunningRecordScreen("Half Marathon") }
        binding.containerMarathon.setOnClickListener { launchRunningRecordScreen("Marathon") }
    }

    private fun launchRunningRecordScreen(distance: String) {
        val intent = Intent(context, EditRecordActivity::class.java)
        intent.putExtra(
            INTENT_EXTRA_SCREEN_DATA, EditRecordActivity.ScreenData(
                distance,
                DataStoreFileName.RUNNING,
                "Time"
            )
        )
        startActivity(intent)
    }

    private fun setScreenData() {
        val data = DataStoreC.of(requireContext(), DataStoreFileName.RUNNING)
            .data
            .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                data.collect { dict ->
                    binding.textView5kmDate.text = dict[dateKey5km] ?: ""
                    binding.textView5kmValue.text = dict[recordKey5km] ?: ""
                    binding.textView10kmDate.text = dict[dateKey10km] ?: ""
                    binding.textView10kmValue.text = dict[recordKey10km] ?: ""
                    binding.textViewHalfMarathonDate.text = dict[dateKeyHM] ?: ""
                    binding.textViewHalfMarathonValue.text = dict[recordKeyHM] ?: ""
                    binding.textViewMarathonDate.text = dict[dateKeyM] ?: ""
                    binding.textViewMarathonValue.text = dict[recordKeyM] ?: ""
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}