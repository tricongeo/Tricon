package com.tricongeophysics.view;

import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;

/**
 * A FlowLayout that actually wraps onto additional rows when components
 * don't fit the available width, instead of just reporting a preferred width
 * equal to the sum of every component's width (which is what plain
 * java.awt.FlowLayout does, and which makes it useless for wrapping inside a
 * fixed region like BorderLayout.NORTH). Used for TraceViewer's toolbar,
 * which has a lot of controls and should wrap to extra rows rather than
 * clip/overflow when the window is narrow.
 */
public class WrapLayout extends FlowLayout
{
    public WrapLayout()
    {
        super();
    }

    public WrapLayout(int align)
    {
        super(align);
    }

    public WrapLayout(int align, int hgap, int vgap)
    {
        super(align, hgap, vgap);
    }

    @Override
    public Dimension preferredLayoutSize(Container target)
    {
        return layoutSize(target, true);
    }

    @Override
    public Dimension minimumLayoutSize(Container target)
    {
        Dimension minimum = layoutSize(target, false);
        minimum.width -= (getHgap() + 1);
        return minimum;
    }

    private Dimension layoutSize(Container target, boolean preferred)
    {
        synchronized (target.getTreeLock())
        {
            // find the width to wrap against: the target's own width if laid out already,
            // otherwise walk up to the nearest ancestor that has one
            Container container = target;
            while (container.getSize().width == 0 && container.getParent() != null)
            {
                container = container.getParent();
            }
            int targetWidth = container.getSize().width;
            if (targetWidth == 0)
            {
                targetWidth = Integer.MAX_VALUE;
            }

            int hgap = getHgap();
            int vgap = getVgap();
            Insets insets = target.getInsets();
            int horizontalInsetsAndGap = insets.left + insets.right + (hgap * 2);
            int maxWidth = targetWidth - horizontalInsetsAndGap;

            Dimension dim = new Dimension(0, 0);
            int rowWidth = 0;
            int rowHeight = 0;

            int nmembers = target.getComponentCount();
            for (int i = 0; i < nmembers; i++)
            {
                Component m = target.getComponent(i);
                if (m.isVisible())
                {
                    Dimension d = preferred ? m.getPreferredSize() : m.getMinimumSize();
                    if (rowWidth + d.width > maxWidth && rowWidth > 0)
                    {
                        addRow(dim, rowWidth, rowHeight);
                        rowWidth = 0;
                        rowHeight = 0;
                    }
                    if (rowWidth != 0)
                    {
                        rowWidth += hgap;
                    }
                    rowWidth += d.width;
                    rowHeight = Math.max(rowHeight, d.height);
                }
            }
            addRow(dim, rowWidth, rowHeight);

            dim.width += horizontalInsetsAndGap;
            dim.height += insets.top + insets.bottom + vgap * 2;

            Container scrollPane = SwingUtilities.getAncestorOfClass(JScrollPane.class, target);
            if (scrollPane != null && target.isValid())
            {
                dim.width -= (hgap + 1);
            }

            return dim;
        }
    }

    private void addRow(Dimension dim, int rowWidth, int rowHeight)
    {
        dim.width = Math.max(dim.width, rowWidth);
        if (dim.height > 0)
        {
            dim.height += getVgap();
        }
        dim.height += rowHeight;
    }
}
