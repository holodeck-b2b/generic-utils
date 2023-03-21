package org.holodeckb2b.commons.security;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.math.BigInteger;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.List;

import javax.security.auth.x500.X500Principal;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.holodeckb2b.commons.testing.TestUtils;
import org.holodeckb2b.commons.util.Utils;
import org.junit.jupiter.api.Test;

class CertificateUtilsTest {

	private static final String PARTYA_MIME64_STRING
				= "MIIFvjCCA6agAwIBAgICEAUwDQYJKoZIhvcNAQELBQAwZjELMAkGA1UEBhMCTkwx\n"
				+ "ETAPBgNVBAoMCENoYXNxdWlzMR0wGwYDVQQLDBRIb2xvZGVjayBCMkIgU3VwcG9y\n"
				+ "dDElMCMGA1UEAwwcY2EuZXhhbXBsZXMuaG9sb2RlY2stYjJiLm9yZzAeFw0yMDA3\n"
				+ "MjkxMTUwMTVaFw0yMTA4MDgxMTUwMTVaMGoxCzAJBgNVBAYTAk5MMREwDwYDVQQK\n"
				+ "DAhDaGFzcXVpczEdMBsGA1UECwwUSG9sb2RlY2sgQjJCIFN1cHBvcnQxKTAnBgNV\n"
				+ "BAMMIHBhcnR5YS5leGFtcGxlcy5ob2xvZGVjay1iMmIuY29tMIICIjANBgkqhkiG\n"
				+ "9w0BAQEFAAOCAg8AMIICCgKCAgEA4T98DsywFKLH6UYqV8N9P8gTbdCEPbb5Gm8n\n"
				+ "dnCWUwSFwVX4CCMwHHAIxxy2gdf4lb7XUzOD6WahQsdpM8Fwcj+SX2HJHtpt6JS6\n"
				+ "Cu9QlPxp5MXW0gWyYv7+RLE2Xj+KM2++b/stBC1I6kjUyevtGmea9ufOA3XEJ5jO\n"
				+ "iQ+afk34UAlN9Ta+qpwrtJKxRq6SIB8zaGlU0OsEVZPP2a1QpBVm/1axbG4XRp+Q\n"
				+ "F7mSh0PV1g2ICrE4xXPqqIWdiTKzTWl4xePnLCxdFQkXOjPxo+GAjNnNhXdtaZS+\n"
				+ "KUN2yLIw0Xay3I8HeLMGBHhAIOHBHvwng367RjO3zwbgvt5dcEKWVF57aOBoksGa\n"
				+ "fEfqhN6KNqZM9d8/Aq46GiqHw/2JtEHledKRW8+9S0ri9yAo7vr2RiHQt74Ey+K+\n"
				+ "+NxpHMmAEmnTwK1ki40Lmeih3oKRucUOOWF62K4T++u7X71xkznIeEGxLznSqnPD\n"
				+ "8mwowHN3StQFiMn+Xt66m+a+K3F3NlWYkzeZRPrEA0Wqv6K+z0MbB3JYv1CXuhb5\n"
				+ "kYEGEqsau395/yrn/MbU8+iWU7fNASlHBktwMXHm9NKcuLqiF8TuamZ/5XVBuPIe\n"
				+ "XwuTcdoOh2wxoH9hZDwerkBHJUOgLiUG4Rh6H332uBljkIESqe1eDEWbPNlHlTpt\n"
				+ "Kxjb5YcCAwEAAaNyMHAwCQYDVR0TBAIwADAOBgNVHQ8BAf8EBAMCBeAwEwYDVR0l\n"
				+ "BAwwCgYIKwYBBQUHAwIwHQYDVR0OBBYEFAPf9TzA6vwmsJlWTQY068Zjcks+MB8G\n"
				+ "A1UdIwQYMBaAFGogotBTFmhJkji5a7pAr+ggs75/MA0GCSqGSIb3DQEBCwUAA4IC\n"
				+ "AQAAdZR/Z4GT6wWwN4RjLF/f8ijlGACHEWcWhv2KhcnRp2wHvL2plRBGQ31N7q5R\n"
				+ "9N0ZOQnSYA3ZVcRmrqNkGWKcXqNdaU1Cs9XPwULbUjU9rN6tBsv7fgx+bla5Ihza\n"
				+ "z6xzed+3qj71P2mZP6DAyoFNVs55V0/86hpJ6VV/bVuMX24harNtTf9IwJBLw4v/\n"
				+ "0+b9w1vET2YYv+NQv649jvD72N5UBjXhLImP2xXVf6O10hZ1mUOZG73RzBBRuDYW\n"
				+ "xX9gAHum/B1Xr6xTVfdfYM7TQHCNlBaZ9ta0viKfunoYSdnIxmWxyAch6qVGb/La\n"
				+ "9KfL5hcyAHPk/m/wvBTw61YzzxxbRbExAnpKBHog2n4Sbzd8ehVrjEASYrNl18d8\n"
				+ "jvk0GcYHkU/P7r8g/p3eVISmjiCflbiTi8/rYo4XuvGWMBR7Mu6OEkBvhJdm0Xti\n"
				+ "s9+7JdJQ/gR3OEnWXokNQKaw/wMBahsHWkavfvOvSya2gn8IOSvhwwIesmxRIZO/\n"
				+ "8qZB5T3/GZrFCQjRp8R9QUgczc8IRG8VHWvMxMQpBXoi/B4otPr1GeVOJ3mNo05w\n"
				+ "smTS+lMXeWUtHerJj7PNf3qNqF7cfxKv4ynAl/qDE9U76keJorX4YXqUcB0+nPov\n"
				+ "LXcT5cmBULZeMLWJKttYX+VqVEImUsOrUPGCLJZWpYj5kQ==\n";

