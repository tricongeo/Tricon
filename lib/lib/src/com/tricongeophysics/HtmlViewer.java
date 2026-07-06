package com.tricongeophysics;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.Border;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

/**
 * Class for displaying HTML encoded text.
 * Supports hypertext links.
 * I wasn't able to get scrolling to work last time I tried, 
 * probably want to place this Component within a scroller.
 * 
 * @author scott
 *
 */
public class HtmlViewer extends JPanel implements HyperlinkListener
{

    private String filename;
    private JEditorPane editorPane;
	private JFrame frame;

    public HtmlViewer(String url)
    {
        this.filename = url;
        editorPane = new JEditorPane();
        editorPane.setBorder(BorderFactory.createEtchedBorder());
        add(editorPane);
        try {
            editorPane.setPage(filename);
            editorPane.setEditable(false);
        } catch (IOException e) {
            e.printStackTrace();
        }
        editorPane.addHyperlinkListener(this);
    }

    public HtmlViewer() {
    	editorPane = new JEditorPane();
    	editorPane.setBorder(BorderFactory.createEtchedBorder());
    	add(editorPane);
    	editorPane.addHyperlinkListener(this);
    	editorPane.setContentType("text/html");
    }

	public static void main(String[] args) {
        JFrame f = new JFrame("html viewer test");
        JScrollPane scroller = new JScrollPane(new HtmlViewer("file:/apdata/tf/tricon/java/mapper/docs/help.html"));
        f.getContentPane().add(scroller);
        scroller.setPreferredSize(new Dimension(1000, 1200));
        f.pack();
        f.addWindowListener( new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
        f.setVisible(true);
    }

    @Override
    public void hyperlinkUpdate(HyperlinkEvent e)
    {
        if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
            try {
                editorPane.setPage(e.getURL());
            } catch (IOException ioe) {
                System.err.println("Error loading: " + ioe);
                try {
                    editorPane.setPage("file://"+filename);
                } catch (IOException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
            }
        };
    }

	public void setHtml(String html) {
		editorPane.setText(html);
	} 
	
	@Override
	public void show() {
		frame = new JFrame();
        JScrollPane scroller = new JScrollPane(this);
        Container p = frame.getContentPane();
        p.setLayout(new BorderLayout());
        p.add(scroller, BorderLayout.CENTER);
        p.add(getButtonPane(), BorderLayout.SOUTH);
        scroller.setPreferredSize(new Dimension(1000, 1200));
        frame.pack();
        frame.addWindowListener( new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
               // System.exit(0);
            }
        });
        frame.setVisible(true);
	}

	private Component getButtonPane() {
		JPanel p = new JPanel();
		JButton print = new JButton("Print");
		print.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				print();
			}});
		
		JButton close = new JButton("Close");
		close.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				close();
			}});
		
		p.add(print);
		p.add(close);
		return p;
	}

	protected void close() {
		frame.setVisible(false);
		frame.dispose();
		frame = null;
	}
	
	@Override
	public void setBorder(Border border) {
		if (editorPane == null) return;
		editorPane.setBorder(border);
	}

	protected void print() {
		PrintUtility.printComponent(this, false);
	}
}
