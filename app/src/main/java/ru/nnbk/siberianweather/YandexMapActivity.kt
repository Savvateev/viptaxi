package ru.nnbk.siberianweather

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.yandex.mapkit.Animation
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.IconStyle
import com.yandex.mapkit.map.PlacemarkMapObject
import com.yandex.mapkit.mapview.MapView
import com.yandex.runtime.image.ImageProvider
import kotlinx.coroutines.launch
import ru.nnbk.siberianweather.databinding.ActivityYandexMapBinding

class YandexMapActivity : AppCompatActivity() {

    private lateinit var mapView: MapView
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var userPlacemark: PlacemarkMapObject? = null

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            showUserLocation()
        } else {
            Toast.makeText(this, "Разрешение на геолокацию отклонено", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Initialize MapKit before setContentView
        MapKitFactory.setApiKey("218804d1-2df9-4356-941b-a1da6fbae09f")
        MapKitFactory.initialize(this)
        var status = intent.getStringExtra("PORT_STATUS")
        val binding = ActivityYandexMapBinding.inflate(layoutInflater)
        enableEdgeToEdge()

        if (status == "R") {
            binding.orderButton.text = getString(R.string.orderTaxiText)
        } else {
            binding.orderButton.text = getString(R.string.unOrderTaxiText)
            binding.text.text = getString(R.string.searchDriverText)
        }

        setContentView(binding.root)
        mapView = findViewById(R.id.mapview)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        checkLocationPermissionAndShowLocation()

        // выставляем текст

        binding.orderButton.setOnClickListener {
            lifecycleScope.launch {
                if (status == "R") {
                    val confirmed = showConfirmDialog("Подтвердить заказ")
                    if (confirmed) {
                        sshCommand("interface disable ether2")
                        binding.orderButton.text = getString(R.string.unOrderTaxiText)
                        binding.text.text = getString(R.string.searchDriverText)
                        status = "X"
                    }

                } else {
                    val confirmed = showConfirmDialog("Отменить заказ")
                    if (confirmed) {
                        sshCommand("interface enaable ether2")
                        binding.orderButton.text = getString(R.string.orderTaxiText)
                        binding.text.text = ""
                        status = "R"
                    }
                }
            }
        }
    }


    override fun onStart() {
        super.onStart()
        MapKitFactory.getInstance().onStart()
        mapView.onStart()
    }

    override fun onStop() {
        mapView.onStop()
        MapKitFactory.getInstance().onStop()
        super.onStop()
    }

    private fun checkLocationPermissionAndShowLocation() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            try {
                showUserLocation()
            } catch (e: SecurityException) {
                Toast.makeText(this, "Нет разрешения на доступ к геолокации", Toast.LENGTH_SHORT)
                    .show()
            }
        } else {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private fun showUserLocation() {
        try {
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                if (location != null) {
                    val userPoint = Point(location.latitude, location.longitude)
                    mapView.map.move(
                        CameraPosition(userPoint, 15.0f, 0.0f, 0.0f),
                        Animation(Animation.Type.SMOOTH, 1f),
                        null
                    )
                    if (userPlacemark == null) {
                        userPlacemark = mapView.map.mapObjects.addPlacemark(userPoint)
                        userPlacemark?.setIcon(
                            ImageProvider.fromResource(
                                this,
                                R.drawable.ic_user_marker
                            )
                        )
                        userPlacemark?.setIconStyle (
                            IconStyle().apply {
                                scale = 0.1f  // уменьшение размера до 50%
                            }
                        )
                    } else {
                        userPlacemark?.geometry = userPoint
                    }
                } else {
                    Toast.makeText(
                        this,
                        "Не удалось получить текущее местоположение",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        } catch (e: SecurityException) {
            Toast.makeText(
                this,
                "Ошибка безопасности: нет разрешения на геолокацию",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}