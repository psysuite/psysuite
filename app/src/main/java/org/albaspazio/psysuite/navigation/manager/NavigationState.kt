package org.albaspazio.psysuite.navigation.manager

import android.os.Bundle

/**
 * Represents the state of navigation at a point in time.
 *
 * Used for saving and restoring navigation state during Fragment recreation.
 */
data class NavigationState(
    val currentNodeLabel: String,
    val navigationPath: List<String>,
    val timestamp: Long = System.currentTimeMillis()
) {
    /**
     * Converts this state to a Bundle for Fragment state preservation.
     *
     * @return Bundle representation of this state
     */
    fun toBundle(): Bundle {
        val bundle = Bundle()
        bundle.putString("current_node_label", currentNodeLabel)
        bundle.putStringArrayList("navigation_path", ArrayList(navigationPath))
        bundle.putLong("timestamp", timestamp)
        return bundle
    }

    companion object {
        /**
         * Creates a NavigationState from a Bundle.
         *
         * @param bundle The bundle containing the state
         * @return NavigationState, or null if the bundle doesn't contain valid state
         */
        fun fromBundle(bundle: Bundle?): NavigationState? {
            if (bundle == null) return null

            val currentNodeLabel = bundle.getString("current_node_label") ?: return null
            val navigationPath = bundle.getStringArrayList("navigation_path") ?: emptyList()
            val timestamp = bundle.getLong("timestamp", System.currentTimeMillis())

            return NavigationState(currentNodeLabel, navigationPath, timestamp)
        }

        /**
         * Creates a NavigationState from a NavigationManager.
         *
         * @param manager The NavigationManager to capture state from
         * @return NavigationState representing the current navigation state
         */
        fun fromNavigationManager(manager: TestsNavigationManager): NavigationState {
            val currentNode = manager.getCurrentNode()
            val history = manager.getNavigationHistory()
            val navigationPath = history.map { it.label }

            return NavigationState(
                currentNodeLabel = currentNode.label,
                navigationPath = navigationPath
            )
        }
    }
}
