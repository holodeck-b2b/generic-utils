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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.TextStyle;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.holodeckb2b.commons.util.spi_test.FirstAvailableProvider;
import org.holodeckb2b.commons.util.spi_test.IProviderInterface;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 *
 * @author Timur Shakuov (t.shakuov at gmail.com)
 * @author Sander Fieten (sander at holodeck-b2b.org)
 */
public class UtilsTest {

	@Test
	void testGetKeyByValue() {
		HashMap<String, String> map = new HashMap<>();
		map.put("key1", "value1");
		map.put("key2", "value2");
		map.put("key3", "value3");

		assertEquals("key1", Utils.getKeyByValue(map, "value1"));
		assertEquals("key2", Utils.getKeyByValue(map, "value2"));
		assertEquals("key3", Utils.getKeyByValue(map, "value3"));
		assertNull(Utils.getKeyByValue(map, "notthere"));
	}

	@Test
	void date2XMLString() {
		Calendar ts = Calendar.getInstance();
		ts.set(2020, 2, 2, 9, 0, 0);
		String xmlNow = Utils.toXMLDateTime(Date.from(ts.toInstant()));

		Matcher patternMatcher = Pattern.compile("(\\d{4})-(\\d{2})-(\\d{2})T(\\d{2}):(\\d{2}):(\\d{2}).(\\d{3})Z")
				.matcher(xmlNow);

		ts.setTimeZone(TimeZone.getTimeZone(ZoneOffset.UTC));
		assertTrue(patternMatcher.matches());
		assertEquals(ts.get(Calendar.YEAR), Integer.parseInt(patternMatcher.group(1)));
		assertEquals(ts.get(Calendar.MONTH) + 1, Integer.parseInt(patternMatcher.group(2)));
		assertEquals(ts.get(Calendar.DAY_OF_MONTH), Integer.parseInt(patternMatcher.group(3)));
		assertEquals(ts.get(Calendar.HOUR_OF_DAY), Integer.parseInt(patternMatcher.group(4)));
		assertEquals(ts.get(Calendar.MINUTE), Integer.parseInt(patternMatcher.group(5)));
		assertEquals(ts.get(Calendar.SECOND), Integer.parseInt(patternMatcher.group(6)));
		assertEquals(ts.get(Calendar.MILLISECOND), Integer.parseInt(patternMatcher.group(7)));
	}

	@Test
	void localDateTime2XMLString() {
		LocalDateTime ts = LocalDateTime.of(2020, 2, 2, 9, 0, 0, 0);
		String xmlNow = Utils.toXMLDateTime(ts);

		Matcher patternMatcher = Pattern.compile("(\\d{4})-(\\d{2})-(\\d{2})T(\\d{2}):(\\d{2}):(\\d{2}).(\\d{3})Z")
				.matcher(xmlNow);

		ts.atOffset(ZoneOffset.UTC);
		assertTrue(patternMatcher.matches());
		assertEquals(ts.get(ChronoField.YEAR), Integer.parseInt(patternMatcher.group(1)));
		assertEquals(ts.get(ChronoField.MONTH_OF_YEAR), Integer.parseInt(patternMatcher.group(2)));
		assertEquals(ts.get(ChronoField.DAY_OF_MONTH), Integer.parseInt(patternMatcher.group(3)));
		assertEquals(ts.get(ChronoField.HOUR_OF_DAY), Integer.parseInt(patternMatcher.group(4)));
		assertEquals(ts.get(ChronoField.MINUTE_OF_HOUR), Integer.parseInt(patternMatcher.group(5)));
		assertEquals(ts.get(ChronoField.SECOND_OF_MINUTE), Integer.parseInt(patternMatcher.group(6)));
		assertEquals(ts.get(ChronoField.MILLI_OF_SECOND), Integer.parseInt(patternMatcher.group(7)));
	}

