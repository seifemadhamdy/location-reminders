package seifemadhamdy.locationreminders.locationreminders.savereminder

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.PointOfInterest
import kotlinx.coroutines.launch
import seifemadhamdy.locationreminders.R
import seifemadhamdy.locationreminders.base.BaseViewModel
import seifemadhamdy.locationreminders.base.NavigationCommand
import seifemadhamdy.locationreminders.locationreminders.data.ReminderDataSource
import seifemadhamdy.locationreminders.locationreminders.data.dto.ReminderDTO
import seifemadhamdy.locationreminders.locationreminders.reminderslist.ReminderDataItem

class SaveReminderViewModel(val app: Application, private val dataSource: ReminderDataSource) :
    BaseViewModel(app) {

    val reminderTitle = MutableLiveData<String?>()
    val reminderDescription = MutableLiveData<String?>()
    val reminderSelectedLocationStr = MutableLiveData(getReminderSelectedLocationStrDefaultText())
    private val selectedPOI = MutableLiveData<PointOfInterest?>()
    val latitude = MutableLiveData<Double?>()
    val longitude = MutableLiveData<Double?>()

    /**
     * Clear the live data objects to start fresh next time the view model gets called
     */
    fun onClear() {
        reminderTitle.value = null
        reminderDescription.value = null
        reminderSelectedLocationStr.value = getReminderSelectedLocationStrDefaultText()
        selectedPOI.value = null
        latitude.value = null
        longitude.value = null
    }

    /**
     * Validate the entered data then saves the reminder data to the DataSource
     */
    fun validateAndSaveReminder(reminderData: ReminderDataItem) {
        if (validateEnteredData(reminderData)) {
            saveReminder(reminderData)
        }
    }

    /**
     * Save the reminder to the data source
     */
    fun saveReminder(reminderData: ReminderDataItem) {
        showLoading.value = true

        viewModelScope.launch {
            dataSource.saveReminder(
                ReminderDTO(
                    reminderData.title,
                    reminderData.description,
                    reminderData.location,
                    reminderData.latitude,
                    reminderData.longitude,
                    reminderData.id
                )
            )

            showLoading.value = false
            showToast.value = app.getString(R.string.reminder_saved)
            navigationCommand.value = NavigationCommand.Back
        }
    }

    /**
     * Validate the entered data and show error to the user if there's any invalid data
     */
    fun validateEnteredData(reminderData: ReminderDataItem): Boolean {
        if (reminderData.title.isNullOrEmpty()) {
            showSnackBarInt.value = R.string.err_enter_title
            return false
        }

        if (reminderData.location.isNullOrEmpty()) {
            showSnackBarInt.value = R.string.err_select_location
            return false
        }

        return true
    }

    // Get default text when no location is selected
    fun getReminderSelectedLocationStrDefaultText() =
        app.getString(R.string.no_location_is_selected)
}