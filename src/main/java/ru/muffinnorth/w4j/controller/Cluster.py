from PIL import Image, ImageDraw, ImageFilter
from matplotlib import pyplot as plt
import numpy as np
from tqdm import tqdm

img = Image.fromarray(img)
in_pixels = int(dpi * 0.39 * field_size)
pix = img.load()
bounds = getBounds(img, in_pixels)

print(bounds)
matrix = list()
clusters = list()
for i in tqdm(range(int(img.size[0] / in_pixels)), position=0, desc="col", leave=False,):
    for j in range(int(img.size[1] / in_pixels)):
        x1 = bounds[0] + i * in_pixels
        x2 = bounds[0] + i * in_pixels + in_pixels
        y1 = bounds[1] + j * in_pixels
        y2 = bounds[1] + j * in_pixels + in_pixels
        crop = img.crop((x1, y1, x2, y2))
        cell = Cell(crop, i, j)
        cell.cluster_analysis()
        clusters.append(cell)
clusters.append(cell)
clusters = sorted(clusters, key=lambda cell: (cell.cluster, cell.sigma))