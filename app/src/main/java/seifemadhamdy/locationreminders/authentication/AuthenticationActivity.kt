package seifemadhamdy.locationreminders.authentication

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.firebase.ui.auth.AuthMethodPickerLayout
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.google.firebase.auth.FirebaseAuth
import seifemadhamdy.locationreminders.R
import seifemadhamdy.locationreminders.databinding.ActivityAuthenticationBinding
import seifemadhamdy.locationreminders.locationreminders.RemindersActivity

/**
 * This class should be the starting point of the app, It asks the users to sign in / register, and redirects the
 * signed in users to the RemindersActivity.
 */
class AuthenticationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAuthenticationBinding

    private val signInLauncher = registerForActivityResult(
        FirebaseAuthUIActivityResultContract()
    ) { firebaseAuthUIAuthenticationResult ->
        onSignInResult(firebaseAuthUIAuthenticationResult)
    }

    private val tag by lazy { javaClass.simpleName }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthenticationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (FirebaseAuth.getInstance().currentUser != null) {
            // Navigate authenticated user to RemindersActivity
            navigateToRemindersActivity()
        } else {
            binding.loginMaterialButton.setOnClickListener {
                // Launch login UI using Firebase AuthUI
                signInLauncher.launch(
                    AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(
                            // List of login providers
                            arrayListOf(
                                // Email login provider
                                AuthUI.IdpConfig.EmailBuilder().build(),
                                // Google login provider
                                AuthUI.IdpConfig.GoogleBuilder().build()
                            )
                        )
                        // Set a custom layout for the AuthMethodPickerActivity screen
                        .setAuthMethodPickerLayout(
                            AuthMethodPickerLayout.Builder(R.layout.layout_auth_method_picker)
                                // Set the ID of the Email sign in button in the custom layout
                                .setEmailButtonId(R.id.email_material_button)
                                // Set the ID of the Google sign in button in the custom layout
                                .setGoogleButtonId(R.id.google_material_button)
                                .build()
                        )
                        // Set the theme of the login flow to that of the base application instead of the default one
                        .setTheme(R.style.Theme_LocationReminders)
                        .build()
                )
            }
        }
    }

    private fun onSignInResult(firebaseAuthUIAuthenticationResult: FirebaseAuthUIAuthenticationResult) {
        if (firebaseAuthUIAuthenticationResult.resultCode == RESULT_OK) {
            // Successfully signed in

            // Navigate the user to RemindersActivity
            navigateToRemindersActivity()

            // Log that the user's authentication is successful
            Log.i(
                tag,
                "onSignInResult: User ${FirebaseAuth.getInstance().currentUser?.displayName} has successfully signed in."
            )
        } else {
            // Sign in failed.

            // Log the user's failed authentication error code
            Log.i(
                tag,
                "onSignInResult: ${firebaseAuthUIAuthenticationResult.idpResponse?.error?.errorCode}"
            )
        }
    }

    private fun navigateToRemindersActivity() {
        Intent(this, RemindersActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(this)
            finish()
        }
    }
}