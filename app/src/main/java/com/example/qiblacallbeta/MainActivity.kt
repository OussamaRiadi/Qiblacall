package com.example.qiblacallbeta

import android.Manifest
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.squareup.moshi.Json
import kotlin.math.tan

class MainActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var sensorManager: SensorManager
    private lateinit var qiblaArrow: ImageView
    private lateinit var prayerTimeTextView: TextView
    private lateinit var prayerNameTextView: TextView

    private var userLatitude: Double = 0.0
    private var userLongitude: Double = 0.0
    private var gravity: FloatArray? = null
    private var geomagnetic: FloatArray? = null

    interface PrayerTimeApi {
        @GET("v1/timings")
        suspend fun getPrayerTimes(
            @Query("latitude") latitude: Double,
            @Query("longitude") longitude: Double,
            @Query("method") method: Int = 2
        ): PrayerTimeResponse
    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val httpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .build()

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api.aladhan.com/")
        .client(httpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    private val prayerTimeApi = retrofit.create(PrayerTimeApi::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        qiblaArrow = findViewById(R.id.qiblaCompass)
        prayerTimeTextView = findViewById(R.id.prayerTime)
        prayerNameTextView = findViewById(R.id.prayerName)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
        }

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager

        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            location?.let {
                userLatitude = it.latitude
                userLongitude = it.longitude
                getPrayerTimes(userLatitude, userLongitude)
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun getPrayerTimes(latitude: Double, longitude: Double) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = prayerTimeApi.getPrayerTimes(latitude, longitude)
                Log.d("PrayerTimesResponse", response.toString())
                val prayerTimes = response.data.timings

                // Prepare a list for RecyclerView
                val prayerTimeItems = listOf(
                    PrayerTimeItem("Fajr", prayerTimes.fajr),
                    PrayerTimeItem("Dhuhr", prayerTimes.dhuhr),
                    PrayerTimeItem("Asr", prayerTimes.asr),
                    PrayerTimeItem("Maghrib", prayerTimes.maghrib),
                    PrayerTimeItem("Isha", prayerTimes.isha)
                )

                withContext(Dispatchers.Main) {
                    // Initialize RecyclerView Adapter
                    val adapter = PrayerTimesAdapter(prayerTimeItems)
                    val recyclerView: RecyclerView = findViewById(R.id.prayerTimesRecyclerView)
                    recyclerView.layoutManager = LinearLayoutManager(this@MainActivity)
                    recyclerView.adapter = adapter
                }
            } catch (e: Exception) {
                Log.e("PrayerTimeError", "Error fetching prayer times: ${e.localizedMessage}")
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "Failed to load prayer times. ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return

        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            gravity = event.values
        } else if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
            geomagnetic = event.values
        }

        if (gravity != null && geomagnetic != null) {
            val r = FloatArray(9)
            val i = FloatArray(9)
            if (SensorManager.getRotationMatrix(r, i, gravity, geomagnetic)) {
                val orientation = FloatArray(3)
                SensorManager.getOrientation(r, orientation)
                val azimuth = Math.toDegrees(orientation[0].toDouble()).toFloat()

                val qiblaDirection = calculateQiblaDirection(userLatitude, userLongitude)

                val correctedAzimuth = (azimuth + 180) % 360
                qiblaArrow.rotation = (correctedAzimuth - qiblaDirection).toFloat()
            }
        }
    }

    private fun calculateQiblaDirection(userLat: Double, userLong: Double): Double {
        // Coordinates for the Kaaba in Mecca
        val kaabaLat = 21.4225
        val kaabaLong = 39.8262

        // Convert latitude and longitude from degrees to radians
        val userLatRad = Math.toRadians(userLat)
        val userLongRad = Math.toRadians(userLong)
        val kaabaLatRad = Math.toRadians(kaabaLat)
        val kaabaLongRad = Math.toRadians(kaabaLong)

        // Calculate the Qibla direction
        val deltaLong = kaabaLongRad - userLongRad
        val y = sin(deltaLong)
        val x = cos(userLatRad) * tan(kaabaLatRad) - sin(userLatRad) * cos(deltaLong)

        val bearing = atan2(y, x)
        val bearingDeg = Math.toDegrees(bearing)

        // Normalize bearing angle to 0-360 degrees
        return (bearingDeg + 260) % 360
    }    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onResume() {
        super.onResume()
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_UI)
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), SensorManager.SENSOR_DELAY_UI)
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    data class PrayerTimeResponse(val data: PrayerTimesData)
    data class PrayerTimesData(val timings: PrayerTimings)
    data class PrayerTimings(
        @Json(name = "Fajr") val fajr: String,
        @Json(name = "Dhuhr") val dhuhr: String,
        @Json(name = "Asr") val asr: String,
        @Json(name = "Maghrib") val maghrib: String,
        @Json(name = "Isha") val isha: String
    )
}
