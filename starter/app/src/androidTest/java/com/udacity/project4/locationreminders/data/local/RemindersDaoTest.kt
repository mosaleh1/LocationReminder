package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;
import com.udacity.project4.locationreminders.data.dto.ReminderDTO

import org.junit.Before;
import org.junit.Rule;
import org.junit.runner.RunWith;

import kotlinx.coroutines.ExperimentalCoroutinesApi;
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Test

@ExperimentalCoroutinesApi
//Unit test the DAO
@SmallTest
@RunWith(AndroidJUnit4::class)
class RemindersDaoTest {
    // DONE    TODO: Add testing implementation to the RemindersDao.kt
    private lateinit var remindersDB: RemindersDatabase


    @Before
    fun initializeDatabase() {
        remindersDB = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        )
            .allowMainThreadQueries()
            .build()
    }

    @Test
    fun getReminders() {
        runBlocking {
            //Given
            val reminder = ReminderDTO(
                "Buy a book",
                "Cooking book",
                "Cairo",
                30.1, 31.0
            )
            //When
            remindersDB.reminderDao().saveReminder(
                reminder
            )

            //Then
            val reminders = remindersDB.reminderDao().getReminders()
            assertThat(reminders.size, `is`(1))

            val reminderLoaded = reminders[0]
            assertThat(reminderLoaded.id, `is`(reminder.id))
        }
    }

    @After
    fun closeDatabase() {
        remindersDB.close()
    }


}