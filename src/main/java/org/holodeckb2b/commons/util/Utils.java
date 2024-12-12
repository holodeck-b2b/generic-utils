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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ServiceLoader;
import java.util.TimeZone;

/**
 * A container for some generic helper methods not related to a specific topic.
 *
 * @author Sander Fieten (sander at holodeck-b2b.org)
 */
public final class Utils {

	private static final String XML_DATETIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSXX";

	/**
     * Transform a {@link Date} into a {@link String} formatted according to the specification of the <code>dateTime
     * </code> datatype of XML schema and using the UTC time zone.<br>
     * See <a href="http://www.w3.org/TR/xmlschema-2/#dateTime">section 3.2.7 of the XML Specification</a> for details.
     *
     * @param   date  date object to convert to an XML formatted date string.
     * @return  The date as an <code>xs:dateTime</code> formatted String
     *          or <code>null</code> when date object was <code>null</code>
     */
    public static String toXMLDateTime(final Date date) {
        if (date == null)
            return null;
		SimpleDateFormat xmlDateFormatter = new SimpleDateFormat(XML_DATETIME_FORMAT);
		xmlDateFormatter.setTimeZone(TimeZone.getTimeZone("UTC"));
		return xmlDateFormatter.format(date);
    }

	/**
     * Transform a {@link LocalDateTime} into a {@link String} formatted according to the specification of the <code>
     * dateTime</code> datatype of XML schema and using the UTC time zone.<br>
     * See <a href="http://www.w3.org/TR/xmlschema-2/#dateTime">section 3.2.7 of the XML Specification</a> for details.
     *
     * @param   timestamp	object to be converted to an XML formatted date string.
     * @return  The date as an <code>xs:dateTime</code> formatted String
     *          or <code>null</code> when date object was <code>null</code>
     */
    public static String toXMLDateTime(final LocalDateTime timestamp) {
    	if (timestamp == null)
    		return null;
    	DateTimeFormatter xmlDateFormatter = DateTimeFormatter.ofPattern(XML_DATETIME_FORMAT);
    	return timestamp.atZone(ZoneOffset.UTC).format(xmlDateFormatter);
    }

    /**
     * Parses a {@link String} for XML dateTime (see <a href="http://www.w3.org/TR/xmlschema-2/#dateTime">section 3.2.7
     * of the XML Specification</a>) and when a valid date is found return a {@link ZonedDateTime} object representing
     * the same time stamp. NOTE: When the given XML date time does not include a time zone the returned date time will
     * be in the system's default time zone.
     *
     * @param   xmlDateTimeString   string that should contain the <code>xs:dateTime</code> formatted date
     * @return  A {@link Date} object for the parsed date or,<br>
     * 			<code>null</code> when the input is empty or <code>null</code>
     * @throws  ParseException on date time parsing error
     */
    public static ZonedDateTime parseDateTimeFromXML(final String xmlDateTimeString) throws ParseException {
    	final String[] formatAndC14NValue = getFormatAndC14N(xmlDateTimeString);
    	if (formatAndC14NValue == null)
    		return null;
    	else if (formatAndC14NValue[0].endsWith("Z"))
    		return ZonedDateTime.parse(formatAndC14NValue[1], DateTimeFormatter.ofPattern(formatAndC14NValue[0]));
    	else
    		return ZonedDateTime.of(LocalDateTime.parse(formatAndC14NValue[1],
    													DateTimeFormatter.ofPattern(formatAndC14NValue[0])),
    								ZoneId.systemDefault());
    }

    /**
     * Parses a {@link String} for XML dateTime (see <a href="http://www.w3.org/TR/xmlschema-2/#dateTime">section 3.2.7
     * of the XML Specification</a>) and when a valid date is found return a {@link Date} object representing the same
     * time stamp in the default time zone.
     *
     * @param   xmlDateTimeString   string that should contain the <code>xs:dateTime</code> formatted date
     * @return  A {@link Date} object for the parsed date or,<br>
     * 			<code>null</code> when the input is empty or <code>null</code>
     * @throws  ParseException on date time parsing error
     */
    public static Date fromXMLDateTime(final String xmlDateTimeString) throws ParseException {
    	final String[] formatAndC14NValue = getFormatAndC14N(xmlDateTimeString);
    	return formatAndC14NValue != null ? new SimpleDateFormat(formatAndC14NValue[0]).parse(formatAndC14NValue[1])
    									  : null;
    }

