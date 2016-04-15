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
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.MediaStore.Images.Thumbnails;
import android.text.TextUtils;
import android.widget.ImageView;

public class Util {

	public static final int IMAGE_TYPE = 0x01;
	public static final int VIDEO_TYPE = 0x02;

	public static final int ALL_TYPE = IMAGE_TYPE & VIDEO_TYPE;

	private Context mContext;

	public Util(Context context) {
		this.mContext = context;
	}

	@SuppressWarnings("unchecked")
	public List<UzFileTraversal> LocalImgFileList(int filterType) {

		List<UzFileTraversal> data = new ArrayList<UzFileTraversal>();
		String filename = "";
		List<FileInfo> allimglist = listAlldirForFileList(filterType);

		List<String> retulist = new ArrayList<String>();
		if (allimglist != null) {
			@SuppressWarnings("rawtypes")
			Set set = new TreeSet();
			String[] str;

			for (int i = 0; i < allimglist.size(); i++) {
				
				FileInfo fileInfo = allimglist.get(i);
				if (fileInfo != null) {
					retulist.add(getfileinfo(fileInfo.path));
				}
			}

			for (int i = 0; i < retulist.size(); i++) {
				set.add(retulist.get(i));
			}

			str = (String[]) set.toArray(new String[0]);
			for (int i = 0; i < str.length; i++) {
				filename = str[i];
				UzFileTraversal ftl = new UzFileTraversal();
				ftl.filename = filename;
				data.add(ftl);
			}

			for (int i = 0; i < data.size(); i++) {
				for (int j = 0; j < allimglist.size(); j++) {
					FileInfo fileInfo = allimglist.get(j);
					if (fileInfo != null && data.get(i).filename.equals(getfileinfo(fileInfo.path))) {
						data.get(i).filecontent.add(fileInfo.path);
						data.get(i).fileInfos.add(fileInfo);
					}
				}
			}

		}
		return data;
	}

	public ArrayList<FileInfo> listAlldirForFileList(int filterType) {

		ArrayList<FileInfo> list = new ArrayList<FileInfo>();

		if ((filterType | IMAGE_TYPE) == IMAGE_TYPE) {
			Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
			Uri uri = intent.getData();

			String[] proj = { MediaStore.Images.Media.DATA, MediaStore.Images.Media.MIME_TYPE, MediaStore.Images.Media._ID };
			Cursor cursor = mContext.getContentResolver().query(uri, proj, null, null, null);
			if (cursor == null) {
				return list;
			}
			while (cursor.moveToNext()) {
				String path = cursor.getString(0);
				String mimeType = cursor.getString(1);
				int imgId = cursor.getInt(2);

				FileInfo fileInfo = new FileInfo();
				File tmpFile = new File(path);
				fileInfo.path = tmpFile.getAbsolutePath();
				fileInfo.mimeType = mimeType;
				fileInfo.size = tmpFile.length();
				fileInfo.imgId = imgId;
				fileInfo.time = tmpFile.lastModified();

				list.add(fileInfo);
			}
		}

		if ((filterType | VIDEO_TYPE) == VIDEO_TYPE) {
			ArrayList<FileInfo> videoList = listAllVideo();
			list.addAll(videoList);
		}

		return list;
	}

	@SuppressLint("UseSparseArrays")
	@SuppressWarnings("unused")
	public HashMap<Integer, ThumbnailInfo> listAllThumbnail() {

		HashMap<Integer, ThumbnailInfo> allThumbnailMap = new HashMap<Integer, ThumbnailInfo>();

		String[] projection = { Thumbnails._ID, Thumbnails.IMAGE_ID, Thumbnails.DATA };
		Cursor cur = mContext.getContentResolver().query(Thumbnails.EXTERNAL_CONTENT_URI, projection, null, null, null);
		if (cur == null) {
			return allThumbnailMap;
		}
		if (cur.moveToFirst()) {
			int _id;
			int image_id;
			String image_path;
			int _idColumn = cur.getColumnIndex(Thumbnails._ID);
			int image_idColumn = cur.getColumnIndex(Thumbnails.IMAGE_ID);
			int dataColumn = cur.getColumnIndex(Thumbnails.DATA);

			do {
				_id = cur.getInt(_idColumn);
				image_id = cur.getInt(image_idColumn);
				image_path = cur.getString(dataColumn);

				ThumbnailInfo thumbInfo = new ThumbnailInfo();
				thumbInfo.imageId = image_id;
				thumbInfo.imagePath = image_path;
				allThumbnailMap.put(image_id, thumbInfo);

			} while (cur.moveToNext());

		}

		return allThumbnailMap;

	}

	public static final String SDCARD_PATH = Environment.getExternalStorageDirectory().getAbsolutePath();

	public static final String THUMBNAIL_SAVE_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/DCIM/.thumbnails_for_me";

	private int fileCount = 0;

