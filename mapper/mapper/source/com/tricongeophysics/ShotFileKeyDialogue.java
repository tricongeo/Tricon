package com.tricongeophysics;

import java.awt.*;

import com.tricongeophysics.FileKey.Key;


public class ShotFileKeyDialogue extends StationFileKeyDialogue {
	
    private Key upholeKey = new FileKey.Key("Uphole", 37, 41);
    private Key depthKey = new FileKey.Key("Depth", 31, 35);
    private Key srcTypeKey = new FileKey.Key("SrcType", 77, 78);
//    {
//        BUTTON_LABELS = new String[] {"First Line","Last Line","Key","Line","Station","X","Y","Z"};
//    }

    public ShotFileKeyDialogue(FileKey fileKey, Frame parent){
        super(fileKey, parent);

        fileKey.addOptionalFileKey(upholeKey);
        fileKey.addOptionalFileKey(depthKey);
        fileKey.addOptionalFileKey(srcTypeKey);
    }
	
	public ShotFileKeyDialogue(Frame parent, FileKey fileKey){
		this(fileKey, parent);
	}
}
