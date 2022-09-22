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
package org.holodeckb2b.commons.security;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Arrays;

import javax.security.auth.x500.X500Principal;

import org.bouncycastle.asn1.x500.RDN;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x500.style.IETFUtils;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.util.encoders.Base64;
import org.holodeckb2b.commons.util.Utils;

/**
 * Is a utility class for processing of X509 certificates.
 *
 * @author Sander Fieten (sander at holodeck-b2b.org)
 */
public class CertificateUtils {
	/**
	 * The start boundary of a PEM formatted certificate
	 */
    private static final String PEM_START_BOUNDARY = "-----BEGIN CERTIFICATE-----";
	/**
	 * The end boundary of a PEM formatted certificate
	 */
    private static final String PEM_END_BOUNDARY = "-----END CERTIFICATE-----";

	/**
     * Certificate factory to create the X509 Certificate object
     */
    private static CertificateFactory certificateFactory;

    /**
     * Gets the X509 Certificate from the base64 encoded DER byte array which may be PEM encapsulated.
     *
     * @param b64EncodedCertificate The string containing the base64 encoded bytes
     * @return  The decoded Certificate instance, <code>null</code> if the given string is <code>null</code> or empty
     * @throws CertificateException When the string does not contain a valid base64 encoded certificate or when there is
     *                              a problem in loading required classes for certificate processing
     */
    public static X509Certificate getCertificate(final String b64EncodedCertificate) throws CertificateException {
    	if (Utils.isNullOrEmpty(b64EncodedCertificate))
    		return null;
    	// Strip everything up to first and after the last possible PEM boundaries
		final int startBIdx = b64EncodedCertificate.indexOf(PEM_START_BOUNDARY);
		final int endBIdx = b64EncodedCertificate.indexOf(PEM_END_BOUNDARY);
    	final String encodedBytes = startBIdx >= 0 ?
										b64EncodedCertificate.substring(b64EncodedCertificate.indexOf('\n', startBIdx)
															, endBIdx > 0 ? endBIdx : b64EncodedCertificate.length())
    								: b64EncodedCertificate;

    	byte[] certBytes = null;
    	try {
    		certBytes = Base64.decode(encodedBytes);
    	} catch (Exception decodingFailure) {
    		throw new CertificateException("String is not a valid base64 encoding", decodingFailure);
    	}

        return getCertificate(certBytes);
    }

    /**
     * Gets X509 Certificate from the specified file. The format of the file can be both the plain binary DER or PEM
	 * (base64 encoded) format.
     *
     * @param certificateFile  Path to the file containing the certificate, must not be <code>null</code>
     * @return  The decoded Certificate instance
     * @throws CertificateException When an error occurs reading the certificate from the file. For example when the
     * 								file can not be read or does not contain a valid certificate.
     */
    public static X509Certificate getCertificate(final Path certificateFile) throws CertificateException {
    	if (certificateFile == null)
    		throw new IllegalArgumentException("A path must be specified");
    	try(FileInputStream fis = new FileInputStream(certificateFile.toFile())) {
    		return getCertificate(fis);
    	} catch (IOException e) {
    		throw new CertificateException("Error accessing certificate file");
    	}
    }

    /**
     * Converts the given byte array into a X509 Certificate.
     *
     * @param certBytes The byte array containing the DER encoded certificate
     * @return  The Certificate instance
     * @throws CertificateException When there is a problem in loading required classes for certificate processing
     */
    public static X509Certificate getCertificate(final byte[] certBytes) throws CertificateException {
        if (certBytes == null || certBytes.length == 0)
            return null;
        else
        	return getCertificate(new ByteArrayInputStream(certBytes));
    }

    /**
     * Gets X509 Certificate from the given input stream. The format of the certificate can be both the plain binary DER 
     * or PEM (base64 encoded) format. After reading the certificate, the stream is left open at the position where the
     * certificate ended.
     *
     * @param is  input stream containing the certificate, must not be <code>null</code>
     * @return  The decoded Certificate instance
     * @throws CertificateException When an error occurs reading the certificate from the stream.
     * @since 1.2.0 
     */
    public static X509Certificate getCertificate(final InputStream is) throws CertificateException {
    	if (is == null)
    		throw new IllegalArgumentException("A stream must be specified");
    	return (X509Certificate) getCertificateFactory().generateCertificate(is);
    }
    
    
	/**
	 * Converts the certificate object into a PEM encoded string.
	 *
	 * @param cert	the certificate object
	 * @return		the PEM encoded version of the certificate
	 * @throws CertificateException When there is a problem in encoding the certificate
	 * @since 1.1.0
	 */
	public static String getPEMEncoded(X509Certificate cert) throws CertificateException {
		if (cert == null)
			return null;

		StringBuilder pemEncoded = new StringBuilder(PEM_START_BOUNDARY).append('\n');
		pemEncoded.append(java.util.Base64.getMimeEncoder(64, "\n".getBytes()).encodeToString(cert.getEncoded()));
		pemEncoded.append('\n').append(PEM_END_BOUNDARY);
		return pemEncoded.toString();
	}

