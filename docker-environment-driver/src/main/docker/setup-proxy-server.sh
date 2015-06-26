#!/bin/bash
#
# sets proxy server in maven settings.xml file from environment variables
# proxyIPAddress
# proxyPort

file=/usr/share/maven/conf/settings.xml

if [[ ! -z "$proxyIPAddress" ]]; then
   if [[ ! -z "$proxyPort" ]]; then
      sed -i "s/\${proxyIPAddress}/${proxyIPAddress}/" $file
      sed -i "s/\${proxyPort}/${proxyPort}/" $file
      sed -i "s/\${isHttpActive}/true/" $file
   fi
fi
