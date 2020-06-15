# -*- coding: utf-8 -*-
"""
https://www.grauonline.de/alexwww/ardumower/filter/filter.html
Code example: http://users.camk.edu.pl/bejger/snr-periodic-signal/
Created on Wed Jun  3 12:54:57 2020

@author: mjackson302
"""

import numpy as np
import matplotlib.pyplot as plt

fs = 1000           # sampling rate [Hz]  
T = 4               # duration [s]
amp = 1           # amplitude of the sinusoid 
f = 1            # frequency of the signal 

Num = T*fs            # total number of points 

# time interval spaced with 1/fs 
t = np.arange(0, T, 1./fs)
 
# white noise 
#noise = np.random.normal(size=t.shape)
noise = np.squeeze(np.random.randn(Num,2).view(np.complex128)) #Complex noise

# sinusoidal signal with amplitude amp
#template = amp*np.sin(f*2*np.pi*t)
template = amp*np.exp(1j*2*np.pi*f*t[:fs])
signal = np.zeros(np.size(noise)) + 0j
signal[int(np.size(t)/2-fs/2):int(np.size(t)/2+fs/2)] = template
 
# data: signal (template) with added noise 
data = signal + noise

N = np.size(data)+np.size(template)-1 #Size for cross correlation

plt.close(1)
plt.figure(1)
plt.plot( np.abs(data), '-', color="grey")
plt.plot( np.abs(signal), '-', color="blue", linewidth=2)
plt.xlabel('sample')
plt.ylabel('data = signal + noise')
plt.grid(True)

# FFT of the template and the data (not normalized)  
template_fft = np.fft.fftshift(np.fft.fft(template,N))
data_fft = np.fft.fftshift(np.fft.fft(data,N))

# Sampling frequencies up to the Nyquist limit (fs/2)  
sample_freq = np.linspace(-fs/2, fs/2, N)

# FT power 
plt.close(2)
plt.figure(2)
plt.plot(sample_freq, np.abs(template_fft), color="red", alpha=0.5, linewidth=4)
plt.plot(sample_freq, np.abs(data_fft), color="grey")


# Applying the matched filter (template is the signal)
matched_filter = np.fft.fftshift(np.fft.ifft(data_fft * template_fft.conjugate()))
mf_shift = np.zeros(np.size(matched_filter)-1) + 0j
mf_shift[:int(np.size(matched_filter)-np.size(data)/2)-1] = matched_filter[int(np.size(data)/2+1):]
mf_shift[int(np.size(matched_filter)-np.size(data)/2)-1:] = matched_filter[:int(np.size(data)/2)]

plt.close(3)
plt.figure(3)
plt.plot(np.abs(mf_shift))
plt.xlabel('sample')
plt.ylabel('Matched Filtered Signal')
plt.grid(True)