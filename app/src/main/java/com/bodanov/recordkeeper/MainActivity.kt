package com.bodanov.recordkeeper

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.datastore.preferences.core.edit
import androidx.fragment.app.commit
import androidx.lifecycle.lifecycleScope
import com.bodanov.recordkeeper.cycling.CyclingFragment
import com.bodanov.recordkeeper.databinding.ActivityMainBinding
import com.bodanov.recordkeeper.running.RunningFragment
import com.google.android.material.navigation.NavigationBarView
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity(), NavigationBarView.OnItemSelectedListener {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.bottomNav.setOnItemSelectedListener(this)

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.reset_running -> {
            showConfirmationDialog(DataStoreFileName.RUNNING)
            true
        }

        R.id.reset_cycling -> {
            showConfirmationDialog(DataStoreFileName.CYCLING)
            true
        }

        R.id.reset_all -> {
            showConfirmationDialog(ALL)
            true
        }

        else -> super.onOptionsItemSelected(item)
    }

    private fun showConfirmationDialog(selection: String) {
        AlertDialog.Builder(this)
            .setTitle("Reset $selection records")
            .setMessage("Are you sure you want to clear the records?")
            .setPositiveButton("Yes") { _, _ ->
                lifecycleScope.launch {
                    when (selection) {
                        ALL -> {
                            DataStoreC.of(
                                this@MainActivity,
                                DataStoreFileName.RUNNING
                            ).edit { preferences ->
                                preferences.clear()
                            }
                            DataStoreC.of(
                                this@MainActivity,
                                DataStoreFileName.CYCLING
                            ).edit { preferences ->
                                preferences.clear()
                            }
                        }

                        else -> {
                            DataStoreC.of(this@MainActivity, selection).edit { preferences ->
                                preferences.clear()
                            }
                        }
                    }
                    showConfirmation(selection)
                }
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }.show()
    }

    private fun showConfirmation(selection: String) {
        val snackbar = Snackbar.make(
            binding.frameContent,
            "${selection.replaceFirstChar { c -> c.uppercaseChar() }} records cleared successfully!",
            Snackbar.LENGTH_LONG
        )
        snackbar.anchorView = binding.bottomNav
        snackbar.show()
    }

    private fun onCyclingClicked(): Boolean {
        supportFragmentManager.commit {
            replace(R.id.frame_content, CyclingFragment())
        }
        return true
    }

    private fun onRunningClicked(): Boolean {
        supportFragmentManager.commit {
            replace(R.id.frame_content, RunningFragment())
        }
        return true
    }

    override fun onNavigationItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.nav_cycling -> onCyclingClicked()
        R.id.nav_running -> onRunningClicked()
        else -> false
    }

}
