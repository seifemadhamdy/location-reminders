package seifemadhamdy.locationreminders.locationreminders.savereminder

import android.Manifest
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.PendingIntent
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.material.snackbar.Snackbar
import org.koin.android.ext.android.inject
import seifemadhamdy.locationreminders.BuildConfig
import seifemadhamdy.locationreminders.R
import seifemadhamdy.locationreminders.base.BaseFragment
import seifemadhamdy.locationreminders.base.NavigationCommand
import seifemadhamdy.locationreminders.databinding.FragmentSaveReminderBinding
import seifemadhamdy.locationreminders.locationreminders.geofence.GeofenceBroadcastReceiver
import seifemadhamdy.locationreminders.locationreminders.geofence.utils.GeofenceUtils
import seifemadhamdy.locationreminders.locationreminders.reminderslist.ReminderDataItem
import seifemadhamdy.locationreminders.utils.setDisplayHomeAsUpEnabled

private const val LOCATION_UPDATE_INTERVAL = 10000L
private const val TAG = "SaveReminderFragment"

class SaveReminderFragment : BaseFragment() {

    companion object {
        internal const val ACTION_GEOFENCE_EVENT =
            "SaveReminder.reminder.action.ACTION_GEOFENCE_EVENT"
    }

    // Get the view model this time as a single to be shared with the another fragment
    override val viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSaveReminderBinding
    private lateinit var reminderDataItem: ReminderDataItem

    // Used to check if the platform version is equal to Android Q or later
    private val isAndroidQOrLater by lazy {
        Build.VERSION.SDK_INT >=
                Build.VERSION_CODES.Q
    }

    private lateinit var geofencingClient: GeofencingClient

    private val sendIntentLauncher =
        registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { activityResult ->
            if (activityResult != null) {
                addGeoFenceForRemainder()
            } else {
                checkDeviceLocationSettingsAndStartGeofence(false)
            }
        }

    // Pending geofence intent to handle geofence transition
    private val geofencePendingIntent: PendingIntent by lazy {
        PendingIntent.getBroadcast(
            requireContext(),
            0,
            Intent(requireContext(), GeofenceBroadcastReceiver::class.java).apply {
                action = ACTION_GEOFENCE_EVENT
            },
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }
        )
    }

    private val requestMultiplePermissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (!permissions.entries.all { it.value }) {
            Snackbar.make(
                binding.saveReminderCoordinatorLayout,
                R.string.permission_denied_explanation,
                Snackbar.LENGTH_INDEFINITE
            ).setAction(R.string.settings) {
                startActivity(Intent().apply {
                    action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                    data = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                })
            }.show()
        } else {
            checkDeviceLocationSettingsAndStartGeofence()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_save_reminder, container, false)

        setDisplayHomeAsUpEnabled(true)
        binding.viewModel = viewModel
        geofencingClient = LocationServices.getGeofencingClient(requireActivity())
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
            // Store the user entered reminder details into a reminderDataItem object
            reminderDataItem = ReminderDataItem(
                title = viewModel.reminderTitle.value,
                description = viewModel.reminderDescription.value,
                location = viewModel.reminderSelectedLocationStr.value,
                latitude = viewModel.latitude.value,
                longitude = viewModel.longitude.value
            )

            if (viewModel.validateEnteredData(reminderDataItem)) {
                if (isForegroundAndBackgroundLocationPermissionsApproved()) {
                    checkDeviceLocationSettingsAndStartGeofence()
                } else {
                    requestForegroundAndBackgroundLocationPermissions()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Make sure to clear the view model after destroy, as it's a single view model.
        viewModel.onClear()
    }

    @SuppressLint("MissingPermission")
    private fun addGeoFenceForRemainder() {
        reminderDataItem.apply {
            // Add a geofencing request
            geofencingClient.addGeofences(
                GeofencingRequest.Builder()
                    .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                    .addGeofence(
                        Geofence.Builder()
                            .setRequestId(id)
                            .setCircularRegion(
                                latitude!!,
                                longitude!!,
                                GeofenceUtils.GEOFENCE_RADIUS_IN_METERS
                            )
                            .setExpirationDuration(GeofenceUtils.NEVER_EXPIRES)
                            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                            .build()
                    )
                    .build(), geofencePendingIntent
            ).run {
                addOnSuccessListener {
                    // Save the reminder to the local db after validation
                    viewModel.validateAndSaveReminder(reminderDataItem)
                }

                addOnFailureListener {
                    Toast.makeText(
                        requireContext(),
                        "An unexpected error occurred.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun checkDeviceLocationSettingsAndStartGeofence(resolve: Boolean = true) {
        val locationRequest =
            LocationRequest.Builder(Priority.PRIORITY_LOW_POWER, LOCATION_UPDATE_INTERVAL)
                .build()

        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        val settingsClient = LocationServices.getSettingsClient(requireActivity())

        val locationSettingsResponseTask =
            settingsClient.checkLocationSettings(builder.build())

        locationSettingsResponseTask.addOnFailureListener { exception ->
            if ((exception is ResolvableApiException) && resolve) {
                try {
                    sendIntentLauncher.launch(
                        IntentSenderRequest.Builder(exception.resolution.intentSender).build()
                    )
                } catch (sendIntentException: IntentSender.SendIntentException) {
                    Log.e(
                        TAG,
                        "Error getting location settings resolution: ${sendIntentException.message}"
                    )
                }
            } else {
                Snackbar.make(
                    requireView(),
                    R.string.location_required_error,
                    Snackbar.LENGTH_INDEFINITE
                ).setAction(android.R.string.ok) {
                    checkDeviceLocationSettingsAndStartGeofence()
                }.show()
            }
        }

        locationSettingsResponseTask.addOnCompleteListener {
            if (it.isSuccessful) {
                Log.i(TAG, "Success: $it")
                addGeoFenceForRemainder()
            }
        }
    }

    @TargetApi(29)
    private fun isForegroundAndBackgroundLocationPermissionsApproved() = (
            PackageManager.PERMISSION_GRANTED ==
                    ActivityCompat.checkSelfPermission(
                        requireContext(),
                        Manifest.permission.ACCESS_FINE_LOCATION
                    )) && if (isAndroidQOrLater)
        PackageManager.PERMISSION_GRANTED ==
                ActivityCompat.checkSelfPermission(
                    requireContext(), Manifest.permission.ACCESS_BACKGROUND_LOCATION
                )
    else
        true

    @TargetApi(29)
    private fun requestForegroundAndBackgroundLocationPermissions() {
        if (isForegroundAndBackgroundLocationPermissionsApproved())
            return

        var permissionsArray = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)

        if (isAndroidQOrLater)
            permissionsArray += Manifest.permission.ACCESS_BACKGROUND_LOCATION

        requestMultiplePermissionsLauncher.launch(
            permissionsArray
        )
    }
}