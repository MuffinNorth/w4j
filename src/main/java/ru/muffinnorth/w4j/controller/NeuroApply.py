import numpy as np
from PIL import Image, ImageOps
im_data = data.reshape((int(width), int(height)))
img = Image.fromarray(im_data)
img = ImageOps.mirror(img.rotate(-90, expand=True))
img.show()