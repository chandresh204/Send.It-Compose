package chad.orionsoft.sendit.repository

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.util.Log
import chad.orionsoft.sendit.utils.RuntimeConstants
import chad.orionsoft.sendit.utils.SharedPreferencesConstants
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class PreferenceRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private fun addLog(msg: String) {
        Log.d("Preferences", msg)
    }

    private val sharedPreferences = context.getSharedPreferences(SharedPreferencesConstants.APP_PREFS_NAMES, MODE_PRIVATE)

    fun setUserNameInPreferences(name: String) {
        sharedPreferences.edit().putString(
            SharedPreferencesConstants.PREFS_USER_NAME, name
        ).apply()
    }

    fun getUserNameFromPreferences() : String? =
        sharedPreferences.getString(SharedPreferencesConstants.PREFS_USER_NAME, null)

    fun getKnownReceivers() : MutableSet<String>? =
        sharedPreferences.getStringSet(SharedPreferencesConstants.PREFS_KNOWN_RECEIVER, emptySet())

    fun addToKnownReceiver(id: String)  {
        addLog("Adding to known receiver: $id")
        val knownReceivers = getKnownReceivers()?.toMutableSet() ?: mutableSetOf()
        knownReceivers.add(id)
        sharedPreferences.edit().putStringSet(
            SharedPreferencesConstants.PREFS_KNOWN_RECEIVER, knownReceivers
        ).apply()
    }

    fun getMyUniqueId() : String {
        val myId = sharedPreferences.getString(SharedPreferencesConstants.PREFS_MY_UNIQUE_ID, null)
        return if(myId == null) {
            val newId = RuntimeConstants.generateRandomCode(12)
            sharedPreferences.edit().putString(SharedPreferencesConstants.PREFS_MY_UNIQUE_ID, newId).apply()
            newId
        } else myId
    }
}