	@ParameterizedTest
	@ValueSource(strings = { "2020-05-04T19:13:51.0", "2020-05-04T17:13:51Z", "2020-05-04T15:13:51-02:00",
			"2020-05-04T19:13:51", "2020-05-04T19:13:51.000+02:00", "2020-05-04T19:13:51.00050",
			"2020-05-04T18:13:51+01:00", "2020-05-04T17:13:51+00:00" })
	void testFromXMLDateTime(String xmlTimestamp) {
		TimeZone defaultZone = TimeZone.getDefault();
		TimeZone.setDefault(TimeZone.getTimeZone("CET"));
		Calendar ts = Calendar.getInstance();
		try {
			Date d = Utils.fromXMLDateTime(xmlTimestamp);
			ts.setTime(d);
		} catch (ParseException e) {
			fail(e);
		}

		assertEquals(2020, ts.get(Calendar.YEAR));
		assertEquals(5, ts.get(Calendar.MONTH) + 1);
		assertEquals(4, ts.get(Calendar.DAY_OF_MONTH));
		assertEquals(19, ts.get(Calendar.HOUR_OF_DAY));
		assertEquals(13, ts.get(Calendar.MINUTE));
		assertEquals(51, ts.get(Calendar.SECOND));
		assertEquals(0, ts.get(Calendar.MILLISECOND));

		TimeZone.setDefault(defaultZone);
	}

	@Test
	void testParseEmptyDate() {
		try {
			assertNull(Utils.fromXMLDateTime(""));
		} catch (ParseException e) {
			fail();
		}
		try {
			assertNull(Utils.fromXMLDateTime(null));
		} catch (ParseException e) {
			fail();
		}
		
		try {
			assertNull(Utils.parseDateTimeFromXML(""));
		} catch (ParseException e) {
			fail();
		}
		try {
			assertNull(Utils.parseDateTimeFromXML(null));
		} catch (ParseException e) {
			fail();
		}
		
	}
	
	@Test
	void testParseDateTimeFromXML() {
		ZonedDateTime ts = null;
		try {
			ts = Utils.parseDateTimeFromXML("2020-05-04T19:13:51.0");
		} catch (ParseException e) {
			fail(e);
		}
		assertEquals(ZoneOffset.systemDefault().getDisplayName(TextStyle.SHORT_STANDALONE, Locale.getDefault()),
				ts.getZone().getDisplayName(TextStyle.SHORT_STANDALONE, Locale.getDefault()));
		assertEquals(2020, ts.get(ChronoField.YEAR));
		assertEquals(5, ts.get(ChronoField.MONTH_OF_YEAR));
		assertEquals(4, ts.get(ChronoField.DAY_OF_MONTH));
		assertEquals(19, ts.get(ChronoField.HOUR_OF_DAY));
		assertEquals(13, ts.get(ChronoField.MINUTE_OF_HOUR));
		assertEquals(51, ts.get(ChronoField.SECOND_OF_MINUTE));
		assertEquals(0, ts.get(ChronoField.MILLI_OF_SECOND));

		try {
			ts = Utils.parseDateTimeFromXML("2020-05-04T17:13:51.0Z");
		} catch (ParseException e) {
			fail(e);
		}
		assertEquals(ZoneOffset.UTC.getDisplayName(TextStyle.SHORT_STANDALONE, Locale.getDefault()),
				ts.getZone().getDisplayName(TextStyle.SHORT_STANDALONE, Locale.getDefault()));
		
		assertEquals(2020, ts.get(ChronoField.YEAR));
		assertEquals(5, ts.get(ChronoField.MONTH_OF_YEAR));
		assertEquals(4, ts.get(ChronoField.DAY_OF_MONTH));
		assertEquals(17, ts.get(ChronoField.HOUR_OF_DAY));
		assertEquals(13, ts.get(ChronoField.MINUTE_OF_HOUR));
		assertEquals(51, ts.get(ChronoField.SECOND_OF_MINUTE));
		assertEquals(0, ts.get(ChronoField.MILLI_OF_SECOND));
		
		try {
			ts = Utils.parseDateTimeFromXML("2020-05-04T17:13:51.0-02:00");
		} catch (ParseException e) {
			fail(e);
		}
		assertEquals(ZoneOffset.ofHours(-2).getDisplayName(TextStyle.SHORT_STANDALONE, Locale.getDefault()),
					ts.getZone().getDisplayName(TextStyle.SHORT_STANDALONE, Locale.getDefault()));		
		assertEquals(2020, ts.get(ChronoField.YEAR));
		assertEquals(5, ts.get(ChronoField.MONTH_OF_YEAR));
		assertEquals(4, ts.get(ChronoField.DAY_OF_MONTH));
		assertEquals(17, ts.get(ChronoField.HOUR_OF_DAY));
		assertEquals(13, ts.get(ChronoField.MINUTE_OF_HOUR));
		assertEquals(51, ts.get(ChronoField.SECOND_OF_MINUTE));
		assertEquals(0, ts.get(ChronoField.MILLI_OF_SECOND));		
	}

