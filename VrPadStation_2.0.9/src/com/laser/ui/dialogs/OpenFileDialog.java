package com.laser.ui.dialogs;


import com.laser.VrPadStation.R;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.widget.Toast;


public abstract class OpenFileDialog implements OnClickListener {
	

	protected abstract FileReader createReader();
	protected abstract void onDataLoaded(FileReader reader);

	public interface FileReader 
	{
		public String getPath();
		public String[] getFileList();
		public boolean openFile(String file);
	}

	private String[] itemsList;
	private Context context;
	private FileReader reader;

	public void launchDialog(Context context) 
	{
		this.context = context;
		reader = createReader();

		itemsList = reader.getFileList();
		if (itemsList.length == 0)
		{
			Toast.makeText(context, R.string.no_files, Toast.LENGTH_SHORT).show();
			return;
		}
		
		AlertDialog.Builder dialog = new AlertDialog.Builder(context);
		dialog.setTitle(R.string.select_file_to_open);
		dialog.setItems(itemsList, this);
		dialog.create().show();
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		boolean isFileOpen = reader.openFile(reader.getPath() + itemsList[which]);

		if (isFileOpen)
			Toast.makeText(context, itemsList[which], Toast.LENGTH_LONG).show();
		else
			Toast.makeText(context, R.string.error_when_opening_file, Toast.LENGTH_SHORT).show();

		onDataLoaded(reader);
	}

}