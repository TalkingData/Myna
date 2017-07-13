
import os
import numpy as np
import matplotlib.pyplot as plt
import pywt
from sklearn.metrics import precision_score, recall_score, f1_score, confusion_matrix
from sklearn.preprocessing import label_binarize
from sklearn.metrics import roc_curve, auc
from itertools import cycle


batch_size = 128


def getMin(a):
    return np.min(a)


def getMax(a):
    return np.max(a)


def getMean(a):
    return np.mean(a)


def getStd(a):
    return np.std(a)


def getMedian(a):
    return np.median(a)


def getFreq(a):
    return np.fft.fft(a, len(a))


def getEnergy(a):
    return np.sum(np.power(a, 2))


def getPeakCount(a):
    peaks = 0
    for i in range(1, len(a) - 1):
        if (a[i] >= a[i - 1] and a[i] > a[i + 1]) or (a[i] <= a[i - 1] and a[i] < a[i + 1]):
            peaks += 1
    return peaks


def removeOutliers(a, m=5):
    mu = np.mean(a, axis=0)
    sigma = np.std(a, axis=0)
    modified = [v for v in a if np.abs(v - mu) < m * sigma]
    return modified


def normalize(a):
    mu = np.mean(a, axis=0)
    sigma = np.std(a, axis=0)
    if sigma == 0.0:
        return a - mu
    return (a - mu) / sigma


def smooth(a):
    # print("a[0] = {}, a[2] = {}".format(a[0], a[2]))
    for i in range(2, len(a) - 2):
        a[i] = (a[i - 2] + a[i - 1] + a[i] + a[i + 1] + a[i + 2]) / 5
    # print("a[0] = {}, a[2] = {}".format(a[0], a[2]))
    return a


def check_and_create_dir(folder):
    if not os.path.exists(folder):
        os.makedirs(folder)
    elif not os.path.isdir(folder):
        os.makedirs(folder)


def save_utf8(file_path, content, para_mode="wb"):
    """
    save the given content to the given path
    """
    """
    save the given content to the given path
    """
    with open(file_path, para_mode) as log:
        log.write(content)


def plotArray(a):
    plt.plot(a)
    plt.show()


def get_files(folder, prefix=""):
    """
    get all files from the given folder
    :return: return empty if found nothing
    """
    if os.path.exists(folder):
        files = os.listdir(folder)
        result = []
        for inner_file in files:
            if os.path.isfile(os.path.join(folder, inner_file)) and ".DS_Store" not in inner_file:
                if prefix == "":
                    result.append(inner_file)
                elif prefix in inner_file:
                    result.append(inner_file)
        return result
    else:
        return []


def extractSingleAxis(a):
    array = []
    for i in range(0, len(a) - batch_size, batch_size):
        src = a[i: i + batch_size]
        coe, det = pywt.dwt(src, "haar")

        N = batch_size
        comp = np.fft.fft(src)
        magnitude = np.sqrt(comp.real * comp.real + comp.imag * comp.imag)
        max_magnitude = magnitude[1]
        max_index = 1
        for j in range(2, N / 2):
            if magnitude[j] > max_magnitude:
                max_magnitude = magnitude[j]
                max_index = j

        featureData = "{},{},{},{},{},{},{},{}".format(
            getMin(src),
            getMax(src),
            getMean(src),
            getStd(src),
            # getMedian(src),
            # getPeakCount(src) / batch_size,
            getMean(coe),
            getStd(coe),
            # getMedian(coe),
            max_magnitude / batch_size,
            20.0 / N * max_index
        )
        array.append(featureData)
    return array


def get_features(dataset_path, fFile):
    if os.path.exists(fFile):
        os.remove(fFile)

    dataFilePath = dataset_path
    records = get_files(dataFilePath)
    if len(records) == 0:
        print("No record found in path: {}".format(dataFilePath))
        return
    for record in records:
        data = np.loadtxt(os.path.join(dataFilePath, record), delimiter=',')
        label = str(record).split(".")[0]
        print("Generating features from file {}, label: {}".format(record, label))
        xF = extractSingleAxis(data[:, 0])
        yF = extractSingleAxis(data[:, 1])
        zF = extractSingleAxis(data[:, 2])
        lastLine = ""
        for i in range(0, len(xF)):
            currentLine = xF[i] + "," + yF[i] + "," + zF[i] + "," + str(get0605(label)) + "\n"
            if currentLine != lastLine:
                lastLine = currentLine
                save_utf8(fFile, currentLine, "ap")