	/**
	 * Test possible results of
	 * {@link org.holodeckb2b.common.util.Utils#compareStrings(String, String)
	 * compareStrings}
	 */
	@Test
	void testCompareStrings() {
		assertTrue(Utils.compareStrings("a", "b") == -2);

		assertTrue(Utils.compareStrings(null, null) == -1);
		assertTrue(Utils.compareStrings(null, "") == -1);
		assertTrue(Utils.compareStrings("", null) == -1);
		assertTrue(Utils.compareStrings("", "") == -1);

		assertTrue(Utils.compareStrings("a", "a") == 0);

		assertTrue(Utils.compareStrings("a", "") == 1);
		assertTrue(Utils.compareStrings("a", null) == 1);

		assertTrue(Utils.compareStrings(null, "b") == 2);
		assertTrue(Utils.compareStrings("", "b") == 2);
	}

	@Test
	void testGetValueOrDefault() {
		assertEquals("default", Utils.getValueOrDefault(null, "default"));
		assertEquals("default", Utils.getValueOrDefault("", "default"));
		assertEquals("data", Utils.getValueOrDefault("data", "default"));
	}
	
	@ParameterizedTest
	@ValueSource(strings = { "T", "true", "True", "tRuE", "y", "Y", "1", "yes" , "yEs" })
	void testIsTrueCorrect(String v) {
		assertTrue(Utils.isTrue(v));
	}

	@ParameterizedTest
	@ValueSource(strings = { "F", "false", "False", "tREu", "0", "11", "eys", "TT" })
	void testIsTrueInCorrect(String v) {
		assertFalse(Utils.isTrue(v));
	}
	
	@Test
	void testParseIntArray() {
		assertNull(Utils.parseIntArray(null));
		assertNull(Utils.parseIntArray(""));
		
		assertArrayEquals(new int[] { 1 }, Utils.parseIntArray("1"));
		assertArrayEquals(new int[] { 1, 2, 3, 4 }, Utils.parseIntArray(" 1 ,2,3 ,4 "));
		assertArrayEquals(new int[] { 1, 2, -3, 4 }, Utils.parseIntArray(" 1 ,2,-3 ,4 "));

		assertNull(Utils.parseIntArray("A"));
		assertNull(Utils.parseIntArray("1, A, 2"));
		assertNull(Utils.parseIntArray("1, 2,A"));
	}
	
	@Test
	void testCopyStream() throws IOException {
		byte[] source = "This string will be used as the source stream for testing the copy stream method".getBytes();
		
		ByteArrayInputStream bis = new ByteArrayInputStream(source);
		CloseableByteArrayOutputStream bos = new CloseableByteArrayOutputStream();
		
		long copyCount = 0;
		try {
			copyCount = Utils.copyStream(bis, bos);
		} catch (Throwable t) {
			fail(t);		
		}
		
		assertEquals(0, bis.available());		
		assertEquals(source.length, copyCount);
		assertArrayEquals(source, bos.toByteArray());
		assertFalse(bos.isClosed);
		
		assertDoesNotThrow(() -> bis.close());
		assertDoesNotThrow(() -> bos.close());
	}
	
