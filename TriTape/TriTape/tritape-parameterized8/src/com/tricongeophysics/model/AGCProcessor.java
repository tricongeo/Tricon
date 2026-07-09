package com.tricongeophysics.model;

/** sliding-window RMS automatic gain control */
public class AGCProcessor implements TraceProcessor
{
    private final double windowMs;

    public AGCProcessor(double windowMs)
    {
        this.windowMs = windowMs;
    }

    @Override
    public float[] process(float[] samples, int sampleRateMicros)
    {
        if (samples.length == 0) return samples.clone();
        int windowSamples = Math.max(1, (int) Math.round((windowMs * 1000.0) / Math.max(1, sampleRateMicros)));
        int half = windowSamples / 2;
        float[] out = new float[samples.length];

        //running sum of squares over the sliding window for O(n) performance
        double sumSq = 0;
        for (int i = 0; i <= Math.min(half, samples.length - 1); i++)
        {
            sumSq += samples[i] * (double) samples[i];
        }
        int count = Math.min(half, samples.length - 1) + 1;

        for (int i = 0; i < samples.length; i++)
        {
            int addIdx = i + half;
            int remIdx = i - half - 1;
            if (i > 0)
            {
                if (addIdx < samples.length) { sumSq += samples[addIdx] * (double) samples[addIdx]; count++; }
                if (remIdx >= 0) { sumSq -= samples[remIdx] * (double) samples[remIdx]; count--; }
            }
            double rms = Math.sqrt(Math.max(sumSq, 1e-20) / Math.max(count, 1));
            double gain = rms > 1e-9 ? 1.0 / rms : 1.0;
            out[i] = (float) (samples[i] * gain);
        }

        //normalize so AGC output is roughly comparable in scale to the input
//        float maxAbs = 0;
//        for (float v : samples) maxAbs = Math.max(maxAbs, Math.abs(v));
//        float maxOut = 0;
//        for (float v : out) maxOut = Math.max(maxOut, Math.abs(v));
//        if (maxOut > 1e-9f && maxAbs > 0)
//        {
//            float scale = maxAbs / maxOut;
//            for (int i = 0; i < out.length; i++) out[i] *= scale;
//        }
        return out;
    }

    @Override
    public String getName() { return "AGC (" + windowMs + " ms)"; }
}
