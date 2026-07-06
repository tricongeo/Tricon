package com.tricongeophysics;

import javax.swing.JComboBox;

import com.tricongeophysics.MapperInputFilesPane.DataChanged;

public interface FilesChangedListener {

	void inputFilesChanged(DataChanged dataChanged);

}
