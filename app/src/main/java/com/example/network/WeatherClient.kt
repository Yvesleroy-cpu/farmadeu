package com.example.network

import android.util.Log
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class OpenMeteoResponse(
    @Json(name = "latitude") val latitude: Double,
    @Json(name = "longitude") val longitude: Double,
    @Json(name = "daily") val daily: DailyData?
)

@JsonClass(generateAdapter = true)
data class DailyData(
    @Json(name = "time") val time: List<String>?,
    @Json(name = "temperature_2m_max") val tempMax: List<Double>?,
    @Json(name = "temperature_2m_min") val tempMin: List<Double>?,
    @Json(name = "precipitation_sum") val precipitationSum: List<Double>?
)

interface OpenMeteoApiService {
    @GET("v1/forecast")
    suspend fun getDailyForecast(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("daily") daily: String = "temperature_2m_max,temperature_2m_min,precipitation_sum",
        @Query("timezone") timezone: String = "auto"
    ): OpenMeteoResponse
}

object WeatherClient {
    private const val BASE_URL = "https://api.open-meteo.com/"

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    private val service: OpenMeteoApiService by lazy {
        retrofit.create(OpenMeteoApiService::class.java)
    }

    /**
     * Structure describing the resolved weather report including alerts
     */
    data class FarmWeatherReport(
        val cityName: String,
        val latitude: Double,
        val longitude: Double,
        val currentTemp: Double,
        val minTemp: Double,
        val maxTemp: Double,
        val prepSum: Double,
        val skyDescription: String,
        val isGelAlert: Boolean,
        val isPluieAlert: Boolean,
        val advice: String,
        val isMock: Boolean = false
    )

    /**
     * Map of standard agricultural cities / regions with their coordinate configurations
     */
    data class AgriculturalRegion(
        val city: String,
        val regionName: String,
        val latitude: Double,
        val longitude: Double,
        val climateDescription: String
    )

    val regions = listOf(
        AgriculturalRegion("Valence", "Vallée du Rhône (Arboriculture / Gel tardif)", 44.93, 4.89, "Climat semi-continental chaud, très sensible au gel de printemps pour les vergers."),
        AgriculturalRegion("Reims", "Champagne (Vignobles / Risque de gel)", 49.25, 4.03, "Climat océanique dégradé, fort historique de gelures printanières sur bourgeons."),
        AgriculturalRegion("Brest", "Finistère (Légumes de plein champ / Fortes pluies)", 48.39, -4.48, "Climat hyper-océanique humide, sujet à de grandes hauteurs d'eau risquant la pourriture."),
        AgriculturalRegion("Toulouse", "Haute-Garonne (Grandes cultures / Tempéré)", 43.60, 1.44, "Climat de transition, propice aux céréales et oléagineux mais sensible aux pluies intenses."),
        AgriculturalRegion("Lyon", "Haut-Rhône (Polyculture)", 45.76, 4.83, "Climat mixte, risques modérés d'orages de grêle d'été ou frais matinal printanier."),
        AgriculturalRegion("Meknès", "Maroc Centre (Arboriculture & Vignes)", 33.89, -5.55, "Climat méditerranéen contrasté, gelées d'altitude en hiver et sécheresses estivales."),
        AgriculturalRegion("Dakar", "Sénégal Sahel (Maraîchage & Élevage)", 14.69, -17.44, "Climat sahélien sous influence océanique, risques d'orages torrentiels hivernaux.")
    )

    /**
     * Safely query the OpenMeteo API or fallback to simulated data on network failures.
     * Also integrates a custom simulation query override to allow users to force test certain alert states.
     */
    suspend fun getFarmWeather(
        city: String,
        latitude: Double,
        longitude: Double,
        overrideSimulation: String? = null // null, "GEL", "PLUIE", "SOLEIL"
    ): FarmWeatherReport {
        if (overrideSimulation != null) {
            return generateMockReport(city, latitude, longitude, overrideSimulation)
        }

        try {
            val response = service.getDailyForecast(latitude, longitude)
            val daily = response.daily
            if (daily != null && !daily.time.isNullOrEmpty()) {
                // Read first day's forecast
                val minT = daily.tempMin?.firstOrNull() ?: 15.0
                val maxT = daily.tempMax?.firstOrNull() ?: 22.0
                val prep = daily.precipitationSum?.firstOrNull() ?: 0.0
                val avgTemp = (minT + maxT) / 2.0

                val (isGel, isPluie, sky, advice) = computeWeatherAlertsAndAdvice(minT, prep)

                return FarmWeatherReport(
                    cityName = city,
                    latitude = latitude,
                    longitude = longitude,
                    currentTemp = avgTemp,
                    minTemp = minT,
                    maxTemp = maxT,
                    prepSum = prep,
                    skyDescription = sky,
                    isGelAlert = isGel,
                    isPluieAlert = isPluie,
                    advice = advice,
                    isMock = false
                )
            }
        } catch (e: Exception) {
            Log.e("WeatherClient", "Failed to query OpenMeteo API, using fallback generator", e)
        }

        // Return a realistic mock-up report as a graceful fallback if offline
        return generateMockReport(city, latitude, longitude, null)
    }

