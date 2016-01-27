#!/bin/sh
#
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
#

#
# This script limit outband to defined $firewallAllowedDestinations IP addresses and its ports

# Check if we would like to create network isolation
if [[ -z "$firewallAllowedDestinations" ]]; then
  echo "\$firewallAllowedDestinations not defined, no ip address will be allowed to connect!"
elif [[ "$firewallAllowedDestinations" == "all" ]]; then
  echo "\$firewallAllowedDestinations is set to all ";
  exit;
else
  echo "iptables for network isolation will be set"
fi


# basic preset for DNS discovery and accepting established connection
iptables -I OUTPUT -p udp --dport 53 -j ACCEPT
iptables -I OUTPUT 1 -m conntrack --ctstate RELATED,ESTABLISHED -j ACCEPT

# parses firewallAllowedDestinations environment variable
# it should be in format IPAddress:port,[IPAddress:port, [ ... ]]
OIFS=$IFS
IFS=','
for address in $firewallAllowedDestinations
do
 IFS=':'
 destination=($address)
 if [ -n "${destination[1]}" ]
 then
     iptables -A OUTPUT -p tcp --dport ${destination[1]} -d ${destination[0]} -j ACCEPT
 else
     iptables -A OUTPUT -p tcp -d ${destination[0]} -j ACCEPT
 fi
done
IFS=$OIFS

iptables -A OUTPUT -j REJECT

# print out set the rules
iptables -L OUTPUT
