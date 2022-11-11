package seifemadhamdy.locationreminders.locationreminders.geofence

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import seifemadhamdy.locationreminders.locationreminders.savereminder.SaveReminderFragment.Companion.ACTION_GEOFENCE_EVENT

/**
 * Triggered by the Geofence.  Since we can have many Geofences at once, we pull the request
 * ID from the first Geofence, and locate it within the cached data in our Room DB
 *
 * Or users can add the reminders and then close the app, So our app has to run in the background
 * and handle the geofencing in the background.
 * To do that you can use https://developer.android.com/reference/android/support/v4/app/JobIntentService to do that.
 *
 */
class GeofenceBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // Method to receive the geofencing events at the background

        if (intent.action == ACTION_GEOFENCE_EVENT) {
            // Log that the Geofence event is received successfully
            Log.i(javaClass.simpleName, "Geofence event received.")
            // Start the JobIntentService to handle the geofencing transition events
            GeofenceTransitionsJobIntentService.enqueueWork(context, intent)
        }
    }
}