package org.holodeckb2b.commons.xml;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.xml.namespace.QName;

import org.holodeckb2b.commons.testing.TestUtils;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

class XMLElementFinderTest {

	private static File TEST_FILE = TestUtils.getTestResource("test.xml").toFile();
	
	@Test
	void testFindLocalName() {
		Element found = null;
		
		try (FileInputStream fis = new FileInputStream(TEST_FILE)) {
			found = XMLElementFinder.parse(fis, new QName("ChildLevelTwo"));			
		} catch (IOException e) {
			fail(e);
		}
		
		assertNotNull(found);
		assertEquals("http://holodeck-b2b.org/schemas/2020/12/xmlelementfindertest/a", found.getNamespaceURI());
		assertEquals("ChildLevelTwo", found.getLocalName());
		NodeList childElements = found.getElementsByTagName("a:ChildLevelThree");
		assertEquals(1, childElements.getLength());
		assertEquals("http://holodeck-b2b.org/schemas/2020/12/xmlelementfindertest/a", childElements.item(0).getNamespaceURI());
		assertEquals("ChildLevelThree", childElements.item(0).getLocalName());		
	}

	@Test
	void testFindQName() {
		Element found = null;
		
		try (FileInputStream fis = new FileInputStream(TEST_FILE)) {
			found = XMLElementFinder.parse(fis, 
											new QName("http://holodeck-b2b.org/schemas/2020/12/xmlelementfindertest/b", 
													  "ChildLevelTwo"));			
		} catch (IOException e) {
			fail(e);
		}
		
		assertNotNull(found);
		assertEquals("http://holodeck-b2b.org/schemas/2020/12/xmlelementfindertest/b", found.getNamespaceURI());
		assertEquals("ChildLevelTwo", found.getLocalName());
		NodeList childNodes = found.getChildNodes();
		assertEquals(1, childNodes.getLength());
		assertEquals("Hello World!", found.getTextContent());
	}
	
	@Test
	void testLimitExceeded() {
		Element found = null;
		
		try (FileInputStream fis = new FileInputStream(TEST_FILE)) {
			found = XMLElementFinder.parse(fis, new QName("ChildLevelThree"), 4);			
		} catch (IOException e) {
			fail(e);
		}
		
		assertNotNull(found);
		
		try (FileInputStream fis = new FileInputStream(TEST_FILE)) {
			found = XMLElementFinder.parse(fis, new QName("ChildLevelThree"), 3);			
		} catch (IOException e) {
			fail(e);
		}
		
		assertNull(found);			
	}
	
	

}
