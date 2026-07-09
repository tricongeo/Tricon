package com.tricongeophysics.model;

/** simple windowed-sinc FIR bandpass filter */
public class BandpassFilterProcessor implements TraceProcessor
{
    private final double lowHz;
    private final double highHz;
    private final int taps;

    public BandpassFilterProcessor(double lowHz, double highHz)
    {
        this(lowHz, highHz, 65);
    }

    public BandpassFilterProcessor(double lowHz, double highHz, int taps)
    {
        this.lowHz = lowHz;
        this.highHz = highHz;
        this.taps = (taps % 2 == 0) ? taps + 1 : taps; //force odd length for a symmetric kernel
    }

    @Override
    public float[] process(float[] samples, int sampleRateMicros)
    {
        double fs = 1_000_000.0 / Math.max(1, sampleRateMicros); //samples per second
        double[] kernel = buildKernel(fs);
        return convolveSame(samples, kernel);
    }

    private double[] buildKernel(double fs)
    {
        int m = taps;
        int center = m / 2;
        double fLow = lowHz / fs;
        double fHigh = highHz / fs;
        double[] kernel = new double[m];
        double sum = 0;
        for (int i = 0; i < m; i++)
        {
            int n = i - center;
            double lowPassHigh = sinc(2 * fHigh * n) * 2 * fHigh;
            double lowPassLow = sinc(2 * fLow * n) * 2 * fLow;
            double bandpass = lowPassHigh - lowPassLow;
            double window = 0.54 - 0.46 * Math.cos(2 * Math.PI * i / (m - 1)); //Hamming window
            kernel[i] = bandpass * window;
            sum += kernel[i];
        }
        if (Math.abs(sum) > 1e-9)
        {
            for (int i = 0; i < m; i++) kernel[i] /= sum; //unity gain at DC-normalized passband
        }
        return kernel;
    }

    private static double sinc(double x)
    {
        if (Math.abs(x) < 1e-9) return 1.0;
        double px = Math.PI * x;
        return Math.sin(px) / px;
    }

    private static float[] convolveSame(float[] samples, double[] kernel)
    {
        int n = samples.length;
        int m = kernel.length;
        int center = m / 2;
        float[] out = new float[n];
        for (int i = 0; i < n; i++)
        {
            double acc = 0;
            for (int k = 0; k < m; k++)
            {
                int idx = i + k - center;
                if (idx >= 0 && idx < n)
                {
                    acc += samples[idx] * kernel[k];
                }
            }
            out[i] = (float) acc;
        }
        return out;
    }

    @Override
    public String getName() { return "Bandpass (" + lowHz + "-" + highHz + " Hz)"; }
}
