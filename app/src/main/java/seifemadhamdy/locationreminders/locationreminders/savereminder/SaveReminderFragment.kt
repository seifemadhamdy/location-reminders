package seifemadhamdy.locationreminders.locationreminders.savereminder

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import org.koin.android.ext.android.inject
import seifemadhamdy.locationreminders.R
import seifemadhamdy.locationreminders.base.BaseFragment
import seifemadhamdy.locationreminders.base.NavigationCommand
import seifemadhamdy.locationreminders.databinding.FragmentSaveReminderBinding
import seifemadhamdy.locationreminders.utils.setDisplayHomeAsUpEnabled

class SaveReminderFragment : BaseFragment() {

    // Get the view model this time as a single to be shared with the another fragment
    override val viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSaveReminderBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_save_reminder, container, false)

        setDisplayHomeAsUpEnabled(true)
        binding.viewModel = viewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = this

        binding.locateMaterialButton.setOnClickListener {
            // Navigate to another fragment to get the user location
            viewModel.navigationCommand.value =
                NavigationCommand.To(SaveReminderFragmentDirections.actionSaveReminderFragmentToSelectLocationFragment())
        }

        binding.saveReminderFloatingActionButton.setOnClickListener {
            val title = viewModel.reminderTitle.value
            val description = viewModel.reminderDescription
            val location = viewModel.reminderSelectedLocationStr.value
            val latitude = viewModel.latitude
            val longitude = viewModel.longitude.value

            // TODO: Use the user entered reminder details to:
            // 1) add a geofencing request
            // 2) save the reminder to the local db
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Make sure to clear the view model after destroy, as it's a single view model.
        viewModel.onClear()
    }
}
