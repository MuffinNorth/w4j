import numpy as np
from PIL import Image, ImageOps
import os
import tqdm
from tensorflow import keras
from tensorflow.keras import layers


from tensorflow import keras
from tqdm import tqdm


class TQDMPredictCallback(keras.callbacks.Callback):
    def __init__(self, custom_tqdm_instance=None, tqdm_cls=tqdm, **tqdm_params):
        super().__init__()
        self.tqdm_cls = tqdm_cls
        self.tqdm_progress = None
        self.prev_predict_batch = None
        self.custom_tqdm_instance = custom_tqdm_instance
        self.tqdm_params = tqdm_params

    def on_predict_batch_begin(self, batch, logs=None):
        pass

    def on_predict_batch_end(self, batch, logs=None):
        self.tqdm_progress.update(batch - self.prev_predict_batch)
        self.prev_predict_batch = batch

    def on_predict_begin(self, logs=None):
        self.prev_predict_batch = 0
        if self.custom_tqdm_instance:
            self.tqdm_progress = self.custom_tqdm_instance
            return

        total = self.params.get('steps')
        if total:
            total -= 1

        self.tqdm_progress = self.tqdm_cls(total=total, **self.tqdm_params)

    def on_predict_end(self, logs=None):
        if self.tqdm_progress and not self.custom_tqdm_instance:
            self.tqdm_progress.close()

os.environ['CUDA_VISIBLE_DEVICES'] = '-1'

model = keras.Sequential([
    layers.InputLayer(input_shape=(9,)),
    layers.Dense(500, activation='relu'),
    layers.Dense(300, activation='relu'),
    layers.Dense(200, activation='relu'),
    layers.Dense(100, activation='relu'),
    layers.Dense(50, activation='relu'),
    layers.Dense(25, activation='relu'),
    layers.Dense(3, activation='sigmoid'),
])

model.compile(
    optimizer='adam',
    loss='mean_squared_error',
    metrics=['accuracy'],
)
model.load_weights(path_to_model)

im_data = data.reshape((int(width), int(height)))
img = Image.fromarray(im_data)
img = ImageOps.mirror(img.rotate(-90, expand=True))
img.show()

img = img.resize((int(width * scale), int(height * scale)))

plastin = img
plastin_ds = list()

for x in tqdm(range(plastin.size[0])):
    for y in range(plastin.size[1]):
        inp = np.zeros(9)
        index = 0
        for i in range(-1, 2):
            for j in range(-1, 2):
                if not(0 <= (x + i) < plastin.size[0]) or not(0 <= (y + j) < plastin.size[1]):
                    inp[index] = 0
                else:
                    inp[index] = plastin.getpixel((x+i, y+j)) / 255
                index += 1
        plastin_ds.append(inp)

plastin_ds = np.array(plastin_ds)

out = model.predict(plastin_ds, callbacks=[TQDMPredictCallback()])
out = out * 255
out = out.astype(np.uint8)
out = out.reshape(plastin.size[0], plastin.size[1], 3)
img = Image.fromarray(out)
img = img.resize((int(height), int(width)))
img = ImageOps.mirror(img.rotate(-90, expand=True))
img.show()

data = np.array(img)
data = data.reshape((int(height * width * 3)))
for i in range(len(data)):
    out_data[i] = data[i]
