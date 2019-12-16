from sklearn import svm, neighbors, metrics, preprocessing
from sklearn.model_selection import train_test_split, cross_val_score, GridSearchCV, cross_validate, StratifiedShuffleSplit
from sklearn.metrics import classification_report, accuracy_score, confusion_matrix
from sklearn.externals import joblib
import pandas as pd
import numpy as np
import csv

version = "1213_p01"
dataset = pd.read_csv("./collectData/data_" + version + ".csv", header=None)

# data_train, data_test = train_test_split(dataset, test_size=0.2)
# train_label = data_train.iloc[:, 6]
# train_data = data_train.iloc[:, 0:6]
# test_label = data_test.iloc[:, 6]
# test_data = data_test.iloc[:, 0:6]

sss = StratifiedShuffleSplit(test_size=0.2)
data = dataset.iloc[:, 0:6]
label = dataset.iloc[:, 6]
for train_index, test_index in sss.split(data, label):
    train_data,  test_data = data.loc[train_index], data.loc[test_index]
    train_label, test_label = label.loc[train_index], label.loc[test_index]

#  訓練データ確認
# print(train_data)

# クロスバリデーションで最適化したいパラメータをセット
tuned_parameters = [{'kernel': ['rbf'], 'gamma': [1e-3, 1e-4],
                     'C': [0.1, 1, 10]},
                    {'kernel': ['linear'], 'C': [0.1, 1, 10]}]

scores = ['precision', 'recall', 'f1']

print("# Tuning hyper-parameters for accuracy")

#  グリッドサーチと交差検証法
clf = GridSearchCV(svm.SVC(), tuned_parameters, cv=5,
                    scoring='accuracy', n_jobs=-1)
clf.fit(train_data, train_label)
print(clf.best_estimator_)
print(classification_report(test_label, clf.predict(test_data)))

joblib.dump(clf, './learningModel/test_'+ version + '.pkl')

for score in scores:
    print("# Tuning hyper-parameters for {}".format(score))

    # グリッドサーチと交差検証法
    clf = GridSearchCV(svm.SVC(), tuned_parameters, cv=5,
                       scoring='%s_weighted' % score, n_jobs=-1)
    clf.fit(train_data, train_label)
    print(clf.best_estimator_)
    print(classification_report(test_label, clf.predict(test_data)))



# 学習フェーズ
# clf_svc = svm.SVC(C=0.1, kernel='linear')
# print(clf_svc)
# scores = cross_validate(clf_svc, train_data, train_label, cv=5, n_jobs=-1, return_estimator=True)
# clf_svc = scores['estimator'][0]
# joblib.dump(clf_svc, 'test1022.pkl')

# result = clf_svc.fit(train_data, train_label)

# 予測フェーズ
# clf_svc = joblib.load('test1002.pkl')
pred = clf.predict(test_data)
touch_true = test_label.tolist()
print(pred)
print(touch_true)
c_matrix = confusion_matrix(touch_true, pred)
print(confusion_matrix(touch_true, pred))
with open('./confusionMatrix/confusion_matrix_cv_' + version + '.csv', 'w') as file:
    writer = csv.writer(file, lineterminator='\n')
    writer.writerows(c_matrix)
print(classification_report(test_label, pred))
print("正答率 = ", metrics.accuracy_score(test_label, pred))
