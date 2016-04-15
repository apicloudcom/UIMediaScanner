/**
 * APICloud Modules
 * Copyright (c) 2014-2015 by APICloud, Inc. All Rights Reserved.
 * Licensed under the terms of the The MIT License (MIT).
 * Please see the license.html included with this distribution for details.
 */
package com.uzmap.pkg.uzmodules.UIMediaScanner;

import java.util.ArrayList;
import android.annotation.SuppressLint;
import android.os.Parcel;
import android.os.Parcelable;

@SuppressLint("ParcelCreator")
public class UzFileTraversal implements Parcelable {
	public String filename;

	public ArrayList<String> filecontent = new ArrayList<String>();
	public ArrayList<FileInfo> fileInfos = new ArrayList<FileInfo>();

	@Override
	public int describeContents() {
		return 0;
	}
	
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(filename);
		dest.writeList(filecontent);
		dest.writeList(fileInfos);
	}

	public static final Parcelable.Creator<UzFileTraversal> CREATOR = new Creator<UzFileTraversal>() {

		@Override
		public UzFileTraversal[] newArray(int size) {
			return null;
		}

		@SuppressWarnings("unchecked")
		@Override
		public UzFileTraversal createFromParcel(Parcel source) {

			UzFileTraversal ft = new UzFileTraversal();
			ft.filename = source.readString();
			ft.filecontent = source.readArrayList(UzFileTraversal.class.getClassLoader());
			ft.fileInfos = source.readArrayList(UzFileTraversal.class.getClassLoader());
			return ft;
			
		}
		
	};
}