def get0605(original):
    if original == "7":
        return 0
    elif original == "8":
        return 1
    elif original == "101":
        return 2
    elif original == "102":
        return 3
    elif original == "103":
        return 4
    elif original == "3":
        return 5


def prepare_dataset(folderPath):
    act_files = get_files(folderPath)
    if len(act_files) == 0:
        print("No activity found in path: {}".format(folderPath))
        return
    for act_file in act_files:
        print("Handling file {}".format(act_file))
        label = str(act_file).split("_")[3]
        print("\tlabel: {}".format(label))
        re_sampled = get_resample_dataset(os.path.join(folderPath, act_file))
        overlapped = get_half_overlap_dataset(re_sampled)
        train_dataset, test_dataset = split_train_test(overlapped)
        generate_train_test_dataset(train_dataset, "train", label)
        generate_train_test_dataset(test_dataset, "test", label)


def get_resample_dataset(file_path):
    re_sampled = []
    with open(file_path, "r") as lines:
        index = 0
        last_value = ""
        for line in lines:
            index += 1
            if index == 5:
                values = line.split(",")
                if len(values) == 6:
                    current_value = "{},{},{}".format(values[3], values[4], values[5])
                    if current_value != last_value:
                        re_sampled.append(current_value)
                        last_value = current_value
                        index = 0
                    else:
                        index -= 1
                else:
                    index -= 1
    print("\tAfter re-sampling, the count of the lines are: {}".format(len(re_sampled)))
    return re_sampled


def get_half_overlap_dataset(dataset):
    overlapped = []
    for i in range(0, len(dataset) - batch_size, batch_size / 2):
        overlapped.append(dataset[i: i + batch_size])
    print("\tThe number of the groups after half-overlapping is: {}".format(len(overlapped)))
    return overlapped


def split_train_test(dataset):
    total_dataset = np.array(dataset)
    train_test_split = np.random.rand(len(total_dataset)) < 0.70
    train_dataset = total_dataset[train_test_split]
    test_dataset = total_dataset[~train_test_split]
    print("\t\tCount of train dataset: {}\n\t\tCount of test dataset: {}".format(len(train_dataset), len(test_dataset)))
    return train_dataset.tolist(), test_dataset.tolist()


def generate_train_test_dataset(dataset, folder_name, label):
    file_name = "{}.csv".format(label)
    file_path = os.path.join("data", folder_name, file_name)
    if os.path.exists(file_path):
        os.remove(file_path)

    for record in dataset:
        for item in record:
            save_utf8(file_path, item, "ap")
    print("\tSaved file: {}".format(file_path))


def xgTestSelfDataset(train_X, train_Y, test_X, test_Y):
    import xgboost as xgb
    import time

    # label need to be 0 to num_class -1
    xg_train = xgb.DMatrix(train_X, label=train_Y)
    xg_test = xgb.DMatrix(test_X, label=test_Y)
    # setup parameters for xgboost
    param = {'objective': 'multi:softprob',
             "learning_rate": 0.05,
             'max_depth': 5,
             'min_child_weight': 4,
             'silent': 1,
             'num_class': 6,
             "n_estimators": 100,
             "subsample": 0.85,
             "scale_pos_weight": 0.75,
             "seed": 32}

    watchlist = [(xg_train, 'train'), (xg_test, 'test')]
    num_round = 100

    start = time.time()
    bst = xgb.train(param, xg_train, num_round, watchlist)
    trainDuration = time.time() - start
    start = time.time()
    yprob = bst.predict(xg_test).reshape(test_Y.shape[0], 6)
    testDuration = time.time() - start
    ylabel = np.argmax(yprob, axis=1)

    if os.path.exists("rhar.model"):
        os.remove("rhar.model")
    bst.save_model("rhar.model")

    l_test_x = test_X.tolist()
    l_test_y = test_Y.tolist()
    if os.path.exists("rhar.test"):
        os.remove("rhar.test")
    for i in range(0, len(l_test_y)):
        line = "{},".format(int(l_test_y[i]))
        for j in range(0, len(l_test_x[i])):
            line = line + "{},".format(l_test_x[i][j])
        save_utf8("rhar.test", line[0: len(line) - 1] + "\n", "ap")

    print(test_Y.shape)
    print(ylabel.shape)
    plot_confusion_matrix(confusion_matrix(test_Y, ylabel))
    plot_roc(test_Y, yprob)
    return printResult(test_Y, ylabel), trainDuration, testDuration, "XGBoost"


