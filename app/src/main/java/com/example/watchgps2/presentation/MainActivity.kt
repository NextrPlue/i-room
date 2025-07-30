package com.example.watchgps2.presentation

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.Text
import com.example.watchgps2.R
import com.example.watchgps2.presentation.ForegroundLocationService

class MainActivity : ComponentActivity() {

    private lateinit var locationPermissionRequest: ActivityResultLauncher<Array<String>>
    private val locationText = mutableStateOf("위치 정보 없음")
    private var isTracking = false

    private val locationPermissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        locationPermissionRequest = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
            if (granted) {
                Toast.makeText(this, "GPS 권한 허용됨", Toast.LENGTH_SHORT).show()
                startLocationService()
            } else {
                Toast.makeText(this, "GPS 권한 거부됨", Toast.LENGTH_SHORT).show()
                locationText.value = "위치 권한이 필요합니다"
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "location_channel",
                "위치 추적",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        setContent {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = locationText.value,
                        color = Color.White,
                        modifier = Modifier.padding(8.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = {
                        val fineGranted = ActivityCompat.checkSelfPermission(
                            this@MainActivity,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED

                        if (fineGranted) {
                            if (!isTracking) {
                                startLocationService()
                            } else {
                                stopLocationService()
                            }
                        } else {
                            locationPermissionRequest.launch(locationPermissions)
                            Toast.makeText(
                                this@MainActivity,
                                "위치 권한이 '항상 허용'인지 확인해주세요",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }) {
                        Text(
                            if (isTracking) "위치 추적 중지" else "위치 추적 시작",
                            color = Color.White
                        )
                    }
                }
            }
        }
    }

    private fun startLocationService() {
        val intent = Intent(this, ForegroundLocationService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
        isTracking = true
        locationText.value = "위치 추적 시작됨"
    }

    private fun stopLocationService() {
        val intent = Intent(this, ForegroundLocationService::class.java)
        stopService(intent)
        isTracking = false
        locationText.value = "위치 추적 중지됨"
    }
}
