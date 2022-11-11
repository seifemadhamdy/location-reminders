package seifemadhamdy.locationreminders.locationreminders.reminderslist

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import seifemadhamdy.locationreminders.base.BaseViewModel
import seifemadhamdy.locationreminders.locationreminders.data.ReminderDataSource
import seifemadhamdy.locationreminders.locationreminders.data.dto.ReminderDTO
import seifemadhamdy.locationreminders.locationreminders.data.dto.Result

class ReminderListViewModel(
    app: Application,
    private val dataSource: ReminderDataSource
) : BaseViewModel(app) {
    // List that holds the reminder data to be displayed on the UI
    val remindersList = MutableLiveData<List<ReminderDataItem>>()

    /**
     * Get all the reminders from the DataSource and add them to the remindersList to be shown on the UI,
     * or show error if any
     */
    fun loadReminders() {
        showLoading.value = true

        viewModelScope.launch {
            // Interacting with the dataSource has to be through a coroutine
            val result = dataSource.getReminders()
            showLoading.postValue(false)

            when (result) {
                is Result.Success<*> -> {
                    val dataList = ArrayList<ReminderDataItem>()

                    @Suppress("UNCHECKED_CAST")
                    dataList.addAll((result.data as List<ReminderDTO>).map { reminderDTO ->
                        // Map the reminder data from the DB to the be ready to be displayed on the UI
                        ReminderDataItem(
                            reminderDTO.title,
                            reminderDTO.description,
                            reminderDTO.location,
                            reminderDTO.latitude,
                            reminderDTO.longitude,
                            reminderDTO.id
                        )
                    })

                    remindersList.value = dataList
                }

                is Result.Error ->
                    showSnackBar.value = result.message
            }

            // Check if no data has to be shown
            invalidateShowNoData()
        }
    }

    /**
     * Inform the user that there's not any data if the remindersList is empty
     */
    private fun invalidateShowNoData() {
        showNoData.value = remindersList.value == null || remindersList.value!!.isEmpty()
    }
}