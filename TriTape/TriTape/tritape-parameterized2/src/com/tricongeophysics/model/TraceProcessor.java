package com.tricongeophysics.model;

/**
 * Strategy pattern: a single processing step (AGC, gain, filter, ...) that can
 * be applied to a trace's sample array. Implementations must return a NEW
 * array and must not mutate the input, so the original SeismicTrace data is
 * never altered by preview processing.
 */
public interface TraceProcessor
{
    float[] process(float[] samples, int sampleRateMicros);

    String getName();
}
