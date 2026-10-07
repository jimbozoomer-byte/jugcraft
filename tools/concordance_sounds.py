"""Original sounds for the Arcane Concordance (tools/concordance.py), synthesised here (no recorded or third-party audio).

Eleven short mono cues, one per entry in concordance.SOUND_EVENTS (tools/generate_material_data.py writes their
sounds.json entries, "jugcraft:concordance/<name>"; this script only draws the .ogg files):

- kindle_gather (0.6 s, Kindle's cast, KINDLE_CAST_SECONDS long): light gathering, a few detuned sine partials that
  rise together from F#5 to A5 under a slow swell, the upper partials arriving last;
- kindle (0.8 s, Kindle's release): a bright bell chime on A4 struck over a warm D4, a fifth below it;
- examine (0.35 s): a small, high, quiet crystal ping on E7;
- study_complete (0.8 s): two soft ascending chimes, A5 then E6, the same rising fifth;
- lantern_ignite (0.6 s): a breath of band-passed noise that swells and brightens, settling into a warm low D3;
- lantern_snuff (0.4 s): a soft puff of noise whose band falls away as the flame goes out;
- aegis (0.9 s, Dawn Aegis): a low D3 and A3 swelling up into a held, softly beating D4, A4, E5 chord, the shell
  closing round you;
- revelation (0.8 s): a shimmer of detuned voices gliding up from A4 to E6, ending in a quiet E6 ping;
- lance (0.45 s, Lance of Dawn): a bright crack of high band-passed noise and a tone falling fast from E7 to A5 over
  a short A5 bell, sharp enough to read as an attack;
- flash (0.35 s, Flashstep): an airy whoosh, noise swept up from 600 Hz to 3 kHz and gone;
- lanternward (1.0 s): a warm low D3 under three bell strikes rising D4, A4, D5, the light spreading to allies.

The Concordance's cues share one key (D, A, E: stacked fifths), so the gather leads into the kindle and the study chime
answers it. Every cue starts and ends at silence (no clicks), is normalised to a set peak (0.8 like the choir and drone
sounds, lower for the quiet ones) and is encoded like them: 44.1 kHz mono Ogg Vorbis, libvorbis -q:a 4. The noise comes
from a seeded generator and the encoder runs bit-exact with no metadata, so a rerun writes identical files with the same
ffmpeg build. Writes assets/jugcraft/sounds/concordance/*.ogg (needs numpy and ffmpeg with libvorbis).
Run from anywhere:  python3 tools/concordance_sounds.py
"""
import os
import subprocess
import sys

import numpy as np

sys.path.insert(0, os.path.dirname(__file__))
import concordance  # noqa: E402

RATE = 44100
OUT = os.path.join(os.path.dirname(__file__), "..", "src/main/resources/assets/jugcraft/sounds/concordance")
# The loudest a cue is drawn (fraction of full scale before encoding); the choir and drone sounds use 0.8.
PEAK = 0.8

# The shared key, in Hz (equal temperament, A4 = 440).
D3, D4, A4, FS5, A5, E6, E7 = 146.83, 293.66, 440.0, 739.99, 880.0, 1318.51, 2637.02


def samples(seconds):
    return int(round(RATE * seconds))


def times(n):
    return np.arange(n) / RATE


def fade(n, start, end):
    """A raised-cosine fade-in over the first {@code start} seconds and fade-out over the last {@code end}, so every
    cue begins and ends at exactly zero."""
    env = np.ones(n)
    a, b = max(1, samples(start)), max(1, samples(end))
    env[:a] = 0.5 - 0.5 * np.cos(np.pi * np.arange(a) / a)
    env[n - b:] *= 0.5 + 0.5 * np.cos(np.pi * np.arange(1, b + 1) / b)
    return env


