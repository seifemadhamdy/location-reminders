package seifemadhamdy.locationreminders

import android.app.Application
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.get
import seifemadhamdy.locationreminders.locationreminders.data.ReminderDataSource
import seifemadhamdy.locationreminders.locationreminders.data.local.LocalDB
import seifemadhamdy.locationreminders.locationreminders.data.local.RemindersLocalRepository
import seifemadhamdy.locationreminders.locationreminders.reminderslist.ReminderListViewModel
import seifemadhamdy.locationreminders.locationreminders.savereminder.SaveReminderViewModel
import seifemadhamdy.locationreminders.util.AutoCloseKoinTest

@RunWith(AndroidJUnit4::class)
@LargeTest
// END TO END test to black box test the app
class RemindersActivityTest :
    AutoCloseKoinTest() {
    // Extended Koin Test - embed autoclose @after method to close Koin after every test

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

    // TODO: Add End to End testing to the app
}
