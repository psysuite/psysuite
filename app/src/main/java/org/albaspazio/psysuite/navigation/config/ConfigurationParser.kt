package org.albaspazio.psysuite.navigation.config

import org.json.JSONArray
import org.json.JSONObject

/**
 * Parses JSON configuration files into ConfigurationNode trees.
 *
 * The JSON format should follow this structure:
 * ```json
 * {
 *   "label": "@string/label_name",
 *   "type": "menu",
 *   "value": [
 *     {
 *       "label": "@string/child_label",
 *       "type": "test",
 *       "value": "org.example.TestClass"
 *     }
 *   ]
 * }
 * ```
 */
object ConfigurationParser {

    /**
     * Parses a JSON string into a ConfigurationNode tree.
     *
     * @param jsonString The JSON string to parse
     * @return The root ConfigurationNode
     * @throws ConfigurationException if the JSON is invalid or malformed
     */
    fun parse(jsonString: String): ConfigurationNode {
        return try {
            val jsonObject = JSONObject(jsonString)
            parseNode(jsonObject)
        } catch (e: org.json.JSONException) {
            throw ConfigurationException("Invalid JSON syntax: ${e.message}", e)
        }
    }

    /**
     * Parses a JSON object into a ConfigurationNode.
     *
     * @param jsonObject The JSON object to parse
     * @return The ConfigurationNode
     * @throws ConfigurationException if the object is malformed
     */
    private fun parseNode(jsonObject: JSONObject): ConfigurationNode {
        // Extract and validate label
        val label = try {
            jsonObject.getString("label")
        } catch (e: org.json.JSONException) {
            throw ConfigurationException("Node missing required 'label' field", e)
        }

        if (label.isBlank()) {
            throw ConfigurationException("Node label cannot be empty")
        }

        // Extract and validate type
        val typeString = try {
            jsonObject.getString("type")
        } catch (e: org.json.JSONException) {
            throw ConfigurationException("Node '$label' missing required 'type' field", e)
        }

        val type = try {
            NodeType.valueOf(typeString.uppercase())
        } catch (e: IllegalArgumentException) {
            throw ConfigurationException("Node '$label' has invalid type '$typeString'. Must be 'menu' or 'test'", e)
        }

        // Extract and validate value based on type
        val value = when (type) {
            NodeType.MENU -> {
                try {
                    val jsonArray = jsonObject.getJSONArray("value")
                    parseMenuValue(jsonArray, label)
                } catch (e: org.json.JSONException) {
                    throw ConfigurationException("MENU node '$label' missing required 'value' array", e)
                }
            }
            NodeType.TEST -> {
                try {
                    jsonObject.getString("value")
                } catch (e: org.json.JSONException) {
                    throw ConfigurationException("TEST node '$label' missing required 'value' (class name)", e)
                }
            }
        }

        // Create and validate the node
        val node = ConfigurationNode(label, type, value)
        node.validate()
        return node
    }

    /**
     * Parses a JSON array of menu items.
     *
     * @param jsonArray The JSON array to parse
     * @param parentLabel The label of the parent node (for error messages)
     * @return List of ConfigurationNode objects
     * @throws ConfigurationException if any child is malformed
     */
    private fun parseMenuValue(jsonArray: JSONArray, parentLabel: String): List<ConfigurationNode> {
        if (jsonArray.length() == 0) {
            throw ConfigurationException("MENU node '$parentLabel' must have at least one child")
        }

        val children = mutableListOf<ConfigurationNode>()
        for (i in 0 until jsonArray.length()) {
            try {
                val childObject = jsonArray.getJSONObject(i)
                children.add(parseNode(childObject))
            } catch (e: org.json.JSONException) {
                throw ConfigurationException("MENU node '$parentLabel' child at index $i is not a valid JSON object", e)
            } catch (e: ConfigurationException) {
                throw ConfigurationException("MENU node '$parentLabel' child at index $i: ${e.message}", e)
            }
        }

        return children
    }
}
