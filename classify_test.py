from sklearn import svm, neighbors, metrics, preprocessing
from sklearn.model_selection import train_test_split, cross_val_score, GridSearchCV
from sklearn.metrics import classification_report, accuracy_score
import pandas as pd

tuned_parameters = [
    {'C': [1, 10, 100, 1000], 'kernel': ['linear']},
    {'C': [1, 10, 100, 1000], 'kernel': ['rbf'], 'gamma': [0.001, 0.0001]},
    {'C': [1, 10, 100, 1000], 'kernel': ['poly'],
        'degree': [2, 3, 4], 'gamma': [0.001, 0.0001]},
    {'C': [1, 10, 100, 1000], 'kernel': ['sigmoid'], 'gamma': [0.001, 0.0001]}
]
dataset = pd.read_csv("test.csv", header=None)
data_train, data_test = train_test_split(dataset, test_size=0.2)

train_label = data_train.iloc[:, 6]
train_data = data_train.iloc[:, 0:5]

test_label = data_test.iloc[:, 6]
test_data = data_test.iloc[:, 0:5]

scorings = ['accuracy', 'precision', 'recall']

# for s in scorings:

#     classifier = GridSearchCV(svm.SVC(),            # 使用したいモデル
#                               tuned_parameters,  # 最適化したいパラメータ
#                               cv=5,  # 交差検証の回数
#                               scoring=s  # 評価関数の指定
#                               )
#     classifier.fit(train_data, train_label)
#     print(classifier.best_estimator_)
#     print(classification_report(test_label, classifier.predict(test_data)))


clf_svc = svm.LinearSVC(loss='hinge', C=1,
                        class_weight='balanced', random_state=None)
result = clf_svc.fit(train_data, train_label)
pred = clf_svc.predict(test_data)
# print(test_pred);
print(classification_report(test_label, pred))
print("正答率 = ", metrics.accuracy_score(test_label, pred))