def strike(n, attack, decay, at=0.0):
    """A struck envelope: silent before {@code at}, a raised-cosine attack, then an exponential decay (time constant
    {@code decay} seconds)."""
    t = times(n) - at
    rise = np.clip(t / attack, 0.0, 1.0)
    return np.where(t < 0, 0.0, (0.5 - 0.5 * np.cos(np.pi * rise)) * np.exp(-np.maximum(t - attack, 0.0) / decay))


def chime(n, hz, partials, attack, at=0.0, beat=0.0):
    """A struck tone: (ratio, strength, decay seconds) partials over {@code hz}. {@code beat} Hz adds a quieter partner
    a little above the lowest partial, the slow beating a real bell has (it swells and dips, never cancels)."""
    t = times(n) - at
    out = np.zeros(n)
    for index, (ratio, amp, decay) in enumerate(partials):
        tone = np.sin(2 * np.pi * hz * ratio * t)
        if index == 0 and beat:
            tone = 0.72 * tone + 0.28 * np.sin(2 * np.pi * (hz * ratio + beat) * t + 0.9)
        out += amp * tone * strike(n, attack, decay, at)
    return out


def band(noise, centre, q):
    """Noise through a state-variable filter whose centre (Hz, one value per sample) may move; returns its band-pass and
    low-pass outputs (a breath needs some of both)."""
    g = np.tan(np.pi * np.clip(centre, 20.0, RATE * 0.45) / RATE)
    k = 1.0 / q
    a1 = 1.0 / (1.0 + g * (g + k))
    a2 = g * a1
    a3 = g * a2
    bp = np.empty(len(noise))
    lp = np.empty(len(noise))
    s1 = s2 = 0.0
    for i in range(len(noise)):
        v3 = noise[i] - s2
        v1 = a1[i] * s1 + a2[i] * v3
        v2 = s2 + a2[i] * s1 + a3[i] * v3
        s1 = 2 * v1 - s1
        s2 = 2 * v2 - s2
        bp[i] = v1
        lp[i] = v2
    return bp, lp


def rms(signal):
    return np.sqrt(np.mean(signal ** 2))


def glide(t, start, end, seconds):
    """A smooth exponential pitch glide from {@code start} to {@code end} Hz over {@code seconds} (smoothstep)."""
    s = np.clip(t / seconds, 0.0, 1.0)
    s = s * s * (3 - 2 * s)
    return start * (end / start) ** s


def kindle_gather():
    """Light gathering: detuned sine partials rising together, a slow swell, the upper partials arriving last."""
    n = samples(concordance.KINDLE_CAST_SECONDS)
    t = times(n)
    base = glide(t, FS5, A5, 0.55)
    # (ratio, strength, when the partial is half in as a fraction of the cast). Each partial is a voice in tune and two
    # quieter companions detuned a few cents either side, so it beats (shimmers) without ever cancelling.
    partials = [(0.5, 0.3, 0.0), (1.0, 1.0, 0.0), (1.5, 0.42, 0.35), (2.0, 0.26, 0.55), (3.0, 0.1, 0.75)]
    voices = [(0.0, 1.0), (-7.0, 0.35), (6.0, 0.35)]
    out = np.zeros(n)
    for index, (ratio, amp, half) in enumerate(partials):
        swell = 1.0 / (1.0 + np.exp(-(t / t[-1] - half) * 14)) if half else 1.0
        for j, (cents, level) in enumerate(voices):
            phase = np.cumsum(base * ratio * 2 ** ((cents + index) / 1200)) / RATE
            out += amp * level * swell * np.sin(2 * np.pi * phase + 0.7 * j)
    # The shimmer: a shallow flutter that quickens as the light gathers.
    flutter = 1 + 0.1 * np.sin(2 * np.pi * np.cumsum(7 + 9 * t / t[-1]) / RATE)
    rise = (t / t[-1]) ** 1.4
    return out * flutter * rise * fade(n, 0.01, 0.05)


