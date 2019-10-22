import seaborn as sns
from sklearn import svm, neighbors, metrics, preprocessing
from sklearn.model_selection import train_test_split, cross_val_score, GridSearchCV, cross_validate, StratifiedShuffleSplit
from sklearn.metrics import classification_report, accuracy_score, confusion_matrix
from sklearn.externals import joblib
import pandas as pd
import numpy as np
import csv
import matplotlib.pyplot as plt


dataset = pd.read_csv("testdata1022.csv", header=None)

# data_train, data_test = train_test_split(dataset, test_size=0.2)
# train_label = data_train.iloc[:, 6]
# train_data = data_train.iloc[:, 0:6]
# test_label = data_test.iloc[:, 6]
# test_data = data_test.iloc[:, 0:6]

test_data = dataset.iloc[:, 0:9]
test_label = dataset.iloc[:, 9]

#  訓練データ確認
print(dataset)


# 学習フェーズ
# clf_svc = svm.SVC(C=0.1, kernel='linear')
# print(clf_svc)
# scores = cross_validate(clf_svc, train_data, train_label, cv=5, n_jobs=-1, return_estimator=True)
# clf_svc = scores['estimator'][0]
# joblib.dump(clf_svc, 'test1002.pkl')

# result = clf_svc.fit(train_data, train_label)

# 予測フェーズ
clf = joblib.load('test1022.pkl')
pred = clf.predict(test_data)
touch_true = test_label.tolist()
print(pred)
print(touch_true)
c_matrix = confusion_matrix(touch_true, pred)
print(confusion_matrix(touch_true, pred))
sns.heatmap(confusion_matrix(touch_true, pred), annot=True, cmap="Reds")
plt.savefig('./confusion_matrix_heatmap.png')
with open('confusion_matrix_testdata1022.csv', 'w') as file:
    writer = csv.writer(file, lineterminator='\n')
    writer.writerows(c_matrix)
print(classification_report(test_label, pred))
print("正答率 = ", metrics.accuracy_score(test_label, pred))
