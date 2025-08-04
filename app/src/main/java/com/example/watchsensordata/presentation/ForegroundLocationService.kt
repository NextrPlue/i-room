package com.example.watchsensordata.presentation

import android.Manifest
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.*

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.HttpURLConnection
import java.net.URL

class ForegroundLocationService : Service() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    override fun onCreate() {
        super.onCreate()

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5_000L)
            .build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val location = result.lastLocation
                location?.let {
                    Log.d("GPS", "위도: ${it.latitude}, 경도: ${it.longitude}")
                    sendLocationToUI(it.latitude, it.longitude)

                    // 서버 전송
                    sendLocationToServer(workerId = 1L, it.latitude, it.longitude)
                }
            }
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.requestLocationUpdates(request, locationCallback, Looper.getMainLooper())
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = NotificationCompat.Builder(this, "location_channel")
            .setContentTitle("GPS 추적 중")
            .setContentText("위치 정보를 주기적으로 수집 중입니다.")
            .build()

        startForeground(1, notification)
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    // 위치 정보를 Broadcast로 전달하는 함수
    private fun sendLocationToUI(latitude: Double, longitude: Double) {
        val intent = Intent().apply {
            setClassName(packageName, "com.example.watchgps2.presentation.LocationReceiver")
            action = "LOCATION_UPDATE"
            putExtra("latitude", latitude)
            putExtra("longitude", longitude)
        }

        sendBroadcast(intent)
    }

    //서버 전송 함수
    private fun sendLocationToServer(workerId: Long, latitude: Double, longitude: Double) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                //서버 url 변경필요
                val url = URL("https://my-wearos-test.free.beeceptor.com/api/location")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.setRequestProperty("Content-Type", "application/json")
                connection.doOutput = true

                val json = """
                {
                    "workerId": $workerId,
                    "latitude": $latitude,
                    "longitude": $longitude
                }
            """.trimIndent()

                connection.outputStream.use { it.write(json.toByteArray()) }

                val responseCode = connection.responseCode
                Log.d("Server", "서버 응답 코드: $responseCode")

                connection.disconnect()
            } catch (e: Exception) {
                Log.e("Server", "위치 전송 실패: ${e.message}")
            }
        }
    }

}