package com.zarathustra.mnemosyne

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.viewModels
import androidx.lifecycle.viewModelScope
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SeekBarPreference
import androidx.preference.SwitchPreferenceCompat
import kotlinx.coroutines.launch

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
        val aboutPreference: Preference? = findPreference("aboutFolder")
        val clearhPreference: Preference? = findPreference("clearHistory")
        //val nightPreference: SwitchPreferenceCompat? = findPreference("nightMode")
        val instantPreference: SwitchPreferenceCompat? = findPreference("noInstant")
        val setHistoryPreference: SeekBarPreference? = findPreference("setHistory")
        val reviewThresholdPreference: SeekBarPreference? = findPreference("reviewThreshold")
        val privacyPreference: Preference? = findPreference("privacy")

        if (reportPreference != null) {
            val openURL = Intent(android.content.Intent.ACTION_VIEW)
            openURL.data = Uri.parse("https://github.com/aporeticgadfly/Mnemosyne/issues")
            reportPreference.intent = openURL
        }
        if (privacyPreference != null) {
            val privacyURL = Intent(android.content.Intent.ACTION_VIEW)
            privacyURL.data = Uri.parse("https://aporeticgadfly.github.io/MnemosynePrivacyPolicy")
            privacyPreference.intent = privacyURL
        }
        if (supportPreference != null) {
            val openURL = Intent(android.content.Intent.ACTION_VIEW)
            openURL.data = Uri.parse("https://liberapay.com/Zarathustra")
            supportPreference.intent = openURL
        }
        if (clearhPreference != null) {
            clearhPreference.setOnPreferenceClickListener {
                val deleteThread = Thread {
                    viewModel.deleteSessions()
                }
                deleteThread.start()
                true
            }
        }
        if(aboutPreference != null) {
            aboutPreference.summary = "Version Number: 2.0"
        }
        if(instantPreference != null) {
            instantPreference.setDefaultValue(false)
        }
        /*if(nightPreference != null) {
            nightPreference.setDefaultValue(false)
            AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_YES)
            UiModeManager.MODE_NIGHT_YES

            nightPreference.setOnPreferenceChangeListener(Preference.OnPreferenceChangeListener { preference, newValue ->
                Log.d("nightMode", newValue.toString())
                if(newValue == true) {
                    AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_NO)
                    UiModeManager.MODE_NIGHT_NO
                    activity?.recreate()
                }
                else {
                    AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_YES)
                    UiModeManager.MODE_NIGHT_YES
                }
                true
            })
        }*/
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
                val setHistoryThread = Thread {
                    viewModel.viewModelScope.launch {
                        val sessionNum = viewModel.sessionNum()
                        if (sessionNum > newValue as Int) {
                            val lowestId = viewModel.lowestId()
                            val num = (sessionNum - newValue) + lowestId
                            viewModel.deleteCutoff(num)
                        }
                    }
                    }
                setHistoryThread.start()
                true
            })
        }
        if(reviewThresholdPreference != null) {
            reviewThresholdPreference.setDefaultValue(75)
            reviewThresholdPreference.min = 0
            reviewThresholdPreference.max = 100
            reviewThresholdPreference.seekBarIncrement = 1

        }
    }
}
