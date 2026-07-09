package com.tricongeophysics.model;

/** simple parameter holder passed to a TraceWriter when a batch reformat job starts */
public class WriterConfig
{
    public int sampleRateMicros;
    public int samplesPerTrace;
    public String textualHeader = "reformatted by SeismicReformatApp";

    public WriterConfig(int sampleRateMicros, int samplesPerTrace)
    {
        this.sampleRateMicros = sampleRateMicros;
        this.samplesPerTrace = samplesPerTrace;
    }
}
