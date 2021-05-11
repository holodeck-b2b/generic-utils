/*******************************************************************************
 * Copyright (C) 2020 The Holodeck Team, Sander Fieten
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.holodeckb2b.commons.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Created at 14:36 14.01.17
 *
 * @author Timur Shakuov (t.shakuov at gmail.com)
 */
public class MessageIdUtilsTest {

    @Test
    public void testCreateMessageId() throws Exception {
        String id = MessageIdUtils.createMessageId();
        assertNotNull(id);
        String[] parts = id.split("@");
        assertTrue(parts.length == 2);
        assertTrue(parts[0].length()>0);
        assertTrue(parts[1].contains("."));
    }
    
    @Test
    public void testCreateMessageIdWithRightPart() throws Exception {
    	String rightpart = "a.nice.and.valid.right.part";
        String id = MessageIdUtils.createMessageId(rightpart);
        assertNotNull(id);
        String[] parts = id.split("@");
        assertTrue(parts.length == 2);
        assertTrue(parts[0].length()>0);
        assertEquals(rightpart, parts[1]);

        id = MessageIdUtils.createMessageId("not.(0_nice. .)right.part");
        assertNotNull(id);
        parts = id.split("@");
        assertTrue(parts.length == 2);
        assertEquals("not._0_nice._._right.part", parts[1]);
    }   
    
    @Test
    public void testCreateContentId() throws Exception {
        String id = MessageIdUtils.createMessageId();
        String[] idParts = id.split("@");
        String contentId = MessageIdUtils.createContentId(id);
        assertNotNull(contentId);
        String[] contentIdParts = contentId.split("@");
        assertTrue(contentIdParts.length == 2);
        assertTrue(contentIdParts[0].contains(idParts[0]));
        assertEquals(idParts[0], contentIdParts[0].substring(0, contentIdParts[0].lastIndexOf("-")));
    }
    
    @Test
    public void testCheckMessageId() {
    	assertTrue(MessageIdUtils.isCorrectFormat("just.a.test@holodeck-b2b.org"));
    	assertTrue(MessageIdUtils.isCorrectFormat("justatest@holodeck-b2b.org"));
    	assertTrue(MessageIdUtils.isCorrectFormat("just_a.test@holodeck-b2b.org"));
    	assertTrue(MessageIdUtils.isCorrectFormat("just.8.test@holodeck-b2b.org"));
    	assertTrue(MessageIdUtils.isCorrectFormat("`a#very$spe.cial%id!@some-where+{%&'*/}?^~|"));
    	assertFalse(MessageIdUtils.isCorrectFormat(""));
    	assertFalse(MessageIdUtils.isCorrectFormat("just.a.test"));    	
    	assertFalse(MessageIdUtils.isCorrectFormat("just[8]test@holodeck-b2b.org"));    	
    }
    
    @Test
    public void testIsAllowed() {
    	assertTrue(MessageIdUtils.isAllowed("`a#very$special%id!@some-where+{%&'*/}?^~|"));
    	assertFalse(MessageIdUtils.isAllowed(""));
    	assertFalse(MessageIdUtils.isAllowed("just a test@holodeck-b2b.org"));    	
    	assertFalse(MessageIdUtils.isAllowed("just[8]test@holodeck-b2b.org"));    	
    }
    
    @Test
    public void testSanitize() {
    	assertNull(MessageIdUtils.sanitizeId(null));
    	assertEquals("", MessageIdUtils.sanitizeId(""));
    	assertEquals("hello", MessageIdUtils.sanitizeId("hello"));
    	assertEquals("hello_world", MessageIdUtils.sanitizeId("hello world"));
    	assertEquals("hello@world", MessageIdUtils.sanitizeId("hello@world"));
    	assertEquals("hello.world@earth.org", MessageIdUtils.sanitizeId("hello.world@earth.org"));
    	assertEquals("hello.world@earth@org", MessageIdUtils.sanitizeId("hello.world@earth@org"));
    	assertEquals("hello.world@earth__org", MessageIdUtils.sanitizeId("hello.world@earth::org"));
    }
    
    
}
