package seifemadhamdy.locationreminders

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.dsl.module
import seifemadhamdy.locationreminders.locationreminders.data.ReminderDataSource
import seifemadhamdy.locationreminders.locationreminders.data.local.LocalDB
import seifemadhamdy.locationreminders.locationreminders.data.local.RemindersLocalRepository
import seifemadhamdy.locationreminders.locationreminders.reminderslist.ReminderListViewModel
import seifemadhamdy.locationreminders.locationreminders.savereminder.SaveReminderViewModel

class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()

        /**
         * Use Koin Library as a service locator
         */
        val myModule = module {
            // Declare a ViewModel - be later inject into Fragment with dedicated injector using by viewModel()
            viewModel {
                ReminderListViewModel(
                    get(),
                    get() as ReminderDataSource
                )
            }

            // Declare singleton definitions to be later injected using by inject()
            single {
                // This view model is declared singleton to be used across multiple fragments
                SaveReminderViewModel(
                    get(),
                    get() as ReminderDataSource
                )
            }

            single<ReminderDataSource> { RemindersLocalRepository(get()) }
            single { LocalDB.createRemindersDao(this@MyApp) }
        }

        startKoin {
            androidContext(this@MyApp)
            modules(listOf(myModule))
        }
    }
}