    /**
     * Helper method to determine the format of given XML date time string and how it should be parsed. The string is
     * reformatted so it can be correctly parsed.
     *
     * @param xmlDateTimeString	string containing the XML date time
     * @return	a String array with two elements; first the pattern to parse the date time and second the reformated
     * 			XML date time
     */
    private static String[] getFormatAndC14N(final String xmlDateTimeString) {
    	if (Utils.isNullOrEmpty(xmlDateTimeString))
    		return null;

    	String s = xmlDateTimeString;
        String f = null;

        // If there is no text, there is no date
        if (s == null || s.isEmpty())
            return null;

        // Check whether UTC is specified as timezone using "Z" and replace with "+00:00" if yes
        if (s.indexOf("Z") > 0)
            s = s.replace("Z", "+00:00");

        // Check if value contains fractional seconds
        int i = s.indexOf(".");
        if (i > 0) {
            // Contains fractional seconds, limit to milliseconds (3 digits)
            // Get start of timezone which is indicated by either "+" or "-"
            // Because "-" also occurs in the date part, only start looking for it from start of the time part
            int z = Math.max(s.indexOf("+"), s.indexOf("-", s.indexOf("T")));
            z = (z == -1 ? s.length() : z); // It's possible that no timezone was included, then fractional seconds are last part of string
            // Limit the number of digits to extract to 3 but use less if no more available
            final int S = Math.min(z-i-1, 3);
            s = s.substring(0, i + S + 1) + s.substring(z);
            // Remove ':' from timezone (if there is a timezone)
            i = s.indexOf(":", i + S + 1);
            f = "yyyy-MM-dd'T'HH:mm:ss." + "SSS".substring(0, S);
            if (i > 0) {
                s = s.substring(0, i) + s.substring(i + 1);
                f = f + "Z";
            }
        } else {
            // No fractional seconds, just remove the ':' from the timezone indication (when it is there)
            if (s.length() > 22 ) {
                s = s.substring(0, 22) + s.substring(23);
                // Set format
                f = "yyyy-MM-dd'T'HH:mm:ssZ";
            } else {
                // Only set format
                f = "yyyy-MM-dd'T'HH:mm:ss";
            }
        }

        return new String[] { f , s };
    }

    /**
     * Gets the key of the entry in a one-to-one Map that has the given value. Note that this function simply searches
     * for the first entry in the map that has the given value and does not check if other entries exists with the same
     * value. As this function uses the {@link #equals(Object)} to compare the map entry values with the given value,
     * its result depends on the correct implementation of this method.
     *
     * @param map   map to search
     * @param value value to search the corresponding key value for, must not be <code>null</code>
     * @param <K>	type of the key value
     * @param <V>	type of the value stored in the map
     * @return The key corresponding to the provided value or <code>null</code> if no entry with this value is found
     */
    public static <K, V> K getKeyByValue(final Map<K, V> map, final String value) {
    	if (value == null)
    		throw new IllegalArgumentException("Cannot search for null value");
    	final Entry<K, V> f = map.entrySet().parallelStream()
    							  .filter(e -> value.equals(e.getValue())).findFirst().orElse(null);
    	return f != null ? f.getKey() : null;
    }

    /**
     * Compares two strings for equality returning a code that differentiates in the reason of inequality or equality.
     *
     * @param s     First input string
     * @param p     Second input string
     * @return      -2 when both strings are non-empty and their values are different,<br>
     *              -1 when both strings are <code>null</code> or empty,<br>
     *              0  when both strings are non-empty but equal,<br>
     *              1  when only the first string is non-empty,<br>
     *              2  when only the second string is non-empty
     */
    public static int compareStrings(final String s, final String p) {
        if (isNullOrEmpty(s)) {
            if (!isNullOrEmpty(p)) {
                return 2;
            } else {
                return -1;
            }
        } else if (!isNullOrEmpty(p)) {
            if (s.equals(p)) {
                return 0;
            } else {
                return -2;
            }
        } else {
            return 1;
        }
    }

