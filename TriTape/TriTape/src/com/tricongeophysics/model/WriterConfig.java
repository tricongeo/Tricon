package com.tricongeophysics.model;

/**
 * Simple parameter holder passed to a TraceWriter when a batch reformat job
 * starts. textualHeaderRaw/binaryHeaderRaw, when set, carry the exact bytes
 * physically read from the input file (see TraceMonitor.submitReformat(),
 * which populates these when both input and output are SEG-Y) so the output
 * file's textual/binary headers default to the input's actual content rather
 * than SegyWriter's generic fallback - textualHeader is only used as a
 * fallback when textualHeaderRaw is null (e.g. non-SEG-Y input, or the input
 * headers couldn't be read).
 */
public class WriterConfig
{
    public int sampleRateMicros;
    public int samplesPerTrace;
    public String textualHeader = "reformatted by SeismicReformatApp";
    public byte[] textualHeaderRaw;
    public byte[] binaryHeaderRaw;

    public WriterConfig(int sampleRateMicros, int samplesPerTrace)
    {
        this.sampleRateMicros = sampleRateMicros;
        this.samplesPerTrace = samplesPerTrace;
    }
}
