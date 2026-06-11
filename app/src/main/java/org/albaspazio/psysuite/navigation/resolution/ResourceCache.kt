package org.albaspazio.psysuite.navigation.resolution

/**
 * Caches resolved string resources for performance optimization.
 *
 * Reduces repeated resource lookups by storing resolved values.
 */
class ResourceCache {

    private val cache = mutableMapOf<String, String>()

    /**
     * Gets a cached value if it exists.
     *
     * @param key The cache key (typically the resource reference)
     * @return The cached value, or null if not in cache
     */
    fun get(key: String): String? = cache[key]

    /**
     * Stores a value in the cache.
     *
     * @param key The cache key
     * @param value The value to cache
     */
    fun put(key: String, value: String) {
        cache[key] = value
    }

    /**
     * Checks if a key is in the cache.
     *
     * @param key The cache key
     * @return True if the key is cached
     */
    fun contains(key: String): Boolean = cache.containsKey(key)

    /**
     * Clears all cached values.
     */
    fun clear() {
        cache.clear()
    }

    /**
     * Gets the current cache size.
     *
     * @return Number of cached entries
     */
    fun size(): Int = cache.size
}