    /**
     * Checks whether the given String is non-empty and returns its value if true, otherwise the supplied default will
     * be returned.
     *
     * @param value         The String to check
     * @param defaultValue  The default value to use if the given string is <code>null</code> or empty
     * @return      <code>value</code> if it is a non-empty string,<br>
     *              <code>defaultValue</code> otherwise
     */
    public static String getValueOrDefault(final String value, final String defaultValue) {
        return (value != null && !value.isEmpty() ? value : defaultValue);
    }

    /**
     * Parses a string containing a comma-separated list of integers to an array of integers.
     *
     * @param csText  	Text containing the comma separated list
     * @return  		An array containing the integers from the list, or<br>
     *          		<code>null</code> if the string could not be parsed
     */
    public static int[] parseIntArray(final String csText) {
        if (Utils.isNullOrEmpty(csText))
            return null;

        try {
            final String[] sIntervals = csText.split(",");
            final int[] intervals = new int[sIntervals.length];
            for(int i = 0; i < sIntervals.length ; i++)
                intervals[i] = Integer.parseInt(sIntervals[i].trim());
            return intervals;
        } catch (NumberFormatException nan) {
            // The given text does not contain a comma-separated list of integers
            return null;
        }
    }

    /**
     * Converts an integer array to a String containing the integers from the array as a comma-separated list.
     *
     * @param aInt	integer array to convert
     * @return		String containing the integers as a comma-separated list. When the given array is <code>null</code>
     * 				or empty, an empty String is returned
     * @since 1.2.0
     */
    public static String toCSLString(final int[] aInt) {
    	StringBuffer txt = new StringBuffer();
    	if (aInt != null && aInt.length > 0) {
			txt.append(aInt[0]);
			for(int i = 1; i < aInt.length; i++)
				txt.append(',').append(aInt[i]);
    	}
		return txt.toString();
    }

    /**
     * Checks whether the given String is <code>null</code> or is an empty string, i.e. does not contain any other
     * characters then whitespace.
     *
     * @param s     The string to check
     * @return      <code>true</code> if <code>s == null || s.trim().isEmpty() == true</code>,<br>
     *              <code>false</code> otherwise
     */
    public static boolean isNullOrEmpty(final String s) {
        return s == null || s.trim().isEmpty();
    }

    /**
     * Checks whether the given Collection is <code>null</code> or is empty.
     *
     * @param c     The Collection to check
     * @return      <code>true</code> if <code>c == null || c.isEmpty() == true</code>,<br>
     *              <code>false</code> otherwise
     */
    public static boolean isNullOrEmpty(final Collection<?> c) {
        return c == null || c.isEmpty();
    }

    /**
     * Checks whether the given Map is <code>null</code> or is empty.
     *
     * @param m     The Map to check
     * @return      <code>true</code> if <code>m == null || m.isEmpty() == true</code>,<br>
     *              <code>false</code> otherwise
     */
    public static boolean isNullOrEmpty(final Map<?,?> m) {
        return m == null || m.isEmpty();
    }

    /**
     * Checks whether the given Iterator is <code>null</code> or does not contain any more objects.
     *
     * @param i     The Iterator to check
     * @return      <code>true</code> if <code>i == null || i.hasNext() == true</code>,<br>
     *              <code>false</code> otherwise
     */
    public static boolean isNullOrEmpty(final Iterator<?> i) {
        return i == null || !i.hasNext();
    }

    /**
     * Gets the root cause of the exception by traversing the exception stack and returning the
     * last available exception in it.
     *
     * @param t     The {@link Throwable} object to get the root cause for. May not be <code>null</code>
     *              because otherwise it crashes with an ArrayIndexOutOfBoundsException
     * @return      The root cause (note that this can be the throwable itself)
     */
    public static Throwable getRootCause(final Throwable t) {
        final List<Throwable> exceptionStack = getCauses(t);
        return exceptionStack.get(exceptionStack.size() - 1);
    }

