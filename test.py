from http import HTTPStatus
from http.server import BaseHTTPRequestHandler

import sys
import json
import socketserver


PORT = 8080

# for test
# curl -X POST -H "Content-Type: application/json" -d { "x":"0" , "y": "53" , "z":"-6" } localhost:8080


class MagnetHTTPRequestHandler(BaseHTTPRequestHandler):
    def __init__(self, *args, **kwargs):
        super().__init__(*args, **kwargs)
    
    def do_POST(self):
        content_len = int(self.headers.get('content-length'))
        request_body = json.loads(
            self.rfile.read(content_len).decode('utf-8'))

        response = {'status': 200}
        self.send_response(200)
        self.send_header('Content-type', 'application/json')
        self.end_headers()
        responseBody = json.dumps(response)

        self.wfile.write(responseBody.encode('utf-8'))
        # self.send_response(HTTPStatus.OK)
        print(request_body)
        self.recognize_magnet(request_body)
        # self.wfile.write("OK.")

    def recognize_magnet(self, jsons):
        print(type(jsons['x']))



Handler = MagnetHTTPRequestHandler
with socketserver.TCPServer(("", PORT), Handler) as httpd:
    print("serving at port", PORT)
    httpd.serve_forever()
