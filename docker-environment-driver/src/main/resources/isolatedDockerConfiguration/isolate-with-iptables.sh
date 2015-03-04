#!/bin/sh
#
# This script limit outband to defined $firewallAllowedDestinations IP addresses and its ports in $firewallAllowedPorts

iptables -I OUTPUT -p udp --dport 53 -j ACCEPT
iptables -I OUTPUT 1 -m conntrack --ctstate RELATED,ESTABLISHED -j ACCEPT
iptables -A OUTPUT -p tcp --match multiport --dport $firewallAllowedPorts -d $firewallAllowedDestinations -j ACCEPT
iptables -A OUTPUT -j REJECT