    /**
     * Gets the exception stack of an exception, i.e. the list of all exception that where registered as causes.
     *
     * @param t     The {@link Throwable} object to get the exception stack for
     * @return      A list of {@link Throwable} object with the first item being the exception itself and the last
     *              item the root cause.
     */
    public static List<Throwable> getCauses(final Throwable t) {
        final List<Throwable> exceptionStack = new ArrayList<>();
        Throwable i = t;
        while (i != null) {
            exceptionStack.add(i);
            i = i.getCause();
        }

        return exceptionStack;
    }

    /**
     * Creates a string for inclusion in logs containing the list of exceptions including their exception messages that
     * caused the given exception.
     * <p>The format of the generated string is with the exception message only included if one is provided by the
     * exception:
     * <pre>
     * «Exception class name» : «exception message»
     *      Caused by: «Exception class name» : «exception message»
     *      Caused by: «Exception class name» : «exception message»
     *      ...
     * </pre>
     *
     * @param t 		The exception that occurred
     * @return  The list of exceptions that caused this exception including their error message
     */
    public static String getExceptionTrace(final Throwable t) {
    	return getExceptionTrace(t, false);
    }

    /**
     * Creates a string for inclusion in logs containing the list of exceptions including their exception messages that
     * caused the given exception.
     * <p>The format of the generated string is with the exception message only included if one is provided by the
     * exception:
     * <pre>
     * «Exception class name» : «exception message»
     *      Caused by: «Exception class name» : «exception message»
     *      Caused by: «Exception class name» : «exception message»
     *      ...
     * </pre>
     * Optionally an additional tab character can be added to all lines so the exception trace is better alligned.
     *
     * @param t 		The exception that occurred
     * @param indent	Indicates whether an additional tab character should be added
     * @return  The list of exceptions that caused this exception including their error message
     */
    public static String getExceptionTrace(final Throwable t, final boolean indent) {
        final StringBuffer r = new StringBuffer();

        if (indent)
        	r.append('\t');
        r.append(t.getClass().getSimpleName());
        if (!isNullOrEmpty(t.getMessage()))
            r.append(" : ").append(t.getMessage());
        Throwable cause = t.getCause();
    	while (cause != null) {
            r.append('\n').append('\t');
            if (indent)
            	r.append('\t');
            r.append("Caused by: ").append(cause.getClass().getSimpleName());
            if (!isNullOrEmpty(cause.getMessage()))
                r.append(" : ").append(cause.getMessage());
            cause = cause.getCause();
        }

        return r.toString();
    }

    /**
     * Compare any 2 objects in a <code>null</code> safe manner. If both passed
     * objects are <code>null</code> they are interpreted as being equal. If only
     * one object is <code>null</code> they are different. If both objects are
     * non-<code>null</code> than the {@link #equals(Object)} method is invoked on
     * them.
     *
     * @param o1	First object. May be <code>null</code>.
     * @param o2 	Second object. May be <code>null</code>.
     * @param <T> the class of the objects to compare
     * @return <code>true</code> if both are <code>null</code> or if both are
     *         equal.
     */
    public static <T> boolean nullSafeEqual (final T o1, final T o2) {
        return o1 == null ? o2 == null : o1.equals (o2);
    }

    /**
     * Compare any 2 {@link ZonedDateTime}s in a <code>null</code> safe manner. If both passed
     * objects are <code>null</code> they are interpreted as being equal. If only
     * one object is <code>null</code> they are different. If both objects are
     * non-<code>null</code> than they are converted to {@link Instant}s on the time line
     * and compared for equality.
     *
     * @param t1	First date time. May be <code>null</code>.
     * @param t2	Second date time. May be <code>null</code>.
     * @return <code>true</code> if both are <code>null</code> or if both represent
     *         the same instant on the time line
     * @since 1.1.0
     */
    public static boolean nullSafeEqual(final ZonedDateTime t1, final ZonedDateTime t2) {
    	return t1 == null ? t2 == null : t2 != null && t1.toInstant().equals(t2.toInstant());
    }

