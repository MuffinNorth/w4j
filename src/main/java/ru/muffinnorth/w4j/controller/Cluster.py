from PIL import Image, ImageDraw, ImageFilter
from matplotlib import pyplot as plt
import numpy as np
from tqdm import tqdm

img = Image.fromarray(img)
in_pixels = dpi * 0.39 * field_size
pix = img.load()
bounds = getBounds(img, in_pixels)
matrix = list()
clusters = list()
for i in tqdm(range(int(img.size[0] / in_pixels))):
    print(i);
    for j in range(int(img.size[1] / in_pixels)):
        print(str(i))
        x1 = bounds[0] + i * in_pixels
        x2 = bounds[0] + i * in_pixels + in_pixels
        y1 = bounds[1] + j * in_pixels
        y2 = bounds[1] + j * in_pixels + in_pixels
        crop = img.crop((x1, y1, x2, y2))
        cell = Cell(crop, i, j)
        cell.cluster_analysis()
        clusters.append(cell)
clusters = sorted(clusters, key=lambda cell: (cell.cluster, cell.sigma))