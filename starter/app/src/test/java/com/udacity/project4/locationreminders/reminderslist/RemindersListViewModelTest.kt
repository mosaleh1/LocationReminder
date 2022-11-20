package com.udacity.project4.locationreminders.reminderslist

import android.os.Build
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
@Config(sdk = [Build.VERSION_CODES.P])
class RemindersListViewModelTest {

    //DONE TODO: provide testing to the RemindersListViewModel and its live data objects

    private lateinit var dataSource: FakeDataSource

    private lateinit var viewModel: RemindersListViewModel


    @Before
    fun init() {
        dataSource = FakeDataSource()
        viewModel = RemindersListViewModel(
            ApplicationProvider.getApplicationContext(),
            dataSource
        )
        stopKoin()
        val module = module {
            single {
                viewModel
            }
        }
        startKoin {
            modules(listOf(module))
        }
    }


    @Test
    fun getReminders() {
        runBlockingTest{
            val reminderDemo1 =
                ReminderDTO(
                    "Title 1 for Test", "Description 1 for Test",
                    "Cairo Test", 30.1, 30.1
                )
            val reminderDemo2 =
                ReminderDTO(
                    "Title 2 for Test", "Description 2 for Test",
                    "Aswan", 31.1, 32.0
                )

            dataSource.saveReminder(reminderDemo1)
            dataSource.saveReminder(reminderDemo2)

            viewModel.loadReminders()

            val count = viewModel.remindersList.value?.size
            Assert.assertEquals(count, 2)
        }
    }


}