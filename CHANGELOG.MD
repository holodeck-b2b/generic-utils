# Change Log
All notable changes to this project will be documented in this file.
This project adheres to [Semantic Versioning](http://semver.org/).

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