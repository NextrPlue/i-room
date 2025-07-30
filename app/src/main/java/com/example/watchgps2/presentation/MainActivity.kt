package com.example.watchgps2.presentation

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
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
import androidx.core.content.ContextCompat
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.Text

class MainActivity : ComponentActivity() {

    private lateinit var locationPermissionRequest: ActivityResultLauncher<Array<String>>
    private val locationText = mutableStateOf("위치 정보 없음")
    private var isTracking = false

    private val locationPermissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    // 내부에서 상태를 직접 바꾸는 BroadcastReceiver
    private val locationReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val lat = intent?.getDoubleExtra("latitude", 0.0)
            val lon = intent?.getDoubleExtra("longitude", 0.0)

            if (lat != null && lon != null) {
                locationText.value = "위도: $lat\n경도: $lon"
            }
        }
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 리시버 등록 (ContextCompat 필요 없음)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            registerReceiver(locationReceiver, IntentFilter("LOCATION_UPDATE"), RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(locationReceiver, IntentFilter("LOCATION_UPDATE"))
        }

        // 권한 요청 등록
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

        // 알림 채널 생성 (O 이상)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "location_channel",
                "위치 추적",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        // ✅ UI 구성
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

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(locationReceiver)
    }
}
