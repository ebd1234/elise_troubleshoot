import numpy as np
from scipy.io import wavfile

samplerate = 44100

chirplength = 6  #generate 6 second chirp

f0 = 440
f1 = 1000

signal = np.arange(chirplength*samplerate)/(chirplength*samplerate)
signal = np.interp(signal, [0, 1], [f0, f1])
signal = np.sin(signal.cumsum() * 2 * np.pi / samplerate)
signal = np.float32(signal)wavfile.write("audio.wav", samplerate, signal)