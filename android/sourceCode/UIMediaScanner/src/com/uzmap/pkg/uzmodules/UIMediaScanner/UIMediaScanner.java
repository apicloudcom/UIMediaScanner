/**
 * APICloud Modules
 * Copyright (c) 2014-2015 by APICloud, Inc. All Rights Reserved.
 * Licensed under the terms of the The MIT License (MIT).
 * Please see the license.html included with this distribution for details.
 */
package com.uzmap.pkg.uzmodules.UIMediaScanner;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;

import android.view.WindowManager;

import com.uzmap.pkg.uzcore.UZCoreUtil;
import com.uzmap.pkg.uzcore.UZWebView;
import com.uzmap.pkg.uzcore.annotation.UzJavascriptMethod;
import com.uzmap.pkg.uzcore.uzmodule.UZModule;
import com.uzmap.pkg.uzcore.uzmodule.UZModuleContext;
import com.uzmap.pkg.uzkit.UZUtility;

public class UIMediaScanner extends UZModule {

	public static final int REQUEST_CODE = 0x100;
	public static final int RESULT_CODE = 0x101;

	public static final String CONFIG_TAG = "configData";

	public static final String TAG = UIMediaScanner.class.getSimpleName();

	private UZModuleContext mMContext;

	public UIMediaScanner(UZWebView webView) {
		super(webView);
		CACHE_PATH = mContext.getCacheDir().getAbsolutePath();
	}

	@UzJavascriptMethod
	@SuppressWarnings("deprecation")
	public void jsmethod_open(UZModuleContext moduleContext) {

		mMContext = moduleContext;

		final ConfigInfo config = new ConfigInfo();
		if (!moduleContext.isNull("column")) {
			config.col = moduleContext.optInt("column");
			if (config.col == 0) {
				config.col = 4;
			}
		}

		if (!moduleContext.isNull("max")) {
			config.selectedMax = moduleContext.optInt("max");
		}

		if (!moduleContext.isNull("classify")) {
			config.classify = moduleContext.optBoolean("classify");
		}

		if (!moduleContext.isNull("type")) {
			config.filterType = moduleContext.optString("type");
		}

		if (!moduleContext.isNull("rotation")) {
			config.rotation = moduleContext.optBoolean("rotation");
		}

		/**
		 * screen width
		 */
		WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
		int width = wm.getDefaultDisplay().getWidth();

		config.mark_size = UZCoreUtil.pixToDip(width / config.col / 3);

		if (!moduleContext.isNull("bounces")) {
			config.isBounces = moduleContext.optBoolean("bounces");
		}

		JSONObject scrollToBottomObj = moduleContext.optJSONObject("scrollToBottom");
		if (scrollToBottomObj != null && !scrollToBottomObj.isNull("intervalTime")) {
			config.intervalTime = scrollToBottomObj.optInt("intervalTime");
		}

		if (!moduleContext.isNull("exchange")) {
			config.exchange = moduleContext.optBoolean("exchange");
		}

		JSONObject sortObj = moduleContext.optJSONObject("sort");
		if (sortObj != null) {
			if (!sortObj.isNull("key")) {
				config.key = sortObj.optString("key");
			}
			if (!sortObj.isNull("order")) {
				config.order = sortObj.optString("order");
			}
		}

		JSONObject dataObj = moduleContext.optJSONObject("texts");
		if (dataObj != null) {
			if (!dataObj.isNull("stateText")) {
				config.navi_title = dataObj.optString("stateText");
			}
			if (!dataObj.isNull("cancelText")) {
				config.cancel_title = dataObj.optString("cancelText");
			}
			if (!dataObj.isNull("finishText")) {
				config.finish_title = dataObj.optString("finishText");
			}
		}

		JSONObject stylesObj = moduleContext.optJSONObject("styles");
		if (stylesObj != null) {
			// bg
			if (!stylesObj.isNull("bg")) {
				config.bgColor = UZUtility.parseCssColor(stylesObj.optString("bg"));
			}
			// mark
			JSONObject markObj = stylesObj.optJSONObject("mark");
			if (markObj != null) {
				if (!markObj.isNull("position")) {
					config.mark_position = markObj.optString("position");
				}
				if (!markObj.isNull("icon")) {
					config.mark_icon = makeRealPath(markObj.optString("icon"));
				}
				if (!markObj.isNull("size")) {
					config.mark_size = markObj.optInt("size");
				}
			}
			// nav
			JSONObject navObj = stylesObj.optJSONObject("nav");
			if (navObj != null) {
				if (!navObj.isNull("bg")) {
					Bitmap naviBgBitmap = getBitmap(makeRealPath(navObj.optString("bg")));
					if (naviBgBitmap != null) {
						ConfigInfo.navBgBitmap = naviBgBitmap;
					} else {
						config.navi_bg = UZUtility.parseCssColor(navObj.optString("bg"));
					}
				}

				if (!navObj.isNull("stateColor")) {
					config.navi_title_color = UZUtility.parseCssColor(navObj.optString("stateColor"));
				}

				if (!navObj.isNull("stateSize")) {
					config.navi_title_size = navObj.optInt("stateSize");
				}

				if (!navObj.isNull("cancleBg")) {
					Bitmap cancelBgBitmap = getBitmap(makeRealPath(navObj.optString("cancleBg")));
					if (cancelBgBitmap != null) {
						ConfigInfo.cancelBgBitmap = getBitmap(makeRealPath(navObj.optString("cancleBg")));
					} else {
						config.cancel_bg = UZUtility.parseCssColor(navObj.optString("cancleBg"));
					}
				}

				if (!navObj.isNull("cancelColor")) {
					config.cancel_title_color = UZUtility.parseCssColor(navObj.optString("cancelColor"));
				}

				if (!navObj.isNull("cancelSize")) {
					config.cancel_title_size = navObj.optInt("cancelSize");
				}

				// finish button setting
				if (!navObj.isNull("finishBg")) {
					Bitmap finishBgBitmap = getBitmap(makeRealPath(navObj.optString("finishBg")));
					if (finishBgBitmap != null) {
						ConfigInfo.finishBgBitmap = finishBgBitmap;
					} else {
						config.finish_bg = UZUtility.parseCssColor(navObj.optString("finishBg"));
					}
				}

				if (!navObj.isNull("finishColor")) {
					config.finish_title_color = UZUtility.parseCssColor(navObj.optString("finishColor"));
				}

				if (!navObj.isNull("finishSize")) {
					config.finish_title_size = navObj.optInt("finishSize");
				}
			}
		}

		Intent intent = new Intent();
		if (config.classify) {
			intent.setClass(getContext(), UzImgFileListActivity.class);
		} else {
			intent.setClass(getContext(), UzImgsActivity.class);
		}
		intent.putExtra(CONFIG_TAG, config);
		startActivityForResult(intent, REQUEST_CODE);
	}