def kindle():
    """The light kindles: a bright bell on A4 over a warm D4 a fifth below, the bell's upper partials dying first."""
    n = samples(0.8)
    warm = chime(n, D4, [(1.0, 1.0, 0.42), (2.0, 0.3, 0.25), (3.0, 0.1, 0.16)], attack=0.018)
    bell = chime(n, A4, [(1.0, 1.0, 0.38), (2.0, 0.55, 0.24), (3.0, 0.3, 0.15), (4.16, 0.16, 0.09),
                         (5.43, 0.08, 0.06)], attack=0.003, beat=1.6)
    return (0.55 * warm + bell) * fade(n, 0.0005, 0.08)


def examine():
    """A small crystal ping: a high E7 with one glassy inharmonic overtone, short and quiet."""
    n = samples(0.35)
    ping = chime(n, E7, [(1.0, 1.0, 0.085), (2.76, 0.22, 0.03), (5.4, 0.05, 0.012)], attack=0.0015, beat=6.0)
    return ping * fade(n, 0.0003, 0.05)


def study_complete():
    """Two soft ascending chimes, A5 then E6 (the Concordance's rising fifth)."""
    n = samples(0.8)
    soft = [(1.0, 1.0, 0.22), (2.0, 0.16, 0.12), (2.76, 0.05, 0.06), (3.0, 0.05, 0.08)]
    first = chime(n, A5, soft, attack=0.006, beat=2.0)
    second = chime(n, E6, [(r, a, d * 1.35) for r, a, d in soft], attack=0.006, at=0.17, beat=2.5)
    return (0.85 * first + second) * fade(n, 0.0005, 0.08)


def lantern_ignite(seed):
    """A breath of noise that swells and brightens, then settles into a warm, faintly flickering low D3."""
    n = samples(0.6)
    t = times(n)
    rng = np.random.default_rng(seed)
    # The breath's band opens from 450 Hz to 2.35 kHz as the wick catches, then closes to 600 Hz.
    centre = np.where(t < 0.16, 450 + 1900 * (t / 0.16) ** 1.2, 600 + 1750 * np.exp(-(t - 0.16) / 0.08))
    bp, lp = band(rng.normal(size=n), centre, q=1.1)
    bp, _ = band(bp, centre, q=1.4)  # a second pass steepens the band's skirts: breath, not hiss
    breath = bp / rms(bp) + 0.4 * lp / rms(lp)
    whoosh = breath * np.clip(t / 0.12, 0, 1) ** 1.5 * np.exp(-np.maximum(t - 0.12, 0) / 0.09)
    # The flame's tone: D3 with a few soft harmonics, fading in under the whoosh, with a slow flicker.
    tone = sum(amp * np.sin(2 * np.pi * D3 * k * t + 0.4 * k) for k, amp in ((1, 1.0), (2, 0.5), (3, 0.22), (4, 0.08)))
    flicker = 1 + 0.06 * np.sin(2 * np.pi * 5.3 * t) + 0.03 * np.sin(2 * np.pi * 8.9 * t + 1.1)
    body = tone / rms(tone) * flicker * np.clip((t - 0.07) / 0.12, 0, 1) ** 2 * np.exp(-np.maximum(t - 0.19, 0) / 0.32)
    return (0.7 * whoosh + body) * fade(n, 0.003, 0.09)


def lantern_snuff(seed):
    """A soft falling puff: noise low-passed from 1.6 kHz down to 250 Hz as it dies away."""
    n = samples(0.4)
    t = times(n)
    rng = np.random.default_rng(seed)
    centre = 250 + 1350 * np.exp(-t / 0.1)
    bp, lp = band(rng.normal(size=n), centre, q=0.8)
    puff = lp / rms(lp) + 0.45 * bp / rms(bp)
    return puff * strike(n, 0.012, 0.13) * fade(n, 0.0, 0.06)


A3, E5, D5 = 220.0, 659.26, 587.33


