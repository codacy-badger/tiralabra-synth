package synth;

import audiothread.*;

import java.util.function.Supplier;

public class Synth {

    private Keyboard keyboard;
    private Oscillator[] oscillators;
    private ADSR adsr;
    private double envelope;
    private boolean shouldStopGenerating;

    /**
     * supplier tarjoaa AudioThreadille buffereita, jotka sisältävät syntetisaattorin tuottaman äänen
     */
    public Supplier<short[]> supplier = () -> {

        // Muuten luodaan puskurin kokoinen (512) short-array
        short[] buffer = new short[AudioThread.BUFFER_SIZE];

        // Täytetään array, jossa on 220Hz sini-aallon arvot skaalattuna välille 0-32767.
        // Näytteenottotaajuus on 44100Hz, joten yhteen sekuntiin tulee 220 aaltoa.
        for (int i = 0; i < AudioThread.BUFFER_SIZE; i++) {
            double amplitude = 0;
            envelope = adsr.getEnvelopeNext();

            // Kun envolope saa negatiivisen arvon, eli release on mennyt loppuun, lopetetaan generointi
            if (envelope < 0 || shouldStopGenerating) {
                adsr.resetEnvelopes();
                shouldStopGenerating = false;
                return null;
            }

            // Summataan kaikkien oskillaattorien aallot tietyssä ajankohdassa. Saadaan summa-aalto
            for (Oscillator osc : oscillators) {
                amplitude += osc.nextSample();
            }
            // Skaalataan arvot 16 bittiseksi
            buffer[i] = (short) (envelope * (Short.MAX_VALUE * amplitude / oscillators.length));
        }
        return buffer;
    };

    /**
     * Määritellään sovelluksen ulkoasu, oskillaattorit ja liu'ut
     */
    public Synth(ADSR adsr, Keyboard keyboard, Oscillator[] oscillators) {
        this.adsr = adsr;
        this.keyboard = keyboard;
        this.oscillators = oscillators;

    }

    public static class AudioInfo {
        // Käytössä on yleinen näytteenottotaajuus 44100 Hz
        public static final int SAMPLE_RATE = 44100;
    }


    public void setShouldStopGenerating(boolean value) {
        this.shouldStopGenerating = value;
    }
}