	private ArrayList<FileInfo> allScanFileList;
	private int startIndex = -1;
	private int fetchCount = -1;
	
	public int thumbWidth = 100;
	public int thumbHeight = 100;

	@UzJavascriptMethod
	public void jsmethod_scan(final UZModuleContext moduleConztext) {
		
		JSONObject thumbnailObj = moduleConztext.optJSONObject("thumbnail");
		if(thumbnailObj != null){
			if(!thumbnailObj.isNull("w")){
				thumbWidth = thumbnailObj.optInt("w");
			}
			if(!thumbnailObj.isNull("h")){
				thumbHeight = thumbnailObj.optInt("h");
			}
		}
		
		new Thread(new Runnable() {
			@Override
			public void run() {

				String type = "all";
				if (!moduleConztext.isNull("type")) {
					type = moduleConztext.optString("type");
				}

				// type
				if (allScanFileList == null) {
					Util util = new Util(getContext());
					if ("all".equals(type)) {
						allScanFileList = util.listAlldir(Util.ALL_TYPE);
					}
					if ("picture".equals(type)) {
						allScanFileList = util.listAlldir(Util.IMAGE_TYPE);
					}
					if ("video".equals(type)) {
						allScanFileList = util.listAllVideo();
					}
				}

				// sort
				JSONObject sortObj = moduleConztext.optJSONObject("sort");

				String key = "time";
				String order = "desc";

				if (sortObj != null) {
					if (!sortObj.isNull("key")) {
						key = sortObj.optString("key");
					}
					if (!sortObj.isNull("order")) {
						order = sortObj.optString("order");
					}
				}

				if ("size".equals(key)) {
					FileInfo.SORT_FLAG = FileInfo.SORT_BY_SIZE;
				} else {
					FileInfo.SORT_FLAG = FileInfo.SORT_BY_TIME;
				}

				if ("desc".equals(order)) {
					FileInfo.SORT_LAW = FileInfo.SORT_BY_DESC;
				} else {
					FileInfo.SORT_LAW = FileInfo.SORT_BY_ASC;
				}

				if (FileInfo.SORT_BY_TIME == FileInfo.SORT_FLAG) {
					if (FileInfo.SORT_LAW == FileInfo.SORT_BY_ASC) {
						SortUtils.ascSortByTime(allScanFileList);
					}
					if (FileInfo.SORT_LAW == FileInfo.SORT_BY_DESC) {
						SortUtils.dascSortByTime(allScanFileList);
					}
				}

				if (FileInfo.SORT_BY_SIZE == FileInfo.SORT_FLAG) {
					if (FileInfo.SORT_LAW == FileInfo.SORT_BY_ASC) {
						SortUtils.ascSortBySize(allScanFileList);
					}

					if (FileInfo.SORT_LAW == FileInfo.SORT_BY_DESC) {
						SortUtils.dascSortBySize(allScanFileList);
					}
				}

				// count
				int count = -1;
				if (!moduleConztext.isNull("count")) {
					count = moduleConztext.optInt("count");
					fetchCount = count;
				}

				if (allScanFileList.size() <= 0) {
					return;
				}

				List<FileInfo> subList;
				if (count >= allScanFileList.size() || count < 0) {
					subList = allScanFileList.subList(0, allScanFileList.size());
					startIndex = -1;
				} else {
					subList = allScanFileList.subList(0, count);
					startIndex = count;
				}
				moduleConztext.success(creatRetJSON(subList, true, thumbWidth, thumbHeight), true);
				
			}
		}).start();
	}