def aegis():
    """The shell forms: a low fifth swelling up into a held, softly beating chord a fifth apart."""
    n = samples(0.9)
    t = times(n)
    low = sum(amp * np.sin(2 * np.pi * hz * t) for hz, amp in ((D3, 1.0), (A3, 0.6)))
    low *= np.clip(t / 0.18, 0, 1) ** 2 * np.exp(-np.maximum(t - 0.18, 0) / 0.25)
    chord = np.zeros(n)
    for hz, amp in ((D4, 1.0), (A4, 0.75), (E5, 0.45)):
        for cents, level in ((0.0, 1.0), (-6.0, 0.3), (5.0, 0.3)):
            chord += amp * level * np.sin(2 * np.pi * hz * 2 ** (cents / 1200) * t)
    chord *= np.clip((t - 0.1) / 0.2, 0, 1) ** 1.5 * np.exp(-np.maximum(t - 0.3, 0) / 0.35)
    return (0.6 * low / rms(low) + chord / rms(chord)) * fade(n, 0.005, 0.12)


def revelation():
    """Hidden things shine: detuned voices gliding up from A4 to E6, ending in a quiet ping."""
    n = samples(0.8)
    t = times(n)
    base = glide(t, A4, E6, 0.5)
    out = np.zeros(n)
    for j, (cents, level) in enumerate(((0.0, 1.0), (-9.0, 0.4), (8.0, 0.4), (1200.0, 0.18))):
        phase = np.cumsum(base * 2 ** (cents / 1200)) / RATE
        out += level * np.sin(2 * np.pi * phase + 0.6 * j)
    out *= np.clip(t / 0.3, 0, 1) * np.exp(-np.maximum(t - 0.45, 0) / 0.12)
    ping = chime(n, E6, [(1.0, 1.0, 0.16), (2.76, 0.12, 0.05)], attack=0.002, at=0.48, beat=3.0)
    return (out / rms(out) * 0.5 + ping / np.max(np.abs(ping))) * fade(n, 0.01, 0.08)


def lance(seed):
    """The beam strikes: a bright crack of high noise and a tone falling fast from E7 to A5 over a short bell."""
    n = samples(0.45)
    t = times(n)
    rng = np.random.default_rng(seed)
    bp, _ = band(rng.normal(size=n), np.full(n, 4200.0), q=1.6)
    crack = bp / rms(bp) * strike(n, 0.002, 0.03)
    fall = np.sin(2 * np.pi * np.cumsum(glide(t, E7, A5, 0.09)) / RATE) * strike(n, 0.002, 0.07)
    bell = chime(n, A5, [(1.0, 1.0, 0.12), (2.0, 0.4, 0.07), (3.0, 0.15, 0.04)], attack=0.003, at=0.01, beat=4.0)
    return (0.45 * crack + 0.8 * fall + bell) * fade(n, 0.0005, 0.06)


def flash(seed):
    """A flash of movement: airy noise swept up from 600 Hz to 3 kHz, swelling and gone."""
    n = samples(0.35)
    t = times(n)
    rng = np.random.default_rng(seed)
    centre = 600 * (3000 / 600) ** np.clip(t / 0.25, 0, 1)
    bp, lp = band(rng.normal(size=n), centre, q=0.9)
    air = bp / rms(bp) + 0.3 * lp / rms(lp)
    return air * np.sin(np.pi * np.clip(t / 0.3, 0, 1)) ** 2 * fade(n, 0.002, 0.04)


def lanternward():
    """Warding light spreads: a warm low D3 under three bell strikes rising D4, A4, D5."""
    n = samples(1.0)
    t = times(n)
    hum = sum(amp * np.sin(2 * np.pi * D3 * k * t) for k, amp in ((1, 1.0), (2, 0.35), (3, 0.12)))
    hum *= np.clip(t / 0.25, 0, 1) ** 2 * np.exp(-np.maximum(t - 0.4, 0) / 0.3)
    bells = np.zeros(n)
    soft = [(1.0, 1.0, 0.28), (2.0, 0.3, 0.15), (3.0, 0.1, 0.09)]
    for at, hz in ((0.08, D4), (0.22, A4), (0.36, D5)):
        bells += chime(n, hz, soft, attack=0.004, at=at, beat=1.8)
    return (0.45 * hum / rms(hum) + bells / np.max(np.abs(bells))) * fade(n, 0.005, 0.12)


