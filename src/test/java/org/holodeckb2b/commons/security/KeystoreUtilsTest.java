package org.holodeckb2b.commons.security;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;
import java.security.KeyStore.PrivateKeyEntry;
import java.security.KeyStoreException;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.Enumeration;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.holodeckb2b.commons.security.KeystoreUtils.KeystoreType;
import org.holodeckb2b.commons.testing.TestUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class KeystoreUtilsTest {
	
	@BeforeAll
	static void setBCAsPrfdProvider() {
		Security.insertProviderAt(new BouncyCastleProvider(), 1);		
	}
	
	@AfterAll
	static void removeBC() {
		Security.removeProvider("BC");
	}

	@Test
	void testGetPKFromPKCS12() {		
		try {
			assertNotNull(KeystoreUtils.readKeyPairFromPKCS12(TestUtils.getTestResource("singlekey.p12"), "singlekey"));
		} catch (CertificateException e) {
			fail(e);
		}
		
		try {
			assertNotNull(KeystoreUtils.readKeyPairFromPKCS12(TestUtils.getTestResource("keyandcert.p12"), 
																"keyandcert"));
		} catch (CertificateException e) {
			fail(e);
		}
		
		PrivateKeyEntry pk = null;
		try {
			pk = KeystoreUtils.readKeyPairFromPKCS12(TestUtils.getTestResource("twokeys.p12"), "twokeys");
			fail("Should have raised exception because of the two key pairs");
		} catch (CertificateException e) {
			assertNull(pk);
		}		
	}

	@Test
	void testCheck() {
		try {
			assertTrue(KeystoreUtils.check(TestUtils.getTestResource("singlekey.p12"), "singlekey"));
		} catch (Throwable t) {
			fail(t);
		}

		try {
			assertFalse(KeystoreUtils.check(TestUtils.getTestResource("singlekey.p12"), "wrongpassword"));
		} catch (Throwable t) {
			fail(t);
		}
		
		try {
			assertTrue(KeystoreUtils.check(TestUtils.getTestResource("singlekey.p12"), "singlekey", 
											KeystoreType.PKCS12));
		} catch (Throwable t) {
			fail(t);
		}
		try {
			assertFalse(KeystoreUtils.check(TestUtils.getTestResource("singlekey.p12"), "singlekey", 
											KeystoreType.JKS));
		} catch (Throwable t) {
			fail(t);
		}
		
		try {
			assertTrue(KeystoreUtils.check(TestUtils.getTestResource("trustedcerts.jks"), "trusted", 
											KeystoreType.JKS));
		} catch (Throwable t) {
			fail(t);
		}
		try {
			assertFalse(KeystoreUtils.check(TestUtils.getTestResource("trustedcerts.jks"), "trusted", 
											KeystoreType.PKCS12));
		} catch (Throwable t) {
			fail(t);
		}

		try {
			assertFalse(KeystoreUtils.check(TestUtils.getTestResource("notakeystore.jks"), null));
		} catch (Throwable t) {
			fail(t);
		}
		
		try {
			assertTrue(KeystoreUtils.check(TestUtils.getTestResource("sharedkey.jceks"), "sharedkey", 
											KeystoreType.JCEKS));
		} catch (Throwable t) {
			fail(t);
		}
		try {
			assertFalse(KeystoreUtils.check(TestUtils.getTestResource("sharedkey.jceks"), "sharedkey", 
											KeystoreType.JKS));
		} catch (Throwable t) {
			fail(t);
		}
	}
	
	@Test
	void testLoadPKCS12() {
		KeyStore ks = null;
		
		try {
			ks = KeystoreUtils.load(TestUtils.getTestResource("keyandcert.p12"), "keyandcert");
		} catch (Throwable t) {
			fail(t);
		}
		assertNotNull(ks);
		int n = 0, pk = 0, certs = 0;
		try {
			for(Enumeration<String> aliases = ks.aliases(); aliases.hasMoreElements();) {
				String a = aliases.nextElement(); n++;
				if (ks.isKeyEntry(a))
					pk++;
				else
					certs++;
			}
		} catch (KeyStoreException kse) {
			fail(kse);
		}
		// NOTE: the number of entries will differ when the SUN JSSE provider is used as it groups the key pair
		//  and associated certificate chain in one entry
		if ("BC".equals(ks.getProvider().getName())) {
			assertEquals(3, n);
			assertEquals(1, pk);
			assertEquals(2, certs);
		} else {
			assertEquals(2, n);
			assertEquals(1, pk);
			assertEquals(1, certs);			
		}
			
		PrivateKeyEntry kp = null;
		try {
			kp = (PrivateKeyEntry) ks.getEntry("partyc", new KeyStore.PasswordProtection("keyandcert".toCharArray()));
		} catch (Exception e) {
			fail(e);
		}
		assertNotNull(kp);
		Certificate[] chain = kp.getCertificateChain();
		assertNotNull(chain);
		assertEquals(2, chain.length);
	}
	
	@Test
	void testLoadJKS() {
		KeyStore ks = null;
		
		try {
			ks = KeystoreUtils.load(TestUtils.getTestResource("keyandcert.jks"), "keyandcert");
		} catch (Throwable t) {
			fail(t);
		}
		assertNotNull(ks);
		int n = 0, pk = 0, certs = 0;
		try {
			for(Enumeration<String> aliases = ks.aliases(); aliases.hasMoreElements();) {
				String a = aliases.nextElement(); n++;
				if (ks.isKeyEntry(a))
					pk++;
				else
					certs++;
			}
		} catch (KeyStoreException kse) {
			fail(kse);
		}
		assertEquals(2, n);
		assertEquals(1, pk);
		assertEquals(1, certs);			
		
		PrivateKeyEntry kp = null;
		try {
			kp = (PrivateKeyEntry) ks.getEntry("partya", new KeyStore.PasswordProtection("keypair".toCharArray()));
		} catch (Exception e) {
			fail(e);
		}
		assertNotNull(kp);
		Certificate[] chain = kp.getCertificateChain();
		assertNotNull(chain);
		assertEquals(1, chain.length);
	}
	
	@Test
	void testSaveJKS() {
		PrivateKeyEntry kp = null;
		try {
			KeyStore ks = KeystoreUtils.load(TestUtils.getTestResource("twokeys.p12"), "twokeys");
			kp = (PrivateKeyEntry) ks.getEntry("partyb_dev",  new KeyStore.PasswordProtection("twokeys".toCharArray()));			
		} catch (Throwable t) {
			fail(t);
		}
				
		KeyStore ks = null;
		try {
			ks = KeyStore.getInstance(KeystoreType.JKS.name());
			ks.load(null, null);
			ks.setEntry("newentry", kp, new KeyStore.PasswordProtection("newentry".toCharArray()));
		} catch (Exception e) {
			fail(e);
		}
		
		Path ksPath = TestUtils.getTestClassBasePath().resolve("newkeystore.jks");
		try {
			try {
				KeystoreUtils.save(ks, ksPath, "newkeystore");
			} catch (Throwable t) {
				fail(t);
			}
			
			try {
				ks = KeystoreUtils.load(KeystoreType.JKS, ksPath, "newkeystore");
				assertTrue(ks.containsAlias("newentry"));
				assertTrue(ks.isKeyEntry("newentry"));
				PrivateKeyEntry copyKP = (PrivateKeyEntry) ks.getEntry("newentry", 
																new KeyStore.PasswordProtection("newentry".toCharArray()));
				assertArrayEquals(kp.getCertificate().getEncoded(), copyKP.getCertificate().getEncoded());
			} catch (Throwable t) {
				fail(t);
			}
		} finally {
			try {
				Files.deleteIfExists(ksPath);
			} catch (IOException e) {
				System.err.println("Could not delete temp test file " + ksPath.toString());
			}
		}		
	}
	
	@Test
	void testSavePKCS12() {
		PrivateKeyEntry kp = null;
		try {
			KeyStore ks = KeystoreUtils.load(TestUtils.getTestResource("twokeys.p12"), "twokeys");
			kp = (PrivateKeyEntry) ks.getEntry("partyb_dev",  new KeyStore.PasswordProtection("twokeys".toCharArray()));			
		} catch (Throwable t) {
			fail(t);
		}
		
		KeyStore ks = null;
		try {
			ks = KeyStore.getInstance(KeystoreType.PKCS12.name());
			ks.load(null, null);
			ks.setEntry("newentry", kp, new KeyStore.PasswordProtection("newentry".toCharArray()));
		} catch (Exception e) {
			fail(e);
		}
		
		Path ksPath = TestUtils.getTestClassBasePath().resolve("newkeystore.pfx");
		try {
			try {
				KeystoreUtils.save(ks, ksPath, "newkeystore");
			} catch (Throwable t) {
				fail(t);
			}
			
			try {
				ks = KeystoreUtils.load(KeystoreType.PKCS12, ksPath, "newkeystore");
				assertTrue(ks.containsAlias("newentry"));
				assertTrue(ks.isKeyEntry("newentry"));
				PrivateKeyEntry copyKP = (PrivateKeyEntry) ks.getEntry("newentry", 
						new KeyStore.PasswordProtection("newentry".toCharArray()));
				assertArrayEquals(kp.getCertificate().getEncoded(), copyKP.getCertificate().getEncoded());
			} catch (Throwable t) {
				fail(t);
			}
		} finally {
			try {
				Files.deleteIfExists(ksPath);
			} catch (IOException e) {
				System.err.println("Could not delete temp test file " + ksPath.toString());
			}
		}		
	}
	
	@Test
	void testLoadJCEKS() {
		KeyStore ks = null;
		
		try {
			ks = KeystoreUtils.load(TestUtils.getTestResource("sharedkey.jceks"), "sharedkey");
		} catch (Throwable t) {
			fail(t);
		}
		assertNotNull(ks);
		int n = 0, pk = 0;
		try {
			for(Enumeration<String> aliases = ks.aliases(); aliases.hasMoreElements();) {
				String a = aliases.nextElement(); n++;
				if (ks.isKeyEntry(a))
					pk++;
			}
		} catch (KeyStoreException kse) {
			fail(kse);
		}
		assertEquals(1, n);
		assertEquals(1, pk);
	}	
}
