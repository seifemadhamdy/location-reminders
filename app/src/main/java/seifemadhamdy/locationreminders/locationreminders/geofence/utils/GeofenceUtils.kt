package seifemadhamdy.locationreminders.locationreminders.geofence.utils

import android.content.res.Resources
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofenceStatusCodes
import seifemadhamdy.locationreminders.R

object GeofenceUtils {
    const val GEOFENCE_RADIUS_IN_METERS = 100f
    const val NEVER_EXPIRES = Geofence.NEVER_EXPIRE
}

fun errorMessage(resources: Resources, errorCode: Int) = when (errorCode) {
    GeofenceStatusCodes.GEOFENCE_NOT_AVAILABLE -> resources.getString(R.string.geofence_not_available)
    GeofenceStatusCodes.GEOFENCE_TOO_MANY_GEOFENCES -> resources.getString(R.string.geofence_too_many_geofences)
    GeofenceStatusCodes.GEOFENCE_TOO_MANY_PENDING_INTENTS -> resources.getString(R.string.geofence_too_many_pending_intents)
    else -> resources.getString(R.string.geofence_unknown_error)
}