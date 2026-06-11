package org.albaspazio.psysuite.navigation.resolution

import android.content.Context
import android.util.Log

/**
 * Resolves string labels from configuration, supporting both @string/ references and direct text.
 *
 * Resolution order:
 * 1. If label starts with @string/, resolve from resources
 * 2. Otherwise, use label as direct text
 *
 * Resource lookup order:
 * 1. psysuitetests resources
 * 2. psysuitecore resources (fallback)
 */
class StringResolver(private val context: Context) {

    companion object {
        private const val TAG = "StringResolver"
        private const val STRING_PREFIX = "@string/"
    }

    /**
     * Resolves a label to its final string value.
     *
     * @param label The label to resolve (can be @string/resource_name or direct text)
     * @return The resolved string
     * @throws StringResolutionException if a @string/ reference cannot be resolved
     */
    fun resolve(label: String): String {
        if (label.startsWith(STRING_PREFIX)) {
            return resolveStringResource(label)
        }
        return label
    }

    /**
     * Resolves a @string/ reference to its actual value.
     *
     * @param resourceReference The resource reference (e.g., "@string/label_name")
     * @return The resolved string value
     * @throws StringResolutionException if the resource cannot be found
     */
    private fun resolveStringResource(resourceReference: String): String {
        val resourceName = resourceReference.substring(STRING_PREFIX.length)

        if (resourceName.isBlank()) {
            throw StringResolutionException("Empty resource name in reference: $resourceReference")
        }

        // Try packages in order: app, psysuitetests, psysuitecore
        val packages = listOf(
            "org.albaspazio.psysuite",      // Main app package
            "org.albaspazio.psysuite.tests", // Tests package
            "org.albaspazio.psysuite.core"   // Core package
        )

        for (packageName in packages) {
            val resourceId = getResourceId(resourceName, packageName)
            if (resourceId != 0) {
                return try {
                    context.resources.getString(resourceId)
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to get string from $packageName for $resourceName", e)
                    continue
                }
            }
        }

        throw StringResolutionException("Resource not found in any package: $resourceReference")
    }

    /**
     * Gets the resource ID for a string resource name from a specific package.
     *
     * @param resourceName The name of the resource (without @string/ prefix)
     * @param packageName The package to search in
     * @return The resource ID, or 0 if not found
     */
    private fun getResourceId(resourceName: String, packageName: String): Int {
        return try {
            val resources = context.resources
            resources.getIdentifier(resourceName, "string", packageName)
        } catch (e: Exception) {
            Log.d(TAG, "Error getting resource ID for $resourceName in $packageName", e)
            0
        }
    }
}

/**
 * Exception thrown when string resolution fails.
 */
class StringResolutionException(message: String, cause: Throwable? = null) : Exception(message, cause)
