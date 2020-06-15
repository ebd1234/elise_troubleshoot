# -*- coding: utf-8 -*-
"""
Script to show off sampling rates
Read: https://makersportal.com/blog/2018/9/13/audio-processing-in-python-part-i-sampling-and-the-fast-fourier-transform
Created on Wed Jun  3 12:52:59 2020

@author: mjackson302
"""
import numpy as np
from matplotlib import pyplot as plt

freq = 5  # Hz, signal frequency
fs = 50 # Hz, sampling rate ( >= 2*f) 
t = np.arange(0,1,1/fs) # sample interval
x = np.sin(2*np.pi*freq*t)
plt.close(1)
plt.figure(1)
plt.plot(t,x,'o-')
plt.xlabel('time',fontsize=18)
plt.ylabel('amplitude',fontsize=18)
#See what happens as change sampling rate
