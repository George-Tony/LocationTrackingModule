package com.example.locationtrackingmodule

import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

 class MainActivity : AppCompatActivity() {

     val TAG = MainActivity::class.simpleName

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        scheduleJob()
    }

     @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
     fun scheduleJob() {

         val componentName = ComponentName(this, MyJobService::class.java)
         val info = JobInfo.Builder(123, componentName)
             .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
             .setPersisted(true)
             .setPeriodic((15 * 60 * 1000).toLong())
             .build()

         val jobScheduler = getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
         val resultCode = jobScheduler.schedule(info)
         if (resultCode == JobScheduler.RESULT_SUCCESS) {
             Log.e(TAG, "sheduleJob: Job Scheduled.")
         } else {
             Log.e(TAG, "sheduleJob: Job scheduled cancelled.")
         }
     }


     @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
     fun cancelJob() {
         val jobScheduler = getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
         jobScheduler.cancel(123)
         Log.e(TAG, "cancelJob: Job Cancelled.")
     }
}
