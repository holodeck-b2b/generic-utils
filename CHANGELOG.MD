# Change Log
All notable changes to this project will be documented in this file.
This project adheres to [Semantic Versioning](http://semver.org/).

## 1.2.1
##### 2023-03-21
### Fixed
* Incorrect equality check of issuer in `CertificateUtils.hasIssuerSerial()`

## 1.2.0
##### 2023-02-28
### Added
* Method `CertificateUtils.getCertificate(InputStream)` to read a X509 certificate from an input stream
* Method `CertificateUtils.hasSKI()` to determine if a certificate has the specified _Subject Key Identifier_
* Method `CertificateUtils.hasIssuerSerial()` to determine if a certificate is issued by the specified issuer and has
  the specified serial number
* Method `CertificateUtils.hasThumbprint()` to determine if a certificate has the specified thumbprint
* Method `CertificateUtils.getIssuerCN(X509Certificate)` to get the CN of the certificate's issuer
* Method `Utils.toCSLString(int [])` to convert an integer to a comma-separated list String representation
* Method `Utils.isTrue(Boolean)` to check if given Boolean is not null and true
* Method `CertificateUtils.getCertificates(InputStream)` to read a X509 certificate chain from an input stream

## 1.1.1
##### 2022-07-11
### Changed
* Project available on Maven Central
* Updated README with Maven coordinates
* Dependency on Tika to version 2.4.1
* Dependency on BouncyCastle to version 1.71

### Fixed
* Errors in Javadocs
* Problem in repeatly running test class `FileUtilsTest`
* NPE in `Utils.nullSafeEqual(ZonedDateTime, ZonedDateTime)`

## 1.1.0
##### 2022-05-10
### Added
* Method `Utils.nullSafeEqual(ZonedDateTime, ZonedDateTime)` to compare two date times for equality, i.e. if they
  represent the same spot on the time line
* `requireNotNull` methods in `org.holodeckb2b.commons.util.Utils` to ensure that a String/Collection/Map/Iterator
  is not `null` or empty.
* Method `CertificateUtils.getPEMEncoded()` to get the PEM encoding of a certificate
* Method `CertificateUtils.getSubjectName()` to get the Subject's name of a certificate
* Method `CertificateUtils.getIssuerName()` to get the Issuer's name of a certificate
* Methods `KeystoreUtils.readKeyPairFromKeystore(InputStream, String)` and `KeystoreUtils.saveKeyPairToPKCS12()` to read
  and write the key pair from/to a PKCS12 input/output stream
* Added methods `setResponseEntityBody()` and `getResponseHeaders()` to `HttpBackendMock` to set the http response
  entity respectively the response http headers.
* Added method `clearRequests()` to `HttpBackendMock` to clear request data

### Fixed
* Incorrect handling of PEM end boundary

## 1.0.0
##### 2021-05-11
Initial version of the project based on common utility code extracted from Holodeck B2B, BDXR and internal Chasquis
projects.

### Added
* Add support for JCEKS type key stores

### Changed
* Make `MessageIdUtils.sanitizeId()` method to ensure only valid characters in a RFC2822 MessageId public

### Fixed
* Incomplete copying of stream in `Utils.copyStream()`