package org.holodeckb2b.commons.security;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.nio.file.Path;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import org.holodeckb2b.commons.testing.TestUtils;
import org.holodeckb2b.commons.util.Utils;
import org.junit.jupiter.api.Test;

class CertificateUtilsTest {

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
		
		String dn = cert.getSubjectDN().getName();	
		assertAll("DN check", () -> assertTrue(dn.contains("CN=partya.examples.holodeck-b2b.com")),
						      () -> assertTrue(dn.contains("OU=Holodeck B2B Support")),
						      () -> assertTrue(dn.contains("O=Chasquis")),
						      () -> assertTrue(dn.contains("C=NL")));		
		assertEquals(0x1005, cert.getSerialNumber().intValue());
		
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
		String mimeB64String 
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
		
		StringBuilder buf = new StringBuilder(); 
		for (String line : mimeB64String.split("\n"))
			buf.append(line);
		String longB64String = buf.toString();
		
		String pemString = "-----BEGIN CERTIFICATE-----\n" + mimeB64String + "-----END CERTIFICATE-----";
		String pemWithPrologString = "some-prop-to-ignore: partya\n" + pemString;
				
		X509Certificate cert = null;		
		try {
			cert = CertificateUtils.getCertificate(mimeB64String);
		} catch (Throwable t) {
			fail(t);			
		}		
		assertPartyACert(cert);
		
		try {
			cert = CertificateUtils.getCertificate(longB64String);
		} catch (Throwable t) {
			fail(t);			
		}		
		assertPartyACert(cert);
		
		try {
			cert = CertificateUtils.getCertificate(pemString);
		} catch (Throwable t) {
			fail(t);			
		}		
		assertPartyACert(cert);		
		
		try {
			cert = CertificateUtils.getCertificate(pemWithPrologString);
		} catch (Throwable t) {
			fail(t);			
		}		
		assertPartyACert(cert);		
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