	public String createThumbPath(String orgPath) {

		Bitmap srcBitmap = BitmapFactory.decodeFile(orgPath);

		if (srcBitmap == null) {
			return null;
		}

		Bitmap createdBitmap = ThumbnailUtils.extractThumbnail(srcBitmap, 157, 157);

		String realPathStr = SDCARD_PATH + "/DCIM/.thumbnails_for_me";
		File realPath = new File(realPathStr);
		if (!realPath.exists()) {
			realPath.mkdirs();
		}

		File imagePath = new File(realPath, fileCount + ".jpg");

		/* replace the old ThunmbNail image */
		fileCount++;

		FileOutputStream outStream;
		try {
			outStream = new FileOutputStream(imagePath);
			createdBitmap.compress(Bitmap.CompressFormat.JPEG, 80, outStream);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		return imagePath.getAbsolutePath();
	}

	public ArrayList<FileInfo> listAlldir(int filterType) {

		ArrayList<FileInfo> list = new ArrayList<FileInfo>();

		if ((filterType | IMAGE_TYPE) == IMAGE_TYPE) {
			HashMap<Integer, ThumbnailInfo> allThumbnailInfo = listAllThumbnail();
			Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
			Uri uri = intent.getData();

			String[] proj = { MediaStore.Images.Media.DATA, MediaStore.Images.Media.MIME_TYPE, MediaStore.Images.Media.SIZE, MediaStore.Images.Media._ID, MediaStore.Images.Media.DATE_MODIFIED };
			Cursor cursor = mContext.getContentResolver().query(uri, proj, null, null, null);
			if (cursor == null) {
				return list;
			}

			while (cursor.moveToNext()) {

				String path = cursor.getString(0);
				String mimeType = cursor.getString(1);
				int size = cursor.getInt(2);
				int imgId = cursor.getInt(3);

				FileInfo info = new FileInfo();
				info.path = new File(path).getAbsolutePath();
				info.mimeType = mimeType;
				info.size = size;
				info.imgId = imgId;
				info.time = new File(path).lastModified();

				ThumbnailInfo tmpInfo = allThumbnailInfo.get(imgId);
				if (tmpInfo != null && !TextUtils.isEmpty(tmpInfo.imagePath)) {
					info.thumbImgPath = tmpInfo.imagePath;
				}

				list.add(info);
			}

		}

		/* scan the video file */
		if ((filterType | VIDEO_TYPE) == VIDEO_TYPE) {
			ArrayList<FileInfo> videoList = listAllVideo();
			list.addAll(videoList);
		}

		return list;
	}

	public ArrayList<FileInfo> listAllVideo() {

		ArrayList<FileInfo> list = new ArrayList<FileInfo>();

		String[] thumbColumns = new String[] { MediaStore.Video.Thumbnails.DATA, MediaStore.Video.Thumbnails.VIDEO_ID };

		ContentResolver testcr = mContext.getContentResolver();
		String[] projection = { MediaStore.Video.Media.DATA, MediaStore.Video.Media.MIME_TYPE, MediaStore.Video.Media.SIZE, MediaStore.Video.Media._ID };
		Cursor videoCur = testcr.query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, projection, null, null, null);
		if (videoCur == null) {
			return list;
		}
		while (videoCur.moveToNext()) {

			String path = videoCur.getString(0);
			String mimeType = videoCur.getString(1);
			int size = videoCur.getInt(2);
			int imgId = videoCur.getInt(3);

			FileInfo info = new FileInfo();
			info.path = new File(path).getAbsolutePath();
			info.mimeType = mimeType;
			info.size = size;
			info.imgId = imgId;
			info.time = new File(path).lastModified();

			int id = videoCur.getInt(videoCur.getColumnIndexOrThrow(MediaStore.Video.Media._ID));
			String selection = MediaStore.Video.Thumbnails.VIDEO_ID + "=?";
			String[] selectionArgs = new String[] { id + "" };

			Cursor thumbCursor = mContext.getContentResolver().query(MediaStore.Video.Thumbnails.EXTERNAL_CONTENT_URI, thumbColumns, selection, selectionArgs, null);
			if (thumbCursor.moveToFirst()) {
				info.thumbImgPath = thumbCursor.getString(videoCur.getColumnIndexOrThrow(MediaStore.Video.Thumbnails.DATA));
			}

			list.add(info);
		}
		return list;
	}

	public Bitmap getThumbnailBitmap(File imagePath) {
		if (imagePath == null) {
			return null;
		}
		return BitmapFactory.decodeFile(imagePath.getAbsolutePath());
	}

	public Bitmap getPathBitmap(Uri imageFilePath, int dw, int dh) throws FileNotFoundException {

		BitmapFactory.Options op = new BitmapFactory.Options();
		op.inJustDecodeBounds = true;
		Bitmap pic = BitmapFactory.decodeStream(mContext.getContentResolver().openInputStream(imageFilePath), null, op);

		int wRatio = (int) Math.ceil(op.outWidth / (float) dw);
		int hRatio = (int) Math.ceil(op.outHeight / (float) dh);

		if (wRatio > 1 && hRatio > 1) {
			if (wRatio > hRatio) {
				op.inSampleSize = wRatio;
			} else {
				op.inSampleSize = hRatio;
			}
		}
		op.inJustDecodeBounds = false;
		pic = BitmapFactory.decodeStream(mContext.getContentResolver().openInputStream(imageFilePath), null, op);
		return pic;
	}

