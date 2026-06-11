package org.albaspazio.psysuite.navigation.config

/**
 * Represents a node in the test navigation configuration tree.
 *
 * @property label The display label for this node (can be @string/resource_name or direct text)
 * @property type The type of node (MENU or TEST)
 * @property value The value associated with this node:
 *                 - For MENU nodes: List<ConfigurationNode> representing child nodes
 *                 - For TEST nodes: String representing the fully qualified class name
 */
data class ConfigurationNode(
    val label: String,
    val type: NodeType,
    val value: Any
) {
    /**
     * Validates this configuration node.
     *
     * @throws ConfigurationException if the node is invalid
     */
    fun validate() {
        // Validate label
        if (label.isBlank()) {
            throw ConfigurationException("Node label cannot be empty")
        }

        // Validate type-specific value
        when (type) {
            NodeType.MENU -> {
                if (value !is List<*>) {
                    throw ConfigurationException("MENU node value must be a List, got ${value.javaClass.simpleName}")
                }
                val children = value as List<*>
                if (children.isEmpty()) {
                    throw ConfigurationException("MENU node '$label' must have at least one child")
                }
                // Validate all children are ConfigurationNode
                children.forEach { child ->
                    if (child !is ConfigurationNode) {
                        throw ConfigurationException("MENU node '$label' contains non-ConfigurationNode child: ${child?.javaClass?.simpleName}")
                    }
                    child.validate()
                }
            }
            NodeType.TEST -> {
                if (value !is String) {
                    throw ConfigurationException("TEST node value must be a String (class name), got ${value.javaClass.simpleName}")
                }
                if ((value as String).isBlank()) {
                    throw ConfigurationException("TEST node '$label' must have a non-empty class name")
                }
            }
        }
    }

    /**
     * Gets the children of this node if it's a MENU node.
     *
     * @return List of child ConfigurationNode objects
     * @throws IllegalStateException if this is not a MENU node
     */
    fun getChildren(): List<ConfigurationNode> {
        if (type != NodeType.MENU) {
            throw IllegalStateException("Cannot get children of a TEST node")
        }
        @Suppress("UNCHECKED_CAST")
        return value as List<ConfigurationNode>
    }

    /**
     * Gets the test class name if this is a TEST node.
     *
     * @return Fully qualified class name
     * @throws IllegalStateException if this is not a TEST node
     */
    fun getTestClassName(): String {
        if (type != NodeType.TEST) {
            throw IllegalStateException("Cannot get test class name from a MENU node")
        }
        return value as String
    }
}

/**
 * Enum representing the type of a configuration node.
 */
enum class NodeType {
    /**
     * A menu node that contains other nodes (menu or test)
     */
    MENU,

    /**
     * A test node that represents a runnable test
     */
    TEST
}

/**
 * Exception thrown when configuration is invalid.
 */
class ConfigurationException(message: String, cause: Throwable? = null) : Exception(message, cause)
