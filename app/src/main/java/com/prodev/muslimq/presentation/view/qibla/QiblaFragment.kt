package com.prodev.muslimq.presentation.view.qibla

import android.annotation.SuppressLint
import android.location.Address
import android.location.Geocoder
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.prodev.muslimq.R
import com.prodev.muslimq.core.utils.Compass
import com.prodev.muslimq.core.utils.SOTWFormatter
import com.prodev.muslimq.core.utils.vibrateApp
import com.prodev.muslimq.databinding.FragmentQiblaBinding
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.Locale
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

class QiblaFragment : Fragment() {

    private var _binding: FragmentQiblaBinding? = null
    private val binding get() = _binding!!

    private var userLat = 0.0
    private var userLon = 0.0
    private var currentAzimuth = 0f
    private lateinit var compass: Compass
    private lateinit var sotwFormatter: SOTWFormatter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentQiblaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.ivBack.setOnClickListener { findNavController().popBackStack() }

        userLat = arguments?.getDouble(USER_LATITUDE) ?: 0.0
        userLon = arguments?.getDouble(USER_LONGITUDE) ?: 0.0

        getAddressGeocoder(userLat, userLon)
        setupCompass(userLat)
    }

    @SuppressLint("SetTextI18n")
    private fun getAddressGeocoder(lat: Double, lon: Double) {
        val geocoder = Geocoder(requireContext(), Locale.getDefault())
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            geocoder.getFromLocation(lat, lon, 1, object : Geocoder.GeocodeListener {
                override fun onGeocode(addresses: MutableList<Address>) {
                    if (addresses.isNotEmpty()) {
                        val city = addresses[0].locality
                        val country = addresses[0].countryName
                        binding.tvYourLocation.text = "$city, $country"
                    }
                }

                override fun onError(error: String?) {
                    super.onError(error)
                    Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
                }
            })
        } else {
            try {
                val addresses = geocoder.getFromLocationName("$lat, $lon", 1)
                if (!addresses.isNullOrEmpty()) {
                    val city = addresses[0].locality
                    val country = addresses[0].countryName
                    binding.tvYourLocation.text = "$city, $country"
                } else {
                    Toast.makeText(requireContext(), "Alamat tidak ditemukan", Toast.LENGTH_SHORT)
                        .show()
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun setupCompass(userLat: Double) {
        compass = Compass(requireContext())
        sotwFormatter = SOTWFormatter()
        val compassListener = object : Compass.CompassListener {
            override fun onNewAzimuth(azimuth: Float) {
                viewLifecycleOwner.lifecycleScope.launch {
                    lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                        adjustArrow(azimuth, userLat)
                    }
                }
            }

            override fun onAccuracyChanged(accuracy: Int) {
                // do nothing
            }
        }
        compass.setListener(compassListener)
    }

    @SuppressLint("SetTextI18n")
    private fun adjustArrow(azimuth: Float, userLat: Double) {
        // ka'bah Position https://www.latlong.net/place/kaaba-mecca-saudi-arabia-12639.html
        val kaabaLng = 39.826206
        val kaabaLat = Math.toRadians(21.422487)

        val myLatRad = Math.toRadians(userLat)
        val longDiff = Math.toRadians(kaabaLng - 107.446274)
        val y = sin(longDiff) * cos(kaabaLat)
        val x = cos(myLatRad) * sin(kaabaLat) - sin(myLatRad) * cos(kaabaLat) * cos(longDiff)
        val result = (Math.toDegrees(atan2(y, x)) + 360) % 360
        val sotwNumeric = extractNumericValue(sotwFormatter.format(azimuth))
        val sotwNumericRange = sotwNumeric?.minus(8)!!..sotwNumeric.plus(8)
        val validRange = result.toInt() in sotwNumericRange

        binding.apply {
            tvAzimuth.text = sotwFormatter.format(azimuth)
            ivArrowFalse.isVisible = !validRange
            tvQibla.text = if (validRange) resources.getString(R.string.found_qibla)
            else resources.getString(R.string.find_qibla)

            val vibrator = vibrateApp(requireContext())
            val durationDone = 100L
            if (validRange) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(
                        VibrationEffect.createOneShot(
                            durationDone,
                            VibrationEffect.DEFAULT_AMPLITUDE
                        )
                    )
                } else {
                    @Suppress("DEPRECATION") vibrator.vibrate(durationDone)
                }
            }
        }

        val an: Animation = RotateAnimation(
            -currentAzimuth, -azimuth,
            Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
            0.5f
        )
        currentAzimuth = azimuth
        an.duration = 10
        an.repeatCount = 0
        an.fillAfter = true
        binding.clDirection.startAnimation(an)
    }

    private fun extractNumericValue(inputString: String): Int? {
        val regex = """\d+""".toRegex()
        val matchResult = regex.find(inputString)
        return matchResult?.value?.toIntOrNull()
    }

    override fun onResume() {
        super.onResume()
        compass.start()
    }

    override fun onStop() {
        super.onStop()
        compass.stop()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        compass.stop()
    }

    companion object {
        const val USER_LATITUDE = "user_latitude"
        const val USER_LONGITUDE = "user_longitude"
    }
}