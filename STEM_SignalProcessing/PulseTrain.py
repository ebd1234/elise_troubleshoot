# -*- coding: utf-8 -*-
"""
Generating a pulse train from a LFM (Chirp)

@author: mjackson302
"""

#Pulse train
import numpy as np
import matplotlib.pyplot as plt
from array import array

tau = 200e-6 #Pulse Length
PRI = 2.5e-3 #Pulse Repetition Interval
fs = 10e6 #Sampling Frequency
BW = 1e6 #LFM bandwidth
fc = 0e3 #Carrier frequency (e.g., shift from LO leakage)
pnum = 10 #Number of pulses
delay = 10e-3 #Time before the puslses start in sec

t = np.arange(-tau/2, (tau/2-1/fs), 1/fs); #Time

z = 1j #Imag
mod = np.exp(z*np.pi*(BW/tau)*np.square(t)) #LFM
sig = np.zeros((np.size(mod)+int(fs*(PRI-tau))),dtype=complex) #Initiate
sig[0:np.size(mod)] = mod #Signal with modulation + time between a single pulse
repsig = np.kron(np.ones((1,10)),sig) #repeat signal for the number of pulses
stackSig = np.zeros((int(delay*fs+repsig.size+PRI*fs)),dtype=complex) #Initiate
stackSig[(int(delay*fs)):(int(delay*fs)+repsig.size)] = repsig #stack up the number of desired pulses

carrier = np.exp(2*np.pi*z*(fc/fs)*(stackSig.size)) #If have carrier offset then shift baseband away from zero
Tx = np.multiply(stackSig,carrier); #Transmit signal

plt.close(1)
plt.figure(1)
plt.plot(Tx);
plt.title("LFM")
plt.show()