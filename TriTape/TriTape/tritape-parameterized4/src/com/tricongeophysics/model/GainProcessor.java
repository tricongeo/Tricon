package com.tricongeophysics.model;

/** simple constant scalar gain */
public class GainProcessor implements TraceProcessor
{
    private final double gain;

    public GainProcessor(double gain)
    {
        this.gain = gain;
    }

    @Override
    public float[] process(float[] samples, int sampleRateMicros)
    {
        float[] out = new float[samples.length];
        for (int i = 0; i < samples.length; i++)
        {
            out[i] = (float) (samples[i] * gain);
        }
        return out;
    }

    @Override
    public String getName() { return "Gain (x" + gain + ")"; }
}
