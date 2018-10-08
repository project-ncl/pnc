/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2018 Red Hat, Inc., and individual contributors
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
(function () {
  'use strict';

  angular.module('pnc.build-records').controller('RecordRepourResultController', [
    'repourLog',
    'REST_BASE_URL',
    'BUILD_RECORD_ENDPOINT',
    'recordDetail',
    function(repourLog, REST_BASE_URL, BUILD_RECORD_ENDPOINT, recordDetail) {
      this.logUrl = REST_BASE_URL + BUILD_RECORD_ENDPOINT.replace(':recordId', recordDetail.id) + '/repour-log';
      this.logFileName = recordDetail.id + '_' + recordDetail.buildConfigurationName + '_' + recordDetail.status + '_repour-log.txt';
      this.log = repourLog.payload;
    }
  ]);

})();
