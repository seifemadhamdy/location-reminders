package seifemadhamdy.locationreminders.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import seifemadhamdy.locationreminders.locationreminders.data.dto.ReminderDTO
import seifemadhamdy.locationreminders.locationreminders.data.dto.Result

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
// Medium Test to test the repository
@MediumTest
class RemindersLocalRepositoryTest {

    // Add testing implementation to the RemindersLocalRepository.kt

    @get: Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var remindersLocalRepository: RemindersLocalRepository

    private lateinit var remindersDatabase: RemindersDatabase

    @Before
    fun init() {
        // Using an in-memory database so that the information stored here disappears when the
        // process is killed.

        remindersDatabase = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        ).allowMainThreadQueries().build()

        remindersLocalRepository = RemindersLocalRepository(
            remindersDatabase.reminderDao(),
            Dispatchers.Main
        )
    }

    @After
    fun closeDb() = remindersDatabase.close()

    @Test
    fun saveReminder_getReminderByID() =
        runBlocking {
            val reminder = ReminderDTO(
                "Buy a New Backpack",
                "Make sure that the backpack's color is black.",
                "Cairo Festival City Mall",
                30.028007880132908,
                31.40725133604065
            )

            remindersLocalRepository.saveReminder(reminder)
            val result = remindersLocalRepository.getReminder(reminder.id) as? Result.Success
            assertThat(result is Result.Success, `is`(true))
            result as Result.Success
            assertThat(result.data.title, `is`(reminder.title))
            assertThat(result.data.description, `is`(reminder.description))
            assertThat(result.data.latitude, `is`(reminder.latitude))
            assertThat(result.data.longitude, `is`(reminder.longitude))
            assertThat(result.data.location, `is`(reminder.location))
        }

    @Test
    fun deleteReminders_emptyList() = runBlocking {
        remindersLocalRepository.saveReminder(
            ReminderDTO(
                "Buy a New Backpack",
                "Make sure that the backpack's color is black.",
                "Cairo Festival City Mall",
                30.028007880132908,
                31.40725133604065
            )
        )

        remindersLocalRepository.deleteAllReminders()
        val result = remindersLocalRepository.getReminders()
        assertThat(result is Result.Success, `is`(true))
        result as Result.Success
        assertThat(result.data, `is`(emptyList()))
    }

    @Test
    fun getReminderById_returnError() = runBlocking {
        val reminder = ReminderDTO(
            "Buy a New Backpack",
            "Make sure that the backpack's color is black.",
            "Cairo Festival City Mall",
            30.028007880132908,
            31.40725133604065
        )

        remindersLocalRepository.saveReminder(reminder)
        remindersLocalRepository.deleteAllReminders()
        val result = remindersLocalRepository.getReminder(reminder.id)
        assertThat(result is Result.Error, `is`(true))
        result as Result.Error
        assertThat(result.message, `is`("Reminder not found!"))
    }
}