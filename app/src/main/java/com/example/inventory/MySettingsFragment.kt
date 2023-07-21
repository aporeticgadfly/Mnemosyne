package com.example.inventory

import android.app.UiModeManager
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
import androidx.fragment.app.viewModels
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SeekBarPreference
import androidx.preference.SwitchPreferenceCompat

class MySettingsFragment : PreferenceFragmentCompat() {
    private val viewModel: MnemosyneViewModel by viewModels {
        MnemosyneViewModelFactory(
            (this?.activity?.application as Mnemosyne).database
                .itemDao()
        )
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
        val reportPreference: Preference? = findPreference("reportBug")
        val supportPreference: Preference? = findPreference("support")
        val folderPreference: Preference? = findPreference("exportFolder")
        val aboutPreference: Preference? = findPreference("aboutFolder")
        val clearhPreference: Preference? = findPreference("clearHistory")
        val nightPreference: SwitchPreferenceCompat? = findPreference("nightMode")
        val instantPreference: SwitchPreferenceCompat? = findPreference("noInstant")
        val setHistoryPreference: SeekBarPreference? = findPreference("setHistory")

        if (reportPreference != null) {
            val openURL = Intent(android.content.Intent.ACTION_VIEW)
            openURL.data = Uri.parse("https://github.com/aptaabye/Mnemosyne/issues")
            reportPreference.intent = openURL
        }
        if (supportPreference != null) {
            val openURL = Intent(android.content.Intent.ACTION_VIEW)
            openURL.data = Uri.parse("https://github.com/aptaabye/Mnemosyne")
            supportPreference.intent = openURL
        }
        if (clearhPreference != null) {
            clearhPreference.setOnPreferenceClickListener {
                viewModel.deleteSessions()
                true
            }
        }
        if(aboutPreference != null) {
            aboutPreference.summary = "Version Number: 0.0.1"
        }
        if(folderPreference != null) {
            folderPreference.setOnPreferenceClickListener {
                val getFile =
                    registerForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri: Uri? ->
                        if (uri != null) {
                            folderPreference.summary = uri.toString()
                            folderPreference.setDefaultValue(uri)
                            //save value
                        }
                    }
                val resolver = this.activity?.contentResolver
                val values = ContentValues()
                values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                val uri = resolver?.insert(MediaStore.Files.getContentUri("external"), values)
                folderPreference.setDefaultValue(uri)
                getFile.launch(uri)
                true
            }

        }
        if(instantPreference != null) {
            instantPreference.setDefaultValue(false)
        }
        if(nightPreference != null) {
            nightPreference.setDefaultValue(false)
            AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_YES)
            UiModeManager.MODE_NIGHT_YES

            nightPreference.setOnPreferenceChangeListener(Preference.OnPreferenceChangeListener { preference, newValue ->

                if(newValue == true) {
                    AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_NO)
                    UiModeManager.MODE_NIGHT_NO
                }
                else {
                    AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_YES)
                    UiModeManager.MODE_NIGHT_YES
                }
                true
            })
        }
        if(setHistoryPreference != null) {
            setHistoryPreference.setDefaultValue(50)
            setHistoryPreference.min = 10
            setHistoryPreference.max = 100
            setHistoryPreference.seekBarIncrement = 1
            //assumes session objects ordered by time added w autoincrement id
            //any time session added, must check current number and trim one off if over
            //when new value set, delete all items over cutoff
            //get lowest id; delete all from lowestid to sessionNum-value
            setHistoryPreference.setOnPreferenceChangeListener(Preference.OnPreferenceChangeListener { preference, newValue ->
                val sessionNum = viewModel.sessionNum()
                if (sessionNum > newValue as Int) {
                    val lowestId = viewModel.lowestId()
                    val num = (sessionNum - newValue) + lowestId
                    viewModel.deleteCutoff(num)
                }
                true
            })
        }
    }
}
