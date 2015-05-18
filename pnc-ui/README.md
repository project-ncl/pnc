# PNC - UI 

### Setting Up The Development Environment

Pre-reqs:

* Node.js - [Download Here](http://nodejs.org/)
* Bower( version >= 0.9.1 ) - [Download Here](http://bower.io/)

Once the pre-reqs have been taken care of, run:

    npm install -g grunt-cli bower
    
    npm install

    bower install


Run:

    grunt initRestConfig

This task will create a file `rest-config.json` with default values:

    {
        "endpointsLocalhost": "localhost"
    };
    
`endpointsLocalhost` points to a local installation of PNC application.


 while `endpointsCIServer` points to a remote installation of PNC application (defaults to PNC CI environment). This is useful if you want to develop UI without having a local installation of PNC, as you would consume REST endpoints from the remote installation.

If you wish to develop without a local installation of PNC, you can edit `rest-config.json` to add a remote server as an additional profile like so:
    
    {
        "endpointsLocalhost": "localhost",
        "endpointsCIServer": "my-ci-server.example.com"
    };

To run consuming REST endpoints from the local installation (`endpointsLocalhost` value):

    grunt serve


To run consuming REST endpoints from the remote installation (`endpointsCIServer` value):

    grunt serve --target=CIEndpoints 

    
Now everytime you save a file (html, css, js), Grunt is watching and will copy to configured directories, performing a livereload.

_note: To make the consuming of remote REST endpoints working without incurring in the Cross-origin resource sharing (CORS) restriction, the `grunt-connect-proxy` has been used. Basically, all the request sent to `/pnc-web/rest` are proxied to `<endpointsLocalhost>`:`8080` (or `<endpointsCIServer>`:`8080`), while letting the browser think that they are all in the right domain._


#### Grunt watch ENOSPC error

The system has a limit to how many files can be watched by a user. You can run out of watches pretty quickly if you have Grunt running with other programs like Dropbox. This command increases the maximum amount of watches a user can have (refer to http://stackoverflow.com/questions/16748737/grunt-watch-error-waiting-fatal-error-watch-enospc):

    echo fs.inotify.max_user_watches=524288 | sudo tee -a /etc/sysctl.conf && sudo sysctl -p

_note: For Arch Linux add `fs.inotify.max_user_watches=524288` to `/etc/sysctl.d/99-sysctl.conf` and then execute `sysctl --system`. This will also persist across reboots_

### Generate distribution

To create a distribution in `pnc-ui/dist/` directory:

    grunt dist
    
In order to create a WAR application, go into `../pnc-web/` folder and run Maven build:

    cd ../pnc-web/
    mvn clean install
    
Similarly, in order to create the full EAR application, go into the root path of PNC and run Maven build:

    cd ../
    mvn clean install

### Cleaning the PNC UI build

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

### Authentication

The UI can be built with authentication either enabled or disabled, by default it is disabled. Authentication can be enabled at build time, for both the UI and backend, by adding the flag: `-Dauth=true` to your maven build command.

At the UI level authentication can be enabled through grunt by adding the flag `--enable-auth=true` to your grunt command. This will also work when using the grunt live-reload server, for example by using the command `grunt serve --enable-auth=true`.

### Build errors

The `frontend-maven-plugin` build may suffer from inconsistent downloads when you killed the previous build prematurely. This typically leads to such errors:

    [INFO] --- frontend-maven-plugin:0.0.16:grunt (grunt build) @ unifiedpush-admin-ui ---
    [INFO] Running 'grunt dist --no-color'
    [INFO] module.js:340
    [INFO]     throw err;
    [INFO]           ^
    [INFO] Error: Cannot find module 'findup-sync'

or

    [INFO] --- frontend-maven-plugin:0.0.16:npm (npm install) @ unifiedpush-admin-ui ---
    [INFO] Running 'npm install --color=false'
    [INFO] npm ERR! cb() never called!
    [INFO] npm ERR! not ok code 0

The build currently can't recover itself from these error.

In order to fix this issue, you should fully clean the `pnc-ui/` build resources:

    mvn clean install -Dfrontend.clean.force

