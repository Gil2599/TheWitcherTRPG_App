package com.example.thewitcherrpg.feature_character_list.presentation

import android.app.Dialog
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario.launch
import com.example.thewitcherrpg.R
import com.example.thewitcherrpg.core.Constants.dataStore
import com.example.thewitcherrpg.databinding.ActivityLauncherBinding
import com.example.thewitcherrpg.databinding.CustomDialogHelpInfoBinding
import com.example.thewitcherrpg.feature_character_creation.presentation.CharCreationActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LauncherActivity : AppCompatActivity() {

    private lateinit var mCharListViewModel: CharacterListViewModel

    lateinit var recyclerView: RecyclerView
    private lateinit var addButton: FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding: ActivityLauncherBinding = ActivityLauncherBinding.inflate(layoutInflater)
        setContentView(binding.root)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)

        recyclerView = binding.recyclerView
        addButton = binding.addButton

        addButton.setOnClickListener() {
            val intent = Intent(this, CharCreationActivity::class.java)
            this.startActivity(intent)
        }

        val adapter = ListAdapter(this)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        mCharListViewModel = ViewModelProvider(this)[CharacterListViewModel::class.java]

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Repeat when the lifecycle is STARTED, cancel when PAUSED
                launch {
                    mCharListViewModel.characterList.collectLatest {
                        adapter.setData(it)
                    }
                }
            }
        }

        lifecycleScope.launch {
            launch {
                if (read("disclaimer") != true){
                    showDialogDisclaimer()
                }
            }
        }
    }

    private suspend fun save(key: String, value: Boolean){
        val dataStoreKey = booleanPreferencesKey(key)
        dataStore.edit { settings ->
            settings[dataStoreKey] = value
        }
    }

    private suspend fun read(key: String): Boolean?{
        val dataStoreKey = booleanPreferencesKey(key)
        val preferences = dataStore.data.first()
        return preferences[dataStoreKey]
    }

    private fun showDialogDisclaimer() {
        val dialog = Dialog(this)
        dialog.setCancelable(true)
        dialog.setCanceledOnTouchOutside(true)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val bind : CustomDialogHelpInfoBinding = CustomDialogHelpInfoBinding.inflate(layoutInflater)
        dialog.setContentView(bind.root)

        bind.textViewInfo.text = resources.getString(R.string.disclaimer)
        bind.textViewInfo.typeface = Typeface.DEFAULT

        bind.customTitle.setTitle("Disclaimer")
        bind.customTitle.setTitleSize(18F)

        bind.okButton.setOnClickListener {
            if (bind.checkBox.hasSelection()){
                lifecycleScope.launch {
                    launch {
                        save("disclaimer", true)
                    }
                }
            }
            dialog.dismiss()
        }
        dialog.show()
    }
}
