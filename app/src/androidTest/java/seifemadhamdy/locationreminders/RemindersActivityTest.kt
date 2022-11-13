package seifemadhamdy.locationreminders

import android.app.Activity
import android.app.Application
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.closeSoftKeyboard
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.RootMatchers.withDecorView
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.not
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.get
import seifemadhamdy.locationreminders.locationreminders.RemindersActivity
import seifemadhamdy.locationreminders.locationreminders.data.ReminderDataSource
import seifemadhamdy.locationreminders.locationreminders.data.local.LocalDB
import seifemadhamdy.locationreminders.locationreminders.data.local.RemindersLocalRepository
import seifemadhamdy.locationreminders.locationreminders.reminderslist.ReminderListViewModel
import seifemadhamdy.locationreminders.locationreminders.savereminder.SaveReminderViewModel
import seifemadhamdy.locationreminders.util.AutoCloseKoinTest
import seifemadhamdy.locationreminders.util.DataBindingIdlingResource
import seifemadhamdy.locationreminders.util.monitorActivity
import seifemadhamdy.locationreminders.utils.EspressoIdlingResource

@RunWith(AndroidJUnit4::class)
@LargeTest
// END TO END test to black box test the app
class RemindersActivityTest :
    AutoCloseKoinTest() {

    // Extended Koin Test - embed autoclose @after method to close Koin after every test

    private lateinit var reminderDataSource: ReminderDataSource
    private lateinit var application: Application

    // An idling resource that waits for Data Binding to have no pending bindings.
    private val dataBindingIdlingResource = DataBindingIdlingResource()

    /**
     * As we use Koin as a Service Locator Library to develop our code, we'll also use Koin to test our code.
     * at this step we will initialize Koin related code to be able to use it in out testing.
     */
    @Before
    fun init() {
        // Stop the original app koin
        stopKoin()
        application = getApplicationContext()
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

    // Add End to End testing to the app

    /**
     * Idling resources tell Espresso that the app is idle or busy. This is needed when operations
     * are not scheduled in the main Looper (for example when executed on a different thread).
     */
    @Before
    fun registerIdlingResource() {
        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().register(dataBindingIdlingResource)
    }

    /**
     * Unregister your Idling Resource so it can be garbage collected and does not leak any memory.
     */
    @After
    fun unregisterIdlingResource() {
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().unregister(dataBindingIdlingResource)
    }

    @Test
    fun saveReminderFragment_showSnackBarNoTitleError() {
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)
        onView(withId(R.id.add_new_reminder_floating_action_button)).perform(ViewActions.click())
        onView(withId(R.id.save_reminder_floating_action_button)).perform(ViewActions.click())
        val snackBarMessage = application.getString(R.string.err_enter_title)
        onView(withText(snackBarMessage)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        activityScenario.close()
    }

    @Test
    fun saveReminderFragment_showSnackBarNoLocationSelectedError() {
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)
        onView(withId(R.id.add_new_reminder_floating_action_button)).perform(ViewActions.click())
        onView(withId(R.id.title_text_input_edit_text)).perform(ViewActions.typeText("Title"))
        closeSoftKeyboard()
        onView(withId(R.id.save_reminder_floating_action_button)).perform(ViewActions.click())
        val snackBarMessage = application.getString(R.string.err_select_location)
        onView(withText(snackBarMessage)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        activityScenario.close()
    }

    @Test
    fun saveReminderFragment_showToastMessage() {
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)
        onView(withId(R.id.add_new_reminder_floating_action_button)).perform(ViewActions.click())
        onView(withId(R.id.title_text_input_edit_text)).perform(ViewActions.typeText("Title"))
        closeSoftKeyboard()
        onView(withId(R.id.description_text_input_edit_text)).perform(ViewActions.typeText("Description"))
        closeSoftKeyboard()
        onView(withId(R.id.locate_material_button)).perform(ViewActions.click())
        onView(withId(R.id.map_fragment_container_view)).perform(ViewActions.longClick())
        onView(withId(R.id.done_floating_action_button)).perform(ViewActions.click())
        onView(withId(R.id.save_reminder_floating_action_button)).perform(ViewActions.click())

        onView(withText(R.string.reminder_saved)).inRoot(
            withDecorView(
                not(
                    `is`(
                        getActivity(
                            activityScenario
                        ).window.decorView
                    )
                )
            )
        ).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))

        activityScenario.close()
    }

    private fun getActivity(activityScenario: ActivityScenario<RemindersActivity>): Activity {
        lateinit var activity: Activity

        activityScenario.onActivity {
            activity = it
        }

        return activity
    }
}