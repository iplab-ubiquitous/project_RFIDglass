import csv
from http import HTTPStatus
from http.server import BaseHTTPRequestHandler

import sys
import json
import socketserver
import numpy as np
from sklearn.externals import joblib
from sklearn.metrics import confusion_matrix, classification_report

import matplotlib.pyplot as plt
import seaborn as sns

from Logput import Logput

np.set_printoptions(suppress=True)
training_data = np.empty([0, 7])
data_count = 0
correct_count = 0

testversion = "0120_p04"  # 保存テストデータのバージョン
version = "0120_p01"  # 学習モデルのバージョン："mmdd_p(No.)"
model = "KNN"  #モデルの種類　[ SVC, KNN, RF ]
clf = joblib.load('./learningModel/test' + model + "_" + version + '.pkl')
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

    def collect_magnet_data(self, jsons):
        
        global training_data, data_count, correct_count
        parsed_json = [[
            # jsons['45']['x'], jsons['45']['y'], jsons['45']['z'],
            jsons['47']['x'], jsons['47']['y'], jsons['47']['z'],
            jsons['49']['x'], jsons['49']['y'], jsons['49']['z'],
            jsons['label']
        ]]
        data = np.array(parsed_json[0])
        training_data = np.append(training_data, parsed_json, axis=0)
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
    datalog = Logput("data")
    modellog = Logput(model)
    modellog.logput("program: predictor.py\n")
    modellog.logput('Use model: test' + model + "_" + version + '.pkl\n')
    modellog.logput("Save testData: testData_" + version + ".csv\n")
    c_matrix = confusion_matrix(true_list, pred_list)
    print(c_matrix)
    np.savetxt("./testData/testData_" + testversion + ".csv", training_data, delimiter=',', fmt='%.0f')
    datalog.logput("Save testData: testData_" + testversion + ".csv\n")
    numTag = int(max(true_list)) + 1
    datalog.logput("Number of Each data: {}".format(data_count / numTag) + ", Positions: {}".format(numTag) + "Total data: {}".format(data_count) + "\n")
    with open('./confusionMatrix/test/csv/confusion_matrix_data_' + model + "_" + version + '.csv', 'w') as file:
        writer = csv.writer(file, lineterminator='\n')
        writer.writerows(c_matrix)


    modellog.logput('Save: confusion_matrix_data_' + model + "_" + version + '.csv\n')

    #混同行列の画像表示
    sns.heatmap(c_matrix, annot=True, cmap="Reds")
    plt.savefig('./confusionMatrix/test/png/confusion_matrix_data_' + model + "_" + version + '.png')
    modellog.logput('Save: confusion_matrix_data_' + model + "_" + version + '.png\n')

    print(classification_report(true_list, pred_list))
    print("\n 正答率： {}".format(float(correct_count) / float(data_count)))
    modellog.logput("正答率： {}".format(float(correct_count) / float(data_count)))
    modellog.logput("\n")