    /**
     * Gets the CN field of the provided X509 certificate's Subject DN.
     *
     * @param cert	The X509 certificate
     * @return	The CN field of the Subject's DN or <code>null</code> if the CN could not be read
     */
    public static String getSubjectCN(final X509Certificate cert) {
    	try {
			X500Name x500name = X500Name.getInstance(cert.getSubjectX500Principal().getEncoded());
			RDN cn = x500name.getRDNs(BCStyle.CN)[0];
			return IETFUtils.valueToString(cn.getFirst().getValue());
		} catch (Exception invalidCert) {
			return null;
		}
    }

    /**
     * Gets the Subject's [distinguished] name of the provided X509 certificate
     *
     * @param cert	The X509 certificate
     * @return	The Subject's DN
	 * @since 1.1.0
     */
    public static String getSubjectName(final X509Certificate cert) {
		return X500Name.getInstance(HB2BStyle.INSTANCE, cert.getSubjectX500Principal().getEncoded()).toString();
    }

    /**
     * Gets the CN field of the provided X509 certificate's Issuer DN.
     *
     * @param cert	The X509 certificate
     * @return	The CN field of the Issuer's DN or <code>null</code> if the CN could not be read
     * @since 1.2.0
     */
    public static String getIssuerCN(final X509Certificate cert) {
    	try {
			X500Name x500name = X500Name.getInstance(cert.getIssuerX500Principal().getEncoded());
			RDN cn = x500name.getRDNs(BCStyle.CN)[0];
			return IETFUtils.valueToString(cn.getFirst().getValue());
		} catch (Exception invalidCert) {
			return null;
		}
    }
    
	/**
     * Gets the Issuer's [distinguished] name of the provided X509 certificate
     *
     * @param cert	The X509 certificate
     * @return	The Issuer's DN
	 * @since 1.1.0
     */
    public static String getIssuerName(final X509Certificate cert) {
		return X500Name.getInstance(HB2BStyle.INSTANCE, cert.getIssuerX500Principal().getEncoded()).toString();
    }

    /**
     * Gets the Serial Number field of the provided X509 certificate's Subject DN.
     * <p>NOTE: This is a different serial number then the serial number of the certificate itself!
     *
     * @param cert	The X509 certificate
     * @return	The Subject's Serial Number field or <code>null</code> if the SN could not be read
     */
    public static String getSubjectSN(final X509Certificate cert)  {
    	try {
    		X500Name x500name = X500Name.getInstance(cert.getSubjectX500Principal().getEncoded());
    		RDN cn = x500name.getRDNs(BCStyle.SN)[0];
    		return IETFUtils.valueToString(cn.getFirst().getValue());
    	} catch (Exception invalidCert) {
    		return null;
    	}
    }

    /**
     * Determines if the given X509 certificate has the the specified SKI
     * 
     * @param cert		certificate to check
     * @param skiBytes	the expected SKI 
     * @return			<code>true</code> if the given certificate has the same SKI, <code>false</code> otherwise
     * @since 1.2.0
     */
    public static boolean hasSKI(final X509Certificate cert, byte[] skiBytes) {
    	byte[] skiExtValue = cert.getExtensionValue(Extension.subjectKeyIdentifier.getId());
		if (skiExtValue != null) {
			byte[] ski = Arrays.copyOfRange(skiExtValue, 4, skiExtValue.length);    			
			return Arrays.equals(ski, skiBytes);
		} else
			return false;
    }
    
    /**
     * Determines if the given X509 certificate has the specified serial number and is issued by specified issuer.
     * 
     * @param cert		certificate to check
     * @param issuer	the expected issuer of the certificate
     * @param serial	the expected serial number
     * @return	<code>true</code> if the given certificate has the same serial number and issuer, 
     * 			<code>false</code> otherwise
     * @since 1.2.0
     */
    public static boolean hasIssuerSerial(final X509Certificate cert, final X500Principal issuer, 
    										final BigInteger serial) {
    	return cert.getIssuerX500Principal().equals(issuer) && cert.getSerialNumber().equals(serial);
    }
    
    /**
     * Determines if the given X509 certificate has the specified hash value calculated by the given diget method
     * 
     * @param cert			certificate to check
     * @param hash 		the expected hash value
     * @param digester	the digest method to calculate the hash
     * @return	<code>true</code> if the given certificate has the same hash value,	<code>false</code> otherwise
     * @since 1.2.0
     */
    public static boolean hasThumbprint(final X509Certificate cert, final byte[] hash, final MessageDigest digester) { 
        try {
        	digester.reset();
        	return Arrays.equals(digester.digest(cert.getEncoded()), hash);    	
        } catch (CertificateEncodingException ex) {            
            return false;
        }
    }    
    
    /**
     * Gets the {@link CertificateFactory} instance to use for creating the <code>X509Certificate</code> object from a
     * byte array.
     *
     * @return  The <code>CertificateFactory</code> to use
     * @throws CertificateException     When the certificate factory could not be loaded
     */
    private static CertificateFactory getCertificateFactory() throws CertificateException {
        if (certificateFactory == null)
            certificateFactory = CertificateFactory.getInstance("X.509");
        return certificateFactory;
    }
}
