###
# JBoss, Home of Professional Open Source.
# Copyright 2014 Red Hat, Inc., and individual contributors
# as indicated by the @author tags.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
###
#!/usr/bin/env bash

#usage
# release.sh <version>
# eg. release.sh 1.4.0

set -e
set -x

version=$1
mvn versions:set -DnewVersion=$version
mvn versions:commit

sed -i -e "s/<tag>HEAD<\/tag>/<tag>${version}<\/tag>/g" pom.xml

git commit -am "Prepare for release ${version}"

mvn clean deploy -P release,production,auth,without-messaging -DskipTests

