package org.albaspazio.psysuite.navigation.ui

import android.content.Context
import android.util.Log
import android.widget.Button
import android.widget.LinearLayout
import org.albaspazio.psysuite.navigation.config.ConfigurationNode
import org.albaspazio.psysuite.navigation.config.NodeType
import org.albaspazio.psysuite.navigation.resolution.StringResolver

/**
 * Generates dynamic buttons for menu nodes at runtime.
 *
 * Creates buttons for each child node and assigns appropriate callbacks.
 */
class DynamicButtonGenerator(
    private val context: Context,
    private val stringResolver: StringResolver
) {

    companion object {
        private const val TAG = "DynamicButtonGenerator"
    }

    /**
     * Generates buttons for a menu node and adds them to a container.
     *
     * @param menuNode The menu node to generate buttons for
     * @param container The LinearLayout to add buttons to
     * @param onMenuNodeClicked Callback when a MENU button is clicked
     * @param onTestNodeClicked Callback when a TEST button is clicked
     * @return List of generated buttons
     * @throws IllegalArgumentException if menuNode is not a MENU node
     */
    fun generateButtons(
        menuNode: ConfigurationNode,
        container: LinearLayout,
        onMenuNodeClicked: (ConfigurationNode) -> Unit,
        onTestNodeClicked: (ConfigurationNode) -> Unit
    ): List<Button> {
        if (menuNode.type != NodeType.MENU) {
            throw IllegalArgumentException("Can only generate buttons for MENU nodes")
        }

        // Clear existing buttons
        clearButtons(container)

        val buttons = mutableListOf<Button>()
        val children = menuNode.getChildren()

        for (child in children) {
            try {
                val button = createButton(child, onMenuNodeClicked, onTestNodeClicked)
                container.addView(button)
                buttons.add(button)
                Log.d(TAG, "Created button for: ${child.label}")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to create button for child: ${child.label}", e)
            }
        }

        Log.d(TAG, "Generated ${buttons.size} buttons for menu: ${menuNode.label}")
        return buttons
    }

    /**
     * Creates a single button for a configuration node.
     *
     * @param node The node to create a button for
     * @param onMenuNodeClicked Callback for MENU nodes
     * @param onTestNodeClicked Callback for TEST nodes
     * @return The created Button
     */
    private fun createButton(
        node: ConfigurationNode,
        onMenuNodeClicked: (ConfigurationNode) -> Unit,
        onTestNodeClicked: (ConfigurationNode) -> Unit
    ): Button {
        val button = Button(context)

        // Resolve and set label
        val resolvedLabel = try {
            stringResolver.resolve(node.label)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to resolve label for node: ${node.label}, using raw label", e)
            node.label
        }
        button.text = resolvedLabel

        // Set click listener based on node type
        button.setOnClickListener {
            when (node.type) {
                NodeType.MENU -> {
                    Log.d(TAG, "Menu button clicked: ${node.label}")
                    onMenuNodeClicked(node)
                }
                NodeType.TEST -> {
                    Log.d(TAG, "Test button clicked: ${node.label}")
                    onTestNodeClicked(node)
                }
            }
        }

        // Set layout parameters with height 300
        val layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 300)
        layoutParams.setMargins(30, 30, 30, 30)
        button.layoutParams = layoutParams

        // Set background color based on node type
        val backgroundColor = when (node.type) {
            NodeType.MENU -> 0xFF6200EE.toInt()      // Purple for menu nodes
            NodeType.TEST -> 0xFF2196F3.toInt()      // Blue for test nodes
        }
        button.setBackgroundColor(backgroundColor)

        // Set text color (white for both)
        button.setTextColor(0xFFFFFFFF.toInt())

        // Set text size (24sp)
        button.textSize = 20f

        return button
    }

    /**
     * Clears all buttons from a container.
     *
     * @param container The LinearLayout to clear
     */
    private fun clearButtons(container: LinearLayout) {
        container.removeAllViews()
        Log.d(TAG, "Cleared all buttons from container")
    }
}
