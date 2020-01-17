import seaborn as sns
from sklearn import svm, neighbors, metrics, preprocessing
from sklearn.model_selection import train_test_split, cross_val_score, GridSearchCV, cross_validate, StratifiedShuffleSplit
from sklearn.metrics import classification_report, accuracy_score, confusion_matrix
from sklearn.externals import joblib
import pandas as pd
import numpy as np
import csv
import matplotlib.pyplot as plt

from Logput import Logput

data_version = "0117_p00"  # テストデータのバージョン
model_version = "0117_p01"  # モデルのバージョン
modelType = "RF"   # モデルの種類　[ SVC, KNN, RF ]

dataset = pd.read_csv("./testData/testData_" + data_version + ".csv", header=None)

test_data = dataset.iloc[:, 0:6]
test_label = dataset.iloc[:, 6]

# 予測フェーズ
clf = joblib.load(('./learningModel/test' + modelType + "_" + model_version + '.pkl'))
pred = clf.predict(test_data)
touch_true = test_label.tolist()
print(pred)
print(touch_true)
c_matrix = confusion_matrix(touch_true, pred)
print(c_matrix)
sns.heatmap(c_matrix, annot=True, cmap="Reds")
plt.savefig('./confusionMatrix/confusion_matrix_data_' + modelType + "_" + model_version + '.png')
with open('./confusionMatrix/confusion_matrix_data_' + modelType + "_" + model_version + '.csv', 'w') as file:
    writer = csv.writer(file, lineterminator='\n')
    writer.writerows(c_matrix)
print(classification_report(test_label, pred))
print("正答率 = ", metrics.accuracy_score(test_label, pred))

modellog = Logput(modelType)
modellog.logput("program: datapredict.py\n")
modellog.logput("Use model: test" + modelType + "_" + model_version + ".pkl\n")
modellog.logput("TestData: testData_" + data_version + ".csv\n")
modellog.logput('Save: confusion_matrix_data_' + modelType + "_" + model_version + '.csv\n')
modellog.logput('Save: confusion_matrix_data_' + modelType + "_" + model_version + '.png\n')
modellog.logput("正答率： {}".format(metrics.accuracy_score(test_label, pred)))
modellog.logput("\n")