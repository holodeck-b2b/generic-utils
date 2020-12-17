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

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;
import java.security.KeyStore.PrivateKeyEntry;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.util.Arrays;
import java.util.Enumeration;

import org.holodeckb2b.commons.util.Utils;

/**
 * Contains some utility functions for accessing the keystores
 *
 * @author Sander Fieten (sander at holodeck-b2b.org)
 */
public final class KeystoreUtils {

	/**
	 * Enumerates the supported keystore types
	 */
	public enum KeystoreType {
		JKS, PKCS12
	}

	/**
	 * Reads the key pair from the specified PKCS#12 file. The PKCS#12 file must contain only one key pair that uses the
	 * same password as for the file itself for this method to work.
	 * 
	 * @param pkcs12File     Path to the PKCS#12 file
	 * @param pkcs12Password Password to get access to the PKCS#12 file
	 * @return The keypair read from the PKCS#12 file
	 * @throws CertificateException When the keypair cannot be read from the specified file, for example because the
	 *                              file is missing, an incorrect password is given or multiple entries exists in the
	 *                              given file
	 */
	public static PrivateKeyEntry readKeyPairFromPKCS12(final Path pkcs12File, final String pkcs12Password)
			throws CertificateException {
		try {
			final KeyStore keyStore = load(KeystoreType.PKCS12, pkcs12File, pkcs12Password);
			final String kpAlias = getSingleKPAlias(keyStore);
			final char[] pwd = (!Utils.isNullOrEmpty(pkcs12Password) ? pkcs12Password.toCharArray() : new char[] {});
			return (PrivateKeyEntry) keyStore.getEntry(kpAlias, new KeyStore.PasswordProtection(pwd));
		} catch (KeyStoreException | UnrecoverableEntryException | NoSuchAlgorithmException ex) {
			throw new CertificateException("Cannot load keypair from specified PKCS#12 file [" + pkcs12File + "]!", ex);
		}
	}

	/**
	 * Checks that the specified PKCS#12 file contains exactly one key pair that is accessible using the given password
	 * and returns the alias of that key pair.
	 * 
	 * @param pkcs12File     Path to the PKCS#12 file
	 * @param pkcs12Password Password to get access to the PKCS#12 file and key pair
	 * @return The alias of the key pair stored in the PKCS#12 file
	 * @throws CertificateException When the key pair cannot be read from the specified file, for example because the
	 *                              file is missing, an incorrect password is given or multiple entries exists in the
	 *                              given file
	 */
	public static String getKeyPairAliasFromPKCS12(final Path pkcs12File, final String pkcs12Password)
			throws CertificateException {
		try {
			final KeyStore keyStore = load(KeystoreType.PKCS12, pkcs12File, pkcs12Password);
			return getSingleKPAlias(keyStore);
		} catch (KeyStoreException ex) {
			throw new CertificateException("Cannot load keypair from specified PKCS#12 file [" + pkcs12File + "]!", ex);
		}
	}

	/**
	 * Helper method to retrieve the alias of the single key pair contained in the keystore.
	 * 
	 * @param ks keystore
	 * @return the alias of the key pair
	 * @throws KeyStoreException if there is a problem reading the entries in the keystore or when the keystore contains
	 *                           multiple key pairs
	 */
	private static String getSingleKPAlias(KeyStore ks) throws KeyStoreException {
		String kpAlias = null;
		for (Enumeration<String> aliases = ks.aliases(); aliases.hasMoreElements();) {
			final String a = aliases.nextElement();
			if (ks.isKeyEntry(a) && kpAlias != null)
				throw new KeyStoreException("More than one keypair in file!");
			else if (ks.isKeyEntry(a))
				kpAlias = a;
		}
		return kpAlias;
	}

	/**
	 * Checks whether the specified keystore is available and can be loaded. The specified file must be either a JKS
	 * and/or PKCS#12 file. 
	 *
	 * @param path Path to the keystore
	 * @param pwd  Password to access the keystore
	 * @throws SecurityProcessingException When the keystore could not be accessed.
	 */
	public static boolean check(final Path path, final String pwd) {
		if (!Files.isRegularFile(path) || !Files.isReadable(path))
			return false;

		try {
			load(path, pwd);
			return true;
		} catch (KeyStoreException spe) {
			return false;
		}
	}
	
