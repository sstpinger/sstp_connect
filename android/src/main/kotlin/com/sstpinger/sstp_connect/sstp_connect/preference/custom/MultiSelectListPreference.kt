package com.sstpinger.sstp_connect.sstp_connect.preference.custom

import android.content.Context
import android.util.AttributeSet
import androidx.preference.MultiSelectListPreference
import androidx.preference.Preference
import androidx.preference.Preference.SummaryProvider
import com.sstpinger.sstp_connect.sstp_connect.preference.OscPrefKey
import com.sstpinger.sstp_connect.sstp_connect.preference.accessor.getSetPrefValue
import javax.net.ssl.SSLContext


internal abstract class ModifiedMultiSelectListPreference(context: Context, attrs: AttributeSet) : MultiSelectListPreference(context, attrs), OscPreference {
    protected abstract val entryValues: Array<String>
    protected open val entries: Array<String>? = null
    protected open val provider: SummaryProvider<Preference>? = null

    override fun updateView() {
        values = getSetPrefValue(oscPrefKey, sharedPreferences!!)
    }

    override fun onAttached() {
        setEntryValues(entryValues)
        setEntries(entries ?: entryValues)

        summaryProvider = provider

        initialize()
    }
}

internal class SSLSuitesPreference(context: Context, attrs: AttributeSet) : ModifiedMultiSelectListPreference(context, attrs) {
    override val oscPrefKey = OscPrefKey.SSL_SUITES
    override val parentKey = OscPrefKey.SSL_DO_SELECT_SUITES
    override val preferenceTitle = "Select Cipher Suites"
    override val entryValues = SSLContext.getDefault().supportedSSLParameters.cipherSuites as Array<String>

    override val provider = SummaryProvider<Preference> {
        val currentValue = getSetPrefValue(oscPrefKey, it.sharedPreferences!!)

        when (currentValue.size) {
            0 -> "[No Suite Entered]"
            1 -> "1 Suite Selected"
            else -> "${currentValue.size} Suites Selected"
        }
    }
}
