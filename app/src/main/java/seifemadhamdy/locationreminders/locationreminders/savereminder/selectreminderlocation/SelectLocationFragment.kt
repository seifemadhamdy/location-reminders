package seifemadhamdy.locationreminders.locationreminders.savereminder.selectreminderlocation

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.Resources
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.MenuProvider
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Lifecycle
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.snackbar.Snackbar
import org.koin.android.ext.android.inject
import seifemadhamdy.locationreminders.BuildConfig
import seifemadhamdy.locationreminders.R
import seifemadhamdy.locationreminders.base.BaseFragment
import seifemadhamdy.locationreminders.base.NavigationCommand
import seifemadhamdy.locationreminders.databinding.FragmentSelectLocationBinding
import seifemadhamdy.locationreminders.locationreminders.savereminder.SaveReminderViewModel
import seifemadhamdy.locationreminders.utils.setDisplayHomeAsUpEnabled
import java.util.*

private const val DEFAULT_ZOOM_LEVEL = 10f

class SelectLocationFragment : BaseFragment(), OnMapReadyCallback {

    private lateinit var map: GoogleMap
    private lateinit var binding: FragmentSelectLocationBinding

    // Use Koin to get the view model of the SaveReminder
    override val viewModel: SaveReminderViewModel by inject()
    private var marker: Marker? = null
    private val _tag = javaClass.simpleName

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            map.getUserLocation()
        } else {
            // Handle when the user revokes the permission
            Snackbar.make(
                binding.selectLocationCoordinatorLayout,
                R.string.permission_denied_explanation,
                Snackbar.LENGTH_INDEFINITE
            ).setAction(R.string.settings) {
                // Close current fragment by returning to the previous one
                viewModel.navigationCommand.value = NavigationCommand.Back

                // Launch app details settings screen for the user to un-revoke the required permissions
                startActivity(Intent().apply {
                    action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                    data = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                })
            }.show()
        }
    }

    private val fusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(requireActivity())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_select_location, container, false)

        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        setDisplayHomeAsUpEnabled(true)

        // Map setup Implementation
        (childFragmentManager.findFragmentById(R.id.map_fragment_container_view) as SupportMapFragment).getMapAsync(
            this
        )

        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.map_options, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                // Change the map type based on the user's selection.
                when (menuItem.itemId) {
                    R.id.normal_map -> map.mapType = GoogleMap.MAP_TYPE_NORMAL
                    R.id.hybrid_map -> map.mapType = GoogleMap.MAP_TYPE_HYBRID
                    R.id.satellite_map -> map.mapType = GoogleMap.MAP_TYPE_SATELLITE
                    R.id.terrain_map -> map.mapType = GoogleMap.MAP_TYPE_TERRAIN
                }

                return true
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)

        // Call @onLocationSelected when the user confirms on the selected location
        binding.doneFloatingActionButton.setOnClickListener {
            onLocationSelected()
        }

        return binding.root
    }

    // When the user confirms on the selected location,
    private fun onLocationSelected() {
        marker?.apply {
            // Send back the selected location details to the view model
            viewModel.latitude.value = position.latitude
            viewModel.longitude.value = position.longitude
            viewModel.reminderSelectedLocationStr.value = title
            // Navigate back to the previous fragment to save the reminder and add the geofence
            viewModel.navigationCommand.value = NavigationCommand.Back
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap.apply {
            // Add style to the map
            setStyle()
            // Put a marker to the user's selected point of interest
            setPoiClick()
            // Put a marker to the user's selected custom location
            setLongClick()

            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private fun GoogleMap.setStyle() {
        try {
            // Customize the styling of the base map using a JSON object defined in a raw resource file.
            setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    requireContext(), R.raw.map_style
                )
            ).apply {
                if (!this) {
                    Log.e(_tag, "Failed to stylize the base map.")
                }
            }
        } catch (e: Resources.NotFoundException) {
            Log.e(_tag, "Style couldn't be found. Error: $e.")
        }
    }

    private fun GoogleMap.setPoiClick() {
        setOnPoiClickListener { pointOfInterest ->
            clear()

            marker = addMarker(
                MarkerOptions().position(pointOfInterest.latLng).title(pointOfInterest.name)
            )?.apply {
                showInfoWindow()
            }

            animateCamera(CameraUpdateFactory.newLatLng(pointOfInterest.latLng))
        }
    }

    private fun GoogleMap.setLongClick() {
        setOnMapLongClickListener { latLng ->
            clear()

            marker = addMarker(
                MarkerOptions().position(latLng).title(getString(R.string.dropped_pin))
                    // Create additional text (called a snippet) that's displayed below the title.
                    .snippet(
                        String.format(
                            Locale.getDefault(),
                            "Lat: %1$.5f, Long: %2$.5f",
                            latLng.latitude,
                            latLng.longitude
                        )
                    )
            )?.apply {
                showInfoWindow()
            }

            animateCamera(CameraUpdateFactory.newLatLng(latLng))
        }
    }

    @SuppressLint("MissingPermission")
    private fun GoogleMap.getUserLocation() {
        isMyLocationEnabled = true

        fusedLocationProviderClient.lastLocation.addOnSuccessListener { location: Location? ->
            location?.let {
                val userLocation = LatLng(location.latitude, location.longitude)

                // Zoom to the user location after taking required permission
                moveCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        userLocation, DEFAULT_ZOOM_LEVEL
                    )
                )
                marker = addMarker(
                    MarkerOptions().position(userLocation)
                        .title(getString(R.string.my_location))
                )?.apply {
                    showInfoWindow()
                }
            }
        }
    }
}