package com.example.locationtrackingmodule

import android.Manifest
import android.app.job.JobParameters
import android.app.job.JobService
import android.content.Context
import android.content.pm.PackageManager
import android.location.Criteria
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.VolleyLog
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONException
import org.json.JSONObject

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
class LocationTrackingJobService : JobService() {

    lateinit var location: Location
    private val jobCancelled = false
    private val TAG = LocationTrackingJobService::class.simpleName

    override fun onStopJob(params: JobParameters?): Boolean {

        doingBackgroundTask(params)
        return true
    }

    private fun doingBackgroundTask(params: JobParameters?) {

        Thread(Runnable {
            if (jobCancelled) {
                return@Runnable
            }
            getLocation()
            try {
                Thread.sleep(1000)
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }

            Log.e(TAG, "run: Job Finished.")
            jobFinished(params, false)
        }).start()

    }

    private fun getLocation() {
        try {
            val locationManager =
                applicationContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val criteria = Criteria()
            val provider = locationManager.getBestProvider(criteria, true)
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
            ) {

                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                ) {

                    location = locationManager.getLastKnownLocation(provider!!)
                    if (location == null) {
                        location =
                            locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                    }
                    if (location == null) {
                        location =
                            locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                    }
                    if (location == null) {
                        location =
                            locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER)
                    }
                    Log.e(TAG, " Latitude: " + location.latitude)
                    Log.e(TAG, " Longitude: " + location.longitude)

                    uploadLocation(location)

                    locationManager.requestLocationUpdates(
                        provider,
                        1000,
                        10f,
                        object : LocationListener {
                            override fun onLocationChanged(location: Location) {
                                Log.e(TAG, "onLocationChanged: Latitude: " + location.latitude)
                                Log.e(TAG, "onLocationChanged: Longitude: " + location.longitude)
                            }

                            override fun onStatusChanged(
                                provider: String,
                                status: Int,
                                extras: Bundle
                            ) {

                            }

                            override fun onProviderEnabled(provider: String) {

                            }

                            override fun onProviderDisabled(provider: String) {

                            }
                        })
                }
            } else {
                Toast.makeText(this, "Gps Location disabled", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    private fun uploadLocation(location: Location?) {
        val jsonObject = JSONObject()
        val queue = Volley.newRequestQueue(this)
        try {

            jsonObject.put("client_id", "958")
            jsonObject.put("user_id", "14040")
            jsonObject.put("latitude", location?.latitude)
            jsonObject.put("longitude", location?.longitude)
            jsonObject.put("altitude", location?.altitude)
            jsonObject.put("accuracy", location?.accuracy)
            jsonObject.put("app_version", "1")
            jsonObject.put("firebase_key", "")
            jsonObject.put("comments", "Kotlin Module")
            jsonObject.put("network_type", location?.provider)
            jsonObject.put("lead_id", "0")

            Log.e(TAG, "PostData: $jsonObject")


        } catch (e: JSONException) {
            e.printStackTrace()
        }


        val url = "https://webcrstravel.com/superadmin/json_update_location_log.php"

        val jsonObjReq = JsonObjectRequest(
            Request.Method.POST,
            url, jsonObject,
            object : Response.Listener<JSONObject> {
                override fun onResponse(response: JSONObject) {
                    Log.e(TAG, "Server response: $response")
                    try {
                        if (response.getString("response") == "success") {
                            Toast.makeText(
                                applicationContext, "Location Uploaded Successfully.",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            Toast.makeText(
                                applicationContext, "Location upload response is wrong.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }

                }
            }, object : Response.ErrorListener {
                override fun onErrorResponse(error: VolleyError) {

                    VolleyLog.d("Request", "Error: " + error.message)
                }
            })

        queue.add(jsonObjReq)
    }

    override fun onStartJob(params: JobParameters?): Boolean {
        return false
    }


}