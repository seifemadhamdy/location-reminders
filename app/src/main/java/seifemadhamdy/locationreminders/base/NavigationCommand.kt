package seifemadhamdy.locationreminders.base

import androidx.navigation.NavDirections

/**
 * Sealed class used with the live data to navigate between the fragments
 */
sealed class NavigationCommand {
    /**
     * Navigate to a direction
     */
    data class To(val directions: NavDirections) : NavigationCommand()

    /**
     * Navigate back to the previous fragment
     */
    object Back : NavigationCommand()

    /**
     * Navigate back to a destination in the back stack
     */
    data class BackTo(val destinationId: Int) : NavigationCommand()
}