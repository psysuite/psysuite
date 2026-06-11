package org.albaspazio.psysuite.navigation

import org.albaspazio.psysuite.navigation.config.ConfigurationException
import org.albaspazio.psysuite.navigation.config.ConfigurationNode
import org.albaspazio.psysuite.navigation.config.NodeType
import org.junit.Test
import org.junit.Assert.*

/**
 * Unit Tests for Configuration Node
 *
 * These tests verify the ConfigurationNode data model and validation.
 */
class ConfigurationNodeTest {

    @Test
    fun testCreateMenuNode() {
        val child = ConfigurationNode("Child", NodeType.TEST, "org.test.Test")
        val node = ConfigurationNode("Menu", NodeType.MENU, listOf(child))
        
        assertEquals("Menu", node.label)
        assertEquals(NodeType.MENU, node.type)
    }

    @Test
    fun testCreateTestNode() {
        val node = ConfigurationNode("Test", NodeType.TEST, "org.test.TestClass")
        
        assertEquals("Test", node.label)
        assertEquals(NodeType.TEST, node.type)
    }

    @Test
    fun testGetChildrenFromMenuNode() {
        val child1 = ConfigurationNode("Child1", NodeType.TEST, "org.test.Test1")
        val child2 = ConfigurationNode("Child2", NodeType.TEST, "org.test.Test2")
        val node = ConfigurationNode("Menu", NodeType.MENU, listOf(child1, child2))
        
        val children = node.getChildren()
        
        assertEquals(2, children.size)
        assertEquals("Child1", children[0].label)
        assertEquals("Child2", children[1].label)
    }

    @Test
    fun testGetChildrenFromTestNodeThrows() {
        val node = ConfigurationNode("Test", NodeType.TEST, "org.test.Test")
        
        try {
            node.getChildren()
            fail("Should throw IllegalStateException")
        } catch (e: IllegalStateException) {
            assertNotNull(e.message)
        }
    }

    @Test
    fun testGetTestClassNameFromTestNode() {
        val className = "org.test.MyTest"
        val node = ConfigurationNode("Test", NodeType.TEST, className)
        
        assertEquals(className, node.getTestClassName())
    }

    @Test
    fun testGetTestClassNameFromMenuNodeThrows() {
        val child = ConfigurationNode("Child", NodeType.TEST, "org.test.Test")
        val node = ConfigurationNode("Menu", NodeType.MENU, listOf(child))
        
        try {
            node.getTestClassName()
            fail("Should throw IllegalStateException")
        } catch (e: IllegalStateException) {
            assertNotNull(e.message)
        }
    }

    @Test
    fun testValidateEmptyLabel() {
        try {
            val child = ConfigurationNode("Child", NodeType.TEST, "org.test.Test")
            ConfigurationNode("", NodeType.MENU, listOf(child)).validate()
            fail("Should throw ConfigurationException")
        } catch (e: ConfigurationException) {
            assertTrue(e.message?.contains("empty") == true)
        }
    }

    @Test
    fun testValidateMenuWithoutChildren() {
        try {
            ConfigurationNode("Menu", NodeType.MENU, emptyList<ConfigurationNode>()).validate()
            fail("Should throw ConfigurationException")
        } catch (e: ConfigurationException) {
            assertTrue(e.message?.contains("child") == true)
        }
    }

    @Test
    fun testValidateTestWithEmptyClassName() {
        try {
            ConfigurationNode("Test", NodeType.TEST, "").validate()
            fail("Should throw ConfigurationException")
        } catch (e: ConfigurationException) {
            assertTrue(e.message?.contains("empty") == true)
        }
    }

    @Test
    fun testValidateMenuWithValidChildren() {
        val child = ConfigurationNode("Child", NodeType.TEST, "org.test.Test")
        val node = ConfigurationNode("Menu", NodeType.MENU, listOf(child))
        
        // Should not throw
        node.validate()
    }

    @Test
    fun testValidateTestWithValidClassName() {
        val node = ConfigurationNode("Test", NodeType.TEST, "org.test.TestClass")
        
        // Should not throw
        node.validate()
    }

    @Test
    fun testNodeTypeEnum() {
        assertEquals(NodeType.MENU, NodeType.valueOf("MENU"))
        assertEquals(NodeType.TEST, NodeType.valueOf("TEST"))
    }

    @Test
    fun testNestedMenuValidation() {
        val test = ConfigurationNode("Test", NodeType.TEST, "org.test.Test")
        val subMenu = ConfigurationNode("SubMenu", NodeType.MENU, listOf(test))
        val rootMenu = ConfigurationNode("Root", NodeType.MENU, listOf(subMenu))
        
        // Should not throw
        rootMenu.validate()
    }
}