	public String getfileinfo(String data) {
		String filename[] = data.split("/");
		if (filename != null) {
			return filename[filename.length - 2];
		}
		return null;
	}

	public void imgExcute(ImageView imageView, UzImgCallBack icb, FileInfo... params) {
		LoadBitmapAsync loadBitAsynk = new LoadBitmapAsync(imageView, icb);
		loadBitAsynk.execute(params);
	}

	public class LoadBitmapAsync extends AsyncTask<FileInfo, Integer, Bitmap> {

		ImageView imageView;
		UzImgCallBack icb;

		LoadBitmapAsync(ImageView imageView, UzImgCallBack icb) {
			this.imageView = imageView;
			this.icb = icb;
		}

		@Override
		protected Bitmap doInBackground(FileInfo... params) {
			Bitmap bitmap = null;
			try {
				if (params != null) {
					for (int i = 0; i < params.length; i++) {

						if (!TextUtils.isEmpty(params[i].mimeType) && params[i].mimeType.startsWith("image")) {

							if (!TextUtils.isEmpty(params[i].path)) {

								String realPathStr = SDCARD_PATH + "/DCIM/.thumbnails_for_me";
								File realPath = new File(realPathStr);
								File imagePath = new File(realPath, Util.stringToMD5(params[i].path) + ".jpg");

								int degree = BitmapToolkit.readPictureDegree(params[i].path);

								if (imagePath.exists()) {
									bitmap = getThumbnailBitmap(imagePath);
								} else {
									bitmap = getPathBitmap(Uri.fromFile(new File(params[i].path)), 200, 200);
									bitmap = BitmapToolkit.rotaingImageView(degree, bitmap);
									saveBitmap(params[i].path, bitmap);
								}
							}

						}
						if (!TextUtils.isEmpty(params[i].mimeType) && params[i].mimeType.startsWith("video")) {
							if (!TextUtils.isEmpty(params[i].thumbImgPath) && new File(params[i].thumbImgPath).exists()) {
								bitmap = getPathBitmap(Uri.fromFile(new File(params[i].thumbImgPath)), 200, 200);
							} else {
								String realPathStr = SDCARD_PATH + "/DCIM/.thumbnails_for_me";
								File realPath = new File(realPathStr);
								File imagePath = new File(realPath, Util.stringToMD5(params[i].path) + ".jpg");

								if (imagePath.exists()) {
									bitmap = getPathBitmap(Uri.fromFile(imagePath), 200, 200);
								} else {
									bitmap = createVideoThumbnail(params[i].path);
									saveBitmap(params[i].path, bitmap);
								}
							}
						}
					}
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			return bitmap;
		}

		@Override
		protected void onPostExecute(Bitmap result) {
			super.onPostExecute(result);
			if (result != null) {
				icb.resultImgCall(imageView, result);
			}
		}
	}

	public static Bitmap createVideoThumbnail(String videoPath) {

		Bitmap srcBitmap = ThumbnailUtils.createVideoThumbnail(videoPath, MediaStore.Images.Thumbnails.MICRO_KIND);

		if (srcBitmap == null) {
			return null;
		}

		srcBitmap = ThumbnailUtils.extractThumbnail(srcBitmap, 200, 200, ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
		return srcBitmap;
	}

	public static void saveBitmap(String savePath, Bitmap bitmap) {

		if (TextUtils.isEmpty(savePath) || bitmap == null) {
			return;
		}

		String realPathStr = SDCARD_PATH + "/DCIM/.thumbnails_for_me";
		File realPath = new File(realPathStr);
		if (!realPath.exists()) {
			realPath.mkdirs();
		}

		File imagePath = new File(realPath, Util.stringToMD5(savePath) + ".jpg");
		FileOutputStream outStream = null;
		try {
			outStream = new FileOutputStream(imagePath);
			bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outStream);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		if (outStream != null) {
			try {
				outStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 将字符串转成MD5值
	 * 
	 * @param string
	 * @return
	 */
	public static String stringToMD5(String string) {

		if (TextUtils.isEmpty(string)) {
			return null;
		}

		byte[] hash;

		try {
			hash = MessageDigest.getInstance("MD5").digest(string.getBytes("UTF-8"));
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			return null;
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return null;
		}

		StringBuilder hex = new StringBuilder(hash.length * 2);
		for (byte b : hash) {
			if ((b & 0xFF) < 0x10)
				hex.append("0");
			hex.append(Integer.toHexString(b & 0xFF));
		}
		return hex.toString();
	}

}