	@Test
	void testGetCertFromFileInvalids() {
		assertThrows(IllegalArgumentException.class, () -> CertificateUtils.getCertificate((Path) null));

		assertThrows(CertificateException.class,
								() -> CertificateUtils.getCertificate(TestUtils.getTestResource("image.png")));
	}

	@Test
	void testGetCertFromFilePEM() {

		X509Certificate cert = null;
		try {
			cert = CertificateUtils.getCertificate(TestUtils.getTestResource("partya.cert"));
		} catch (Throwable t) {
			fail(t);
		}

		assertPartyACert(cert);
	}

	@Test
	void testGetCertFromFileDER() {

		X509Certificate cert = null;
		try {
			cert = CertificateUtils.getCertificate(TestUtils.getTestResource("partya.der"));
		} catch (Throwable t) {
			fail(t);
		}

		assertPartyACert(cert);
	}

	@Test
	void testGetCertFromStringInvalids() {
		try (FileInputStream fis = new FileInputStream(TestUtils.getTestResource("image.png").toFile());
			 ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
			 Utils.copyStream(fis, bos);

			 assertThrows(CertificateException.class, () -> CertificateUtils.getCertificate(bos.toByteArray()));
		} catch (Throwable t) {
			fail(t);
		}

		assertThrows(CertificateException.class,
						() -> CertificateUtils.getCertificate("certainly this is not a valid certificate"));

		X509Certificate cert = null;
		try {
			cert = CertificateUtils.getCertificate("");
		} catch (Throwable t) {
			fail(t);
		}
		assertNull(cert);
		try {
			cert = CertificateUtils.getCertificate((String) null);
		} catch (Throwable t) {
			fail(t);
		}
		assertNull(cert);
	}

	@Test
	void testGetCertFromString() {
		StringBuilder buf = new StringBuilder();
		for (String line : PARTYA_MIME64_STRING.split("\n"))
			buf.append(line);
		String longB64String = buf.toString();

		String pemString = "-----BEGIN CERTIFICATE-----\n" + PARTYA_MIME64_STRING + "-----END CERTIFICATE-----";
		String pemWithPrologString = "some-prop-to-ignore: partya\n" + pemString;
		String pemWithEpilogString = pemString + "some-stuff-to-ignore\n";

		assertPartyACert(assertDoesNotThrow(() -> CertificateUtils.getCertificate(PARTYA_MIME64_STRING)));
		assertPartyACert(assertDoesNotThrow(() -> CertificateUtils.getCertificate(longB64String)));
		assertPartyACert(assertDoesNotThrow(() -> CertificateUtils.getCertificate(pemString)));
		assertPartyACert(assertDoesNotThrow(() -> CertificateUtils.getCertificate(pemWithPrologString)));
		assertPartyACert(assertDoesNotThrow(() -> CertificateUtils.getCertificate(pemWithEpilogString)));
	}

	@Test
	void testGetCertificates() {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try (FileInputStream fis = new FileInputStream(TestUtils.getTestResource("device.cert").toFile())) {
			baos.write("-----BEGIN CERTIFICATE-----\n".getBytes());
			baos.write(PARTYA_MIME64_STRING.getBytes());
			baos.write("-----END CERTIFICATE-----\n".getBytes());
			Utils.copyStream(fis, baos);
		} catch (Exception e) {
			fail(e);
		}

		List<X509Certificate> chain = assertDoesNotThrow(() ->
									CertificateUtils.getCertificates(new ByteArrayInputStream(baos.toByteArray())));

		assertEquals(2, chain.size());
		assertPartyACert(chain.get(0));
		assertEquals(assertDoesNotThrow(() ->
					 CertificateUtils.getCertificate(TestUtils.getTestResource("device.cert"))), chain.get(1));
	}