def printResult(truth, predicated):
    precision = precision_score(truth.tolist(), predicated.tolist(), average='weighted')
    recall = recall_score(truth, predicated, average='weighted')
    f1 = f1_score(truth, predicated, average='weighted')
    print "Precision", precision
    print "Recall", recall
    print "f1_score", f1
    print "confusion_matrix"
    print confusion_matrix(truth, predicated)
    print ('predicting, classification error=%f' % (
        sum(int(predicated[i]) != truth[i] for i in range(len(truth))) / float(len(truth))))

    return precision, recall, f1


def plotSensorData():
    bus = np.loadtxt("dataset/bus_data_set_101", delimiter=',')
    car = np.loadtxt("dataset/car_data_set_103", delimiter=',')
    running = np.loadtxt("dataset/running_data_set_8", delimiter=',')
    subway = np.loadtxt("dataset/subway_data_set_102", delimiter=',')
    walking = np.loadtxt("dataset/walking_data_set_7", delimiter=',')
    fig, axes = plt.subplots(5, 3)
    print(bus.shape)
    axes[0][0].plot(bus[:, 3], label="bus_x")
    axes[0][1].plot(bus[:, 4], label="bus_y")
    axes[0][2].plot(bus[:, 5], label="bus_z")
    axes[1][0].plot(car[:, 3], label="car_x")
    axes[1][1].plot(car[:, 4], label="car_y")
    axes[1][2].plot(car[:, 5], label="car_z")
    axes[2][0].plot(running[:, 3], label="running_x")
    axes[2][1].plot(running[:, 4], label="running_y")
    axes[2][2].plot(running[:, 5], label="running_z")
    axes[3][0].plot(subway[:, 3], label="subway_x")
    axes[3][1].plot(subway[:, 4], label="subway_y")
    axes[3][2].plot(subway[:, 5], label="subway_z")
    axes[4][0].plot(walking[:, 3], label="walking_z")
    axes[4][1].plot(walking[:, 4], label="walking_y")
    axes[4][2].plot(walking[:, 5], label="walking_z")
    fig.tight_layout()
    plt.show()


def plot_confusion_matrix(cm, title='Normalized Confusion matrix', cmap=plt.cm.get_cmap("Blues")):
    activities = ["walking", "running", "bus", "subway", "car", "stationary"]
    cm = cm / cm.astype(np.float).sum(axis=1)
    print "confusion_matrix: \n{}".format(cm)
    plt.imshow(cm, interpolation='nearest', cmap=cmap)
    plt.title(title)
    plt.colorbar()
    tick_marks = np.arange(len(cm))
    plt.xticks(tick_marks, activities, rotation=45)
    plt.yticks(tick_marks, activities)
    plt.tight_layout()
    plt.ylabel('True label')
    plt.xlabel('Predicted label')
    plt.show()


def plot_roc(y_true, y_score):
    y = label_binarize(np.array(y_true), classes=[0, 1, 2, 3, 4, 5])
    n_classes = y.shape[1]
    print("n_classes\n", n_classes)
    print("len(y)\n", len(y))
    print("len(y_score)\n", len(y_score))

    # Compute ROC curve and ROC area for each class
    fpr = dict()
    tpr = dict()
    roc_auc = dict()
    for i in range(n_classes):
        fpr[i], tpr[i], _ = roc_curve(y[:, i], y_score[:, i])
        roc_auc[i] = auc(fpr[i], tpr[i])

    colors = cycle(['aqua', 'darkorange', 'cornflowerblue', "red", "blue", "brown"])
    for i, color in zip(range(n_classes), colors):
        plt.plot(fpr[i], tpr[i], color=color, lw=1,
                 label='ROC curve of class {0} (area = {1:0.2f})'
                       ''.format(get_activity_str(i), roc_auc[i]))

    plt.plot([0, 1], [0, 1], 'k--', lw=2)
    plt.xlim([0.0, 1.0])
    plt.ylim([0.0, 1.05])
    plt.xlabel('False Positive Rate')
    plt.ylabel('True Positive Rate')
    plt.title('Some extension of Receiver operating characteristic to multi-class')
    plt.legend(loc="lower right")
    plt.show()


