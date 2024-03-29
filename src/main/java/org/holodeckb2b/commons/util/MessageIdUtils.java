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

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;


/**
 * Is a utility class to generate and check the message and MIME content-id identifiers as specified in RFC2822.
 *
 * @author Sander Fieten (sander at holodeck-b2b.org)
 */
public class MessageIdUtils {
	// A static random value that can be used as right part of a message or content id
	private static final String RIGHT_PART = "h-" + Long.toHexString(Double.doubleToLongBits(Math.random())) 
											+ "." + Long.toHexString(Double.doubleToLongBits(Math.random())) ;	
	
    /**
     * Generates an unique message id as specified in RFC2822. The uniqueness of the identifier is ensured by using a
     * randomly generated left part. The right part is also random generated, but only created once and reused for all 
     * invocations of this method.  
     *
     * @return A new unique message id conforming to RFC2822.
     */
    public static String createMessageId() {
        return UUID.randomUUID().toString() + '@' + RIGHT_PART;
    }

    /**
     * Generates an unique message id as specified in RFC2822 using the given string as right part. If the specified
     * string contains characters not allowed in a messageId these are replaced by an underscore.
     *
     * @param rightPart the string to use as right part of the identifier
     * @return A new unique message id conforming to RFC2822.
     */
    public static String createMessageId(final String rightPart) {
        return UUID.randomUUID().toString() + '@' + rightPart.replaceAll("[^" + VALID_CHARS + "]", "_");
    }    
    
    /**
     * Generates a unique [MIME] content id based on the given message id.
     * <p><b>NOTE:</b> If the given message id is <code>null</code> or empty a random content id will be generated.
     *
     * @param msgId     The message id to use as base for the content id
     * @return          A unique content id
     */
    public static String createContentId(final String msgId) {
        String leftPart, rightPart;

        if (Utils.isNullOrEmpty(msgId))
            // Because there is no message id to base the cid on, just create
            // a completely new id which is equivalent to a msg id
            return createMessageId();
        else {
            // Split msgId in left and right part (including the '@'). When msg
            // id does not contain '@' use empty right part
            final int i = msgId.indexOf("@");
            if (i > 0) {
                leftPart = msgId.substring(0, i);
                rightPart = msgId.substring(i);
            } else {
                leftPart = msgId;
                rightPart = "";
            }
            // Add random number to left part
            leftPart += "-" + String.valueOf(ThreadLocalRandom.current().nextInt(0, Integer.MAX_VALUE));

            // And return with rightPart added again, but ensure that the contentId does not contain any special chars
            // as this can cause issues in security processing
            
            return (leftPart + rightPart).replaceAll("[^" + VALID_CHARS + "]", "_");
        }
    }
    
    /**
     * Ensures that the given message id does not contain non valid characters as defined by the format as specified in 
     * RFC2822 by replacing all non valid characters with '_'.
     * 
     * @param msgId		the message id to make compliant
     * @return			the message id without any invalid characters
     */
    public static String sanitizeId(final String msgId) {
    	return msgId == null ? null : msgId.replaceAll("[^" + VALID_CHARS + "]", "_");
    }
    
    /**
     * Contains the regular expression to use for checking on conformance to RFC2822. The regular expression is based
     * on the ABNF definition of messageId given in the RFC.
     */
    private static final String	RFC2822_MESSAGE_ID;
    private static final String	VALID_CHARS;    
    static {
        String  ALPHA  = "\\p{Alpha}";
        String  DIGIT  = "\\d";
        String  achars = ALPHA + DIGIT + "\\Q" +
                            "!" + "#" +
                            "$" + "%" +
                            "&" + "'" +
                            "*" + "+" +
                            "-" + "/" +
                            "=" + "?" +
                            "^" + "_" +
                            "`" + "{" +
                            "|" + "}" +
                            "~" + "\\E";
        String 	atext  = "[" + achars + "]";

        String  dot_atom_text   =   atext + "+" +  "(\\." + atext + "+)*";

        String  id_left         =   dot_atom_text;
        String  id_right        =   dot_atom_text;

        RFC2822_MESSAGE_ID = id_left + "@" + id_right;
        VALID_CHARS = achars + "\\.@";
    }

    /**
     * Checks whether the given messageId only contains characters that are allowed by the <a href=
     * "https://tools.ietf.org/html/rfc2822">RFC2822</a>.
     *
     * @param messageId     The message id to check for valid characters
     * @return              <code>true</code> if the given messageId does contain only valid characters,<br>
     *                      <code>false</code> otherwise
     */
    public static boolean isAllowed(final String messageId) {
        if (Utils.isNullOrEmpty(messageId))
            return false;
        else
            return messageId.matches("[" + VALID_CHARS + "]*");    	
    }
    
    /**
     * Checks whether the given messageId is correctly formatted as specified in <a href=
     * "https://tools.ietf.org/html/rfc2822">RFC2822</a>.
     *
     * @param messageId     The message id to check for correct syntax
     * @return              <code>true</code> if the given messageId is in correct format,<br>
     *                      <code>false</code> otherwise
     */
    public static boolean isCorrectFormat(final String messageId) {
        if (Utils.isNullOrEmpty(messageId))
            return false;
        else
            return messageId.matches(RFC2822_MESSAGE_ID);
    }
    
    /**
     * Strips the brackets (&lt; and &gt;) from the given MessageId string. 
     * 
     * @param messageId	The MessageId string
     * @return	The messageId value without brackets
     */
    public static String stripBrackets(final String messageId) {
    	if (messageId == null)
    		return null;
    	
    	String msgIdOnly = messageId;
        if (msgIdOnly.startsWith("<"))
        	msgIdOnly = msgIdOnly.substring(1);
        if (msgIdOnly.endsWith(">"))
        	msgIdOnly = msgIdOnly.substring(0, msgIdOnly.length() - 1);
        return msgIdOnly;
    }
}
