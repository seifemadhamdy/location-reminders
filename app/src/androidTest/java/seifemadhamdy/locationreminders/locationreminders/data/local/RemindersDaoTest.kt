package seifemadhamdy.locationreminders.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import seifemadhamdy.locationreminders.locationreminders.data.dto.ReminderDTO

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
// Unit test the DAO
@SmallTest
class RemindersDaoTest {

    // Add testing implementation to the RemindersDao.kt

    @get: Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var remindersDatabase: RemindersDatabase

    @Before
    fun initDb() {
        // Using an in-memory database so that the information stored here disappears when the
        // process is killed.
        remindersDatabase = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        ).allowMainThreadQueries().build()
    }

    @After
    fun closeDb() = remindersDatabase.close()

    @Test
    fun saveReminder_GetById() =
        @Suppress("DEPRECATION")
        runBlockingTest {
            val reminder = ReminderDTO(
                "Buy a New Backpack",
                "Make sure that the backpack's color is black.",
                "Cairo Festival City Mall",
                30.028007880132908,
                31.40725133604065
            )

            remindersDatabase.reminderDao().saveReminder(reminder)
            val result = remindersDatabase.reminderDao().getReminderById(reminder.id)

            assertThat(result as ReminderDTO, CoreMatchers.notNullValue())
            assertThat(result.id, `is`(reminder.id))
            assertThat(result.title, `is`(reminder.title))
            assertThat(result.description, `is`(reminder.description))
            assertThat(result.location, `is`(reminder.location))
            assertThat(result.latitude, `is`(reminder.latitude))
            assertThat(result.longitude, `is`(reminder.longitude))
        }

    @Test
    @Suppress("DEPRECATION")
    fun getAllRemindersFromDb() = runBlockingTest {
        remindersDatabase.apply {
            reminderDao().saveReminder(
                ReminderDTO(
                    "Go Bouncing",
                    "Make sure to have fun at Gravity Code!",
                    "Gravity Code",
                    30.028029547857983,
                    31.495506926699523
                )
            )

            reminderDao().saveReminder(
                ReminderDTO(
                    "Go to The Cinema",
                    "Make sure to enjoy your movie at VOX!",
                    "VOX Cinemas - City Centre Almaza",
                    30.088039436513313,
                    31.3630053833782
                )
            )

            reminderDao().saveReminder(
                ReminderDTO(
                    "Go See The Pyramids",
                    "Don't forget to take some photos!",
                    "Giza Necropolis",
                    29.977412961162287,
                    31.13281787722262
                )
            )
        }

        val remindersList = remindersDatabase.reminderDao().getReminders()
        assertThat(remindersList, `is`(CoreMatchers.notNullValue()))
    }

    @Test
    @Suppress("DEPRECATION")
    fun insertReminders_deleteAllReminders() = runBlockingTest {
        remindersDatabase.apply {
            reminderDao().saveReminder(
                ReminderDTO(
                    "Go Bouncing",
                    "Make sure to have fun at Gravity Code!",
                    "Gravity Code",
                    30.028029547857983,
                    31.495506926699523
                )
            )

            reminderDao().saveReminder(
                ReminderDTO(
                    "Go to The Cinema",
                    "Make sure to enjoy your movie at VOX!",
                    "VOX Cinemas - City Centre Almaza",
                    30.088039436513313,
                    31.3630053833782
                )
            )

            reminderDao().saveReminder(
                ReminderDTO(
                    "Go See The Pyramids",
                    "Don't forget to take some photos!",
                    "Giza Necropolis",
                    29.977412961162287,
                    31.13281787722262
                )
            )
        }

        remindersDatabase.reminderDao().deleteAllReminders()
        val remindersList = remindersDatabase.reminderDao().getReminders()
        assertThat(remindersList, `is`(emptyList()))
    }
}