	@Test
	void testGetPEMEncoded() {
		X509Certificate cert = assertDoesNotThrow(() -> CertificateUtils.getCertificate(PARTYA_MIME64_STRING));

		String pem = assertDoesNotThrow(() -> CertificateUtils.getPEMEncoded(cert));

		assertEquals("-----BEGIN CERTIFICATE-----\n" + PARTYA_MIME64_STRING + "-----END CERTIFICATE-----", pem);
	}

	@Test
	void testGetSubjectCN() {
		X509Certificate cert = null;
		try {
			cert = CertificateUtils.getCertificate(TestUtils.getTestResource("partya.cert"));
		} catch (CertificateException e) {
			fail(e);
		}
		assertEquals("partya.examples.holodeck-b2b.com", CertificateUtils.getSubjectCN(cert));
	}

	@Test
	void testGetSubjectSN() {
		X509Certificate cert = null;
		try {
			cert = CertificateUtils.getCertificate(TestUtils.getTestResource("device.cert"));
		} catch (CertificateException e) {
			fail(e);
		}
		assertEquals("000102637-T", CertificateUtils.getSubjectSN(cert));
	}

	@Test
	void testHasSKI() {
		X509Certificate cert =
				assertDoesNotThrow(() -> CertificateUtils.getCertificate(TestUtils.getTestResource("device.cert")));

		byte[] skiExtValue = cert.getExtensionValue("2.5.29.14");
		byte[] ski = Arrays.copyOfRange(skiExtValue, 4, skiExtValue.length);

		assertTrue(CertificateUtils.hasSKI(cert, ski));

		byte[] randomSki = Long.toHexString(Double.doubleToLongBits(Math.random())).getBytes();
		assertFalse(CertificateUtils.hasSKI(cert, randomSki));
	}

	@Test
	void testHasIssuerSerial() {
		X509Certificate cert =
				assertDoesNotThrow(() -> CertificateUtils.getCertificate(TestUtils.getTestResource("device.cert")));

		X500Principal issuer = cert.getIssuerX500Principal();
		BigInteger serial = cert.getSerialNumber();

		assertTrue(CertificateUtils.hasIssuerSerial(cert, issuer, serial));
		String rIssuer = X500Name.getInstance(BCStyle.INSTANCE, cert.getIssuerX500Principal().getEncoded()).toString();
		assertTrue(CertificateUtils.hasIssuerSerial(cert, new X500Principal(rIssuer), serial));
		
		assertFalse(CertificateUtils.hasIssuerSerial(cert, issuer, serial.add(serial)));
		assertFalse(CertificateUtils.hasIssuerSerial(cert,
								new X500Principal("CN=Tester, OU=Testing, O=HolodeckB2B, C=NL"), serial.add(serial)));
	}

	@Test
	void testHasThumbprint() {
		X509Certificate cert =
				assertDoesNotThrow(() -> CertificateUtils.getCertificate(TestUtils.getTestResource("partya.cert")));

		MessageDigest sha1 = assertDoesNotThrow(() -> MessageDigest.getInstance("SHA1"));
		MessageDigest sha2 = assertDoesNotThrow(() -> MessageDigest.getInstance("SHA-256"));

		byte[] sha1Hash = assertDoesNotThrow(() -> sha1.digest(cert.getEncoded()));
		byte[] sha2Hash = assertDoesNotThrow(() -> sha2.digest(cert.getEncoded()));

		assertTrue(CertificateUtils.hasThumbprint(cert, sha2Hash, sha2));
		assertFalse(CertificateUtils.hasThumbprint(cert, sha1Hash, sha2));
		assertFalse(CertificateUtils.hasThumbprint(cert, sha2Hash, sha1));
	}

	/**
	 * Helper method to assert that the given certificate is the test certificate issued to <i>partya</i>.
	 *
	 * @param cert 	certificate to test
	 */
	private void assertPartyACert(X509Certificate cert) {
		assertNotNull(cert);
		String dn = cert.getSubjectDN().getName();
		assertAll("DN check", () -> assertTrue(dn.contains("CN=partya.examples.holodeck-b2b.com")),
				() -> assertTrue(dn.contains("OU=Holodeck B2B Support")),
				() -> assertTrue(dn.contains("O=Chasquis")),
				() -> assertTrue(dn.contains("C=NL")));
		assertEquals(0x1005, cert.getSerialNumber().intValue());

	}
}
