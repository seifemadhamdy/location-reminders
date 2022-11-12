package seifemadhamdy.locationreminders.reminderslist

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.pauseDispatcher
import kotlinx.coroutines.test.resumeDispatcher
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import seifemadhamdy.locationreminders.MainCoroutineRule
import seifemadhamdy.locationreminders.data.FakeDataSource
import seifemadhamdy.locationreminders.getOrAwaitValue
import seifemadhamdy.locationreminders.locationreminders.data.dto.ReminderDTO
import seifemadhamdy.locationreminders.locationreminders.reminderslist.ReminderListViewModel

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class RemindersListViewModelTest {

    // Provide testing to the RemindersListViewModel and its live data objects

    @get: Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @get: Rule
    var mainCoroutineRule = MainCoroutineRule()

    private lateinit var fakeDataSource: FakeDataSource

    // Subject under testing
    private lateinit var viewModel: ReminderListViewModel

    @Before
    fun setupViewModel() {
        fakeDataSource = FakeDataSource()
        viewModel =
            ReminderListViewModel(ApplicationProvider.getApplicationContext(), fakeDataSource)
    }

    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun loadReminders_showLoading() {
        @Suppress("DEPRECATION")
        mainCoroutineRule.pauseDispatcher()
        viewModel.loadReminders()
        assertThat(viewModel.showLoading.getOrAwaitValue()).isTrue()
        @Suppress("DEPRECATION")
        mainCoroutineRule.resumeDispatcher()
        assertThat(viewModel.showLoading.getOrAwaitValue()).isFalse()
    }

    @Test
    fun loadReminders_remainderListNotEmpty(): Unit =
        @Suppress("DEPRECATION")
        mainCoroutineRule.runBlockingTest {
            fakeDataSource.saveReminder(
                ReminderDTO(
                    "Buy a New Backpack",
                    "Make sure that the backpack's color is black.",
                    "Cairo Festival City Mall",
                    30.028007880132908,
                    31.40725133604065
                )
            )
            viewModel.loadReminders()
            assertThat(viewModel.remindersList.getOrAwaitValue()).isNotEmpty()
        }

    @Test
    fun loadReminders_updateSnackBarValue() {
        @Suppress("DEPRECATION")
        mainCoroutineRule.pauseDispatcher()
        fakeDataSource.setReturnError(true)
        viewModel.loadReminders()
        @Suppress("DEPRECATION")
        mainCoroutineRule.resumeDispatcher()
        assertThat(viewModel.showSnackBar.getOrAwaitValue()).isEqualTo("Error getting reminders.")
    }
}