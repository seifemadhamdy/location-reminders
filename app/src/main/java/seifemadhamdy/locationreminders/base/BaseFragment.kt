package seifemadhamdy.locationreminders.base

import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar

/**
 * Base Fragment to observe on the common LiveData objects
 */
abstract class BaseFragment : Fragment() {

    /**
     * Every fragment has to have an instance of a view model that extends from the BaseViewModel
     */
    abstract val viewModel: BaseViewModel

    override fun onStart() {
        super.onStart()

        viewModel.apply {
            showErrorMessage.observe(this@BaseFragment) {
                Toast.makeText(activity, it, Toast.LENGTH_LONG).show()
            }

            showToast.observe(this@BaseFragment) {
                Toast.makeText(activity, it, Toast.LENGTH_LONG).show()
            }

            showSnackBar.observe(this@BaseFragment) {
                Snackbar.make(this@BaseFragment.requireView(), it, Snackbar.LENGTH_LONG).show()
            }

            showSnackBarInt.observe(this@BaseFragment) {
                Snackbar.make(this@BaseFragment.requireView(), getString(it), Snackbar.LENGTH_LONG)
                    .show()
            }

            navigationCommand.observe(this@BaseFragment) { navigationCommand ->
                when (navigationCommand) {
                    is NavigationCommand.To -> findNavController().navigate(navigationCommand.directions)
                    is NavigationCommand.Back -> findNavController().popBackStack()
                    is NavigationCommand.BackTo -> findNavController().popBackStack(
                        navigationCommand.destinationId,
                        false
                    )
                }
            }
        }
    }
}