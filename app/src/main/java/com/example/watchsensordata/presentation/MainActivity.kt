/* While this template provides a good starting point for using Wear Compose, you can always
 * take a look at https://github.com/android/wear-os-samples/tree/main/ComposeStarter to find the
 * most up to date changes to the libraries and their usages.
 */

package com.example.watchsensordata.presentation

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import android.widget.Button
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.health.services.client.ExerciseClient
import androidx.health.services.client.HealthServices
import androidx.health.services.client.ExerciseUpdateCallback
import androidx.health.services.client.data.*
import androidx.health.services.client.endExercise
import androidx.health.services.client.startExercise
import androidx.lifecycle.lifecycleScope
import com.example.watchsensordata.R
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var exerciseClient: ExerciseClient
    private lateinit var updateCallback: ExerciseUpdateCallback

    // 위치 권한 런타임 요청 등록
    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startExercise()
        } else {
            Log.e("PERMISSION", "위치 권한이 필요합니다.")
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", packageName, null)
            }
            startActivity(intent)
        }
    }

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        lifecycleScope.launch {
            // ExerciseClient 초기화
            exerciseClient = HealthServices.getClient(this@MainActivity).exerciseClient
            // 콜백 등록
            updateCallback = object : ExerciseUpdateCallback {
                override fun onAvailabilityChanged(dataType: DataType<*, *>, availability: Availability) {}

                override fun onExerciseUpdateReceived(update: ExerciseUpdate) {
                    //심박수
                    val heartRateData = update.latestMetrics.getData(DataType.HEART_RATE_BPM)
                    if(heartRateData.isNotEmpty()){
                        val bpm = heartRateData.last().value
                        Log.d("SENSOR","Heart Rate: $bpm")
                    }

                    //분당 걸음수
                    val stepPerMinute = update.latestMetrics.getData(DataType.STEPS_PER_MINUTE)
                    if(stepPerMinute.isNotEmpty()){
                        val stepMinute = stepPerMinute.last().value
                        Log.d("SENSOR", "Step per minutes: $stepMinute")
                    }

                    // 시간당 거리
                    val speedData = update.latestMetrics.getData(DataType.SPEED)
                    if(speedData.isNotEmpty()){
                        val speed = speedData.last().value
                        Log.d("SENSOR", "Speed: $speed")
                    }

                    // 걸음수
                    val stepsData = update.latestMetrics.getData(DataType.STEPS)
                    if(stepsData.isNotEmpty()){
                        val step = stepsData.last().value
                        Log.d("SENSOR", "Steps: $step")
                    }

                    // 1km 당 걸리는 시간
                    val paceData = update.latestMetrics.getData(DataType.PACE)
                    if(paceData.isNotEmpty()){
                        val pace = paceData.last().value
                        Log.d("SENSOR", "Pace: $pace")
                    }

                    //GPS 위치 정보
                    val locationData = update.latestMetrics.getData(DataType.LOCATION)
                    if(locationData.isNotEmpty()) {
                        val location = locationData.last().value
                        val latitude = location.latitude
                        val longitude = location.longitude
                        Log.d("SENSOR", "Location: ($latitude, $longitude)")
                    }
                }

                override fun onLapSummaryReceived(lapSummary: ExerciseLapSummary) {  }
                override fun onRegistered() {}
                override fun onRegistrationFailed(throwable: Throwable) {}
            }

            exerciseClient.setUpdateCallback(updateCallback)
        }

        findViewById<Button>(R.id.startButton).setOnClickListener {
            checkLocationPermissionAndStartExercise()
        }

        findViewById<Button>(R.id.stopButton).setOnClickListener {
            stopExercise()
        }
    }

    // 위치 권한 확인 + 없으면 요청, 있으면 운동 시작
    private fun checkLocationPermissionAndStartExercise() {
        val permission = Manifest.permission.ACCESS_FINE_LOCATION
        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
            startExercise()
        } else {
            locationPermissionLauncher.launch(permission)
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
                    DataType.LOCATION
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
}