	@Test
	void testGetFirstAvailableProvider() {
		
		IProviderInterface provider = null;		
		try {
			provider = Utils.getFirstAvailableProvider(IProviderInterface.class); 
		} catch (Throwable t) {
			fail(t);
		}
		
		assertNotNull(provider);
		assertTrue(provider instanceof FirstAvailableProvider);
	}
	
	@Test
	void testRequireNotNullString() {		
		assertThrows(IllegalArgumentException.class, () -> Utils.requireNotNullOrEmpty((String) null));
		assertThrows(IllegalArgumentException.class, () -> Utils.requireNotNullOrEmpty(""));
		assertDoesNotThrow(() -> Utils.requireNotNullOrEmpty("Hello World"));		
	}

	@Test
	void testRequireNotNullCollection() {		
		assertThrows(IllegalArgumentException.class, () -> Utils.requireNotNullOrEmpty((Collection) null));
		assertThrows(IllegalArgumentException.class, () -> Utils.requireNotNullOrEmpty(new ArrayList<String>()));
		assertDoesNotThrow(() -> Utils.requireNotNullOrEmpty(Collections.singleton("Hello World")));		
	}
	
	@Test
	void testRequireNotNullMap() {		
		assertThrows(IllegalArgumentException.class, () -> Utils.requireNotNullOrEmpty((Map) null));
		assertThrows(IllegalArgumentException.class, () -> Utils.requireNotNullOrEmpty(new HashMap<String, String>()));
		assertDoesNotThrow(() -> Utils.requireNotNullOrEmpty(Collections.singletonMap("Hello", "World")));		
	}
	
	@Test
	void testRequireNotNullIterator() {		
		assertThrows(IllegalArgumentException.class, () -> Utils.requireNotNullOrEmpty((Iterator) null));
		assertThrows(IllegalArgumentException.class, 
											() -> Utils.requireNotNullOrEmpty(new ArrayList<String>().iterator()));
		assertDoesNotThrow(() -> Utils.requireNotNullOrEmpty(Collections.singleton("Hello World").iterator()));		
	}
	
	@Test
	void testAreEqualList() {
		assertTrue(Utils.areEqual((List<String>) null, (List<String>) null));
		assertTrue(Utils.areEqual(new ArrayList<String>(), new ArrayList<String>()));
		assertTrue(Utils.areEqual((List<String>) null, new ArrayList<String>()));
		assertTrue(Utils.areEqual(new ArrayList<String>(), (List<String>) null));
		assertTrue(Utils.areEqual(Arrays.asList("a", "b", "c"), Arrays.asList("a", "b", "c")));

		assertFalse(Utils.areEqual((List<String>) null, Arrays.asList("a", "b", "c")));
		assertFalse(Utils.areEqual(Arrays.asList("a", "b", "c"), (List<String>) null));
		assertFalse(Utils.areEqual(Arrays.asList("a", "b", "c"), new ArrayList<String>()));
		assertFalse(Utils.areEqual(new ArrayList<String>(), Arrays.asList("a", "b", "c")));
		assertFalse(Utils.areEqual(Arrays.asList("a", "c", "b"), Arrays.asList("a", "b", "c")));
		assertFalse(Utils.areEqual(Arrays.asList("a", "b", "c"), Arrays.asList("a", "c", "b")));
		assertFalse(Utils.areEqual(Arrays.asList("a", "b", "c", "d"), Arrays.asList("a", "b", "c")));
		assertFalse(Utils.areEqual(Arrays.asList("a", "b", "c"), Arrays.asList("a", "b", "c", "d")));
	}
	
	class CloseableByteArrayOutputStream extends ByteArrayOutputStream {
		boolean isClosed = false;
		
		@Override
		public void close() throws IOException {
			super.close();
			isClosed = true;
		}
	}
}
