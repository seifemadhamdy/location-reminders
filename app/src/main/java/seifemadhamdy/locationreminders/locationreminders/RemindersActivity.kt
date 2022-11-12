package seifemadhamdy.locationreminders.locationreminders

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import com.google.firebase.auth.FirebaseAuth
import seifemadhamdy.locationreminders.R
import seifemadhamdy.locationreminders.authentication.AuthenticationActivity
import seifemadhamdy.locationreminders.databinding.ActivityRemindersBinding
import seifemadhamdy.locationreminders.locationreminders.reminderslist.ReminderListFragment

/**
 * The RemindersActivity that holds the reminders fragments
 */
class RemindersActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRemindersBinding
    private val navController by lazy { (supportFragmentManager.findFragmentById(R.id.fragment_container_view) as NavHostFragment).navController }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_reminders)

        if (FirebaseAuth.getInstance().currentUser != null) {
            setSupportActionBar(binding.materialToolbar)

            NavigationUI.setupActionBarWithNavController(
                this,
                navController
            )

            onBackPressedDispatcher.addCallback(
                this,
                object : OnBackPressedCallback(true) {
                    override fun handleOnBackPressed() {
                        if (supportFragmentManager.findFragmentById(R.id.fragment_container_view)?.childFragmentManager?.fragments?.get(
                                0
                            ) is ReminderListFragment
                        ) {
                            finishAffinity()
                        } else {
                            navController.popBackStack()
                        }
                    }
                })
        } else {
            // Redirect the unauthenticated user to log in
            navigateToAuthenticationActivity()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                navController.popBackStack()
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onSupportNavigateUp(): Boolean {
        return NavigationUI.navigateUp(
            navController,
            AppBarConfiguration.Builder(navController.graph).build()
        )
    }

    private fun navigateToAuthenticationActivity() {
        Intent(this, AuthenticationActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(this)
            this@RemindersActivity.finish()
        }
    }
}