def get_activity_str(act_id):
    if act_id == 0:
        return "Walking"
    elif act_id == 1:
        return "Running"
    elif act_id == 2:
        return "Bus"
    elif act_id == 3:
        return "Subway"
    elif act_id == 4:
        return "Car"
    elif act_id == 5:
        return "Stationary"
    return "Unknown"


def KMean(X):
    from sklearn.cluster import KMeans
    from sklearn.decomposition import PCA
    num_clusters = 5

    X_pca = PCA(n_components=2, whiten=True).fit_transform(X)

    km = KMeans(n_clusters=num_clusters)
    y_pred = km.fit_predict(X_pca)
    plt.scatter(X_pca[:, 0], X_pca[:, 1], c=y_pred)
    plt.show()


def KMeanTruth(X, Y):
    from sklearn.decomposition import PCA
    from mpl_toolkits.mplot3d import Axes3D

    X_pca = PCA(n_components=3, whiten=False, svd_solver="auto").fit_transform(X)
    fig = plt.figure()
    Axes3D(fig)
    plt.scatter(X_pca[:, 0], X_pca[:, 1], X_pca[:, 2], c=Y)
    plt.show()


def test_3rd_dataset():
    train_feature_file = "3rd_train.csv"
    test_feature_file = "3rd_test.csv"
    #
    # prepare_dataset("dataset/")
    #
    # get_features("data/train", train_feature_file)
    # get_features("data/test", test_feature_file)

    train = np.loadtxt(train_feature_file, delimiter=',')
    test = np.loadtxt(test_feature_file, delimiter=',')

    train_x = train[:, 0:train.shape[1] - 1]
    train_y = train[:, train.shape[1] - 1]
    test_x = test[:, 0:train.shape[1] - 1]
    test_y = test[:, train.shape[1] - 1]

    # KMeanTruth(train_x, train_y)

    print("train.shape = {}, test_x = {}".format(train.shape, test_x.shape))
    xgTestSelfDataset(train_x, train_y, test_x, test_y)


def test_self_dataset():
    train_feature_file = "0629_train.csv"
    test_feature_file = "0629_test.csv"

    # prepare_dataset("dataset_0629/")
    #
    get_features("data/train", train_feature_file)
    get_features("data/test", test_feature_file)

    train = np.loadtxt(train_feature_file, delimiter=',')
    test = np.loadtxt(test_feature_file, delimiter=',')

    train_x = train[:, 0:train.shape[1] - 1]
    train_y = train[:, train.shape[1] - 1]
    test_x = test[:, 0:train.shape[1] - 1]
    test_y = test[:, train.shape[1] - 1]

    # KMeanTruth(train_x, train_y)

    print("train.shape = {}, test_x = {}".format(train.shape, test_x.shape))
    xgTestSelfDataset(train_x, train_y, test_x, test_y)


def create_balance(folderPath):
    act_files = get_files(folderPath)
    if len(act_files) == 0:
        print("No activity found in path: {}".format(folderPath))
        return
    content = {}
    least_line_count = 0
    print("Totally {} files found.\n".format(len(act_files)))
    for act_file in act_files:
        print("Handling {}".format(act_file))
        content[act_file] = []
        current_line_count = 0
        with open(os.path.join(folderPath, act_file), "r") as lines:
            last_line = ""
            for line in lines:
                if last_line != line:
                    content[act_file].append(line)
                    current_line_count += 1
                    last_line = line
        print("\tCurrent line count: {}".format(current_line_count))
        if least_line_count == 0:
            least_line_count = current_line_count
        elif least_line_count > current_line_count:
            least_line_count = current_line_count
        print("\tLeast line count: {}".format(least_line_count))

    for key in content.keys():
        new_file = os.path.join(folderPath, key)
        print("Re_structure file: {}".format(new_file))
        if os.path.exists(new_file):
            os.remove(new_file)
        current_line_index = 0
        for item in content[key]:
            save_utf8(new_file, item, "ap")
            current_line_index += 1
            if current_line_index >= least_line_count:
                break


if __name__ == "__main__":
    # create_balance("dataset_0629/")
    test_self_dataset()
