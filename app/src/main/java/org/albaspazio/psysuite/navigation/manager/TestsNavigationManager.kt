package org.albaspazio.psysuite.navigation.manager

import android.os.Bundle
import android.util.Log
import org.albaspazio.psysuite.navigation.config.ConfigurationNode
import java.util.*

/**
 * Manages navigation between menu nodes in the test navigation tree.
 *
 * Maintains a stack of visited nodes to support back navigation.
 * Invariant: The navigation stack is never empty (always contains at least the root node).
 */
class TestsNavigationManager(private val rootNode: ConfigurationNode) {

    companion object {
        private const val TAG = "NavigationManager"
        private const val STATE_STACK_KEY = "navigation_stack"
    }

    private val navigationStack: Stack<ConfigurationNode> = Stack()

    init {
        // Initialize with root node
        navigationStack.push(rootNode)
        verifyInvariant()
    }

    /**
     * Navigates to a child node.
     *
     * @param node The node to navigate to
     * @throws IllegalArgumentException if node is null
     */
    fun navigateTo(node: ConfigurationNode) {
        if (node.type != org.albaspazio.psysuite.navigation.config.NodeType.MENU) {
            throw IllegalArgumentException("Can only navigate to MENU nodes")
        }
        navigationStack.push(node)
        verifyInvariant()
        Log.d(TAG, "Navigated to: ${node.label} (stack size: ${navigationStack.size})")
    }

    /**
     * Goes back to the previous node.
     *
     * If already at the root, does nothing.
     */
    fun goBack() {
        if (navigationStack.size > 1) {
            navigationStack.pop()
            verifyInvariant()
            Log.d(TAG, "Went back to: ${getCurrentNode().label} (stack size: ${navigationStack.size})")
        } else {
            Log.d(TAG, "Already at root, cannot go back")
        }
    }

    /**
     * Gets the current node.
     *
     * @return The node at the top of the stack
     */
    fun  getCurrentNode(): ConfigurationNode {
        verifyInvariant()
        return navigationStack.peek()
    }

    /**
     * Checks if back navigation is possible.
     *
     * @return True if there's a previous node to go back to
     */
    fun canGoBack(): Boolean {
        return navigationStack.size > 1
    }

    /**
     * Gets the navigation history.
     *
     * @return List of nodes in the current navigation path
     */
    fun getNavigationHistory(): List<ConfigurationNode> {
        return navigationStack.toList()
    }

    /**
     * Saves the current navigation state to a Bundle.
     *
     * @return Bundle containing the navigation state
     */
    fun saveState(): Bundle {
        val bundle = Bundle()
        // Note: ConfigurationNode is not Parcelable, so we save the path as labels
        val labels = navigationStack.map { it.label }
        bundle.putStringArrayList(STATE_STACK_KEY, ArrayList(labels))
        return bundle
    }

    /**
     * Restores navigation state from a Bundle.
     *
     * Reconstructs the node tree by navigating through the saved path.
     *
     * @param bundle The bundle containing the saved state
     */
    fun restoreState(bundle: Bundle) {
        val labels = bundle.getStringArrayList(STATE_STACK_KEY)
        if (labels != null && labels.isNotEmpty()) {
            Log.d(TAG, "Restoring navigation state with ${labels.size} nodes")
            
            // Clear the stack except for root
            navigationStack.clear()
            navigationStack.push(rootNode)
            val currentNode = navigationStack.peek()

            // Skip the first label (root) and navigate through the rest
            for (i in 1 until labels.size) {
                val targetLabel = labels[i]
                // Find the child with matching label
                val targetChild = findChildByLabel(currentNode, targetLabel)
                if (targetChild != null) {
                    navigationStack.push(targetChild)
                    Log.d(TAG, "Restored navigation to: $targetLabel")
                } else {
                    Log.w(TAG, "Could not find child with label: $targetLabel, stopping restoration")
                    break
                }
            }
            verifyInvariant()
        }
    }

    /**
     * Finds a child node by label within the children of a given node.
     *
     * @param parentNode The parent node to search in
     * @param targetLabel The label to search for
     * @return The matching child node, or null if not found
     */
    private fun findChildByLabel(parentNode: ConfigurationNode, targetLabel: String): ConfigurationNode? {
        if (parentNode.type != org.albaspazio.psysuite.navigation.config.NodeType.MENU) {
            return null
        }
        
        val children = parentNode.getChildren()
        return children.find { it.label == targetLabel }
    }

    /**
     * Resets navigation to the root node.
     */
    fun reset() {
        navigationStack.clear()
        navigationStack.push(rootNode)
        verifyInvariant()
        Log.d(TAG, "Navigation reset to root")
    }

    /**
     * Verifies the navigation stack invariant.
     *
     * @throws AssertionError if the invariant is violated
     */
    private fun verifyInvariant() {
        check(navigationStack.isNotEmpty()) { "Navigation stack must never be empty" }
    }
}
