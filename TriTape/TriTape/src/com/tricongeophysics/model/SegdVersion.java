package com.tricongeophysics.model;

/**
 * Which SEG-D revision's general-header structure to assume when opening a
 * file. SEG-D varies significantly between revisions - Rev 3.x in particular
 * adds a General Header Block #3 and moves the extended/external header
 * block counts there instead of General Header Block #2 - so a single fixed
 * layout can't cover both well. See SegdBufferedFileReader's class javadoc
 * for exactly what differs between the two modes here.
 */
public enum SegdVersion
{
    REV1_REV2("Rev 1 / Rev 2"),
    REV3_1("Rev 3.1");

    private final String label;

    SegdVersion(String label) { this.label = label; }

    @Override
    public String toString() { return label; }
}
