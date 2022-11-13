package seifemadhamdy.locationreminders.savereminder

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.pauseDispatcher
import kotlinx.coroutines.test.resumeDispatcher
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import seifemadhamdy.locationreminders.MainCoroutineRule
import seifemadhamdy.locationreminders.R
import seifemadhamdy.locationreminders.data.FakeDataSource
import seifemadhamdy.locationreminders.getOrAwaitValue
import seifemadhamdy.locationreminders.locationreminders.reminderslist.ReminderDataItem
import seifemadhamdy.locationreminders.locationreminders.savereminder.SaveReminderViewModel

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class SaveReminderViewModelTest {

    // Provide testing to the SaveReminderView and its live data objects

    @get: Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @get: Rule
    var mainCoroutineRule = MainCoroutineRule()

    private lateinit var fakeDataSource: FakeDataSource

    // Subject under testing
    private lateinit var saveReminderViewModel: SaveReminderViewModel

    @Before
    fun build() {
        fakeDataSource = FakeDataSource()
        saveReminderViewModel =
            SaveReminderViewModel(ApplicationProvider.getApplicationContext(), fakeDataSource)
    }

    @After
    fun destroy() {
        stopKoin()
    }

    @Test
    fun validateEnteredData_noTitleAndUpdateSnackBar() {
        assertThat(
            saveReminderViewModel.validateEnteredData(
                ReminderDataItem(
                    "",
                    "Have fun with your friends!",
                    "Dream Park",
                    29.964819406474014,
                    31.057827696723606
                )
            )
        ).isFalse()

        assertThat(saveReminderViewModel.showSnackBarInt.getOrAwaitValue()).isEqualTo(R.string.err_enter_title)
    }

    @Test
    fun validateEnteredData_noLocationSelectedAndUpdateSnackBar() {
        assertThat(
            saveReminderViewModel.validateEnteredData(
                ReminderDataItem(
                    "Don't Go There",
                    "I warned you!",
                    saveReminderViewModel.getReminderSelectedLocationStrDefaultText(),
                    25.000932808612767,
                    -71.00004296378529
                )
            )
        ).isFalse()

        assertThat(saveReminderViewModel.showSnackBarInt.getOrAwaitValue()).isEqualTo(R.string.err_select_location)
    }

    @Test
    fun saveReminder_loadingStatus() {
        @Suppress("DEPRECATION")
        mainCoroutineRule.pauseDispatcher()

        saveReminderViewModel.saveReminder(
            ReminderDataItem(
                "Go to The Airport",
                "Remember to say goodbyes before the lift off!",
                "Cairo International Airport",
                30.119439905400746,
                31.401135164293063
            )
        )

        assertThat(saveReminderViewModel.showLoading.getOrAwaitValue()).isTrue()
        @Suppress("DEPRECATION")
        mainCoroutineRule.resumeDispatcher()
        assertThat(saveReminderViewModel.showLoading.getOrAwaitValue()).isFalse()
    }
}