package com.udacity.project4.locationreminders.savereminder

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSaveReminderBinding
import com.udacity.project4.locationreminders.geofence.GeofenceBroadcastReceiver
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.utils.Constants
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject


class SaveReminderFragment : BaseFragment() {
    //Get the view model this time as a single to be shared with the another fragment
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSaveReminderBinding
    private lateinit var geofencingClient: GeofencingClient

    private lateinit var reminderDataItem: ReminderDataItem

    private val runningQOrlater: Boolean
        get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q

    private val geoFencePendingIntent: PendingIntent by lazy {
        val intent = Intent(requireContext(), GeofenceBroadcastReceiver::class.java).apply {
            action = GEO_FENCE_ACTION
        }
        PendingIntent.getBroadcast(
            requireActivity(),
            0, intent, PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_save_reminder, container, false)

        setDisplayHomeAsUpEnabled(true)

        binding.viewModel = _viewModel
        geofencingClient = LocationServices.getGeofencingClient(requireActivity())
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = this
        listeners()
    }

    fun listeners() {
        binding.selectLocation.setOnClickListener {
            _viewModel.navigationCommand.value =
                NavigationCommand.To(SaveReminderFragmentDirections.actionSaveReminderFragmentToSelectLocationFragment())
        }
        binding.saveReminder.setOnClickListener {
            reminderDataItem = ReminderDataItem(
                _viewModel.reminderTitle.value,
                _viewModel.reminderDescription.value,
                _viewModel.reminderSelectedLocationStr.value,
                _viewModel.latitude.value,
                _viewModel.longitude.value
            )
            if (_viewModel.validateEnteredData(reminderDataItem)) {
                if (permissionApproved()) {
                    checkDeviceLocationAndStartGeoFence()
                } else {
                    requestFineAndBackLocation()
                }
            }
        }
    }

    private fun requestFineAndBackLocation() {
        if (permissionApproved()) {
            return
        }
        var permissionArray = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
        val requestCode = when {
            runningQOrlater -> {
                permissionArray += Manifest.permission.ACCESS_BACKGROUND_LOCATION
                REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE
            }
            else -> REQUEST_FOREGROUND_ONLY_PERMISSION_RESULT_CODE
        }
        requestPermissions(permissionArray, requestCode)
    }

    private fun checkDeviceLocationAndStartGeoFence(resolve: Boolean = false) {
        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_LOW_POWER
        }
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        val settingClient = LocationServices.getSettingsClient(requireActivity())
        val locationSettingsResponse = settingClient.checkLocationSettings(builder.build())

        locationSettingsResponse.addOnCompleteListener {
            if (it.isSuccessful) {
                startGeo()
            }
        }
        locationSettingsResponse.addOnFailureListener {
            if (it is ResolvableApiException && resolve) {
                try {
                    startIntentSenderForResult(
                        it.resolution.intentSender,
                        0, null, 0, 0, 0, null
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "checkDeviceLocationAndStartGeoFence: ${e.message}")
                }
            } else {
                Snackbar.make(
                    requireView(),
                    R.string.location_required_error, Snackbar.LENGTH_INDEFINITE
                ).setAction(android.R.string.ok) {
                    checkDeviceLocationAndStartGeoFence()
                }.show()
            }
        }

    }

    @SuppressLint("MissingPermission")
    fun startGeo() {
        val geofence = Geofence.Builder()
            .setRequestId(reminderDataItem.id)
            .setCircularRegion(
                reminderDataItem.latitude!!,
                reminderDataItem.longitude!!,
                120.0f
            )
            .setExpirationDuration(Constants.GEOFENCE_EXPIRATION_IN_MILLISECONDS)
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
            .build()

        val geofenceRequest = getGeoFenceRequest(geofence)

        geofencingClient.removeGeofences(geoFencePendingIntent).addOnCompleteListener() {
            geofencingClient.addGeofences(
                geofenceRequest, geoFencePendingIntent
            ).addOnSuccessListener {
                _viewModel.saveReminder(reminderDataItem)
            }.addOnFailureListener{
                _viewModel.showSnackBarInt.value = R.string.error_adding_geofence
            }
        }
    }

    private fun getGeoFenceRequest(geofence: Geofence): GeofencingRequest =
        GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofence(geofence)
            .build()

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == TURN_DEVICE_LOCATION) {

        }
    }

    private fun permissionApproved(): Boolean {
        val forgroundApproved = (
                PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(
                    requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION
                ))
        val backgroundPermissionApproved =
            if (runningQOrlater) {
                PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(
                    requireActivity(), Manifest.permission.ACCESS_BACKGROUND_LOCATION
                )
            } else
                true
        return backgroundPermissionApproved && forgroundApproved
    }

    override fun onDestroy() {
        super.onDestroy()
        _viewModel.onClear()
    }

    companion object {
        private const val TAG = "SaveReminderActivity"
        const val pendingIntentRequestCode: Int = 502
        const val GEOFENCE_REQUEST_CODE: Int = 505
        const val GEO_FENCE_ACTION = "action_geo_fence"
        const val TURN_DEVICE_LOCATION = 0
        private const val REQUEST_FOREGROUND_ONLY_PERMISSION_RESULT_CODE = 101
        private const val REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE = 45
    }

}
