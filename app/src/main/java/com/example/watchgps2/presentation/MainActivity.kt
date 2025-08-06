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
import android.util.Log
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
import androidx.lifecycle.lifecycleScope
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.Text
import com.example.watchgps2.data.model.ApiKeyRequest
import com.example.watchgps2.data.model.TokenResponse
import com.example.watchgps2.data.remote.RetrofitClient
import com.example.watchgps2.util.TokenManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : ComponentActivity() {

    private lateinit var locationPermissionRequest: ActivityResultLauncher<Array<String>>
    private val locationText = mutableStateOf("위치 정보 없음")
    private var isTracking = mutableStateOf(false)

    private val locationPermissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    override fun onResume() {
        super.onResume()
    }

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Retrofit 초기화
        RetrofitClient.init(applicationContext)

        // 토큰 요청
        val body = ApiKeyRequest("worker-system-api-key-ed720aef-ee6d-40fc-933f-ff9ce8e2bcae")
        RetrofitClient.apiService.authenticate(body)
            .enqueue(object : Callback<TokenResponse> {
                override fun onResponse(call: Call<TokenResponse>, response: Response<TokenResponse>) {
                    if (response.isSuccessful) {
                        val token = response.body()?.data?.token
                        token?.let {
                            TokenManager.saveToken(applicationContext, it)
                            Log.d("TOKEN", "토큰 저장됨: $it")
                        }
                    } else {
                        Log.e("TOKEN", "토큰 요청 실패")
                    }
                }

                override fun onFailure(call: Call<TokenResponse>, t: Throwable) {
                    Log.e("TOKEN", "네트워크 오류: ${t.message}")
                }
            })

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
                            if (!isTracking.value) {
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
                            if (isTracking.value) "중지" else "시작",
                            color = Color.White
                        )
                    }
                }
            }
        }

        // 주기적 위치 확인 루프
        lifecycleScope.launch {
            while (isActive){
                var prefs = getSharedPreferences("location_prefs", MODE_PRIVATE)
                val lat = prefs.getFloat("latitude", 0.0f).toDouble()
                val lon = prefs.getFloat("longitude", 0.0f).toDouble()

                if(lat != 0.0 || lon != 0.0){
                    locationText.value = "위도: $lat\n경도: $lon"
                } else{
                    locationText.value = "위치 정보 없음"
                }

                delay(20000)
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
        isTracking.value = true
        locationText.value = "위치 추적 시작됨"
    }

    private fun stopLocationService() {
        val intent = Intent(this, ForegroundLocationService::class.java)
        stopService(intent)
        isTracking.value = false
        locationText.value = "위치 추적 중지됨"
    }

}
