package com.tricongeophysics.view;

/** trace rendering styles supported by TraceViewer */
public enum DisplayMode
{
    VARIABLE_DENSITY("Variable Density"),
    VAWG("VAWG");

    private final String label;

    DisplayMode(String label) { this.label = label; }

    @Override
    public String toString() { return label; }
}
