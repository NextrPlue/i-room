package com.example.watchgps2.presentation

import IpConfig
import android.Manifest
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.*
import com.example.watchgps2.R

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
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
                    val prefs = getSharedPreferences("equipment_prefs", Context.MODE_PRIVATE)
                    val savedId = prefs.getLong("equipmentId", -1L)

                    if (savedId != -1L) {
                        sendLocationToServer(savedId, it.latitude, it.longitude)
                    } else {
                        Log.e("EQUIPMENT", "equipmentId가 설정되지 않았습니다.")
                    }
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
            .setSmallIcon(R.drawable.ic_launcher_foreground)
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
    private fun sendLocationToServer(equipmentId: Long, latitude: Double, longitude: Double) {

        // 토큰 가져오기
        val authPrefs = getSharedPreferences("auth", Context.MODE_PRIVATE)
        val token = authPrefs.getString("token", null)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                //서버 url 변경필요
                val url = URL("${IpConfig.getBaseUrl()}/api/sensor/heavy-equipments/location")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "PUT"
                connection.setRequestProperty("Content-Type", "application/octet-stream")

                //토큰 추가
                token?.let {
                    connection.setRequestProperty("Authorization", "Bearer $it")
                }

                connection.doOutput = true

                val bos = ByteArrayOutputStream()
                val dos = DataOutputStream(bos)

                dos.writeLong(equipmentId)
                dos.writeDouble(latitude)
                dos.writeDouble(longitude)

                connection.outputStream.use { it.write(bos.toByteArray()) }

                val responseCode = connection.responseCode
                Log.d("Server", "서버 응답 코드: $responseCode")

                connection.disconnect()
            } catch (e: Exception) {
                Log.e("Server", "위치 전송 실패: ${e.message}")
            }
        }
    }

}