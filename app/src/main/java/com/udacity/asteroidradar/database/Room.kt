package com.udacity.asteroidradar.database

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface AsteroidDao {
    @Query("select * from databasea_steroid_table WHERE closeApproachDate = date('now') order by closeApproachDate asc")
    fun getTodayAsteroids(): LiveData<List<DatabaseAsteroids>>

    @Query("SELECT * FROM databasea_steroid_table WHERE closeApproachDate BETWEEN date('now') AND date('now', '+7 day') order by closeApproachDate asc")
    fun getWeeklyAsteroids(): LiveData<List<DatabaseAsteroids>>

    @Query("select * from databasea_steroid_table  order by closeApproachDate asc")
    fun getAsteroidsSaved(): LiveData<List<DatabaseAsteroids>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg asteroid: DatabaseAsteroids)

    @Query("select * from databasea_picture_table order by created_at desc limit 1")
    fun getPictureOfDay(): LiveData<DatabasePicture>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertPictureOfDay(pictureOfDay: DatabasePicture)
}

@Database(entities = [DatabaseAsteroids::class, DatabasePicture::class], version = 1, exportSchema = false)
abstract class AsteroidsDatabase : RoomDatabase() {
    abstract val asteroidDao: AsteroidDao
}

private lateinit var INSTANCE: AsteroidsDatabase

fun getDatabase(context: Context): AsteroidsDatabase {
    synchronized(AsteroidsDatabase::class.java) {
        if (!::INSTANCE.isInitialized) {
            INSTANCE = Room.databaseBuilder(
                context.applicationContext,
                AsteroidsDatabase::class.java,
                "asteroids")
                .fallbackToDestructiveMigration()
                .build()
        }
    }
    return INSTANCE
}