	/**
	 * Checks whether the specified keystore is available and can be loaded. 
	 *
	 * @param path Path to the keystore
	 * @param pwd  Password to access the keystore
	 * @param type The expected type of the keustore, either JKS or PKCS#12 
	 * @throws SecurityProcessingException When the keystore could not be accessed.
	 */
	public static boolean check(final Path path, final String pwd, final KeystoreType type) {
		if (!Files.isRegularFile(path) || !Files.isReadable(path))
			return false;
				
		try {
			KeystoreType fType = detectStoreType(path);
			if (fType != type)
				return false;
			
			load(type, path, pwd);
			return true;
		} catch (KeyStoreException | IOException spe) {
			return false;
		}
	}

	private static final byte[] JKS_MAGIC_NO = new byte[] { (byte) 0xFE, (byte) 0xED, (byte) 0xFE, (byte) 0xED };

	/**
	 * Detects whether the keystore is in JKS or PKCS#12 format. This is checked by reading the first four bytes of the
	 * file and comparing this to the magic number that is at the start of JKS files (0xFEEDFEED).
	 * 
	 * @param ksPath path to the keystore
	 * @return the detected type
	 * @throws IOException when the file at given path could not be read
	 */
	private static KeystoreType detectStoreType(Path ksPath) throws IOException {
		try (FileInputStream fis = new FileInputStream(ksPath.toFile())) {
			byte[] magicno = new byte[4];
			fis.read(magicno);

			return Arrays.equals(JKS_MAGIC_NO, magicno) ? KeystoreType.JKS : KeystoreType.PKCS12;
		}
	}

	/**
	 * Loads the specified JKS or PKCS#12 keystore from disk.  
	 * 
	 * @param path 		Path to the keystore
	 * @param password  Password to access the keystore
	 * @return The keystore loaded from the specified file
	 * @throws KeyStoreException When the keystore could not be loaded from the specified location.
	 */
	public static KeyStore load(final Path path, final String password) throws KeyStoreException {
		try {
			return load(detectStoreType(path), path, password);
		} catch (IOException ex) {
			throw new KeyStoreException("Can not load the keystore [" + path + "]!", ex);
		}
	}

	/**
	 * Loads the specified keystore from disk
	 *
	 * @param type Indicates the keystore type, which can be either JKS or PKCS#12
	 * @param path The path to the keystore
	 * @param pwd  Password to access the keystore
	 * @return The keystore loaded from the specified file
	 * @throws KeyStoreException When the keystore could not be loaded from the specified location.
	 */
	public static KeyStore load(final KeystoreType type, final Path path, final String pwd) throws KeyStoreException {
		try {
			final KeyStore keyStore = KeyStore.getInstance(type.name());
			try (FileInputStream fis = new java.io.FileInputStream(path.toFile())) {
				keyStore.load(fis, (!Utils.isNullOrEmpty(pwd) ? pwd.toCharArray() : new char[] {}));
			}
			return keyStore;
		} catch (NullPointerException | IOException | KeyStoreException | NoSuchAlgorithmException
				| CertificateException ex) {
			throw new KeyStoreException("Can not load the keystore [" + path + "]!", ex);
		}
	}

    /**
     * Saves the given keystore to the specified JKS file on disk
     *
     * @param keystore  The keystore to save
     * @param path      The path to the keystore file
     * @param pwd       Password to access the keystore file
     * @throws SecurityProcessingException When the keystore could not be saved to the specified location.
     */
    public static void save(final KeyStore keystore, final Path path, final String pwd)
                                                                                    throws KeyStoreException {
        try {
            try (FileOutputStream fos = new java.io.FileOutputStream(path.toFile())) {
                keystore.store(fos, (!Utils.isNullOrEmpty(pwd) ? pwd.toCharArray() : new char[] {}));
            }
        } catch (NullPointerException | IOException | KeyStoreException | NoSuchAlgorithmException
                | CertificateException ex) {
            throw new KeyStoreException("Can not save the keystore to file [" + path + "]!", ex);
        }
    }
}
