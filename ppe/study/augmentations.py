# /content/augmentations.py
import cv2
import numpy as np
import random
import sys
sys.path.append('/content/')

def apply_custom_augmentations(im, hyp):
    # Grayscale
    if hyp.get('grayscale', 0) > 0 and random.random() < hyp['grayscale']:
        im = cv2.cvtColor(im, cv2.COLOR_BGR2GRAY)
        im = cv2.cvtColor(im, cv2.COLOR_GRAY2BGR)

    # Brightness
    if hyp.get('brightness', 0) > 0 and random.random() < hyp['brightness']:
        factor = 1.0 + np.random.uniform(-0.2, 0.2)
        im = np.clip(im * factor, 0, 255).astype(np.uint8)

    # Gamma correction
    if hyp.get('gamma', 0) > 0 and random.random() < hyp['gamma']:
        gamma = np.random.uniform(0.7, 1.5)
        inv_gamma = 1.0 / gamma
        table = np.array([((i / 255.0) ** inv_gamma) * 255
                          for i in np.arange(256)]).astype("uint8")
        im = cv2.LUT(im, table)

    # CLAHE
    if hyp.get('clahe', 0) > 0 and random.random() < hyp['clahe']:
        lab = cv2.cvtColor(im, cv2.COLOR_BGR2LAB)
        l, a, b = cv2.split(lab)
        clahe = cv2.createCLAHE(clipLimit=2.0, tileGridSize=(8, 8))
        cl = clahe.apply(l)
        merged = cv2.merge((cl, a, b))
        im = cv2.cvtColor(merged, cv2.COLOR_LAB2BGR)

    # Blur
    if hyp.get('blur', 0) > 0 and random.random() < hyp['blur']:
        k = random.choice([3, 5])  # 3x3 또는 5x5 커널
        im = cv2.GaussianBlur(im, (k, k), 0)

    # Gaussian Noise
    if hyp.get('noise', 0) > 0 and random.random() < hyp['noise']:
        noise = np.random.normal(0, 25, im.shape).astype(np.int16)
        im = np.clip(im.astype(np.int16) + noise, 0, 255).astype(np.uint8)

    # Cutout
    if hyp.get('cutout', 0) > 0 and random.random() < hyp['cutout']:
        h, w, _ = im.shape
        mask_size = random.randint(h // 8, h // 4)
        y1 = random.randint(0, h - mask_size)
        x1 = random.randint(0, w - mask_size)
        im[y1:y1+mask_size, x1:x1+mask_size] = 0  # 검은 사각형 마스크

    return im
