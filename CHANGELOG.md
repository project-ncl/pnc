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

### Added
- Added new Rest API
- [NCL-4584] Add support for Gradle build type

### Changed
- [NCL-4077] Send revision information to Maitai and Repour on build configuration creation. This helps syncing only the particular revision provided by the user, avoiding potential merge conflicts with other branches that we do not use for building
- [NCL-4236] Any authenticated user can create/edit/delete stuff. Before it was restricted to authenticated users having 'user' role
- [NCL-3643] Added NPM-specific identifier format
- [NCL-4117] Restricted editing of productVersion name when having a closed milestone to keep consistency with external data
- [NCL-4240] Add DB indexes on md5 and sha1 fields of Artifact model entity
- [NCL-4300] Fixed updating of brew pushed status
- [NCL-4282] Add Build to MQ message
- [NCL-4290] Add Build to Websocket satus update
- [NCL-4270] Persistent builds ignore successful temporary build when asking whether to rebuild
- [NCL-4345] Add Pre-alignment SCM Revision
- [NCL-3804] UI support for NPM build types
- [NCL-3635] Build Config view rebuild in UI for enhanced performance
- [NCL-4441] Added REST endpoint to restore a Build Config to a specific audited revision

## [1.4.5]

### Changed
- [NCL-4387] Update repo driver to work with consolidated hosted repository for builds

## [1.4.4]

### Added
- [NCL-4393] Add pnc service status visualizations to UI dashboard

### Fixed
- [NCL-4333] A closed milestone now cannot be set as current via REST API
- [NCL-4274] Fixed wrong links when using redhat artifacts as dependencies
- [NCL-4270] Persistent builds successfully ignore temporary build when asking whether to rebuild
- [NCL-4218] Orchestrator must work also if JMS is down
- [NCL-4381] Brew push - make buildRootVersion optional

### Changed
- [NCL-4214] Temporary builds won't be assigned to a milestone anymore

## [1.4.3]

### Changed
- [NCL-4282] New fields in the status update messages

### Deprecated
- [NCL-4282] Some fields were deprecated in the status update messages

### Added
- [NCL-4219] Add generic proxy content promotion and set the promotion target as readonly
- [NCL-4240] Add DB indexes on md5 and sha1 fields of Artifact model entity

### Fixed
- [NCL-4294] Fix credentials used to create and update Indy repositories
- [NCL-4300] Fixed updating of brew pushed status
- [NCL-4314] Build with log containing 0 byte fails to store
- [NCL-4274] Fixed wrong dependencies' links

## [1.4.2]

### Added
- [NCL-4177] Add an option to specify implicit or explicit dependency check for a rebuild
- [NCL-4192] Creating a BuildRecord with status NO_REBUILD_REQUIRED for the builds that don't need to be re-run

### Fixed
- [NCL-4235] Fix potential collision of 2 builds of the same BuildConfiguration started in 1 minute. The build repository in Indy is now named build-<BuildId>

### Changed
- [NCL-4218] Don't break the build if JMS server is down
- [NCL-4177] Force rebuild is propagated to explicit dependencies.

## [1.4.1]

### Changed
- [NCL-4214] Temporary builds won't be assigned to a milestone anymore

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
