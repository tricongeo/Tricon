package com.tricongeophysics.model;

/**
 * Which SEG-D revision's general-header structure to assume when opening a
 * file. SEG-D varies significantly between revisions - Rev 3.x in particular
 * adds a General Header Block #3 and moves the extended/external header
 * block counts there instead of General Header Block #2 - so a single fixed
 * layout can't cover both well. See SegdBufferedFileReader's class javadoc
 * for exactly what differs between the two modes here.
 *
 * REV2_1 models SmartSolo/DTCC's SEG-D Rev 2.1 (V5, format code 8058) SG
 * layout (see "SmartSolo_segd_format_Rev2.1_description" manual). Its
 * overall block structure (fixed-count General Header blocks, Channel Set
 * Descriptor blocks, a single Extended Header block, a single External
 * Header block, then trace data) resembles REV1_REV2, but several of the
 * exact byte offsets differ - and, like REV3_1, its Trace Header Extension
 * #1 needs to be read (not skipped) since RECLINE/RECSTN live there. See
 * SegdBufferedFileReader's class javadoc for the confirmed offsets.
 */
public enum SegdVersion
{
    REV1_REV2("Rev 1 / Rev 2"),
    REV2_1("Rev 2.1"),
    REV3_1("Rev 3.1");

    private final String label;

    SegdVersion(String label) { this.label = label; }

    @Override
    public String toString() { return label; }
}
