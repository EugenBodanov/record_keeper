package com.bodanov.recordkeeper.cycling

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.bodanov.recordkeeper.databinding.FragmentCyclingBinding
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import java.io.IOException

class CyclingFragment : Fragment() {

    private var _binding: FragmentCyclingBinding? = null
    private val binding get() = _binding!!

    private lateinit var recordKeyLongestRide: Preferences.Key<String>
    private lateinit var dateKeyLongestRide: Preferences.Key<String>
    private lateinit var recordKeyBiggestClimb: Preferences.Key<String>
    private lateinit var dateKeyBiggestClimb: Preferences.Key<String>
    private lateinit var recordKeyBestAverageSpeed: Preferences.Key<String>
    private lateinit var dateKeyBestAverageSpeed: Preferences.Key<String>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCyclingBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpClickListener()
        initPreferences()
        setScreenData()
    }

    private fun setScreenData() {
        val data = requireContext().cycling.data.catch { e ->
            if (e is IOException) emit(
                emptyPreferences()
            ) else throw e
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                data.collect { dict ->
                    binding.textViewLongestRideValue.text = dict[recordKeyLongestRide] ?: ""
                    binding.textViewLongestRideDate.text = dict[dateKeyLongestRide] ?: ""
                    binding.textViewBiggestClimbValue.text = dict[recordKeyBiggestClimb] ?: ""
                    binding.textViewBiggestClimbDate.text = dict[dateKeyBiggestClimb] ?: ""
                    binding.textViewBestAverageSpeedValue.text = dict[recordKeyBestAverageSpeed] ?: ""
                    binding.textViewBestAverageSpeedDate.text = dict[dateKeyBestAverageSpeed] ?: ""
                }
            }
        }

    }

    private fun initPreferences() {
        recordKeyLongestRide = stringPreferencesKey("Longest Ride_record")
        dateKeyLongestRide = stringPreferencesKey("Longest Ride_date")
        recordKeyBiggestClimb = stringPreferencesKey("Biggest Climb_record")
        dateKeyBiggestClimb = stringPreferencesKey("Biggest Climb_date")
        recordKeyBestAverageSpeed = stringPreferencesKey("Best Average Speed_record")
        dateKeyBestAverageSpeed = stringPreferencesKey("Best Average Speed_date")
    }

    private fun setUpClickListener() {
        binding.containerLongestRide.setOnClickListener { launchCyclingRecordScreen("Longest Ride") }
        binding.containerBiggestClimb.setOnClickListener { launchCyclingRecordScreen("Biggest Climb") }
        binding.containerBestAverageSpeed.setOnClickListener { launchCyclingRecordScreen("Best Average Speed") }
    }

    private fun launchCyclingRecordScreen(title: String) {
        val intent = Intent(requireContext(), EditCyclingRecordActivity::class.java)
        intent.putExtra("title", title)
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}