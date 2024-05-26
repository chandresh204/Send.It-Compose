package chad.orionsoft.sendit.repository

import android.content.Context
import android.content.Context.MODE_PRIVATE
import chad.orionsoft.sendit.utils.SharedPreferencesConstants
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class PreferenceRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val sharedPreferences = context.getSharedPreferences(SharedPreferencesConstants.APP_PREFS_NAMES, MODE_PRIVATE)

    fun setUserNameInPreferences(name: String) {
        sharedPreferences.edit().putString(
            SharedPreferencesConstants.PREFS_USER_NAME, name
        ).apply()
    }

    fun getUserNameFromPreferences() : String? =
        sharedPreferences.getString(SharedPreferencesConstants.PREFS_USER_NAME, null)

}