	public void jsmethod_transPath(UZModuleContext uzContext) {
		if (!uzContext.isNull("path")) {
			String path = uzContext.optString("path");
			JSONObject ret = new JSONObject();
			try {
				ret.put("path", path);
				uzContext.success(ret, false);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}

	public void jsmethod_fetch(final UZModuleContext moduleConztext) {

		new Thread(new Runnable() {
			@Override
			public void run() {

				List<FileInfo> subListFileInfo = new ArrayList<FileInfo>();
				if (startIndex < 0 || fetchCount < 0 || allScanFileList == null) {
					moduleConztext.success(creatRetJSON(subListFileInfo, false), true);
					return;
				}

				if (startIndex == allScanFileList.size()) {
					moduleConztext.success(creatRetJSON(subListFileInfo, false), true);
					return;
				}

				if (startIndex + fetchCount >= allScanFileList.size()) {
					subListFileInfo = allScanFileList.subList(startIndex, allScanFileList.size());
					startIndex = allScanFileList.size();
				} else {
					subListFileInfo = allScanFileList.subList(startIndex, startIndex + fetchCount);
					startIndex += fetchCount;
				}

				if (subListFileInfo == null) {
					moduleConztext.success(creatRetJSON(subListFileInfo, false), true);
					return;
				}

				moduleConztext.success(creatRetJSON(subListFileInfo, false, thumbWidth, thumbHeight), true);
			}
		}).start();

	}

	@SuppressLint("SimpleDateFormat")
	private SimpleDateFormat dataFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (data == null) {
			return;
		}
		@SuppressWarnings("unchecked")
		ArrayList<FileInfo> filelist = (ArrayList<FileInfo>) data.getSerializableExtra("files");

		if (filelist != null) {
			mMContext.success(creatRetJSON(filelist, false), true);
		}
	}

	// create thumbnail image
	// public static final String SDCARD_PATH = Environment.getExternalStorageDirectory().getAbsolutePath();
	// public static final String THUMBNAIL_SAVE_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/DCIM/thumbnails_for_me";

	public static String CACHE_PATH;
	
	public String createThumbPath(String orgPath, int width, int height) {

		int degree = BitmapToolkit.readPictureDegree(orgPath);
		
// 		Bitmap srcBitmap = BitmapFactory.decodeFile(orgPath);
//		if (srcBitmap == null) {
//			return null;
//		}
		
		Bitmap createdBitmap = Util.decodeSampledBitmapFromFile(orgPath, width, height);// ThumbnailUtils.extractThumbnail(srcBitmap, width, height);
		if (degree != 0) {
			createdBitmap = BitmapToolkit.rotaingImageView(degree, createdBitmap);
		}

		String realPathStr = CACHE_PATH + "/thumbnails_for_me";
		File realPath = new File(realPathStr);
		if (!realPath.exists()) {
			realPath.mkdirs();
		}

		File imagePath = new File(realPath, Util.stringToMD5(orgPath) + ".jpg");

		/* replace the old ThunmbNail image */
		FileOutputStream outStream;
		try {
			outStream = new FileOutputStream(imagePath);
			createdBitmap.compress(Bitmap.CompressFormat.JPEG, 80, outStream);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return imagePath.getAbsolutePath();
	}
	
	public JSONObject creatRetJSON(List<FileInfo> list, boolean isScan) {
		return creatRetJSON(list, isScan, 157, 157);
	}

	public JSONObject creatRetJSON(List<FileInfo> list, boolean isScan, int thumbNailWidth, int thumbNailHeight) {

		JSONObject retJSON = new JSONObject();
		JSONArray array = new JSONArray();
		
		try {
			for (int i = 0; i < list.size(); i++) {
				JSONObject obj = new JSONObject();
				obj.put("path", list.get(i).path);

				if (!TextUtils.isEmpty(list.get(i).thumbImgPath) && new File(list.get(i).thumbImgPath).exists()) {
					obj.put("thumbPath", list.get(i).thumbImgPath);
				} else {

					String realPathStr = CACHE_PATH + "/thumbnails_for_me";
					File realPath = new File(realPathStr);
					File imagePath = new File(realPath, Util.stringToMD5(list.get(i).path) + ".jpg");

					if (imagePath.exists()) {
						obj.put("thumbPath", imagePath.getAbsolutePath());
					} else {
						String tmpPath = createThumbPath(list.get(i).path, thumbNailWidth, thumbNailHeight);
						if (!TextUtils.isEmpty(tmpPath)) {
							obj.put("thumbPath", tmpPath);
						}
					}
				}

				String mimeType = list.get(i).mimeType;

				String suffix = null;
				if (mimeType != null && mimeType.startsWith("image")) {
					suffix = mimeType.replace("image/", "");
				}
				if (mimeType != null && mimeType.startsWith("video")) {
					suffix = mimeType.replace("video/", "");

					if (TextUtils.isEmpty(list.get(i).thumbImgPath) || !new File(list.get(i).thumbImgPath).exists()) {

						String realPathStr = CACHE_PATH + "/thumbnails_for_me";
						File realPath = new File(realPathStr);
						if (!realPath.exists()) {
							realPath.mkdirs();
						}
						File imagePath = new File(realPath, Util.stringToMD5(list.get(i).path) + ".jpg");

						if (imagePath.exists()) {
							obj.put("thumbPath", imagePath);
						} else {

							Bitmap videoThumb = Util.createVideoThumbnail(list.get(i).path);
							Util.saveBitmap(list.get(i).path, videoThumb);

							obj.put("thumbPath", imagePath);
						}

					}
				}

				if (!TextUtils.isEmpty(suffix) && suffix.endsWith("jpeg")) {
					suffix = "jpg";
				}

				obj.put("suffix", suffix);
				obj.put("size", list.get(i).size);

				String timeStr = dataFormat.format(new Date(list.get(i).time));
				obj.put("time", timeStr);
				if (list.get(i).size > 0) {
					array.put(obj);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			if (isScan && allScanFileList != null) {
				retJSON.put("total", allScanFileList.size());
			}
			retJSON.put("list", array);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return retJSON;
	}

	public static Bitmap getBitmap(String path) {
		InputStream input = null;
		Bitmap mBitmap = null;
		if (!TextUtils.isEmpty(path)) {
			String iconPath = path;
			try {
				input = UZUtility.guessInputStream(iconPath);
				mBitmap = BitmapFactory.decodeStream(input);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if (input != null) {
			try {
				input.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return mBitmap;
	}

	@Override
	protected void onClean() {
		super.onClean();
		
		ConfigInfo.cancelBgBitmap = null;
		ConfigInfo.finishBgBitmap = null;
		ConfigInfo.navBgBitmap = null;
	}

	@SuppressWarnings("unused")
	private void removeTmpThumbnail(String path) {
		if (TextUtils.isEmpty(path)) {
			return;
		}

		File tmpFile = new File(path);
		if (!tmpFile.exists()) {
			return;
		}

		final int size = tmpFile.listFiles().length;
		final File[] fileList = tmpFile.listFiles();
		for (int i = 0; i < size; i++) {
			fileList[i].delete();
		}
	}

}
