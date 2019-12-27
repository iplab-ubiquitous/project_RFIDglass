from sklearn import svm, neighbors, metrics, preprocessing
from sklearn.model_selection import train_test_split, cross_val_score, GridSearchCV, cross_validate, StratifiedShuffleSplit
from sklearn.metrics import classification_report, accuracy_score, confusion_matrix
from sklearn.externals import joblib
import pandas as pd
import numpy as np
import csv

from sklearn.neighbors import KNeighborsClassifier

version = "1213_p01"
dataset = np.loadtxt("./collectData/data_" + version + ".csv", delimiter=',', dtype='int64')



sss = StratifiedShuffleSplit(test_size=0.2)

data, label = np.hsplit(dataset, [6])


# 交差検証なし
train_data, test_data, train_label, test_label = train_test_split(data, label, test_size=0.2, random_state=None, stratify=label)
train_label = np.reshape(train_label, (-1))
test_label = np.reshape(test_label, (-1))


# for train_index, test_index in sss.split(data, label):
#     train_data, test_data = data[train_index], data[test_index]
#     train_label, test_label = label[train_index], label[test_index]
#     train_label = np.reshape(train_label, (-1))
#     test_label = np.reshape(test_label, (-1))




# クロスバリデーションで最適化したいパラメータをセット
knn_parameters = {'n_neighbors': [7, 11, 19],
                    'weights': ['uniform', 'distance'],
                    'metric': ['euclidean', 'manhattan']
                  }
scores = ['precision', 'recall', 'f1']

print("# Tuning hyper-parameters for accuracy")

#  グリッドサーチと交差検証法
clf = GridSearchCV(KNeighborsClassifier(), knn_parameters, cv=5,
                   scoring='accuracy', n_jobs=-1)
clf.fit(train_data, train_label)
print(clf.best_estimator_)
print(classification_report(test_label, clf.predict(test_data)))

joblib.dump(clf, './learningModel/testKNN_'+ version + '.pkl')

# スコア別
for score in scores:
    print("# Tuning hyper-parameters for {}".format(score))

    # グリッドサーチと交差検証法
    clf_score = GridSearchCV(KNeighborsClassifier(), knn_parameters, cv=5,
                             scoring='%s_weighted' % score, n_jobs=-1)
    clf_score.fit(train_data, train_label)
    print(clf_score.best_estimator_)
    print(classification_report(test_label, clf_score.predict(test_data)))



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
with open('./confusionMatrix/confusion_matrix_cv_KNN_' + version + '.csv', 'w') as file:
    writer = csv.writer(file, lineterminator='\n')
    writer.writerows(c_matrix)
print(classification_report(test_label, pred))
print("正答率 = ", metrics.accuracy_score(test_label, pred))
