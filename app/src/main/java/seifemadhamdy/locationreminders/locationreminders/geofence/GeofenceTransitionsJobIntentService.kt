@file:Suppress("DEPRECATION")

package seifemadhamdy.locationreminders.locationreminders.geofence

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.JobIntentService
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import kotlinx.coroutines.*
import org.koin.android.ext.android.inject
import seifemadhamdy.locationreminders.locationreminders.data.ReminderDataSource
import seifemadhamdy.locationreminders.locationreminders.data.dto.ReminderDTO
import seifemadhamdy.locationreminders.locationreminders.data.dto.Result
import seifemadhamdy.locationreminders.locationreminders.geofence.utils.errorMessage
import seifemadhamdy.locationreminders.locationreminders.reminderslist.ReminderDataItem
import seifemadhamdy.locationreminders.locationreminders.savereminder.SaveReminderFragment.Companion.ACTION_GEOFENCE_EVENT
import seifemadhamdy.locationreminders.utils.sendNotification
import kotlin.coroutines.CoroutineContext

class GeofenceTransitionsJobIntentService : JobIntentService(), CoroutineScope {

    private var coroutineJob: Job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + coroutineJob

    companion object {
        private const val JOB_ID = 573
        private const val TAG = "GTJIS"

        // Starts the JobIntentService to handle the geofencing transition events when called
        fun enqueueWork(context: Context, intent: Intent) {
            enqueueWork(
                context,
                GeofenceTransitionsJobIntentService::class.java, JOB_ID,
                intent
            )
        }
    }

    override fun onHandleWork(intent: Intent) {
        if (intent.action == ACTION_GEOFENCE_EVENT) {
            // Handle the geofencing transition events appropriately
            GeofencingEvent.fromIntent(intent)?.let {
                if (it.hasError()) {
                    Log.e(TAG, "Error: ${errorMessage(resources, it.errorCode)}")
                    return
                }

                if (it.geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
                    it.triggeringGeofences?.let { triggeringGeofences ->
                        // Calling @sendNotification to notify the user located at the geofence area
                        sendNotification(
                            triggeringGeofences
                        )
                    }

                    Log.i(TAG, "Geofence is found successfully.")
                }
            }
        }
    }

    private fun sendNotification(triggeringGeofences: List<Geofence>) {
        for (triggeringGeofence in triggeringGeofences) {
            // Get the request id of the current geofence
            val requestId = triggeringGeofence.requestId

            // Get the local repository instance
            val remindersLocalRepository: ReminderDataSource by inject()
            // Interaction to the repository has to be through a coroutine scope
            CoroutineScope(coroutineContext).launch(SupervisorJob()) {
                // Get the reminder with the request id
                val result = remindersLocalRepository.getReminder(requestId)

                if (result is Result.Success<ReminderDTO>) {
                    val reminderDTO = result.data

                    // Send a notification to the user with the reminder details
                    sendNotification(
                        this@GeofenceTransitionsJobIntentService, ReminderDataItem(
                            reminderDTO.title,
                            reminderDTO.description,
                            reminderDTO.location,
                            reminderDTO.latitude,
                            reminderDTO.longitude,
                            reminderDTO.id
                        )
                    )
                }
            }
        }
    }
}