def circle_start():
    """A circle awakens: a low D3 drone swelling under a slow upward sweep of soft bells, D4, F#4, A4 (roadmap 12)."""
    n = samples(1.4)
    t = times(n)
    drone = sum(amp * np.sin(2 * np.pi * D3 * k * t) for k, amp in ((1, 1.0), (2, 0.4), (3, 0.15)))
    drone *= np.clip(t / 0.5, 0, 1) ** 2 * np.exp(-np.maximum(t - 0.9, 0) / 0.25)
    bells = np.zeros(n)
    soft = [(1.0, 1.0, 0.4), (2.0, 0.25, 0.2), (3.0, 0.08, 0.1)]
    for at, hz in ((0.25, D4), (0.5, D4 * 1.26), (0.75, A4)):
        bells += chime(n, hz, soft, attack=0.01, at=at, beat=1.2)
    return (0.5 * drone / rms(drone) + bells / np.max(np.abs(bells))) * fade(n, 0.01, 0.2)


def circle_step():
    """A ritual step: one soft low hum pulse on A3 with a faint fifth, like a held breath."""
    n = samples(0.6)
    t = times(n)
    hum = np.sin(2 * np.pi * A4 / 2 * t) + 0.3 * np.sin(2 * np.pi * A4 * 0.75 * t)
    return hum * np.sin(np.pi * np.clip(t / 0.6, 0, 1)) ** 2 * fade(n, 0.005, 0.05)


def circle_complete():
    """A ritual completes: a bright chord struck together, D5 A5 D6, ringing out over the drone's last breath."""
    n = samples(1.6)
    t = times(n)
    bright = [(1.0, 1.0, 0.7), (2.0, 0.35, 0.3), (3.0, 0.12, 0.15), (4.2, 0.05, 0.08)]
    chord = chime(n, D5, bright, attack=0.004) + 0.8 * chime(n, D5 * 1.5, bright, attack=0.004) \
        + 0.6 * chime(n, D5 * 2, bright, attack=0.004)
    low = np.sin(2 * np.pi * D3 * t) * np.exp(-t / 0.5)
    return (chord / np.max(np.abs(chord)) + 0.35 * low) * fade(n, 0.003, 0.25)


def circle_break(seed):
    """A ritual breaks: a dissonant crack (a tritone struck together) and a burst of noise falling away."""
    n = samples(0.9)
    t = times(n)
    rng = np.random.default_rng(seed)
    centre = 2400 * (300 / 2400) ** np.clip(t / 0.6, 0, 1)
    bp, lp = band(rng.normal(size=n), centre, q=1.2)
    noise = (bp / rms(bp) + 0.4 * lp / rms(lp)) * np.exp(-t / 0.18)
    harsh = [(1.0, 1.0, 0.35), (2.7, 0.4, 0.12)]
    crack = chime(n, A4, harsh, attack=0.002) + chime(n, A4 * 1.414, harsh, attack=0.002)
    return (0.6 * noise / np.max(np.abs(noise)) + crack / np.max(np.abs(crack))) * fade(n, 0.002, 0.1)


def crucible_stir(seed):
    """A stir: a low liquid slosh, noise band-passed round 300 Hz swinging up and back, with a soft wooden knock."""
    n = samples(0.7)
    t = times(n)
    rng = np.random.default_rng(seed)
    centre = 220 + 260 * np.sin(np.pi * np.clip(t / 0.6, 0, 1))
    bp, lp = band(rng.normal(size=n), centre, q=2.0)
    slosh = (bp / rms(bp) + 0.5 * lp / rms(lp)) * np.sin(np.pi * np.clip(t / 0.65, 0, 1)) ** 1.5
    knock = chime(n, 180.0, [(1.0, 1.0, 0.05), (2.3, 0.3, 0.03)], attack=0.002, at=0.05)
    return (slosh / np.max(np.abs(slosh)) + 0.4 * knock / np.max(np.abs(knock))) * fade(n, 0.005, 0.08)


