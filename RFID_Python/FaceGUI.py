import random
from http.server import BaseHTTPRequestHandler
import json
import socketserver
import numpy as np

from sklearn.externals import joblib


version = "0214_p02"  # 学習モデルのバージョン："mmdd_p(No.)"
model = "KNN"  # モデルの種類　[ SVC, KNN, RF ]
clf = joblib.load('./learningModel/test' + model + "_" + version + '.pkl')
PORT = 8080


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
        response["predict"] = int(self.collect_magnet_data(request_body))
        # response["predict"] = random.randrange(7)
        responseBody = json.dumps(response)

        self.wfile.write(responseBody.encode('utf-8'))
        # self.send_response(HTTPStatus.OK)


    def collect_magnet_data(self, jsons):
        global training_data, data_count, correct_count
        parsed_json = [[
            # jsons['45']['x'], jsons['45']['y'], jsons['45']['z'],
            jsons['47']['x'], jsons['47']['y'], jsons['47']['z'],
            jsons['49']['x'], jsons['49']['y'], jsons['49']['z']
        ]]
        data = np.array(parsed_json[0])
        return clf.predict(data[0:6].reshape(1, -1))[0]


def main():
    Handler = MagnetHTTPRequestHandler
    try:
        with socketserver.TCPServer(("", PORT), Handler) as httpd:
            print("serving at port", PORT)
            httpd.serve_forever()
    except KeyboardInterrupt:
        httpd.server_close()


if __name__ == '__main__':
    main()
