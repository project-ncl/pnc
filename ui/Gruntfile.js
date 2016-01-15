/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2014 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
'use strict';

// DEFAULT PARAMETERS
var LIVERELOAD_PORT = 35729;
var DEFAULT_PROXY_CONFIG_FILE = './proxy-profiles.json';
var DEFAULT_PROXY_PROFILE = {
  'default': {
      context: '/pnc-rest/rest',
      host: 'localhost',
      port: 8080,
      ws: true
  }
};


module.exports = function (grunt) {

  // Load grunt tasks automatically
  require('load-grunt-tasks')(grunt);

  // Time how long tasks take. Can help when optimizing build times
  require('time-grunt')(grunt);

  // Configurable paths for the application
  var appConfig = {
    app: require('./bower.json').appPath || 'app',
    dist: 'dist',
    lib: 'bower_components',
    tmp: '.tmp'
  };

  /**
   * Task responsible for initializing REST proxy settings, by reading from a config file.
   * Proxy is needed to avoid 'cross-origin resource sharing' (CORS) restriction.
   * See README.md for more details.
   */
  grunt.registerTask('initCORSProxy', function () {

    if (!grunt.file.exists(DEFAULT_PROXY_CONFIG_FILE)) {
      grunt.file.write(DEFAULT_PROXY_CONFIG_FILE, JSON.stringify(DEFAULT_PROXY_PROFILE, null, '\t'));
    }

    var proxyProfiles = grunt.file.readJSON(DEFAULT_PROXY_CONFIG_FILE);
    var target = grunt.option('target') || 'default';

    var selectedProfiles = target.split(',');
    var config = [];

    selectedProfiles.forEach(function(profile) {
      if(proxyProfiles[profile] === undefined) {
        grunt.fatal('Unknown CORS proxy profile: "' + profile +
            '" - please check your desired profile exists in: ' +
            DEFAULT_PROXY_CONFIG_FILE);
      }

      if (Array.isArray(profile)) {
        config = config.concat(proxyProfiles[profile]);
      } else {
        config.push(proxyProfiles[profile]);
      }
    });

    grunt.config('connect.proxies', config);
  });

  // Define the configuration for all the tasks
  grunt.initConfig({

    // Project settings
    yeoman: appConfig,

    // Watches files for changes and runs tasks based on the changed files
    watch: {
      bower: {
        files: ['bower.json'],
        tasks: ['wiredep']
      },
      js: {
        files: ['<%= yeoman.app %>/**/*.js'],
        tasks: ['newer:jshint:all'],
        options: {
          livereload: '<%= connect.options.livereload %>'
        }
      },
      jsTest: {
        files: ['test/spec/{,*/}*.js'],
        tasks: ['newer:jshint:test', 'karma']
      },
      html: {
        files: ['<%= yeoman.app %>/**/*.html'],
        tasks: ['htmlhint'],
        options: {
          livereload: '<%= connect.options.livereload %>'
        }
      },
      styles: {
        files: ['<%= yeoman.app %>/**/*.css'],
        tasks: ['newer:copy:styles', 'autoprefixer']
      },
      gruntfile: {
        files: ['Gruntfile.js']
      },
      livereload: {
        options: {
          livereload: '<%= connect.options.livereload %>'
        },
        files: [
          '<%= yeoman.app %>/**/*.html',
          '{<%= yeoman.app %>,<%= yeoman.tmp %>}/styles/{,*/}*.css',
          '{<%= yeoman.app %>,<%= yeoman.tmp %>}/{,*/}*.js',
          '<%= yeoman.app %>/images/{,*/}*.{png,jpg,jpeg,gif,webp,svg,ico}'
        ]
      },
      includeSource: {
        files: ['<%= yeoman.app %>/index.html'],
        tasks: ['includeSource:server']
      }
    },

    // The actual grunt server settings
    connect: {
      options: {
        port: 9000,
        // Change this to '0.0.0.0' to access the server from outside.
        hostname: 'localhost',
        livereload: LIVERELOAD_PORT
      },
      proxies: [/* This value is set by initCORSProxy task. */],
      livereload: {
        options: {
          open: true,
          middleware: function (connect) {
            return [
              connect.static('.tmp'),
              connect().use(
                '/bower_components',
                connect.static('./bower_components')
              ),
              connect.static(appConfig.app),
              require('grunt-connect-proxy/lib/utils').proxyRequest
            ];
          }
        }
      },
      test: {
        options: {
          port: 9001,
          middleware: function (connect) {
            // Setup the proxy
            var middlewares = [];
            middlewares.push(connect.static('.tmp'));
            middlewares.push(connect.static('test'));
            middlewares.push(connect().use(
              '/<%= yeoman.lib %>',
              connect.static('./<%= yeoman.lib %>')
            ));
            middlewares.push(connect.static(appConfig.app));
            middlewares.push(require('grunt-connect-proxy/lib/utils').proxyRequest);

            return middlewares;
          }
        }
      },
      dist: {
        options: {
          open: true,
          port: 9000,
          middleware: function (connect) {
            return [
              require('grunt-connect-proxy/lib/utils').proxyRequest,
              connect.static(appConfig.dist)
            ];
          }
        }
      }
    },

    // Make sure code styles are up to par and there are no obvious mistakes
    jshint: {
      options: {
        jshintrc: '.jshintrc',
        reporter: require('jshint-stylish')
      },
      all: {
        src: [
          'Gruntfile.js',
          '<%= yeoman.app %>/**/*.js'
        ]
      },
      test: {
        options: {
          jshintrc: 'test/.jshintrc'
        },
        src: ['test/spec/{,*/}*.js']
      }
    },

    htmlhint: {
      html: {
        src: [
          '<%= yeoman.app %>/**/*.html'
        ],
        options: {
          htmlhintrc: '.htmlhintrc'
        }
      }
    },

    // Empties folders to start fresh
    clean: {
      dist: {
        files: [{
          dot: true,
          src: [
            '<%= yeoman.tmp %>',
            '<%= yeoman.dist %>/**/*',
            '!<%= yeoman.dist %>/.git{,*/}*'
          ]
        }]
      },
      server: [
        '<%= yeoman.tmp %>'
      ]
    },

    // Wires our own scripts and styles into index.html
    includeSource: {
      options: {
        basePath: 'app',
        baseUrl: '',
        ordering: 'top-down'
      },
      server: {
        files: {
          '.tmp/index.html': '<%= yeoman.app %>/index.html'
        }
      },
      dist: {
        files: {
          '<%= yeoman.dist %>/index.html': '<%= yeoman.app %>/index.html'
        }
      }
    },

    // Add vendor prefixed styles
    autoprefixer: {
      options: {
        browsers: ['last 1 version']
      },
      dist: {
        files: [{
          expand: true,
          cwd: '<%= yeoman.tmp %>/styles/',
          src: '{,*/}*.css',
          dest: '<%= yeoman.tmp %>/styles/'
        }]
      }
    },

    // Automatically inject Bower components into the app
    wiredep: {
      app: {
        src: ['<%= yeoman.app %>/index.html'],
        exclude: ['<%= yeoman.lib %>/bootstrap/dist/css/bootstrap.css'],
        ignorePath:  /\.\.\//
      }
    },

    // Renames files for browser caching purposes
    filerev: {
      dist: {
        src: [
          '<%= yeoman.dist %>/scripts{,*/}*.js',
          '<%= yeoman.dist %>/styles/{,*/}*.css',
          '<%= yeoman.dist %>/images/{,*/}*.{png,jpg,jpeg,gif,webp,svg,ico}',
          '<%= yeoman.dist %>/styles/fonts/*'
        ]
      }
    },

    // Reads HTML for usemin blocks to enable smart builds that automatically
    // concat, minify and revision files. Creates configurations in memory so
    // additional tasks can operate on them
    useminPrepare: {
      html: '<%= yeoman.dist %>/index.html',
      options: {
        dest: '<%= yeoman.dist %>',
        flow: {
          html: {
            steps: {
              js: ['concat', 'uglifyjs'],
              css: ['concat', 'cssmin']
            },
            post: {}
          }
        }
      }
    },

    usemin: {
      html: ['<%= yeoman.dist %>/{,*/}*.html'],
      css: ['<%= yeoman.dist %>/styles/{,*/}*.css'],
      js: ['<%= yeoman.dist %>/scripts/{,*/}*.js'],
      options: {
        assetsDirs: [
          '<%= yeoman.dist %>',
          '<%= yeoman.dist %>/images',
          '<%= yeoman.dist %>/styles'
        ],
        patterns: {
          js: [[/(images\/[^''""]*\.(png|jpg|jpeg|gif|webp|svg))/g, 'Replacing references to images']]
        }
      }
    },

    imagemin: {
      dist: {
        files: [{
          expand: true,
          cwd: '<%= yeoman.app %>/images',
          src: '{,*/}*.{png,jpg,jpeg,gif,ico}',
          dest: '<%= yeoman.dist %>/images'
        }]
      }
    },

    svgmin: {
      dist: {
        files: [{
          expand: true,
          cwd: '<%= yeoman.app %>/images',
          src: '{,*/}*.svg',
          dest: '<%= yeoman.dist %>/images'
        }]
      }
    },

    htmlmin: {
      dist: {
        options: {
          /*
          Watch out for issue: https://github.com/yeoman/grunt-usemin/issues/44
          In case it doesn't work, comment out all the options below
          */
          collapseWhitespace: true,
          conservativeCollapse: true,
          collapseBooleanAttributes: true,
          removeCommentsFromCDATA: true,
          removeComments: true,

          removeOptionalTags: true
        },
        files: [{
          expand: true,
          cwd: '<%= yeoman.dist %>',
          src: ['**/*.html'],
          dest: '<%= yeoman.dist %>'
        }]
      }
    },

    ngtemplates: {
      dist: {
        options: {
          module: 'pnc',
          htmlmin: '<%= htmlmin.dist.options %>',
          usemin: 'scripts/pnc.js'
        },
        cwd: '<%= yeoman.app %>',
        src: [
          '**/*.html',
          '!index.html'
        ],
        dest: '.tmp/templateCache.js'
      }
    },

    // ng-annotate tries to make the code safe for minification automatically
    // by using the Angular long form for dependency injection.
    ngAnnotate: {
      dist: {
        files: [{
          expand: true,
          cwd: '.tmp/concat/scripts',
          src: ['*.js', '!oldieshim.js'],
          dest: '.tmp/concat/scripts'
        }]
      }
    },

    // Copies remaining files to places other tasks can use
    copy: {
      // we need to put patternfly fonts to the correct destination
      // ( https://github.com/patternfly/patternfly/issues/20 )
      fonts: {
        files: [
          {
            expand: true,
            dot: true,
            cwd: '<%= yeoman.lib %>/font-awesome/fonts/',
            dest: '<%= yeoman.tmp %>/fonts/',
            src: [ '**' ]
          },
          {
            expand: true,
            dot: true,
            cwd: '<%= yeoman.lib %>/patternfly/dist/fonts/',
            dest: '<%= yeoman.tmp %>/fonts/',
            src: [ '**' ]
          }
        ]
      },
      dist: {
        files: [{
          expand: true,
          dot: true,
          cwd: '<%= yeoman.app %>',
          dest: '<%= yeoman.dist %>',
          src: [
            '*.{ico,png,txt}',
            'images/{,*/}*.{webp}',
            'fonts/{,*/}*.*',
            'keycloak.json',
            'config.json'
          ]
        }, {
          expand: true,
          cwd: '.tmp/images',
          dest: '<%= yeoman.dist %>/images',
          src: ['generated/*']
        }, {
          expand: true,
          cwd: '<%= yeoman.lib %>/bootstrap/dist',
          src: 'fonts/*',
          dest: '<%= yeoman.dist %>'
        },{
          expand: true,
          dot: true,
          cwd: '<%= yeoman.lib %>/font-awesome/fonts/',
          dest: '<%= yeoman.dist %>/fonts/',
          src: [ '**' ]
        },
        {
          expand: true,
          dot: true,
          cwd: '<%= yeoman.lib %>/patternfly/dist/fonts/',
          dest: '<%= yeoman.dist %>/fonts/',
          src: [ '**' ]
        }]
      },
      styles: {
        expand: true,
        cwd: '<%= yeoman.app %>/styles',
        dest: '.tmp/styles/',
        src: '**/*.css'
      }
    },

    // Run some tasks in parallel to speed up the build process
    concurrent: {
      server: [
        'copy:styles'
      ],
      test: [
        'copy:styles'
      ],
      dist: [
        'imagemin',
        'svgmin'
      ]
    },

    // Test settings
    karma: {
      unit: {
        configFile: 'test/karma.conf.js',
        singleRun: true
      }
    },

    bower: {
      install: {
        options: {
          targetDir: '<%= yeoman.lib %>'
        }
      }
    }
  });

  grunt.registerTask('serve', 'Compile then start a connect web server', function (target) {
    if (target === 'dist') {
      return grunt.task.run(['build', 'connect:dist:keepalive']);
    }

    grunt.task.run([
      'initCORSProxy',
      'clean:server',
      'wiredep',
      'includeSource:server',
      'concurrent:server',
      'copy:fonts',
      'autoprefixer',
      'configureProxies',
      'connect:livereload',
      'watch'
    ]);
  });

  grunt.registerTask('server', 'DEPRECATED TASK. Use the "serve" task instead', function (target) {
    grunt.log.warn('The `server` task has been deprecated. Use `grunt serve` to start a server.');
    grunt.task.run(['serve:' + target]);
  });

  grunt.registerTask('test', [
    'initCORSProxy',
    'clean:server',
    'concurrent:test',
    'autoprefixer',
    'connect:test'/*,
    'karma'*/
  ]);

  grunt.registerTask('build', [
    'initCORSProxy',
    'clean:dist',
    'copy:fonts',
    'wiredep',
    'includeSource:dist',
    'useminPrepare',
    'concurrent:dist',
    'autoprefixer',
    'ngtemplates',
    'concat',
    'ngAnnotate',
    'copy:dist',
    'cssmin',
    'uglify',
    'filerev',
    'usemin',
    'htmlmin'
  ]);

  grunt.registerTask('default', [
    'newer:jshint',
    'newer:htmlhint',
    'test',
    'build'
  ]);

  grunt.registerTask('dist', [
    'bower:install',
    'newer:jshint',
    'newer:htmlhint',
    'test',
    'build'
  ]);

};
