package com.udacity.project4.locationreminders.savereminder.selectreminderlocation


import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.*
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.BuildConfig
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject

class SelectLocationFragment : BaseFragment(), OnMapReadyCallback {

    //Use Koin to get the view model of the SaveReminder
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSelectLocationBinding
    private lateinit var POI: PointOfInterest
    private val locationProvider by lazy {
        LocationServices.getFusedLocationProviderClient(requireActivity())
    }


    private lateinit var map: GoogleMap

    // #01 DONE
    private val runningQOrlater: Boolean
        get() = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_select_location, container, false)

        binding.viewModel = _viewModel
        binding.lifecycleOwner = this
        clickListeners()
        initMap()

        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(true)
        requestForegroundAndBackgroundPermission()

//     DONE   TODO: zoom to the user location after taking his permission
//          requestForegroundAndBackgroundPermission()
//        DONE TODO: add style to the map
//       DONE  TODO: put a marker to location that the user selected


//
//     DONE   TODO: call this function after the user confirms on the selected location
        return binding.root
    }

    private fun clickListeners() {
        binding.confirmLocationBtn.setOnClickListener {
            onLocationSelected()
        }
    }

    private fun initMap() {
        val fragmentMap = childFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        fragmentMap.getMapAsync(this)
    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Log.d(TAG, "onRequestPermissionsResult: ")
        showToast("works", requireActivity())
        if (
            grantResults.isEmpty() ||
            grantResults[LOCATION_PERMISSION_INDEX] == PackageManager.PERMISSION_DENIED ||
            (requestCode == REQUEST_FOREGROUND_AND_BACKGROUND_LOCATION_RESULT_CODE &&
                    grantResults[BACKGROUND_LOCATION_PERMISSION_INDEX] ==
                    PackageManager.PERMISSION_DENIED)
        ) {
            Log.d(TAG, "onRequestPermissionsResult: true ")
            Snackbar.make(
                binding.selectLocationFragment,
                R.string.permission_denied_explanation,
                Snackbar.LENGTH_INDEFINITE
            )
                .setAction(R.string.settings) {
                    startActivity(Intent().apply {
                        action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                        data = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    })
                }.show()
        } else {
            Log.d(TAG, "onRequestPermissionsResult: false ")
            checkDeviceLocation()
        }
        Log.d(TAG, "onRequestPermissionsResult: ")
    }

    @SuppressLint("MissingPermission")
    private fun checkDeviceLocation() {
        showToast("check device location", context = requireContext())
        Log.d(TAG, "checkDeviceLocation: ")
        map.isMyLocationEnabled = true
        locationProvider.lastLocation.addOnCompleteListener(requireActivity()) { task ->
            val location = task.result
            location?.let {
                showToast("map location got", context = requireContext())
                val latLng = LatLng(location.latitude, location.longitude)
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 23.0F))
                map.addMarker(MarkerOptions().position(latLng))
            }
        }
    }

    //DONE #03
    private fun requestForegroundAndBackgroundPermission() {
        if (checkIfPermissionsAreGranted()) {
            //     DONE   TODO: add the map setup implementation
            checkDeviceLocation()
            return
        }
        var permissionsArray = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
        val resultCode =
            when {
                runningQOrlater -> {
                    permissionsArray += Manifest.permission.ACCESS_BACKGROUND_LOCATION
                    REQUEST_FOREGROUND_AND_BACKGROUND_LOCATION_RESULT_CODE
                }
                else -> {
                    REQUEST_FOREGROUND_ONLY_PERMISSION_RESULT_CODE
                }
            }
        requestPermissions(
            permissionsArray, resultCode
        )
    }

    // DONE #02
    private fun checkIfPermissionsAreGranted(): Boolean {
        val foregroundPermissionApproved =
            PackageManager.PERMISSION_GRANTED ==
                    ActivityCompat.checkSelfPermission(
                        requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
                    )
        val backgroundPermissionApproved =
            if (runningQOrlater) {
                PackageManager.PERMISSION_GRANTED ==
                        ActivityCompat.checkSelfPermission(
                            requireContext(), Manifest.permission.ACCESS_BACKGROUND_LOCATION
                        )
            } else {
                true
            }
        return foregroundPermissionApproved and backgroundPermissionApproved
    }

    private fun onLocationSelected() {
        //  DONE      TODO: When the user confirms on the selected location,

        //    Done  TODO   send back the selected location details to the view model
        _viewModel.selectedPOI.value = POI
        POI.name?.let {
            _viewModel.reminderSelectedLocationStr.value = it
        }
        //  Done    TODO   and navigate back to the previous fragment to save the reminder and add the geofence
        _viewModel.navigationCommand.postValue(
            NavigationCommand.BackTo
                (
                R.id.saveReminderFragment
            )
        )
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.map_options, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        //DONE TODO: Change the map type based on the user's selection.
        R.id.normal_map -> {
            map.mapType = GoogleMap.MAP_TYPE_NORMAL
            true
        }
        R.id.hybrid_map -> {
            map.mapType = GoogleMap.MAP_TYPE_HYBRID
            true
        }
        R.id.satellite_map -> {
            map.mapType = GoogleMap.MAP_TYPE_SATELLITE
            true
        }
        R.id.terrain_map -> {
            map.mapType = GoogleMap.MAP_TYPE_TERRAIN
            true
        }
        else -> super.onOptionsItemSelected(item)
    }


    @SuppressLint("MissingPermission")
    override fun onMapReady(_map: GoogleMap) {
        map = _map

        setUpMapStyle(map)

        handleUserSelection()


    }

    private fun handleUserSelection() {
        map.setOnMapLongClickListener {
            map.clear()
            POI = PointOfInterest(it, it.latitude.toString(), "Current Selected Location")
            map.addMarker(
                MarkerOptions()
                    .position(it)
            )
            if (binding.confirmLocationBtn.visibility != View.VISIBLE) {
                binding.confirmLocationBtn.visibility = View.VISIBLE
            }
        }
    }

    private fun setUpMapStyle(map: GoogleMap) {
        try {

            val success = map.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    requireContext(),
                    R.raw.dark_mode_style_map
                )
            )
            if (!success) {
                showToast("styling maps Failed", context = requireContext())
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    companion object {
        private const val REQUEST_FOREGROUND_AND_BACKGROUND_LOCATION_RESULT_CODE = 202
        private const val REQUEST_FOREGROUND_ONLY_PERMISSION_RESULT_CODE = 101

        //private const val REQUEST_TURN_DEVICE_LOCATION_ON = 29
        private const val TAG = "SelectLocationReminder"
        private const val LOCATION_PERMISSION_INDEX = 0
        private const val BACKGROUND_LOCATION_PERMISSION_INDEX = 1
    }

}
