package seifemadhamdy.locationreminders.locationreminders.reminderslist

import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.core.view.MenuProvider
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Lifecycle
import com.firebase.ui.auth.AuthUI
import org.koin.androidx.viewmodel.ext.android.viewModel
import seifemadhamdy.locationreminders.R
import seifemadhamdy.locationreminders.authentication.AuthenticationActivity
import seifemadhamdy.locationreminders.base.BaseFragment
import seifemadhamdy.locationreminders.base.NavigationCommand
import seifemadhamdy.locationreminders.databinding.FragmentReminderListBinding
import seifemadhamdy.locationreminders.utils.setDisplayHomeAsUpEnabled
import seifemadhamdy.locationreminders.utils.setTitle
import seifemadhamdy.locationreminders.utils.setup

class ReminderListFragment : BaseFragment() {

    private lateinit var binding: FragmentReminderListBinding

    // Use Koin to retrieve the ViewModel instance
    override val viewModel: ReminderListViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(
                inflater,
                R.layout.fragment_reminder_list, container, false
            )

        binding.viewModel = viewModel

        setDisplayHomeAsUpEnabled(false)
        setTitle(getString(R.string.app_name))

        requireActivity().apply {
            addMenuProvider(object : MenuProvider {
                override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                    // Display logout as menu item
                    menuInflater.inflate(R.menu.main_menu, menu)
                }

                override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                    if (menuItem.itemId == R.id.logout) {
                        // Logout current user
                        AuthUI.getInstance().signOut(requireContext()).addOnSuccessListener {
                            // Navigate to AuthenticationActivity
                            navigateToAuthenticationActivity()
                        }
                    }

                    return true
                }
            }, viewLifecycleOwner, Lifecycle.State.RESUMED)

            viewModel.showLoading.observe(this) {
                // End refreshing animation if not loading
                if (!it)
                    binding.reminderListSwipeRefreshLayout.isRefreshing = false
            }
        }

        binding.reminderListSwipeRefreshLayout.setOnRefreshListener {
            viewModel.loadReminders()
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = this
        setupRecyclerView()

        binding.addNewReminderFloatingActionButton.setOnClickListener {
            navigateToAddReminder()
        }
    }

    override fun onResume() {
        super.onResume()
        // Load the reminders list on the ui
        viewModel.loadReminders()
    }

    private fun navigateToAuthenticationActivity() {
        Intent(context, AuthenticationActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(this)
            activity?.finish()
        }
    }

    private fun setupRecyclerView() {
        val adapter = ReminderListAdapter {}

        // Setup the recycler view using the extension function
        binding.reminderListRecyclerView.setup(adapter)
    }

    private fun navigateToAddReminder() {
        // Use the navigationCommand live data to navigate between the fragments
        viewModel.navigationCommand.postValue(
            NavigationCommand.To(
                ReminderListFragmentDirections.toSaveReminder()
            )
        )
    }
}