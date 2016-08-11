/**
 * APICloud Modules
 * Copyright (c) 2014-2015 by APICloud, Inc. All Rights Reserved.
 * Licensed under the terms of the The MIT License (MIT).
 * Please see the license.html included with this distribution for details.
 */
package com.uzmap.pkg.uzmodules.UIMediaScanner;

import java.io.Serializable;
import android.graphics.Bitmap;

public class ConfigInfo implements Serializable {

	private static final long serialVersionUID = 6593439725106812874L;
	
	public static final String FILTER_ALL = "all";
	public static final String FILTER_PICTURE = "picture";
	public static final String FILTER_VIDEO = "video";
	
	public boolean rotation = false;
	
	/**
	 * base style setting
	 */
	public int bgColor = 0xFFFFFFFF;
	public int col = 4;
	public boolean isBounces = false;
	public int selectedMax = 5;
	public boolean classify;
	public String filterType = FILTER_ALL;

	/**
	 * mark styles
	 */
	public String mark_icon;
	public String mark_position = "bottom_left";
	public int mark_size = 18;

	/**
	 * navigation bar styles
	 */
	public int navi_bg = 0xFFEEEEEE;
	public String navi_title = "已选择*项";
	public int navi_title_color = 0xFF000000;
	public int navi_title_size = 18;

	/**
	 * cancel btn styles
	 */
	public int cancel_bg = 0x00000000;
	public int cancel_title_color = 0xFF000000;
	public String cancel_title = "取消";
	public int cancel_title_size = 18;

	public static Bitmap navBgBitmap;
	public static Bitmap cancelBgBitmap;
	public static Bitmap finishBgBitmap;

	/**
	 * finish btn styles
	 */
	public int finish_bg = 0x00000000;
	public int finish_title_color = 0xFF000000;
	public String finish_title = "完成";
	public int finish_title_size = 18;


	/**
	 * the sort type
	 */
	public String key = "time";
	public String order = "desc";
	public boolean isSort = false;
	
	
	/**
	 * ScrollToBottom
	 */
	public int intervalTime = -1;
	public boolean exchange = false;
	
}
