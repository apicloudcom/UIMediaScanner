/**
 * APICloud Modules
 * Copyright (c) 2014-2015 by APICloud, Inc. All Rights Reserved.
 * Licensed under the terms of the The MIT License (MIT).
 * Please see the license.html included with this distribution for details.
 */
package com.uzmap.pkg.uzmodules.UIMediaScanner;

import java.io.Serializable;

public class FileInfo implements Serializable {

	public static int SORT_BY_TIME = 0x0000;
	public static int SORT_BY_SIZE = 0x0001;

	public static int SORT_BY_DESC = 0x0002;
	public static int SORT_BY_ASC = 0x0003;

	public static int SORT_FLAG = SORT_BY_TIME;
	public static int SORT_LAW = SORT_BY_ASC;

	private static final long serialVersionUID = -2011377143646848031L;

	public String path;
	public String thumbImgPath;

	public long size;
	public String mimeType;
	public int imgId;
	public long time;
	
	public String groupName;
	
	public boolean isChecked;

}
