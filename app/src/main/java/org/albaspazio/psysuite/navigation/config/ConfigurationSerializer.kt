package org.albaspazio.psysuite.navigation.config

import org.json.JSONArray
import org.json.JSONObject

/**
 * Serializes ConfigurationNode trees into JSON format.
 *
 * Ensures round-trip compatibility with ConfigurationParser.
 */
object ConfigurationSerializer {

    /**
     * Serializes a ConfigurationNode tree into a JSON string.
     *
     * @param node The root ConfigurationNode to serialize
     * @param prettyPrint If true, formats the JSON with indentation
     * @return The JSON string representation
     */
    fun serialize(node: ConfigurationNode, prettyPrint: Boolean = true): String {
        val jsonObject = serializeNode(node)
        return if (prettyPrint) {
            jsonObject.toString(2)
        } else {
            jsonObject.toString()
        }
    }

    /**
     * Serializes a ConfigurationNode into a JSONObject.
     *
     * @param node The ConfigurationNode to serialize
     * @return The JSONObject representation
     */
    private fun serializeNode(node: ConfigurationNode): JSONObject {
        val jsonObject = JSONObject()
        jsonObject.put("label", node.label)
        jsonObject.put("type", node.type.name.lowercase())

        when (node.type) {
            NodeType.MENU -> {
                val children = node.getChildren()
                val jsonArray = JSONArray()
                for (child in children) {
                    jsonArray.put(serializeNode(child))
                }
                jsonObject.put("value", jsonArray)
            }
            NodeType.TEST -> {
                jsonObject.put("value", node.getTestClassName())
            }
        }

        return jsonObject
    }
}
