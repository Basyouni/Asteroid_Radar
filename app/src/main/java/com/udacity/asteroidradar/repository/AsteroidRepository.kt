package com.udacity.asteroidradar.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.udacity.asteroidradar.Asteroid
import com.udacity.asteroidradar.Constants
import com.udacity.asteroidradar.Constants.API_KEY
import com.udacity.asteroidradar.Constants.API_QUERY_DATE_FORMAT
import com.udacity.asteroidradar.Constants.DEFAULT_END_DATE_DAYS
import com.udacity.asteroidradar.PictureOfDay
import com.udacity.asteroidradar.api.AsteroidApi
import com.udacity.asteroidradar.api.asDatabaseModel
import com.udacity.asteroidradar.api.parseAsteroidsJsonResult
import com.udacity.asteroidradar.database.AsteroidsDatabase
import com.udacity.asteroidradar.database.asDomainModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*

class AsteroidRepository(private val database: AsteroidsDatabase) {

    private val today = getStartDayFormatted()

    val todayAsteroids: LiveData<List<Asteroid>> =
        Transformations.map(database.asteroidDao.getTodayAsteroids()) {
            it.asDomainModel()
        }
    val weeklyAsteroids: LiveData<List<Asteroid>> =
        Transformations.map(database.asteroidDao.getWeeklyAsteroids()) {
            it.asDomainModel()
        }
    val asteroids: LiveData<List<Asteroid>> =
        Transformations.map(database.asteroidDao.getAsteroidsSaved()) {
            it.asDomainModel()
        }
    val pictureOfDay: LiveData<PictureOfDay> = Transformations.map(
        database.asteroidDao.getPictureOfDay()
    ) {
        it?.asDomainModel()
    }

    suspend fun refreshAsteroid() {
        withContext(Dispatchers.IO) {

            try {
                //get json response by ScalarsConverterFactory Retrofit
                val stringResponse = AsteroidApi.retrofitScalarService.getAsteroidProperties(
                    getStartDayFormatted() , getEndDayFormatted(), API_KEY)
                //get JSONObject Array  by ScalarsConverterFactory Retrofit
                val asteroidList = parseAsteroidsJsonResult(JSONObject(stringResponse))
                // convert them to array of DatabaseAsteroids and insert all
                database.asteroidDao.insertAll(*asteroidList.asDatabaseModel())
            } catch (e: Exception) {
                Timber.i("I got exception when try to get Response asteroids: $e")
            }
        }
    }

    suspend fun refreshPictureOfDay() {
        withContext(Dispatchers.IO) {
            try {
                val pictureOfDay = AsteroidApi.retrofitMoshiService.getPictureOfDay(
                    API_KEY
                )
                database.asteroidDao.insertPictureOfDay(pictureOfDay.asDatabaseModel())
            } catch (e: Exception) {
                e.message
            }
        }
    }


    private fun getStartDayFormatted(): String {
        val calendar = Calendar.getInstance()
        val currentTime = calendar.time
        val dateFormat = SimpleDateFormat(Constants.API_QUERY_DATE_FORMAT, Locale.getDefault())
        return dateFormat.format(currentTime)
    }

    private fun getEndDayFormatted(): String {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, DEFAULT_END_DATE_DAYS)
        val currentTime = calendar.time
        val dateFormat = SimpleDateFormat(Constants.API_QUERY_DATE_FORMAT, Locale.getDefault())
        return dateFormat.format(currentTime)
    }

}