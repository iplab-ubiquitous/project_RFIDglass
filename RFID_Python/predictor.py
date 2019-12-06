import csv
from http import HTTPStatus
from http.server import BaseHTTPRequestHandler

import sys
import json
import socketserver
import numpy as np
from sklearn.externals import joblib
from sklearn.metrics import confusion_matrix, classification_report
# from sns import sns
# import matplotlib.pyplot as plt

np.set_printoptions(suppress=True)
training_data = np.empty([0, 7])
data_count = 0
correct_count = 0
version = "1206_p00" # 学習モデルのバージョン
clf = joblib.load('./learningModel/test_' + version + '.pkl')
pred_list = []
true_list = []


PORT = 8080

# for test
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
        
        global training_data, data_count, correct_count
        parsed_json = [
            # jsons['45']['x'], jsons['45']['y'], jsons['45']['z'],
            jsons['47']['x'], jsons['47']['y'], jsons['47']['z'],
            jsons['49']['x'], jsons['49']['y'], jsons['49']['z'],
            jsons['label']
        ]
        data = np.array(parsed_json)
        data_count += 1
        # print(data[0:6])
        true_list.append(data[6])
        predict = clf.predict(data[0:6].reshape(1, -1))
        pred_list.append(predict[0])
        print(predict)
        if data[6] == predict[0]:
            correct_count+=1


        # training_data = np.append(training_data, parsed_json, axis=0)
        # #print(training_data)
        # data_count += 1

    def recognize_finger_pos(self, jsons):
        return 




Handler = MagnetHTTPRequestHandler
try:
    with socketserver.TCPServer(("", PORT), Handler) as httpd:
        print("serving at port", PORT)
        httpd.serve_forever()
except KeyboardInterrupt:
    httpd.server_close()
    print(true_list)
    print(pred_list)
    c_matrix = confusion_matrix(true_list, pred_list)
    print(c_matrix)

    with open('./confusionMatrix/confusion_matrix_' + version + '.csv', 'w') as file:
        writer = csv.writer(file, lineterminator='\n')
        writer.writerows(c_matrix)

    #混同行列の画像表示
    # sns.heatmap(c_matrix, annot=True, cmap="Reds")
    # plt.savefig('/confusionMatrix/confusion_matrix_' + version + '.png')

    print(classification_report(true_list, pred_list))
    print("\n 正答率： {}".format(float(correct_count) / float(data_count)))

