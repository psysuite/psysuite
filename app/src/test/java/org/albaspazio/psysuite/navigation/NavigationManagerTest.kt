package org.albaspazio.psysuite.navigation

import org.albaspazio.psysuite.navigation.config.ConfigurationNode
import org.albaspazio.psysuite.navigation.config.NodeType
import org.albaspazio.psysuite.navigation.manager.TestsNavigationManager
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Unit Tests for Navigation Manager
 *
 * These tests verify navigation stack management.
 * IMPORTANT: navigateTo() only accepts MENU nodes, not TEST nodes.
 */
@RunWith(RobolectricTestRunner::class)
class NavigationManagerTest {

    private lateinit var rootNode: ConfigurationNode
    private lateinit var childMenu: ConfigurationNode
    private lateinit var testNode: ConfigurationNode
    private lateinit var manager: TestsNavigationManager

    @Before
    fun setUp() {
        // Create test nodes
        testNode = ConfigurationNode("Test", NodeType.TEST, "org.test.TestClass")
        childMenu = ConfigurationNode("ChildMenu", NodeType.MENU, listOf(testNode))
        rootNode = ConfigurationNode("Root", NodeType.MENU, listOf(childMenu))
        
        manager = TestsNavigationManager(rootNode)
    }

    @Test
    fun testInitialStateIsRoot() {
        val current = manager.getCurrentNode()
        assertEquals(rootNode, current)
    }

    @Test
    fun testNavigateToMenuNode() {
        manager.navigateTo(childMenu)
        assertEquals(childMenu, manager.getCurrentNode())
    }

    @Test
    fun testCannotNavigateToTestNode() {
        try {
            manager.navigateTo(testNode)
            fail("Should throw IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message?.contains("MENU") == true)
        }
    }

    @Test
    fun testCanGoBackAfterNavigation() {
        manager.navigateTo(childMenu)
        assertTrue(manager.canGoBack())
    }

    @Test
    fun testCannotGoBackAtRoot() {
        assertFalse(manager.canGoBack())
    }

    @Test
    fun testGoBackReturnsToRoot() {
        manager.navigateTo(childMenu)
        manager.goBack()
        assertEquals(rootNode, manager.getCurrentNode())
    }

    @Test
    fun testGoBackAtRootStaysAtRoot() {
        val initialNode = manager.getCurrentNode()
        manager.goBack()
        assertEquals(initialNode, manager.getCurrentNode())
    }

    @Test
    fun testMultipleLevelNavigation() {
        val subMenu = ConfigurationNode("SubMenu", NodeType.MENU, listOf(testNode))
        val newRoot = ConfigurationNode("Root", NodeType.MENU, listOf(subMenu))
        val newManager = TestsNavigationManager(newRoot)
        
        newManager.navigateTo(subMenu)
        assertEquals(subMenu, newManager.getCurrentNode())
        
        newManager.goBack()
        assertEquals(newRoot, newManager.getCurrentNode())
    }

    @Test
    fun testSaveState() {
        manager.navigateTo(childMenu)
        
        val savedState = manager.saveState()
        
        assertNotNull(savedState)
        assertTrue(savedState.containsKey("navigation_stack"))
    }

    @Test
    fun testGetNavigationHistory() {
        manager.navigateTo(childMenu)
        
        val history = manager.getNavigationHistory()
        
        assertEquals(2, history.size)
        assertEquals(rootNode, history[0])
        assertEquals(childMenu, history[1])
    }

    @Test
    fun testReset() {
        manager.navigateTo(childMenu)
        manager.reset()
        
        assertEquals(rootNode, manager.getCurrentNode())
        assertFalse(manager.canGoBack())
    }
}
