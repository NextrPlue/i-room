/* While this template provides a good starting point for using Wear Compose, you can always
 * take a look at https://github.com/android/wear-os-samples/tree/main/ComposeStarter to find the
 * most up to date changes to the libraries and their usages.
 */

package com.example.watchsensordata.presentation

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.mutableStateOf
import androidx.core.app.ActivityCompat
import androidx.health.services.client.ExerciseClient
import androidx.health.services.client.HealthServices
import androidx.health.services.client.ExerciseUpdateCallback
import androidx.health.services.client.data.*
import androidx.health.services.client.endExercise
import androidx.health.services.client.startExercise
import androidx.lifecycle.lifecycleScope
import com.example.watchsensordata.R
import com.example.watchsensordata.presentation.data.model.LoginRequest
import com.example.watchsensordata.presentation.data.model.LoginResponse
import com.example.watchsensordata.presentation.data.remote.RetrofitClient
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class MainActivity : ComponentActivity() {

    private lateinit var exerciseClient: ExerciseClient
    private lateinit var updateCallback: ExerciseUpdateCallback

    private lateinit var locationPermissionRequest: ActivityResultLauncher<Array<String>>
    private val locationText = mutableStateOf("위치 정보 없음")
    private var isTracking = mutableStateOf(false)

    private val locationPermissions = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)

    //센서값 전역 변수
    private var heartRateBpm: Double? = null
    private var steps: Long? = null
    private var speed: Double? = null
    private var pace: Double? = null
    private var stepPerMinute: Long? = null


    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        IpConfig.initialize(applicationContext)

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

        // 1. 처음에는 로그인 화면을 보여줌
        setContentView(R.layout.activity_login)

        findViewById<EditText>(R.id.emailEdit).setText("test@example.com")
        findViewById<EditText>(R.id.passwordEdit).setText("password123!")

        // 2. 로그인 버튼 클릭 시
        findViewById<Button>(R.id.loginButton).setOnClickListener {
            val email = findViewById<EditText>(R.id.emailEdit).text.toString()
            val password = findViewById<EditText>(R.id.passwordEdit).text.toString()

            val loginRequest = LoginRequest(email, password)

            RetrofitClient.apiService.login(loginRequest)
                .enqueue(object : Callback<LoginResponse> {
                    override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                        if (response.isSuccessful) {
                            val token = response.body()?.data?.token
                            Log.d("LOGIN", "토큰 받음: $token")

                            // 토큰 저장
                            val prefs = getSharedPreferences("auth", MODE_PRIVATE)
                            prefs.edit().putString("token", token).apply()

                            // 3. 로그인 성공하면 메인 화면으로 전환
                            setContentView(R.layout.activity_main)
                            setupButtonScreen()
                        } else {
                            Toast.makeText(this@MainActivity, "로그인 실패", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                        Log.e("LOGIN", "요청 실패: ${t.message}")
                        Toast.makeText(this@MainActivity, "네트워크 오류", Toast.LENGTH_SHORT).show()
                    }
                })
        }
    }

    // 4. 기존 버튼 화면 초기화 (start/stop 버튼 이벤트 설정 등)
    private fun setupButtonScreen() {

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

        lifecycleScope.launch {
            // ExerciseClient 초기화
            exerciseClient = HealthServices.getClient(this@MainActivity).exerciseClient
            // 콜백 등록
            updateCallback = object : ExerciseUpdateCallback {
                override fun onAvailabilityChanged(dataType: DataType<*, *>, availability: Availability) {}

                override fun onExerciseUpdateReceived(update: ExerciseUpdate) {
                    update.latestMetrics.getData(DataType.HEART_RATE_BPM).lastOrNull()?.let {
                        heartRateBpm = it.value
                        Log.d("SENSOR", "Heart Rate: $heartRateBpm")
                    }
                    update.latestMetrics.getData(DataType.STEPS_PER_MINUTE).lastOrNull()?.let {
                        stepPerMinute = it.value
                        Log.d("SENSOR", "STEPS_PER_MINUTE: $stepPerMinute")
                    }
                    update.latestMetrics.getData(DataType.SPEED).lastOrNull()?.let {
                        speed = it.value
                        Log.d("SENSOR", "SPEED: $speed")
                    }
                    update.latestMetrics.getData(DataType.STEPS).lastOrNull()?.let {
                        steps = it.value
                        Log.d("SENSOR", "STEPS: $steps")
                    }
                    update.latestMetrics.getData(DataType.PACE).lastOrNull()?.let {
                        pace = it.value
                        Log.d("SENSOR", "PACE: $pace")
                    }

                    saveSensorDataLocally()
                }

                override fun onLapSummaryReceived(lapSummary: ExerciseLapSummary) {}
                override fun onRegistered() {}
                override fun onRegistrationFailed(throwable: Throwable) {}
            }

            exerciseClient.setUpdateCallback(updateCallback)
        }

        // start 버튼
        findViewById<Button>(R.id.startButton).setOnClickListener {
            val fineGranted = ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

            if (fineGranted) {
                startExercise()
                startLocationService()
            } else {
                locationPermissionRequest.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION))
                Toast.makeText(
                    this,
                    "GPS 권한이 필요합니다. 설정에서 항상 허용으로 변경해주세요.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        // stop 버튼
        findViewById<Button>(R.id.stopButton).setOnClickListener {
            stopExercise()
            stopLocationService()
        }
    }


    private fun saveSensorDataLocally() {
        val prefs = getSharedPreferences("SensorPrefs", Context.MODE_PRIVATE)
        with(prefs.edit()) {
            putFloat("heartRate", (heartRateBpm ?: 0.0).toFloat())
            putLong("steps", steps ?: 0L)
            putFloat("speed", (speed ?: 0.0).toFloat())
            putFloat("pace", (pace ?: 0.0).toFloat())
            putLong("stepPerMinute", stepPerMinute ?: 0L)
            apply()
        }
    }

    private fun startExercise() {
        val config = ExerciseConfig.Builder(ExerciseType.WALKING)
            .setDataTypes(
                setOf(
                    DataType.HEART_RATE_BPM,
                    DataType.SPEED,
                    DataType.STEPS,
                    DataType.PACE,
                    DataType.STEPS_PER_MINUTE,
                )
            )
            .setIsGpsEnabled(true)
            .build()

        lifecycleScope.launch {
            try {
                exerciseClient.startExercise(config)
                Log.d("EXERCISE", "운동 시작됨")

            } catch (e: Exception) {
                Log.e("EXERCISE", "운동 시작 실패: ${e.message}")
            }
        }
    }

    private fun stopExercise() {
        lifecycleScope.launch {
            try {
                exerciseClient.endExercise()
                Log.d("EXERCISE", "운동 종료됨")

            } catch (e: Exception) {
                Log.e("EXERCISE", "운동 종료 실패: ${e.message}")
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