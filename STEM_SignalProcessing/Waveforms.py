# -*- coding: utf-8 -*-
"""
Creating a common sonar/radar waveform, the chirp
To not use Scipy take alook here: https://udel.edu/~mm/gr/chirp.py

Created on Wed Jun  3 12:55:17 2020

@author: mjackson302
"""

#Linear Frequency Modulated (LFM) waveform called a chirp
import numpy as np
from scipy.signal.waveforms import chirp
from pylab import figure, plot, xlabel, ylabel, subplot, grid, title, clf

f0 = 12.5 #Initial Frequency
t1 = 10.0 #End time
f1 = 2.5  # End frequency
t = np.linspace(0, t1, 5001) #List of times
w = chirp(t, f0=f0, f1=f1, t1=t1, method='linear') #Chirp waveform

figure(1)
clf()
subplot(2,1,1)
plot(t, w)
tstr = "Linear Chirp, f(0)=%g, f(%g)=%g" % (f0, t1, f1)
title(tstr)

subplot(2,1,2)
plot(t, f0 + (f1-f0)*t/t1, 'r')
grid(True)
ylabel('Frequency (Hz)')
xlabel('time (sec)')


