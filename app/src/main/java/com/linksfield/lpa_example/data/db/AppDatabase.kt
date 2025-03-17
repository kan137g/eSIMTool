package com.linksfield.lpa_example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.linksfield.lpad.utils.DATABASE_NAME

/**
 * CreateDate: 2020/8/12 18:22
 * Author: you
 * Description:
 */
@Database(entities = [BleDevice::class, WifiDevice::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun bleDeviceDao(): BleDeviceDao

    abstract fun wifiDeviceDao(): WifiDeviceDao

    companion object {

        // For Singleton instantiation
        @Volatile
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: buildDatabase(context).also { instance = it }
            }
        }

        private fun buildDatabase(context: Context): AppDatabase {
            return Room.databaseBuilder(context, AppDatabase::class.java, DATABASE_NAME)
                    .allowMainThreadQueries()
                    .addMigrations(migration)
                    .build()
        }

        private val migration: Migration = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
//                database.execSQL("CREATE TABLE WifiDevice (" +
//                        "id LONG PRIMARY KEY NOT NULL," + "ip TEXT)");
            }
        }
    }
}