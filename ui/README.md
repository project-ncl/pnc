# PNC - UI

### Setting Up The Development Environment

Pre-reqs:

* Node.js - [Download Here](http://nodejs.org/)
* Bower( version >= 0.9.1 ) - [Download Here](http://bower.io/)

Once the pre-reqs have been taken care of, run:

    npm install -g grunt-cli bower

    npm install

    bower install

### Running a live reload server
During development it is useful to run the UI in a local server that will watch the file system and automatically reload whenever any files are changed. In order to do this you'll need to set up some environment variables to tell the UI where to find the various PNC services:

```bash
PNC_UI_PNC_URL #The location of the PNC REST API
PNC_UI_PNC_NOTIFICATIONS_URL #The location of the PNC REST websocket notifications endpoint
PNC_UI_DA_URL #The location of the Dependency Analyzer REST API
PNC_UI_DA_IMPORT_URL #The location of the Dependency Analyzer Build Configuration Generator endpoint
PNC_UI_DA_IMPORT_RPC_URL #The location of the Dependency Analyzer WebSocket JSON-RPC endpoint
PNC_UI_KEYCLOAK_URL #The location of the keycloak server
PNC_UI_KEYCLOAK_REALM #The keycloak realm to authenticate with
PNC_UI_KEYCLOAK_CLIENT_ID #The keycloak client id
```
Example configuration:

```bash
export PNC_UI_PNC_URL=http://127.0.0.1:8080/pnc-rest/rest
export PNC_UI_PNC_NOTIFICATIONS_URL=ws://127.0.0.1:8080/pnc-rest/ws/build-records/notifications
export PNC_UI_DA_URL=http://127.0.0.1/da/rest/v-0.4
export PNC_UI_DA_IMPORT_URL=http://127.0.0.1/da-bcg/rest/v-0.3
export PNC_UI_DA_IMPORT_RPC_URL=ws://127.0.0.1/da-bcg/ws
export PNC_UI_KEYCLOAK_URL=https://127.0.0.1/auth
export PNC_UI_KEYCLOAK_REALM=pnc
export PNC_UI_KEYCLOAK_CLIENT_ID=pncweb
```

With these configurations set you can run a development server by simply running:

    grunt serve

### Building the UI

To build the UI:

    grunt dist

This will produce a set of minified resources in the `/dist` folder.

### Generating a distribution

To create a .jar package of the ui:

    mvn clean package

This will build the UI as listed above and then package it in a jar file.

### Invalidating build caches

For sake of quick development turnaround, the `$ mvn clean` will clean just `dist/` and `.tmp/` build directories, but some frontend build related directories will be still cached (`node/`, `node_modules/`, `app/bower_components/`, `.build-tmp`). In order to clean all build related caches, execute:

    mvn clean install -Dfrontend.clean.force


### Managing NPM packages

The versions of packages listed in `package.json` and their transitive dependencies has to be locked down leveraging [NPM Shrinkwrap tool](http://blog.nodejs.org/2012/02/27/managing-node-js-dependencies-with-shrinkwrap/) (standard part of NPM distribution).

Use of [semantic versioning](https://github.com/npm/node-semver) in NPM makes Node module versions resolution in `package.json` undeterministic. `npm-shrinkwrap.json` is an equivalent of `package.json` that locks down all the transitive dependencies.

#### Use of shrink-wrapped NPM configuration

For final user, nothing changes:

    npm install

You just need to be aware that `npm-shrinkwrap.json` configuration takes precedence.

#### Upgrading dependencies

The biggest change comes with changing dependency versions, since simple change of `package.json` won't have any effect. In order to upgrade a package, you can use approach like following one:

    $ npm install <package>@<version> --save--dev

Test the build to verify that the new versions work as expected

To lock down version again:

    $ npm shrinkwrap --dev
    $ git add package.json npm-shrinkwrap.json
    $ git commit -m "upgrading <package> to <version>"

Alternatively, you can remove `npm-shrinkwrap.json` and generate a new one.

### Build errors

In case of build errors with the UI, the first troubleshooting step should always be to run to flush the UI build caches, [see here](#invalidating-build-caches).

#### Grunt watch ENOSPC error

The system has a limit to how many files can be watched by a user. You can run out of watches pretty quickly if you have Grunt running with other programs like Dropbox. This command increases the maximum amount of watches a user can have (refer to http://stackoverflow.com/questions/16748737/grunt-watch-error-waiting-fatal-error-watch-enospc):

    echo fs.inotify.max_user_watches=524288 | sudo tee -a /etc/sysctl.conf && sudo sysctl -p

_note: For Arch Linux add `fs.inotify.max_user_watches=524288` to `/etc/sysctl.d/99-sysctl.conf` and then execute `sysctl --system`. This will also persist across reboots_
