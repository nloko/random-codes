#!/usr/bin/python

""" Port scanner
"""

import sys
import socket
from optparse import OptionParser

parser = OptionParser()
parser.add_option("-H", "--host", dest="host", default="127.0.0.1",
                  help="Host to scan")
parser.add_option("--min",
                  type=int, dest="minport", default=0,
                  help="First port to scan")

parser.add_option("--max",
                  type=int, dest="maxport", default=1023,
                  help="Last port to scan")

(options, args) = parser.parse_args()

try:
    ip = socket.gethostbyname(options.host)
except socket.gaierror:
    print 'Unable to resolve Host %s' % options.host
    sys.exit(1)

minport = options.minport
maxport = min(0xFFFF, options.maxport)

print 'Scanning port range %d through %d on host %s' % (minport, maxport, ip)

socket.setdefaulttimeout(5)

for port in range(minport, maxport + 1):
    s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)

    if s.connect_ex((ip, port)) == 0:
        print 'Port %d: OPEN' % port

    s.close()