def crucible_add(seed):
    """Something drops in: a short plop (a falling tone) and a few bubbles."""
    n = samples(0.5)
    t = times(n)
    plop = np.sin(2 * np.pi * glide(t, 520.0, 180.0, 0.08) * t) * np.exp(-t / 0.06)
    rng = np.random.default_rng(seed)
    bubbles = np.zeros(n)
    for at in sorted(rng.uniform(0.08, 0.35, 4)):
        hz = rng.uniform(600, 1100)
        bubbles += chime(n, hz, [(1.0, 1.0, 0.03)], attack=0.002, at=at)
    return (plop / np.max(np.abs(plop)) + 0.35 * bubbles / max(np.max(np.abs(bubbles)), 1e-9)) * fade(n, 0.002, 0.05)


def crucible_bottle():
    """A dose bottled: a glassy rising fill, two soft tones a fifth apart sliding up."""
    n = samples(0.6)
    t = times(n)
    tone = np.sin(2 * np.pi * glide(t, 440.0, 880.0, 0.45) * t) + 0.5 * np.sin(2 * np.pi * glide(t, 660.0, 1320.0, 0.45) * t)
    return tone * np.sin(np.pi * np.clip(t / 0.6, 0, 1)) ** 2 * fade(n, 0.005, 0.06)


# name -> (signal, peak). Names follow concordance.SOUND_EVENTS ("concordance.<name>").
def cues():
    return {
        "kindle_gather": (kindle_gather(), PEAK * 0.75),
        "kindle": (kindle(), PEAK),
        "examine": (examine(), PEAK * 0.55),
        "study_complete": (study_complete(), PEAK * 0.8),
        "lantern_ignite": (lantern_ignite(seed=41), PEAK),
        "lantern_snuff": (lantern_snuff(seed=43), PEAK * 0.9),
        "aegis": (aegis(), PEAK * 0.85),
        "revelation": (revelation(), PEAK * 0.75),
        "lance": (lance(seed=47), PEAK),
        "flash": (flash(seed=53), PEAK * 0.8),
        "lanternward": (lanternward(), PEAK * 0.85),
        "circle_start": (circle_start(), PEAK * 0.85),
        "circle_step": (circle_step(), PEAK * 0.5),
        "circle_complete": (circle_complete(), PEAK * 0.9),
        "circle_break": (circle_break(seed=59), PEAK * 0.85),
        "crucible_stir": (crucible_stir(seed=61), PEAK * 0.6),
        "crucible_add": (crucible_add(seed=67), PEAK * 0.6),
        "crucible_bottle": (crucible_bottle(), PEAK * 0.6),
    }


def write(name, signal, peak):
    os.makedirs(OUT, exist_ok=True)
    signal = signal / np.max(np.abs(signal)) * peak
    pcm = np.round(signal * 32767).astype("<i2").tobytes()
    path = os.path.join(OUT, name + ".ogg")
    subprocess.run(["ffmpeg", "-y", "-loglevel", "error", "-f", "s16le", "-ar", str(RATE), "-ac", "1", "-i", "-",
                    "-map_metadata", "-1", "-fflags", "+bitexact", "-flags:a", "+bitexact",
                    "-c:a", "libvorbis", "-q:a", "4", path], input=pcm, check=True)
    print("wrote", os.path.normpath(path))


def main():
    drawn = cues()
    expected = {event.split(".", 1)[1] for event in concordance.SOUND_EVENTS}
    if set(drawn) != expected:
        sys.exit(f"concordance.SOUND_EVENTS and this script disagree: {sorted(expected ^ set(drawn))}")
    for name, (signal, peak) in drawn.items():
        write(name, signal, peak)


if __name__ == "__main__":
    main()
