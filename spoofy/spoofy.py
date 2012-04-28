"""Dirty, simple HTTP proxy that spoofs Chrome user agent
"""

import sys
import SocketServer
import SimpleHTTPServer
from urllib import FancyURLopener

PORT = 8000
USER_AGENT = 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_7_2) AppleWebKit/535.7 (KHTML, like Gecko) Chrome/16.0.912.77 Safari/535.7'

class ChromeOpener(FancyURLopener):
    version = USER_AGENT

class Proxy(SimpleHTTPServer.SimpleHTTPRequestHandler):
    def do_POST(self):
	# TODO implement POST properly
	self.do_GET()

    def do_GET(self):
	print 'GET', self.path

	opener = ChromeOpener()
        f = opener.open(self.path)

        self.send_response(f.getcode())
        self.wfile.write(str(f.info()))
	self.end_headers()

        self.copyfile(f, self.wfile)
	f.close()
	self.wfile.flush()

    def send_response(self, code, message=None):
         self.log_request(code)
         if message is None:
             if code in self.responses:
                 message = self.responses[code][0]
             else:
                 message = ''
         if self.request_version != 'HTTP/0.9':
             self.wfile.write("%s %d %s\r\n" %
                              (self.protocol_version, code, message))
def begin():
    port = PORT

    if len(sys.argv) > 1 and sys.argv[1].isdigit():
        port = int(sys.argv[1])

    httpd = SocketServer.ForkingTCPServer(('', port), Proxy)
    print "Proxy is listening on port", port
    httpd.serve_forever()

if __name__ == '__main__':
    begin()
