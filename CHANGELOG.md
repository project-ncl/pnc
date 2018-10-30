# Template

## [<version>] - <yyyy>-<mm>-<dd>
### Added
- Section

### Changed
- Section

### Deprecated
- Section

### Removed
- Section

### Fixed
- Section

### Security
- Section


# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/en/1.0.0/)
and this project adheres to [Semantic Versioning](http://semver.org/spec/v2.0.0.html).


## [Unreleased]

### Changed
- [NCL-4214]: Temporary builds won't be assigned to a milestone anymore
- [NCL-4077] Send revision information to Maitai and Repour on build configuration creation. This helps syncing only the particular revision provided by the user, avoiding potential merge conflicts with other branches that we do not use for building

## [1.4.0]

### Added
- [NCL-3231] Allow editing of the brew tag prefix
- [NCL-3446] Add log text wrapping option
- [NCL-3164] Add SCM repositories management page
- [NCL-3896] Add Build Group Record cancel button
- [NCL-3978] Add dependent builds tree view
- [NCL-4050] Provide a way to download plaintext version of console logs
- [NCL-3993] Force user re-authentication from UI in case of the user token is close to expire when starting a build
- [NCL-4058] Added favicon
- [NCL-3891] Support building a specific revision of a build configuration
- [NCL-4089] Introduce EXECUTION_ROOT_NAME generic param - This is used to override the default value, and can be useful for builds that disable PME. Format is '<groupid>:<artifactid>'
- [NCL-3920] Browser now shows page titles based on displayed page contents
- [NCL-4153] Generic http downloads are stored persistently and handle the changes on remote server correctly

### Changed
- [NCL-3932] Use builds-untested+shared-imports+public in build groups for better usage of indexes in Indy
- [NCL-3886] Relax milestone version restrictions to support Continuous Delivery
- [NCL-4048] Use human friendly identifier for RCs instead of DB ID
- [NCL-4097] Update status icons on build tree when websocket notifications received
- [NCL-4051] Increase size of build script widget in BC edit view

### Fixed
- [NCL-3966] Fix wrong projectName in BuildRecordRest entity
- [NCL-3761] Fix /build-records/get-by-attribute return object
- [NCL-4052] Fixed broken icon for Rejected builds
- [NCL-3698] Brew Push tab appears when push completes / fails without user refresh
- [NCL-4021] Handle rejected build group status correctly in the UI
- [NCL-4100] Fix no push button on Build Group Record page
    
### Security
- [NCL-3549] Use service account when managing repositories in Indy
