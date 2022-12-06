package com.udacity.project4.locationreminders.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Medium Test to test the repository
@MediumTest
class RemindersLocalRepositoryTest {

//  DONE   TODO: Add testing implementation to the RemindersLocalRepository.kt

    private lateinit var reminderDB: RemindersDatabase
    private var repo: RemindersLocalRepository? = null

    @Before
    fun init() {
        reminderDB = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        ).build()
        repo = RemindersLocalRepository(
            reminderDB.reminderDao()
        )
    }


    @Test
    fun saveAndRetrieveReminderTest() {
        //Given
        runBlocking {
            val reminder = ReminderDTO(
                "Buy a book",
                "Cooking book",
                "Cairo",
                30.1, 31.0
            )
            repo?.saveReminder(reminder)

            //When
            val resultState = repo?.getReminder(reminder.id)

            //Then
            assertThat(resultState is Result.Success, `is`(true))

            (resultState as Result.Success).let {
                assertThat(it.data.title, `is`(reminder.title))
                assertThat(it.data.description, `is`(reminder.description))
                assertThat(it.data.latitude, `is`(reminder.latitude))
                assertThat(it.data.longitude, `is`(reminder.longitude))
                assertThat(it.data.location, `is`(reminder.location))
            }
        }
    }

    @Test
    fun saveAndRetrieveRemindersTest() {
        //Given
        runBlocking {
            val reminder = ReminderDTO(
                "1 Buy  a book",
                "Cooking book",
                "Cairo",
                30.1, 31.0
            )
            val reminder2 = ReminderDTO(
                "2 Buy a  book",
                "Cooking book",
                "Cairo",
                30.1, 31.0
            )
            val reminder3 = ReminderDTO(
                "3 Buy a book",
                "Cooking book",
                "Cairo",
                30.1, 31.0
            )
            repo?.saveReminder(reminder)
            repo?.saveReminder(reminder2)
            repo?.saveReminder(reminder3)

            //When
            val reminders = reminderDB.reminderDao().getReminders()

            //Then
            assertThat(reminders.size, `is`(3))
        }
    }

    @After
    fun cleanUp() {
        reminderDB.close()
        repo = null
    }

}