    /**
     * Compare any 2 {@link String}s in a <code>null</code> safe manner. If both passed
     * objects are <code>null</code> they are interpreted as being equal. If only
     * one object is <code>null</code> they are different. If both objects are
     * non-<code>null</code> than the {@link String#equalsIgnoreCase(String)} method is invoked on
     * them.
     *
     * @param s1	First String. May be <code>null</code>.
     * @param s2	Second String. May be <code>null</code>.
     * @return <code>true</code> if both are <code>null</code> or if both are
     *         equal ignoring the case.
     */
    public static boolean nullSafeEqualIgnoreCase (final String s1, final String s2) {
        return s1 == null ? s2 == null : s1.equalsIgnoreCase (s2);
    }

    /**
     * Checks whether the given string is a valid URI as defined in <a href=
     * "https://tools.ietf.org/html/rfc3986#section-3">section 3 of RFC3986</a>.
     *
     * @param uri   The URI value to check
     * @return      <code>true</code> if the given string is a valid URI, <code>false</code> otherwise.
     */
    public static boolean isValidURI(final String uri) {
        boolean isValid;
        try {
            final URI parsedURI = new URI(uri);
            isValid = !Utils.isNullOrEmpty(parsedURI.getScheme());
        } catch (URISyntaxException ex) {
            isValid = false;
        }
        return isValid;
    }

    /**
     * Tests if the given String represents the boolean "true" value.
     *
     * @param  value 	the string value to check
     * @return	 <code>true</code> if the String is equal to, ignoring case, "true", "T", "1", 'yes' or "Y"<br>
     * 		 	 <code>false</code> otherwise
     */
    public static boolean isTrue(String value) {
        return value != null && (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("t") || value.equals("1")
        						|| value.equalsIgnoreCase("yes") || value.equalsIgnoreCase("y"));
    }

	/**
	 * Checks if the given Boolean is not null and is set to true.
	 *
	 * @param value	the Boolean to check
	 * @return	<code>true</code> when the given Boolean is not null and is true, <code>false</code> otherwise
	 * @since 1.2.0
	 */
	public static boolean isTrue(Boolean value) {
		return value != null ? value : false;
	}

    /**
     * Copies the content of an input stream to an output stream and returns the numbers of bytes copied.
     * <p>Note that this method will copy all content <b>remaining</b> on the source stream and <b>append</b> it to the
     * what is already written to the destination stream. Neither the input nor the output stream will be close by this
     * method.
     *
     * @param src	source stream
     * @param dst	destination stream
     * @return	the number of bytes copied from the source to the destination stream
     * @throws IOException	when an error occurs reading from the source or writing to the destination stream
     */
    public static long copyStream(InputStream src, OutputStream dst) throws IOException {
    	return copyStream(src, dst, 64);
    }

    /**
     * Copies the content of an input stream to an output stream using a buffer of the specified size and returns the
     * numbers of bytes copied.
     * <p>Note that this method will copy all content <b>remaining</b> on the source stream and <b>append</b> it to the
     * what is already written to the destination stream. Neither the input nor the output stream will be close by this
     * method.
     *
     * @param src	source stream
     * @param dst	destination stream
     * @param bufferSize	size of the buffer
     * @return	the number of bytes copied from the source to the destination stream
     * @throws IOException	when an error occurs reading from the source or writing to the destination stream
     * @since 1.6.0
     */
	public static long copyStream(InputStream src, OutputStream dst, int bufferSize) throws IOException {
    	long copied = 0;
    	byte buffer[] = new byte[bufferSize];
    	int r;
    	while ((r = src.read(buffer)) > 0) {
    		dst.write(buffer, 0, r);
    		copied += r;
    	}
    	dst.flush();

    	return copied;
    }

    /**
     * Gets the first available provider that implements the given interface. This method uses the Java Service Provider
     * Interface to get the list of providers and returns the first one that can be successfully instantiated.
     *
     * @param <T>	the interface that specifies the provider to be loaded
     * @param providerClass	the provider interface class
     * @return an instance of the first provider that is available, <code>null</code> if no provider is available
     */
    public static <T> T getFirstAvailableProvider(Class<T> providerClass) {
    	T	provider = null;
    	Iterator<T> providers = ServiceLoader.load(providerClass).iterator();

    	while (provider == null && providers.hasNext()) {
            try {
                provider = providers.next();
            } catch (Throwable t) {
            }
        }
    	return provider;
    }

