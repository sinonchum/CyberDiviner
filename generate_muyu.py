#!/usr/bin/env python3
"""
Generate a realistic temple wooden fish (木鱼) sound.

A wooden fish produces a hollow, resonant 'tok' sound with:
- Low fundamental (150-250Hz)
- Wood resonance overtones (not harmonically related)
- Quick sharp attack from mallet strike
- Medium-fast decay with some resonance
- Hollow quality from the wooden cavity
"""

import numpy as np
import wave
import struct

def generate_wooden_fish_sound():
    sample_rate = 44100
    duration = 0.9  # seconds
    
    # Generate time array
    t = np.linspace(0, duration, int(sample_rate * duration), endpoint=False)
    
    # === RESONANT TONES ===
    # Wooden fish has non-harmonic resonances (wood modes, not musical harmonics)
    # These frequencies create a hollow, wooden quality
    frequencies = [
        (180, 0.6),   # Fundamental - deep hollow body
        (340, 0.35),  # First wood resonance
        (520, 0.2),   # Higher wood mode
        (710, 0.12),  # Upper resonance
        (950, 0.06),  # High overtone (subtle)
    ]
    
    # Build the resonant signal
    signal = np.zeros_like(t)
    for freq, amp in frequencies:
        # Add slight frequency modulation for natural quality
        phase_mod = 0.02 * np.sin(2 * np.pi * 3.5 * t)  # subtle wobble
        signal += amp * np.sin(2 * np.pi * freq * t + phase_mod)
    
    # === DECAY ENVELOPES ===
    # Different decay rates for different frequency components
    # Lower frequencies decay slower (more resonance)
    envelope = np.zeros_like(t)
    for i, (freq, amp) in enumerate(frequencies):
        # Lower frequencies: slower decay, higher: faster decay
        decay_rate = 4.0 + (freq / 200.0) * 2.5
        component_env = np.exp(-decay_rate * t)
        envelope += amp * component_env
    
    # Normalize envelope contribution
    envelope /= envelope[0] if envelope[0] > 0 else 1.0
    
    # Apply envelope
    signal = signal * envelope
    
    # === ATTACK NOISE BURST ===
    # The mallet striking wood creates a brief noise burst
    noise_duration = 0.008  # 8ms noise burst
    noise_samples = int(noise_duration * sample_rate)
    
    # Filtered noise (low-pass by averaging)
    raw_noise = np.random.randn(noise_samples)
    # Simple moving average for low-pass effect
    kernel_size = 5
    kernel = np.ones(kernel_size) / kernel_size
    filtered_noise = np.convolve(raw_noise, kernel, mode='same')
    
    # Shape the noise with fast decay
    noise_env = np.exp(-150.0 * t[:noise_samples])
    noise_burst = filtered_noise * noise_env * 0.8
    
    # Add noise to the beginning of signal
    signal[:noise_samples] += noise_burst
    
    # === HOLLOW RESONANCE EFFECT ===
    # Add a slightly delayed version to simulate cavity resonance
    delay_samples = int(0.003 * sample_rate)  # 3ms delay
    delayed = np.zeros_like(signal)
    delayed[delay_samples:] = signal[:-delay_samples] * 0.15
    signal += delayed
    
    # === LOW-PASS FILTER (gentle) ===
    # Simple IIR-style low-pass to soften high frequencies
    # Using convolution with a windowed sinc
    filter_length = 25
    cutoff = 0.15  # normalized frequency (about 3.3kHz at 44100Hz)
    n = np.arange(filter_length)
    center = (filter_length - 1) / 2.0
    # Windowed sinc filter
    h = np.sinc(2 * cutoff * (n - center)) * np.blackman(filter_length)
    h = h / np.sum(h)
    signal = np.convolve(signal, h, mode='same')
    
    # === NORMALIZE ===
    # Normalize to prevent clipping, leave some headroom
    max_val = np.max(np.abs(signal))
    if max_val > 0:
        signal = signal / max_val * 0.85
    
    # === CONVERT TO 16-BIT PCM ===
    signal_int = np.int16(signal * 32767)
    
    return signal_int, sample_rate

def save_wav(filename, samples, sample_rate):
    """Save samples as a WAV file."""
    with wave.open(filename, 'w') as wav_file:
        wav_file.setnchannels(1)  # Mono
        wav_file.setsampwidth(2)  # 16-bit
        wav_file.setframerate(sample_rate)
        wav_file.writeframes(samples.tobytes())

def verify_wav(filename):
    """Verify the WAV file is valid."""
    with wave.open(filename, 'r') as wav_file:
        channels = wav_file.getnchannels()
        sample_width = wav_file.getsampwidth()
        frame_rate = wav_file.getframerate()
        n_frames = wav_file.getnframes()
        duration = n_frames / frame_rate
        
        print(f"File: {filename}")
        print(f"  Channels: {channels}")
        print(f"  Sample width: {sample_width} bytes ({sample_width * 8}-bit)")
        print(f"  Frame rate: {frame_rate} Hz")
        print(f"  Frames: {n_frames}")
        print(f"  Duration: {duration:.3f} seconds")
        print(f"  File size: {n_frames * sample_width} bytes")
        
        return {
            'channels': channels,
            'sample_width': sample_width,
            'frame_rate': frame_rate,
            'n_frames': n_frames,
            'duration': duration
        }

if __name__ == '__main__':
    print("Generating wooden fish (木鱼) sound...")
    
    # Generate the sound
    samples, sample_rate = generate_wooden_fish_sound()
    
    # Save to file
    output_path = '/Users/fsimon/CyberDiviner/app/src/main/res/raw/muyu.wav'
    save_wav(output_path, samples, sample_rate)
    print(f"Saved to: {output_path}")
    
    # Verify
    print("\nVerifying WAV file:")
    info = verify_wav(output_path)
    
    # Validate requirements
    assert info['channels'] == 1, "Must be mono"
    assert info['sample_width'] == 2, "Must be 16-bit"
    assert info['frame_rate'] == 44100, "Must be 44100Hz"
    assert 0.8 <= info['duration'] <= 1.0, f"Duration {info['duration']:.3f}s out of range"
    
    print("\n✓ All requirements met!")
