/* While this template provides a good starting point for using Wear Compose, you can always
 * take a look at https://github.com/android/wear-os-samples/tree/main/ComposeStarter to find the
 * most up to date changes to the libraries and their usages.
 */

package com.example.watchsensordata.presentation

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.net.HttpURLConnection
import java.net.URL

class MainActivity : ComponentActivity() {

    private lateinit var exerciseClient: ExerciseClient
    private lateinit var updateCallback: ExerciseUpdateCallback


    private val workerId = 1L

    private var sensorJob: Job? = null

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
        setContentView(R.layout.activity_main)

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
                        Log.d("SENSOR", "Heart Rate: $stepPerMinute")
                    }
                    update.latestMetrics.getData(DataType.SPEED).lastOrNull()?.let {
                        speed = it.value
                        Log.d("SENSOR", "Heart Rate: $speed")
                    }
                    update.latestMetrics.getData(DataType.STEPS).lastOrNull()?.let {
                        steps = it.value
                        Log.d("SENSOR", "Heart Rate: $steps")
                    }
                    update.latestMetrics.getData(DataType.PACE).lastOrNull()?.let {
                        pace = it.value
                        Log.d("SENSOR", "Heart Rate: $pace")
                    }
                }

                override fun onLapSummaryReceived(lapSummary: ExerciseLapSummary) {  }
                override fun onRegistered() {}
                override fun onRegistrationFailed(throwable: Throwable) {}
            }

            exerciseClient.setUpdateCallback(updateCallback)

        }

        findViewById<Button>(R.id.startButton).setOnClickListener {
            val fineGranted = ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

            if (fineGranted) {
                startExercise()
                startLocationService()
            } else {
                locationPermissionRequest.launch(
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
                )
                Toast.makeText(
                    this,
                    "GPS 권한이 필요합니다. 설정에서 항상 허용으로 변경해주세요.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        findViewById<Button>(R.id.stopButton).setOnClickListener {
            stopExercise()
            stopLocationService()
        }
    }

    //센서 데이터 서버 전송
    private fun sendSensorDataToServer(
        workerId: Long,
        heartRate: Double?,
        steps: Long?,
        speed: Double?,
        pace: Double?,
        stepPerMinute: Long?
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val url = URL("https://my-wearos-test.free.beeceptor.com/")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.setRequestProperty("Content-Type", "application/json")
                connection.doOutput = true

                val json = """
                    {
                        "workerId": $workerId,
                        "heartRate": $heartRate,
                        "steps": $steps,
                        "speed": $speed,
                        "pace": $pace,
                        "stepPerMinute": $stepPerMinute
                    }
                """.trimIndent()

                connection.outputStream.use { it.write(json.toByteArray()) }
                Log.d("SERVER", "응답 코드: ${connection.responseCode}")
                connection.disconnect()
            } catch (e: Exception) {
                Log.e("SERVER", "전송 실패: ${e.message}")
            }
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

                // 센서 전송 루프 시작
                sensorJob = lifecycleScope.launch {
                    while (isActive) {
                        sendSensorDataToServer(
                            workerId = 1L,
                            heartRate = heartRateBpm,
                            speed = speed,
                            steps = steps,
                            pace = pace,
                            stepPerMinute = stepPerMinute
                        )
                        delay(5000)
                    }
                }
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

                sensorJob?.cancelAndJoin()
                sensorJob = null

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