   /**
     * Checks that the given String is not <code>null</code> or an empty string, i.e. does not contain any other
     * characters then whitespace and throws an {@link IllegalArgumentException} if it is.
     *
     * @param s     The string to check
	 * @throws IllegalArgumentException when the given string is <code>null</code> or empty
	 * @since 1.1.0
     */
    public static void requireNotNullOrEmpty(final String s) {
        if (isNullOrEmpty(s))
			throw new IllegalArgumentException();
    }

    /**
     * Checks that the given Collection is not <code>null</code> or empty and throws an {@link IllegalArgumentException}
	 * if it is.
     *
     * @param c     The Collection to check
	 * @throws IllegalArgumentException when the given collection is <code>null</code> or empty
	 * @since 1.1.0
     */
    public static void requireNotNullOrEmpty(final Collection<?> c) {
        if (isNullOrEmpty(c))
			throw new IllegalArgumentException();
    }

    /**
     * Checks that the given Map is not <code>null</code> or empty and throws an {@link IllegalArgumentException} if it
	 * is.
     *
     * @param m     The Map to check
	 * @throws IllegalArgumentException when the given map is <code>null</code> or empty
	 * @since 1.1.0
     */
    public static void requireNotNullOrEmpty(final Map<?,?> m) {
        if (isNullOrEmpty(m))
			throw new IllegalArgumentException();
    }

    /**
     * Checks that the given Iterator is not <code>null</code> or does not contain any more objects and throws an
	 * {@link IllegalArgumentException} if it is.
     *
     * @param i     The Iterator to check
	 * @throws IllegalArgumentException when the given iterator is <code>null</code> or empty
	 * @since 1.1.0
     */
    public static void requireNotNullOrEmpty(final Iterator<?> i) {
        if (isNullOrEmpty(i))
			throw new IllegalArgumentException();
    }

	/**
	 * Checks if two lists are both empty or contain the same elements, in the same order.
	 *
	 * @param l1	the first list
	 * @param l2	the second list
	 * @return	<code>true</code> iff <code>l1</code> and <code>l2</code> are both <code>null</code> or empty lists or
	 *			contain the same elements in the same order.
	 *			<code>false</code> otherwise
	 * @since 1.1.0
	 */
	public static boolean areEqual(List<?> l1, List<?> l2) {
		if (l1 == l2)
			return true;
		if (Utils.isNullOrEmpty(l1) & Utils.isNullOrEmpty(l2))
			return true;
		if (l1 == null || l2 == null)
			return false;
		if (l1.size() != l2.size())
			return false;

		for(int i = 0; i < l1.size(); i++) {
			Object o1 =  l1.get(i), o2 = l2.get(i);
			if (o1 != o2 && (o1 == null || !o1.equals(o2) || o2 == null || !o2.equals(o1)))
				return false;
		}
		return true;
	}

	/**
	 * Checks if two collections are both empty or contain the same elements.
	 *
	 * @param c1	the first collection
	 * @param c2	the second collection
	 * @return	<code>true</code> iff <code>c1</code> and <code>c2</code> are both <code>null</code> or empty or
	 *			contain the same elements with the same cardinality
	 *			<code>false</code> otherwise
	 * @since 1.1.0
	 */
	public static boolean areEqual(Collection<?> c1, Collection<?> c2) {
		if (c1 == c2)
			return true;
		if (Utils.isNullOrEmpty(c1) & Utils.isNullOrEmpty(c2))
			return true;
		if (c1 == null || c2 == null)
			return false;
		if (c1.size() != c2.size())
			return false;

		Map<Object, Integer> card1 = getCardinalities(c1);
		Map<Object, Integer> card2 = getCardinalities(c2);
		for(Object o1 : c1)
			if (card1.get(o1) != card2.get(o1))
				return false;
		return true;
	}

	private static Map<Object, Integer> getCardinalities(Collection<?> c) {
		HashMap<Object, Integer> card = new HashMap<>(c.size());
		for (Object o : c) {
			Integer cc = card.get(o);
			cc = cc != null ? cc + 1 : 1;
			card.put(o, cc);
		}
		return card;
	}
}
