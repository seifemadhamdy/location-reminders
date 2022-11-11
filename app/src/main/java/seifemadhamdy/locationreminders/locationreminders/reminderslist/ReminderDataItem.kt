package seifemadhamdy.locationreminders.locationreminders.reminderslist

import java.io.Serializable
import java.util.*

/**
 * Data class acts as a data mapper between the DB and the UI
 */
data class ReminderDataItem(
    var title: String?,
    var description: String?,
    var location: String?,
    var latitude: Double?,
    var longitude: Double?,
    val id: String = UUID.randomUUID().toString()
) : Serializable