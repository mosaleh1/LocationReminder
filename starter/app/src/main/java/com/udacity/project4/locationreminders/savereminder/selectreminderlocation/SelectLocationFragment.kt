package com.udacity.project4.locationreminders.savereminder.selectreminderlocation


import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.*
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
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
import kotlinx.android.synthetic.main.fragment_select_location.*
import kotlinx.coroutines.MainScope
import org.koin.android.ext.android.inject
import java.util.*


class SelectLocationFragment : BaseFragment(), OnMapReadyCallback {

    //Use Koin to get the view model of the SaveReminder
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSelectLocationBinding
    private lateinit var POI: PointOfInterest
    private lateinit var maps: GoogleMap
    private val locationProvider by lazy {
        LocationServices.getFusedLocationProviderClient(requireActivity())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(
                inflater, R.layout.fragment_select_location, container, false
            )
        listeners()
        setDisplayHomeAsUpEnabled(true)
        setHasOptionsMenu(true)
        initMap()
        return binding.root
    }

    private fun listeners() {
        binding.confirmLocationBtn.setOnClickListener {
            confirmLocation()
        }
    }

    private fun initMap() {
        val fragmentMap = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        fragmentMap.getMapAsync(this)
    }

    fun confirmLocation() {
        _viewModel.latitude.value = POI.latLng.latitude
        _viewModel.longitude.value = POI.latLng.longitude
        _viewModel.reminderSelectedLocationStr.value = POI.name ?: "Selected location"
        _viewModel.navigationCommand.value = NavigationCommand.Back
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.main_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.normal_map -> {
                maps.mapType = GoogleMap.MAP_TYPE_NORMAL
            }
            R.id.satellite_map -> {
                maps.mapType = GoogleMap.MAP_TYPE_SATELLITE

            }
            R.id.hybrid_map -> {
                maps.mapType = GoogleMap.MAP_TYPE_HYBRID
            }
            R.id.terrain_map -> {
                maps.mapType = GoogleMap.MAP_TYPE_TERRAIN

            }
            else -> super.onOptionsItemSelected(item)
        }
        return true
    }


    private fun getCompleteAddressString(LATITUDE: Double, LONGITUDE: Double): String? {
        var addressStr = ""
        val geocoder = Geocoder(context, Locale.getDefault())
        try {
            val addresses: List<Address>? = geocoder.getFromLocation(LATITUDE, LONGITUDE, 1)
            if (addresses != null) {
                val returnedAddress: Address = addresses[0]
                val strReturnedAddress = StringBuilder("")
                for (i in 0..returnedAddress.maxAddressLineIndex) {
                    strReturnedAddress.append(returnedAddress.getAddressLine(i)).append("\n")
                }
                addressStr = strReturnedAddress.toString()
            } else {
                Log.d(TAG, "getCompleteAddressString:  ")
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            //Log.v("My Current loction address", "Canont get Address!")
        }
        return addressStr.ifEmpty {
            "Selected Location"
        }
    }

    companion object {
        private const val REQUEST_FOREGROUND_AND_BACKGROUND_LOCATION_RESULT_CODE = 202
        private const val REQUEST_FOREGROUND_ONLY_PERMISSION_RESULT_CODE = 101

        //private const val REQUEST_TURN_DEVICE_LOCATION_ON = 29
        private const val FINE_LOCATION_REQUEST_CODE = 521
        private const val LOCATION_PERMISSION_INDEX = 0
        private const val BACKGROUND_LOCATION_PERMISSION_INDEX = 1
        private const val TAG = "SelectLocationReminder"
    }

    override fun onMapReady(googleMap: GoogleMap) {
        maps = googleMap
        maps.setOnMapLongClickListener {
            maps.clear()
            binding.confirmLocationBtn.visibility = View.VISIBLE
            POI = PointOfInterest(
                it,
                it.toString(),
                getCompleteAddressString(it.latitude, it.longitude)
            )
            maps.addMarker(
                MarkerOptions()
                    .position(it)
                    .title(getCompleteAddressString(it.latitude, it.longitude))
            ).showInfoWindow()
        }
        maps.setMapStyle(
            MapStyleOptions.loadRawResourceStyle(
                requireActivity(),
                R.raw.dark_mode_style_map
            )
        )
        if (ContextCompat.checkSelfPermission(
                requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            maps.isMyLocationEnabled = true
            navigateToCurrentLocation()
        } else {
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                FINE_LOCATION_REQUEST_CODE
            )
        }
    }

    @SuppressLint("MissingPermission")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == FINE_LOCATION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                maps.isMyLocationEnabled = true
                navigateToCurrentLocation()
            } else {
                _viewModel.showSnackBarInt.value = R.string.permission_denied_explanation
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun navigateToCurrentLocation() {
        locationProvider.lastLocation.addOnCompleteListener(requireActivity()) { locationTask ->
            if (locationTask.isSuccessful) {
                locationTask.result?.let {
                    val latLng = LatLng(it.latitude, it.longitude)
                    maps.moveCamera(
                        CameraUpdateFactory.newLatLngZoom(latLng, 25f)
                    )
                    maps.addMarker(
                        MarkerOptions().position(latLng)
                            .title(getCompleteAddressString(latLng.latitude, latLng.longitude))
                    )
                }
            }
        }
    }
}
