# -*- coding: utf-8 -*-
"""
Code to display Fourier Transform (through FFT)
credit: https://www.ritchievink.com/blog/2017/04/23/understanding-the-fourier-transform-by-example/
Created on Wed Jun  3 12:54:29 2020

@author: mjackson302
"""
import numpy as np 
from matplotlib import pyplot as plt

freq = 40, 90  # Hz, signal frequencies
fs = 500 # Hz, sampling rate ( >= 2*f) 
t = np.arange(0,1,1/fs) # start, end, and samplel rate
#t = np.linspace(0,1,N) #Equivalent if N is endtime/(1/fs)
N = t.size #number of time samples
x = np.sin(freq[0] * 2 * np.pi * t) + 0.5 * np.sin(freq[1] * 2 * np.pi * t) # creating a signla made of two sinusoids at two different frequencies
#Plot in time
plt.close(1)
plt.figure(1)
plt.ylabel("Amplitude")
plt.xlabel("Time [s]")
plt.plot(t, x)
plt.show()

y = np.fft.fftshift(np.fft.fft(x)) #Compute the Fourier Transform to go from time to frequency. Need fftshift to shift the zero frequency to the center
#T = t[1] - t[0]  # sampling interval 
#f = np.linspace(0, 1 / T, N) # List of frequencies 
f = np.linspace(-fs/2, fs/2, N) # List of frequencies
#Plot in frequency
plt.close(2)
plt.figure(2)
plt.ylabel("Amplitude")
plt.xlabel("Frequency [Hz]")
plt.bar(f, np.abs(y) * 1 / N, width=1.5)  # 1 / N is a normalization factor for FFT
plt.show()
#Notice how they appears in both the positive and negative frequencies