package seifemadhamdy.locationreminders.data

import seifemadhamdy.locationreminders.locationreminders.data.ReminderDataSource
import seifemadhamdy.locationreminders.locationreminders.data.dto.ReminderDTO
import seifemadhamdy.locationreminders.locationreminders.data.dto.Result

// Use FakeDataSource that acts as a test double to the LocalDataSource
class FakeDataSource(private var remindersList: MutableList<ReminderDTO>? = mutableListOf()) :
    ReminderDataSource {

    // Create a fake data source to act as a double to the real data source

    private var shouldSimulateError = false

    // Used for controlling the behaviour of the functions in the FakeDataSource to simulate error case
    fun simulateError(shouldSimulateError: Boolean = false) {
        this.shouldSimulateError = shouldSimulateError
    }

    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        // Return the reminders

        // Return a simulated error
        if (shouldSimulateError) {
            return Result.Error("Error getting reminders.")
        }

        // Return remindersList if successful
        remindersList?.let {
            return Result.Success(it)
        }

        // Return error if reminders couldn't be found
        return Result.Error("Reminders couldn't be found.")
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        // Save the Reminder
        remindersList?.add(reminder)
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        // Return the reminder with the id

        // Get the reminder by id from the list of available reminders
        val reminder = remindersList?.find {
            it.id == id
        }

        return when {
            // Return a simulated error
            shouldSimulateError -> Result.Error("Reminders couldn't be found.")
            // Return reminder if successful
            reminder != null -> Result.Success(reminder)
            // Return error that the reminders couldn't be found
            else -> {
                Result.Error("Reminders couldn't be found.")
            }
        }
    }

    override suspend fun deleteAllReminders() {
        // Delete all the reminders

        // Clear the list of reminders in remindersList
        remindersList?.clear()
    }
}