    private fun computeWeatherAlertsAndAdvice(minTemp: Double, prepSum: Double): Quadruple {
        val isGel = minTemp <= 2.0
        val isPluie = prepSum >= 12.0

        val sky = when {
            isPluie && minTemp < 5.0 -> "Pluie glacée intense"
            isPluie -> "Averses orageuses"
            minTemp <= 0.0 -> "Gelée matinale / Ciel clair"
            minTemp <= 3.0 -> "Gelée blanche suspectée"
            prepSum > 2.0 -> "Averses légères"
            else -> "Ciel dégagé / Ensoleillé"
        }

        val advice = when {
            isGel && isPluie -> {
                "🚨 RISQUE EXTRÊME DE GEL ET INONDATION DES SEMIS ! Abritez immédiatement sous tunnel plastique, suspendez les semis extérieurs et évacuez l'eau des caniveaux."
            }
            isGel -> {
                "⚠️ ALERTE GELÉE (Min: $minTemp°C) ! Paillez généreusement les pieds de vos parcelles au stade Semis/Floraison. Mettez en marche le chauffage de verger si disponible, ou paillez sous tunnel."
            }
            isPluie -> {
                "⚠️ ALERTE PLUIES INTENSES ($prepSum mm) ! Attention à la stagnation d'eau stagnante sur vos cultures maraîchères (risque d'asphyxie racinaire). Isolez les jeunes agneaux/poussins."
            }
            else -> {
                "☀️ Conditions optimales constatées. Profitez de cette journée pour mener les arrosages ciblés au pied et le buttage de vos légumes sensibles."
            }
        }

        return Quadruple(isGel, isPluie, sky, advice)
    }

    private fun generateMockReport(
        city: String,
        latitude: Double,
        longitude: Double,
        simulationMode: String?
    ): FarmWeatherReport {
        val minTemp: Double
        val maxTemp: Double
        val prep: Double

        when (simulationMode) {
            "GEL" -> {
                minTemp = -1.5
                maxTemp = 8.0
                prep = 0.0
            }
            "PLUIE" -> {
                minTemp = 11.0
                maxTemp = 16.5
                prep = 24.5
            }
            "SOLEIL" -> {
                minTemp = 16.0
                maxTemp = 28.0
                prep = 0.0
            }
            else -> {
                // Generate a realistic local default depending on context time or city
                if (city.lowercase() == "dakar") {
                    minTemp = 22.0
                    maxTemp = 31.0
                    prep = 0.0
                } else if (city.lowercase() == "brest") {
                    minTemp = 9.5
                    maxTemp = 15.0
                    prep = 6.2
                } else if (city.lowercase() == "valence" || city.lowercase() == "reims") {
                    // Late autumn/spring spring frost simulation
                    minTemp = 1.0
                    maxTemp = 14.0
                    prep = 0.0
                } else {
                    minTemp = 12.0
                    maxTemp = 23.0
                    prep = 0.5
                }
            }
        }

        val avgTemp = (minTemp + maxTemp) / 2.0
        val (isGel, isPluie, sky, advice) = computeWeatherAlertsAndAdvice(minTemp, prep)

        return FarmWeatherReport(
            cityName = city,
            latitude = latitude,
            longitude = longitude,
            currentTemp = avgTemp,
            minTemp = minTemp,
            maxTemp = maxTemp,
            prepSum = prep,
            skyDescription = sky,
            isGelAlert = isGel,
            isPluieAlert = isPluie,
            advice = advice,
            isMock = true
        )
    }

    private data class Quadruple(
        val first: Boolean,
        val second: Boolean,
        val third: String,
        val fourth: String
    )
}
