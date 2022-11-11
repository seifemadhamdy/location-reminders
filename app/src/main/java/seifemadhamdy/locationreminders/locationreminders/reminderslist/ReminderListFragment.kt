package seifemadhamdy.locationreminders.locationreminders.reminderslist

import android.os.Bundle
import android.view.*
import androidx.databinding.DataBindingUtil
import org.koin.androidx.viewmodel.ext.android.viewModel
import seifemadhamdy.locationreminders.R
import seifemadhamdy.locationreminders.base.BaseFragment
import seifemadhamdy.locationreminders.base.NavigationCommand
import seifemadhamdy.locationreminders.databinding.FragmentReminderListBinding
import seifemadhamdy.locationreminders.utils.setDisplayHomeAsUpEnabled
import seifemadhamdy.locationreminders.utils.setTitle
import seifemadhamdy.locationreminders.utils.setup

class ReminderListFragment : BaseFragment() {

    // Use Koin to retrieve the ViewModel instance
    override val viewModel: ReminderListViewModel by viewModel()
    private lateinit var binding: FragmentReminderListBinding

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

        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(false)
        setTitle(getString(R.string.app_name))

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

    private fun navigateToAddReminder() {
        // Use the navigationCommand live data to navigate between the fragments
        viewModel.navigationCommand.postValue(
            NavigationCommand.To(
                ReminderListFragmentDirections.toSaveReminder()
            )
        )
    }

    private fun setupRecyclerView() {
        val adapter = ReminderListAdapter {}

        // Setup the recycler view using the extension function
        binding.reminderListRecyclerView.setup(adapter)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.logout -> {
                // TODO: Add the logout implementation
            }
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        // Display logout as menu item
        inflater.inflate(R.menu.main_menu, menu)
    }
}