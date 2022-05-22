import math
import numpy
import numpy as np
from PIL import Image
from sklearn.cluster import DBSCAN
import matplotlib.pyplot as plt
from mpl_toolkits.mplot3d import Axes3D


def convert_to_dbscan_presentation(matrix: np.ndarray):
    size = matrix.shape
    data = np.zeros((size[0] * size[1], 3), dtype=numpy.int16)
    for i in range(0, size[0]):
        for j in range(0, size[1]):
            data[i * size[0] + j] = [i, j, matrix[i][j]]
    return data


def zbuffer(p, o):
    x = (p[0] - o[0]) * (p[0] - o[0])
    y = (p[1] - o[1]) * (p[1] - o[1])
    z = (p[2] - o[2]) * (p[2] - o[2]) * 1.1
    return math.sqrt(x + y + z)

def getBounds(img, in_pixels):
    return (int((img.size[0] - int(img.size[0] / in_pixels) * in_pixels) / 2),
            int((img.size[1] - int(img.size[1] / in_pixels) * in_pixels) / 2))

class Cell:
    CELL_SIZE = 13
    EPS = 4.2
    MIN_SAMPLES = 10
    SIZE = (CELL_SIZE, CELL_SIZE)
    cluster = 0

    def __init__(self, image, x, y):
        self.x = x
        self.y = y
        self._image = image
        self._original_size = image.size[0]
        self._init_data = np.array(self._image, dtype=numpy.int16)
        self.sigma = len(np.unique(self._init_data))
        self._image.thumbnail(self.SIZE, Image.BICUBIC)
        self._thumb_data = np.array(self._image, dtype=numpy.int16)
        self._data = convert_to_dbscan_presentation(self._thumb_data)
        self._model = DBSCAN(eps=self.EPS, min_samples=self.MIN_SAMPLES, metric=zbuffer)

    def cluster_analysis(self):
        self._model.fit_predict(self._data)
        #self.show()
        self.cluster = len(set(self._model.labels_))
        if self._model.labels_.min() == -1:
            self.cluster -= 1

        self.grayscale_analyse()

    def grayscale_analyse(self):
        gray_dict = dict()
        cluster_count = dict()
        for i in range(0, self.cluster):
            gray_dict[i] = 0
            cluster_count[i] = 0
        for i in self._model.labels_:
            if i == -1:
                continue
            gray_dict[i] += self._data[i, 2]
            cluster_count[i] += 1
        for i in range(0, self.cluster):
            gray_dict[i] = gray_dict[i] / cluster_count[i]
        self._gray_labels = np.zeros((len(self._model.labels_)))
        for i in range(len(self._model.labels_)):
            if self._model.labels_[i] == -1:
                self._gray_labels[i] = -1
                continue
            self._gray_labels[i] = gray_dict[self._model.labels_[i]]
        self.cluster = len(set(self._gray_labels))
        if self._gray_labels.min() == -1:
            self.cluster -= 1
        self._gray_labels = self._gray_labels.reshape((self.CELL_SIZE, self.CELL_SIZE))
        #self._gray_labels = self._model.labels_.reshape((self.CELL_SIZE, self.CELL_SIZE))

    def scaleMatrix(self):
        scale = int(self._original_size / self.CELL_SIZE)
        self._full = np.kron(self._gray_labels, np.ones((scale, scale)))

    def show(self):
        print("number of cluster found: {}".format(len(set(self._model.labels_))))
        #print(self._gray_labels)
        fig = plt.figure()
        ax = Axes3D(fig)
        ax.scatter(self._data[:, 0], self._data[:, 1], self._data[:, 2], c=self._model.labels_, s=25)
        ax.view_init(azim=0, elev=90)
        plt.show()
