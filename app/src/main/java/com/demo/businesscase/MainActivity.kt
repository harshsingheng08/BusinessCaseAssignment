package com.demo.businesscase

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearSnapHelper
import com.demo.businesscase.adapter.HorizontalPanelAdapter
import com.demo.businesscase.databinding.ActivityMainBinding
import com.demo.businesscase.model.Content
import com.demo.businesscase.model.DataModel
import com.demo.businesscase.model.Item
import com.demo.businesscase.model.MapModel
import com.demo.businesscase.model.Users
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    val auth: FirebaseAuth = FirebaseAuth.getInstance()
    val database = FirebaseDatabase.getInstance()
    private val locationPermissionRequestCode = 1001
    lateinit var adapter: HorizontalPanelAdapter

    //private val locationPermissionRequestCode = 1001
    private val minTimeMillis =
        1000L // Minimum time interval between location updates (milliseconds)
    private val minDistanceMeters =
        10f // Minimum distance interval between location updates (meters)

    private lateinit var locationManager: LocationManager
    private lateinit var locationListener: LocationListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        getUserData()

        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        locationListener = MyLocationListener()
        checkLocationPermission()
        binding.btnLogout.setOnClickListener {
            auth.signOut()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun getLocationSettings() {
        val alertDialog: AlertDialog.Builder = AlertDialog.Builder(this)
        alertDialog.setTitle("GPS is settings")
        alertDialog.setMessage("GPS is not enabled. Do you want to go to settings menu?")
        alertDialog.setPositiveButton(
            "Settings"
        ) { dialog, which ->
            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivity(intent)
        }
        alertDialog.setNegativeButton(
            "Cancel"
        ) { dialog, which -> dialog.cancel() }
        alertDialog.show()
    }

    private fun checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Permission is not granted, request it
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                locationPermissionRequestCode
            )
        } else {
            val hasGps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
            if (!hasGps) {
                getLocationSettings()
            }
            getCurrentLocation()
            // Permission is already granted, start requesting location updates
            startLocationUpdates()
        }
    }

    private fun startLocationUpdates() {
        try {
            // Request location updates with specific time and distance intervals
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                minTimeMillis,
                minDistanceMeters,
                locationListener
            )
        } catch (e: SecurityException) {
            Log.e("Location", "Error requesting location updates: ${e.message}")
        }
    }

    private inner class MyLocationListener : LocationListener {
        override fun onLocationChanged(location: Location) {
            // Handle location updates
            val latitude = location.latitude
            val longitude = location.longitude
            Log.d("Location", "Latitude: $latitude, Longitude: $longitude")
            val db1 = FirebaseDatabase.getInstance().getReference("Map")
            val mapModel = MapModel(
                "My Location",
                "https://firebasestorage.googleapis.com/v0/b/businesscase-b84fc.appspot.com/o/map_pin.png?alt=media&token=4d61f028-2704-4f72-8b3f-483ff3bc39a5",
                "$latitude",
                "$longitude"
            )
            val userId = auth.currentUser?.uid
            userId?.let {
                db1.child(it).setValue(mapModel)
            }
        }

        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
            // Handle status changes
        }

        override fun onProviderEnabled(provider: String) {
            // Provider (GPS) is enabled
        }

        override fun onProviderDisabled(provider: String) {
            // Provider (GPS) is disabled
        }
    }

    private fun getUserData() {
        binding.progressCircular.visibility = View.VISIBLE
        val items = ArrayList<Item>()
        setAdapter()
        val userId = auth.currentUser?.uid
        val refUsers = database.getReference("Users")
        val refMap = database.getReference("Map")
        val refData = database.getReference("Data")
        val userReference = userId?.let { refUsers.child(it) }
        val mapReference = userId?.let { refMap.child(it) }
        val dataReference = userId?.let { refData.child(it) }
        userReference?.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    items.removeIf { it.type == "profile" }
                    items.removeIf { it.type == "map" }
                    items.removeIf { it.type == "data" }
                    val user = snapshot.getValue(Users::class.java)
                    val contentUser = Content()
                    contentUser.image = user?.imageUrl
                    contentUser.email = user?.email
                    contentUser.name = user?.fullName
                    val itemUser = Item("profile", contentUser)
                    items.add(itemUser)
                    mapReference?.addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.exists()) {
                                items.removeIf { it.type == "map" }
                                items.removeIf { it.type == "data" }
                                val map = snapshot.getValue(MapModel::class.java)
                                val contentMap = Content()
                                contentMap.title = map?.title
                                contentMap.pin = map?.pin
                                contentMap.lat = map?.lat
                                contentMap.lng = map?.lng
                                val itemMap = Item("map", contentMap)
                                items.add(itemMap)
                                dataReference?.addValueEventListener(object : ValueEventListener {
                                    override fun onDataChange(snapshot: DataSnapshot) {
                                        if (snapshot.exists()) {
                                            items.removeIf { it.type == "data" }
                                            val data = snapshot.getValue(DataModel::class.java)
                                            val contentData = Content()
                                            contentData.titleData = data?.title
                                            contentData.source = data?.source
                                            contentData.value = data?.value
                                            val itemData = Item("data", contentData)
                                            items.add(itemData)
                                            if (adapter != null) {
                                                adapter.setItems(items)
                                            }
                                            binding.progressCircular.visibility = View.GONE
                                        } else {

                                        }
                                    }

                                    override fun onCancelled(error: DatabaseError) {
                                        TODO("Not yet implemented")
                                    }

                                })

                            } else {

                            }
                        }

                        override fun onCancelled(error: DatabaseError) {

                        }

                    })
                } else {
                    // User data does not exist
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }

    private fun setAdapter() {
        adapter = HorizontalPanelAdapter(this@MainActivity)
        binding.rvHome.adapter = adapter
        val snapHelper = LinearSnapHelper()
        snapHelper.attachToRecyclerView(binding.rvHome)
    }

    private fun getCurrentLocation() {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                if (location != null) {
                    val latitude = location.latitude
                    val longitude = location.longitude
                    val db1 = FirebaseDatabase.getInstance().getReference("Map")
                    val mapModel = MapModel(
                        "My Location",
                        "https://firebasestorage.googleapis.com/v0/b/businesscase-b84fc.appspot.com/o/map_pin.png?alt=media&token=4d61f028-2704-4f72-8b3f-483ff3bc39a5",
                        "$latitude",
                        "$longitude"
                    )
                    val userId = auth.currentUser?.uid
                    userId?.let {
                        db1.child(it).setValue(mapModel)
                    }

                    Log.e("TAG", "getCurrentLocation: $latitude $longitude")
                }
            }
            .addOnFailureListener { e ->
                // Handle failure
                Log.e("Location", "Error getting location: ${e.message}")
            }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            locationPermissionRequestCode -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted, start requesting location updates
                    val hasGps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                    if (!hasGps) {
                        getLocationSettings()
                    }
                    getCurrentLocation()
                    startLocationUpdates()
                } else {
                    // Permission denied, handle accordingly
                    // You may show a message to the user or disable location-related functionality
                }
            }
        }
    }
}