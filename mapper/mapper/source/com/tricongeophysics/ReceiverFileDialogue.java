package com.tricongeophysics;

import java.awt.*;

public class ReceiverFileDialogue extends StationFileKeyDialogue {
    
	public ReceiverFileDialogue(FileKey fileKey, Frame parent){
		super(fileKey, parent);
	}
	
	public ReceiverFileDialogue(Frame parent, FileKey fileKey){
		this(fileKey, parent);
	}
}
