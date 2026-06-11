package org.albaspazio.psysuite.navigation.resolution

import android.util.Log
import org.albaspazio.psysuite.tests.SettingsBasic

/**
 * Instantiates test classes via reflection from their fully qualified class names.
 *
 * Supports dynamic test creation without hardcoding test class references.
 */
object TestParcelInstantiator {

    private const val TAG = "TestParcelInstantiator"

    /**
     * Instantiates a test parcel class from its fully qualified name.
     *
     * @param className The fully qualified class name (e.g., "org.albaspazio.tests.temporalbinding.atb.SubjectATBParcel")
     * @return An instance of the test parcel class
     * @throws TestInstantiationException if instantiation fails
     */
    fun instantiate(className: String): SettingsBasic {
        if (className.isBlank()) {
            throw TestInstantiationException("Class name cannot be empty")
        }

        return try {
            // Load the class
            val clazz = Class.forName(className)
            Log.d(TAG, "Loaded class: $className")

            // Verify it's a subclass of SettingsBasic
            if (!SettingsBasic::class.java.isAssignableFrom(clazz)) {
                throw TestInstantiationException(
                    "Class $className does not extend SettingsBasic"
                )
            }

            // Get the no-arg constructor
            val constructor = try {
                clazz.getConstructor()
            } catch (e: NoSuchMethodException) {
                throw TestInstantiationException(
                    "Class $className does not have a no-argument constructor",
                    e
                )
            }

            // Create an instance
            val instance = constructor.newInstance() as SettingsBasic
            Log.d(TAG, "Successfully instantiated: $className")
            instance

        } catch (e: ClassNotFoundException) {
            throw TestInstantiationException(
                "Class not found: $className",
                e
            )
        } catch (e: InstantiationException) {
            throw TestInstantiationException(
                "Failed to instantiate $className: ${e.message}",
                e
            )
        } catch (e: IllegalAccessException) {
            throw TestInstantiationException(
                "Illegal access to constructor of $className: ${e.message}",
                e
            )
        } catch (e: TestInstantiationException) {
            throw e
        } catch (e: Exception) {
            throw TestInstantiationException(
                "Unexpected error instantiating $className: ${e.message}",
                e
            )
        }
    }
}

/**
 * Exception thrown when test instantiation fails.
 */
class TestInstantiationException(message: String, cause: Throwable? = null) : Exception(message, cause)
