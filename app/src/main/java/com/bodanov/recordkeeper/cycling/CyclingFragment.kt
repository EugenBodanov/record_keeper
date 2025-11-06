package com.bodanov.recordkeeper.cycling

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
import com.bodanov.recordkeeper.databinding.FragmentCyclingBinding
import com.bodanov.recordkeeper.editing.EditRecordActivity
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import java.io.IOException

class CyclingFragment : Fragment() {

    private var _binding: FragmentCyclingBinding? = null
    private val binding get() = _binding!!

    private val recordKeyLongestRide = stringPreferencesKey("Longest Ride_${DataStoreKeys.RECORD_KEY}")
    private val dateKeyLongestRide = stringPreferencesKey("Longest Ride_${DataStoreKeys.DATE_KEY}")
    private val recordKeyBiggestClimb = stringPreferencesKey("Biggest Climb_${DataStoreKeys.RECORD_KEY}")
    private val dateKeyBiggestClimb = stringPreferencesKey("Biggest Climb_${DataStoreKeys.DATE_KEY}")
    private val recordKeyBestAverageSpeed = stringPreferencesKey("Best Average Speed_${DataStoreKeys.RECORD_KEY}")
    private val dateKeyBestAverageSpeed = stringPreferencesKey("Best Average Speed_${DataStoreKeys.DATE_KEY}")

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
        setScreenData()
    }

    private fun setScreenData() {
        val data = DataStoreC.of(requireContext(), DataStoreFileName.CYCLING).data.catch { e ->
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
                    binding.textViewBestAverageSpeedValue.text =
                        dict[recordKeyBestAverageSpeed] ?: ""
                    binding.textViewBestAverageSpeedDate.text = dict[dateKeyBestAverageSpeed] ?: ""
                }
            }
        }

    }

    private fun setUpClickListener() {
        binding.containerLongestRide.setOnClickListener {
            launchCyclingRecordScreen(
                "Longest Ride",
                "Distance"
            )
        }
        binding.containerBiggestClimb.setOnClickListener {
            launchCyclingRecordScreen(
                "Biggest Climb",
                "Height"
            )
        }
        binding.containerBestAverageSpeed.setOnClickListener {
            launchCyclingRecordScreen(
                "Best Average Speed",
                "Average Speed"
            )
        }
    }

    private fun launchCyclingRecordScreen(record: String, recordFieldHint: String) {
        val intent = Intent(requireContext(), EditRecordActivity::class.java)
        intent.putExtra(
            INTENT_EXTRA_SCREEN_DATA, EditRecordActivity.ScreenData(
                record,
                DataStoreFileName.CYCLING,
                recordFieldHint
            )
        )
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}