# -*- coding: utf-8 -*-
"""
Basic code to show implementation of windowing
Read: https://flothesof.github.io/FFT-window-properties-frequency-analysis.html
Read: http://qingkaikong.blogspot.com/2016/10/signal-processing-why-do-we-need-taper.html
Created on Wed Jun  3 12:54:17 2020

@author: mjackson302
"""
import numpy as np
import matplotlib.pyplot as plt
from scipy import signal

fs = 100.0  # sampling rate
t = np.arange(0,1,1/fs) # time vector
freq = 5;   # frequency of the signal
x = np.sin(2*np.pi*freq*t + 5) # create the signal (shifting for illustration)
#Plot
plt.close(1)
plt.figure(1)
plt.ylabel("Amplitude")
plt.xlabel("Time [s]")
plt.plot(t, x)
plt.show()

y = np.fft.fftshift(np.fft.fft(x)) #Frequency transform
N = y.size
f = np.linspace(-fs/2, fs/2, N) #Vector of frequencies
#Plot
plt.close(2)
plt.figure(2)
plt.ylabel("Amplitude")
plt.xlabel("Frequency [Hz]")
plt.bar(f, np.abs(y) * 1 / N, width=1.5)  # 1 / N is a normalization factor
plt.show()
#Smooth in frequency as signal segment is periodic

# Repeat but cutting off signal
t = np.arange(0,0.5,1/fs)
x = np.sin(2*np.pi*freq*t + 5)
plt.close(3)
plt.figure(3)
plt.ylabel("Amplitude")
plt.xlabel("Time [s]")
plt.plot(t, x)
plt.show()

y = np.fft.fftshift(np.fft.fft(x))
N = y.size
f = np.linspace(-fs/2, fs/2, N)

plt.close(4)
plt.figure(4)
plt.ylabel("Amplitude")
plt.xlabel("Frequency [Hz]")
plt.bar(f, np.abs(y) * 1 / N, width=1.5)  # 1 / N is a normalization factor
plt.show()
#Not smooth frequency plots as signal segment is aperiodic

#Apply Window
window = signal.windows.hamming(N) #Hamming window. Please visualize
x_tapered = window * x 

plt.close(5)
plt.figure(5)
plt.ylabel("Amplitude")
plt.xlabel("Time [s]")
plt.plot(t,x_tapered)
plt.show()

y_tapered = np.fft.fftshift(np.fft.fft(x_tapered))

plt.close(6)
plt.figure(6)
plt.ylabel("Amplitude")
plt.xlabel("Frequency [Hz]")
plt.bar(f, np.abs(y_tapered) * 1 / N, width=1.5)  # 1 / N is a normalization factor
plt.show()
#What happened to sidelobes, peak height, and peak width?
