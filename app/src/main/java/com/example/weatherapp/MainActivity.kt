package com.example.weatherapp

import android.nfc.Tag
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.SearchView
import android.widget.Toast
import com.example.weatherapp.databinding.ActivityMainBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

//cb58c4a4ebab2715c0b016ff5919c75a
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)

        fetchWeatherData("Delhi")
        searchCity()
    }

    private fun searchCity() {
        binding.searchbar.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (query != null) {
                    fetchWeatherData(query)
                    binding.searchbar.setQuery("", false)
                    binding.searchbar.clearFocus()
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return true
            }

        })
    }

    private fun fetchWeatherData(cityname : String) {
        val retrofit = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl("https://api.openweathermap.org/data/2.5/")
            .build().create(ApiInterface::class.java)

        val response = retrofit.getWeatherData(cityname, "cb58c4a4ebab2715c0b016ff5919c75a", "metric")
        response.enqueue(object : Callback<weatherApp>{
            override fun onResponse(call: Call<weatherApp>, response: Response<weatherApp>) {
                val responsebody = response.body()
                if (response.isSuccessful && responsebody != null){
                    val temperature = responsebody.main.temp.toString()
                    val humidity = responsebody.main.humidity
                    val windSpeed = responsebody.wind.speed
                    val sunrise = responsebody.sys.sunrise.toLong()
                    val sunset = responsebody.sys.sunset.toLong()
                    val sealevel = responsebody.main.pressure
                    val condition = responsebody.weather.firstOrNull()?.main?:"unknown"
                    val maxtemp = responsebody.main.temp_max
                    val mintemp = responsebody.main.temp_min
                    binding.temperatureC.text = "$temperature °C"
                    binding.day.text = condition
                    binding.maxTemp.text = "MAX $maxtemp °C"
                    binding.minTemp.text = "MIN $mintemp °C"
                    binding.humidity.text = "$humidity %"
                    binding.windSpeed.text = "$windSpeed m/s"
                    binding.sunriseTime.text = "${time(sunrise)}"
                    binding.sunsetTime.text = "${time(sunset)}"
                    binding.seaLevel.text = "$sealevel hPa"
                    binding.weatherCondition.text = condition
                    binding.weekday.text = dayName(System.currentTimeMillis())
                    binding.date.text = date()
                    binding.cityname.text = "$cityname"
                    //Log.d("TAG", "onResponse: $temperature")

                   changeImage(condition)
                }

                else{
                    Toast.makeText(this@MainActivity, "This city Name does not exist", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<weatherApp>, t: Throwable) {
                TODO("Not yet implemented")
            }

        })

        }


    private fun changeImage(conditions : String) {
        when(conditions){
            "Clear Sky", "Sunny", "Clear" -> {
                binding.root.setBackgroundResource(R.drawable.sunny_background)
                binding.anim.setAnimation(R.raw.sun)
            }

            "Haze", "Partly Clouds", "Clouds", "Overcast", "Mist", "Foggy" -> {
                binding.root.setBackgroundResource(R.drawable.colud_background)
                binding.anim.setAnimation(R.raw.cloud)
            }

            "Rain", "Light Rain", "Drizzle", "Moderate Rain", " Heavy Rain", "Showers" -> {
                binding.root.setBackgroundResource(R.drawable.rain_background)
                binding.anim.setAnimation(R.raw.rain)
            }

            "Light Snow", "Heavy Snow", "Moderate Snow", "Blizzard" -> {
                binding.root.setBackgroundResource(R.drawable.snow_background)
                binding.anim.setAnimation(R.raw.snow)
            }
            else -> {
                binding.root.setBackgroundResource(R.drawable.sunny_background)
                binding.anim.setAnimation(R.raw.sun)
            }
        }
        binding.anim.playAnimation()
    }

    private fun date(): String {
        val stf = SimpleDateFormat("dd MMMM YYYY", Locale.getDefault())
        return stf.format(Date())

    }

    fun dayName(timestamp : Long) : String{
        val stf = SimpleDateFormat("EEEE", Locale.getDefault())
        return stf.format(Date())
    }

    fun time(timestamp : Long) : String{
        val stf = SimpleDateFormat("HH:mm", Locale.getDefault())
        return stf.format(Date(timestamp * 1000))
    }
}