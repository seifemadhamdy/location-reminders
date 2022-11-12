package seifemadhamdy.locationreminders.locationreminders.reminderslist

import android.app.Application
import android.os.Bundle
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.get
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import seifemadhamdy.locationreminders.R
import seifemadhamdy.locationreminders.locationreminders.data.ReminderDataSource
import seifemadhamdy.locationreminders.locationreminders.data.dto.ReminderDTO
import seifemadhamdy.locationreminders.locationreminders.data.local.LocalDB
import seifemadhamdy.locationreminders.locationreminders.data.local.RemindersLocalRepository
import seifemadhamdy.locationreminders.locationreminders.savereminder.SaveReminderViewModel
import seifemadhamdy.locationreminders.util.AutoCloseKoinTest

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
// UI Testing
@MediumTest
class ReminderListFragmentTest : AutoCloseKoinTest() {

    @get: Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var reminderDataSource: ReminderDataSource
    private lateinit var application: Application

    /**
     * As we use Koin as a Service Locator Library to develop our code, we'll also use Koin to test our code.
     * at this step we will initialize Koin related code to be able to use it in out testing.
     */
    @Before
    fun init() {
        // Stop the original app koin
        stopKoin()
        application = ApplicationProvider.getApplicationContext()

        val myModule = module {
            viewModel {
                ReminderListViewModel(
                    application,
                    get() as ReminderDataSource
                )
            }

            single {
                SaveReminderViewModel(
                    application,
                    get() as ReminderDataSource
                )
            }

            single<ReminderDataSource> { RemindersLocalRepository(get()) }
            single { LocalDB.createRemindersDao(application) }
        }

        // Declare a new koin module
        startKoin {
            modules(listOf(myModule))
        }

        // Get our real repository
        reminderDataSource = get()

        // Clear the data to start fresh
        runBlocking {
            reminderDataSource.deleteAllReminders()
        }
    }

    @Test
    fun clickFAB_navigateToSaveReminderFragment() {
        // Test the navigation of the fragments

        val scenario = launchFragmentInContainer<ReminderListFragment>(
            Bundle(),
            R.style.Theme_LocationReminders
        )

        val navController = mock(NavController::class.java)

        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }

        onView(withId(R.id.add_new_reminder_floating_action_button)).perform(click())

        verify(navController).navigate(
            ReminderListFragmentDirections.toSaveReminder()
        )
    }

    @Test
    fun reminderList_DisplayedInUI(): Unit = runBlocking {
        // Test the displayed data on the UI.

        val reminder = ReminderDTO(
            "Buy a New Backpack",
            "Make sure that the backpack's color is black.",
            "Cairo Festival City Mall",
            30.028007880132908,
            31.40725133604065
        )

        reminderDataSource.saveReminder(reminder)
        launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.Theme_LocationReminders)

        onView(withId(R.id.reminder_list_recycler_view))
            .perform(
                RecyclerViewActions.scrollTo<RecyclerView.ViewHolder>(
                    hasDescendant(withText(reminder.title))
                )
            )
    }

    @Test
    fun onUI_noDataDisplayed(): Unit = runBlocking {
        // Add testing for the error messages (when there is no data)

        reminderDataSource.saveReminder(
            ReminderDTO(
                "Buy a New Backpack",
                "Make sure that the backpack's color is black.",
                "Cairo Festival City Mall",
                30.028007880132908,
                31.40725133604065
            )
        )

        reminderDataSource.deleteAllReminders()
        launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.Theme_LocationReminders)
        onView(withId(R.id.no_reminders_constraint_layout)).check(ViewAssertions.matches(isDisplayed()))
    }
}