package com.company.cabinetConfigurator.data.repository

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class UserSettings(
    val companyName: String = "",
    val logoUri: Uri? = null
)

class UserSettingsRepository(context: Context) {
    private val prefs = context.getSharedPreferences("user_settings", Context.MODE_PRIVATE)

    private val _settings = MutableStateFlow(loadFromPrefs())
    val settings: StateFlow<UserSettings> = _settings.asStateFlow()

    private fun loadFromPrefs(): UserSettings {
        val companyName = prefs.getString("company_name", "") ?: ""
        val uriString = prefs.getString("logo_uri", null)
        val logoUri = uriString?.let { runCatching { Uri.parse(it) }.getOrNull() }
        return UserSettings(companyName = companyName, logoUri = logoUri)
    }

    fun updateCompanyName(name: String) {
        prefs.edit().putString("company_name", name).apply()
        _settings.value = _settings.value.copy(companyName = name)
    }

    fun updateLogoUri(uri: Uri?) {
        prefs.edit().putString("logo_uri", uri?.toString()).apply()
        _settings.value = _settings.value.copy(logoUri = uri)
    }

    fun getSettings(): UserSettings = _settings.value
}
