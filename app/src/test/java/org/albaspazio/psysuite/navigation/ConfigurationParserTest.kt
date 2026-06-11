package org.albaspazio.psysuite.navigation

import org.albaspazio.psysuite.navigation.config.ConfigurationException
import org.albaspazio.psysuite.navigation.config.ConfigurationParser
import org.albaspazio.psysuite.navigation.config.NodeType
import org.junit.Test
import org.junit.Assert.*

/**
 * Unit Tests for Configuration Parser
 *
 * These tests verify that JSON configuration files are parsed correctly.
 * All tests are unit tests (no Android dependencies).
 */
class ConfigurationParserTest {

    @Test
    fun testParseValidMenuWithChild() {
        val json = """{
            "label":"Root",
            "type":"menu",
            "value":[
                {"label":"Child","type":"test","value":"org.test.Test"}
            ]
        }"""
        
        val node = ConfigurationParser.parse(json)
        
        assertNotNull(node)
        assertEquals("Root", node.label)
        assertEquals(NodeType.MENU, node.type)
    }

    @Test
    fun testParseValidTestNode() {
        val json = """{"label":"Test","type":"test","value":"org.test.TestClass"}"""
        
        val node = ConfigurationParser.parse(json)
        
        assertNotNull(node)
        assertEquals("Test", node.label)
        assertEquals(NodeType.TEST, node.type)
    }

    @Test
    fun testParseInvalidJsonThrows() {
        val invalidJson = """{"label":"Test","type":"menu","value":}"""
        
        try {
            ConfigurationParser.parse(invalidJson)
            fail("Should throw ConfigurationException")
        } catch (e: ConfigurationException) {
            assertNotNull(e.message)
        }
    }

    @Test
    fun testParseMissingLabelThrows() {
        val json = """{"type":"menu","value":[{"label":"Child","type":"test","value":"org.test.Test"}]}"""
        
        try {
            ConfigurationParser.parse(json)
            fail("Should throw ConfigurationException")
        } catch (e: ConfigurationException) {
            assertTrue(e.message?.contains("label") == true)
        }
    }

    @Test
    fun testParseInvalidTypeThrows() {
        val json = """{"label":"Test","type":"invalid","value":"org.test.Test"}"""
        
        try {
            ConfigurationParser.parse(json)
            fail("Should throw ConfigurationException")
        } catch (e: ConfigurationException) {
            assertTrue(e.message?.contains("type") == true)
        }
    }

    @Test
    fun testParseMenuWithoutChildrenThrows() {
        val json = """{"label":"Menu","type":"menu","value":[]}"""
        
        try {
            ConfigurationParser.parse(json)
            fail("Should throw ConfigurationException")
        } catch (e: ConfigurationException) {
            assertTrue(e.message?.contains("child") == true)
        }
    }

    @Test
    fun testParseNestedStructure() {
        val json = """{
            "label":"Root",
            "type":"menu",
            "value":[
                {
                    "label":"SubMenu",
                    "type":"menu",
                    "value":[
                        {"label":"Test","type":"test","value":"org.test.Test"}
                    ]
                }
            ]
        }"""
        
        val node = ConfigurationParser.parse(json)
        
        assertEquals("Root", node.label)
        val children = node.getChildren()
        assertEquals(1, children.size)
        assertEquals("SubMenu", children[0].label)
    }
}
