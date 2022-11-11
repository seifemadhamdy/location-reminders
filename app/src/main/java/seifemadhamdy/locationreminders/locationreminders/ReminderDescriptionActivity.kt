package seifemadhamdy.locationreminders.locationreminders

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import seifemadhamdy.locationreminders.R
import seifemadhamdy.locationreminders.databinding.ActivityReminderDescriptionBinding
import seifemadhamdy.locationreminders.locationreminders.reminderslist.ReminderDataItem

/**
 * Activity that displays the reminder details after the user clicks on the notification
 */
class ReminderDescriptionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityReminderDescriptionBinding

    companion object {
        private const val EXTRA_ReminderDataItem = "EXTRA_ReminderDataItem"

        // Receive the reminder object after the user clicks on the notification
        fun newIntent(context: Context, reminderDataItem: ReminderDataItem): Intent {
            val intent = Intent(context, ReminderDescriptionActivity::class.java)
            intent.putExtra(EXTRA_ReminderDataItem, reminderDataItem)
            return intent
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(
            this,
            R.layout.activity_reminder_description
        )

        // The implementation of the reminder details
        binding.item = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.extras?.getSerializable(
                EXTRA_ReminderDataItem,
                ReminderDataItem::class.java
            ) as ReminderDataItem
        } else {
            @Suppress("DEPRECATION")
            intent.extras?.get(EXTRA_ReminderDataItem) as ReminderDataItem
        }

        binding.lifecycleOwner = this

        // Close activity when done
        binding.doneFloatingActionButton.setOnClickListener {
            finish()
        }
    }
}