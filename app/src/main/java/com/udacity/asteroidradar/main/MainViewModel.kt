package com.udacity.asteroidradar.main

import android.app.Application
import androidx.lifecycle.*
import com.udacity.asteroidradar.Asteroid
import com.udacity.asteroidradar.database.getDatabase
import com.udacity.asteroidradar.repository.AsteroidRepository
import kotlinx.coroutines.launch
import timber.log.Timber

enum class AsteroidApiStatus { LOADING, ERROR, DONE }
class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val database = getDatabase(application)
    private val asteroidsRepository = AsteroidRepository(database)

    // The internal MutableLiveData String that stores the status of the most recent request
    private val _status = MutableLiveData<AsteroidApiStatus>()

    // The external immutable LiveData for the request status String
    val status: LiveData<AsteroidApiStatus>
        get() = _status

    //to observe navigation to detail asteroid
    private val _navigateToDetail = MutableLiveData<Asteroid>()
    val navigateToDetail: LiveData<Asteroid>
        get() = _navigateToDetail

    val pictureOfDay = asteroidsRepository.pictureOfDay

    private val _filterSelected = MutableLiveData(AsteroidsApiFilter.SHOW_SAVED)
    val filterSelected: LiveData<AsteroidsApiFilter>
        get() = _filterSelected

    val asteroids = Transformations.switchMap(_filterSelected) {
        when (it!!) {
            AsteroidsApiFilter.SHOW_WEEK -> asteroidsRepository.weeklyAsteroids
            AsteroidsApiFilter.SHOW_DAY -> asteroidsRepository.todayAsteroids
            else -> asteroidsRepository.asteroids
        }
    }

    /**
     * call parseAsteroidsJsonResult() on init so we can display status immediately
     */

    init {
        getAsteroidsProperties()
    }

    private fun getAsteroidsProperties() {
        viewModelScope.launch {
            _status.value = AsteroidApiStatus.LOADING
            try {

                asteroidsRepository.refreshPictureOfDay()
                _status.value = AsteroidApiStatus.DONE
            } catch (e: Exception) {
                Timber.i("I got Exception ${e.message} ")
                _status.value = AsteroidApiStatus.ERROR
            }

        }
        viewModelScope.launch {
            try {
                asteroidsRepository.refreshAsteroid()
            } catch (e: Exception) {
                Timber.i("I got Exception ${e.message} ")
            }


        }
    }


    /**
     * Sets the value of the status LiveData to the Asteroid API status
     */

    fun displayPropertyDetails(asteroid: Asteroid) {
        _navigateToDetail.value = asteroid
    }

    fun doneNavigation() {
        _navigateToDetail.value = null
    }

    fun updateFilter(filter: AsteroidsApiFilter) {
        _filterSelected.value = filter
    }


}