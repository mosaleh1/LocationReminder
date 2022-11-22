package com.udacity.project4

import android.app.Application
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.*
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.withDecorView
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.rule.GrantPermissionRule
import com.udacity.project4.locationreminders.RemindersActivity
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.util.DataBindingIdlingResource
import com.udacity.project4.util.atPosition
import com.udacity.project4.util.monitorActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.koin.test.get
import org.mockito.AdditionalMatchers.not

@RunWith(AndroidJUnit4::class)
@LargeTest
//END TO END test to black box test the app
class RemindersActivityTest :
    AutoCloseKoinTest() {// Extended Koin Test - embed autoclose @after method to close Koin after every test

    private lateinit var repository: ReminderDataSource
    private lateinit var appContext: Application
    private val binding = DataBindingIdlingResource()

    @get:Rule
    val permissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        android.Manifest.permission.ACCESS_FINE_LOCATION
    )

    @get:Rule
    var activityScenarioRule = ActivityScenarioRule(RemindersActivity::class.java)

    /**
     * As we use Koin as a Service Locator Library to develop our code, we'll also use Koin to test our code.
     * at this step we will initialize Koin related code to be able to use it in out testing.
     */
    @Before
    fun init() {
        stopKoin()//stop the original app koin
        appContext = getApplicationContext()
        val myModule = module {
            viewModel {
                RemindersListViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            single {
                SaveReminderViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            single { RemindersLocalRepository(get()) as ReminderDataSource }
            single { LocalDB.createRemindersDao(appContext) }
        }
        //declare a new koin module
        startKoin {
            modules(listOf(myModule))
        }
        //Get our real repository
        repository = get()

        //clear the data to start fresh
        runBlocking {
            repository.deleteAllReminders()
        }

    }


    @Before
    fun registerIdling() {
        IdlingRegistry.getInstance().register(binding)
    }

    @After
    fun unregisterIdlingResources() {
        IdlingRegistry.getInstance().unregister(binding)
        stopKoin()
    }

//    DONE TODO: add End to End testing to the app


    @Test
    fun addNewReminderScenarioTest() = runBlocking {
        //Given  When   Then
        val reminderActivity = ActivityScenario.launch(RemindersActivity::class.java)
        binding.monitorActivity(reminderActivity)



        onView(withId(R.id.addReminderFAB)).perform(click())

        //add title
        val title = "Title Test"
        onView(withId(R.id.reminderTitle)).perform(typeText(title))

        //add discretion
        onView(withId(R.id.reminderDescription)).perform(typeText("description Test"))

        onView(withId(R.id.selectLocation)).perform(click())


        delay(2000)
        onView(withId(R.id.map)).perform(longClick())

        onView(withId(R.id.confirm_location_btn)).perform(click())

        onView(withId(R.id.saveReminder)).perform(click())

        onView(withId(R.id.refreshLayout)).perform(swipeDown())

        runBlocking {
            delay(2000)
        }
        onView(withText(title)).check(
            matches(isDisplayed())
        )

//        onView(withId(R.id.reminderssRecyclerView)).check(
//            matches(
//                atPosition(
//                    0,
//                    withText("Testing title"),
//                    R.id.title
//                )
//            )
//        )
        reminderActivity.close()
    }

    @Test
    fun testSnackbarAndToast() {
        val reminderActivity = ActivityScenario.launch(RemindersActivity::class.java)
        binding.monitorActivity(reminderActivity)

        onView(withId(R.id.addReminderFAB)).perform(click())
        onView(withId(R.id.saveReminder)).perform(click())

        onView(
            withText(
                getApplicationContext<MyApp>()
                    .getString(R.string.err_enter_title)
            )
        ).check(
            matches(
                isDisplayed()
            )
        )

        reminderActivity.close()

    }
}
