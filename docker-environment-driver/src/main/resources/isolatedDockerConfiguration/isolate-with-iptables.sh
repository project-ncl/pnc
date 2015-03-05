#!/bin/sh
#
# This script limit outband to defined $firewallAllowedDestinations IP addresses and its ports in $firewallAllowedPorts

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
 iptables -A OUTPUT -p tcp --dport ${destination[1]} -d ${destination[0]} -j ACCEPT
done   
IFS=$OIFS

iptables -A OUTPUT -j REJECT

# print out set the rules 
iptables -L OUTPUT
