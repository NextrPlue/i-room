package com.example.watchgps2.presentation

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.edit

class LocationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val latitude = intent.getDoubleExtra("latitude", 0.0)
        val longitude = intent.getDoubleExtra("longitude", 0.0)

        Log.d("LocationReceiver", "위치 수신됨: ($latitude, $longitude)")

        // SharedPreferences에 저장
        val prefs = context.getSharedPreferences("location_prefs", Context.MODE_PRIVATE)
        prefs.edit {
            putFloat("latitude", latitude.toFloat())
                .putFloat("longitude", longitude.toFloat())
        }
    }
}