from http import HTTPStatus
from http.server import BaseHTTPRequestHandler

import sys
import json
import socketserver
import numpy as np

from Logput import Logput

np.set_printoptions(suppress=True)
training_data = np.empty([0, 7])
data_count = 0
version = "0214_p02" #収集データのバージョン
true_list = []
PORT = 8080

# for result
# curl -X POST -H "Content-Type: application/json" -d '{"45":{"x":-117,"y":-472,"z":-29},"47":{"x":-987,"y":-49,"z":-1524},"label":6}' localhost:8080


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
        # print(request_body)
        is_exists_model = False
        if is_exists_model:
            self.recognize_finger_pos(request_body)
        else:
            self.collect_magnet_data(request_body)
        # self.wfile.write("OK.")

    def collect_magnet_data(self, jsons):
        global training_data, data_count
        parsed_json = [[
            # jsons['45']['x'], jsons['45']['y'], jsons['45']['z'],
            jsons['47']['x'], jsons['47']['y'], jsons['47']['z'],
            jsons['49']['x'], jsons['49']['y'], jsons['49']['z'],
            jsons['label']
        ]]
        print(parsed_json)
        data = np.array(parsed_json[0])
        training_data = np.append(training_data, parsed_json, axis=0)
        true_list.append(data[6])
        #print(training_data)
        data_count += 1

    def recognize_finger_pos(self, jsons):
        return 




Handler = MagnetHTTPRequestHandler
try:
    with socketserver.TCPServer(("", PORT), Handler) as httpd:
        print("serving at port", PORT)
        httpd.serve_forever()
except KeyboardInterrupt:
    httpd.server_close()
    np.savetxt("./collectData/data_" + version + ".csv", training_data, delimiter=',', fmt='%.0f')
    datalog = Logput("data")
    datalog.logput("Save trainData :data_" + version + ".csv\n")
    numTag = int(max(true_list)) + 1
    datalog.logput("Number of Each data: {}".format(data_count / numTag) + ", Positions: {}".format(numTag) + "Total data: {}".format(data_count) + "\n\n")
