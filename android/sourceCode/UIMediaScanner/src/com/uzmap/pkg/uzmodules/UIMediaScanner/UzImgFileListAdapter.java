/**
 * APICloud Modules
 * Copyright (c) 2014-2015 by APICloud, Inc. All Rights Reserved.
 * Licensed under the terms of the The MIT License (MIT).
 * Please see the license.html included with this distribution for details.
 */
package com.uzmap.pkg.uzmodules.UIMediaScanner;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.uzmap.pkg.uzcore.UZResourcesIDFinder;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore.Images;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class UzImgFileListAdapter extends BaseAdapter {

	private Context mContext;
	private String mFilecount = "filecount";
	private String mFilename = "filename";
	private String mImgpath = "imgpath";
	private List<HashMap<String, String>> mListdata;
	
	private Bitmap[] mBitmaps;
	private int index = -1;
	private List<View> mHolderlist;

	public UzImgFileListAdapter(Context context, List<HashMap<String, String>> listdata) {
		this.mContext = context;
		this.mListdata = listdata;
		mBitmaps = new Bitmap[listdata.size()];
		mHolderlist = new ArrayList<View>();
	}

	@Override
	public int getCount() {
		return mListdata.size();
	}

	@Override
	public Object getItem(int arg0) {
		return mListdata.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		return arg0;
	}

	@Override
	public View getView(final int arg0, View arg1, ViewGroup arg2) {
		Holder holder;
		if (arg0 != index && arg0 > index) {
			holder = new Holder();
			arg1 = LayoutInflater.from(mContext).inflate(UZResourcesIDFinder.getResLayoutID("uz_media_scanner_imgfileadapter"), null);
			holder.photo_imgview = (ImageView) arg1.findViewById(UZResourcesIDFinder.getResIdID("filephoto_imgview"));
			holder.filecount_textview = (TextView) arg1.findViewById(UZResourcesIDFinder.getResIdID("filecount_textview"));
			holder.filename_textView = (TextView) arg1.findViewById(UZResourcesIDFinder.getResIdID("filename_textview"));
			arg1.setTag(holder);
			mHolderlist.add(arg1);
		} else {
			holder = (Holder) mHolderlist.get(arg0).getTag();
			arg1 = mHolderlist.get(arg0);
		}

		holder.filename_textView.setText(mListdata.get(arg0).get(mFilename));
		holder.filecount_textview.setText(mListdata.get(arg0).get(mFilecount));

		if (mBitmaps[arg0] == null) {
			imgExcute(holder.photo_imgview, mListdata.get(arg0).get(mImgpath));
		} else {
			holder.photo_imgview.setImageBitmap(mBitmaps[arg0]);
		}

		return arg1;
	}

	class Holder {
		public ImageView photo_imgview;
		public TextView filecount_textview;
		public TextView filename_textView;
	}

	public void imgExcute(ImageView imageView, String... params) {
		LoadBitAsynk loadBitAsynk = new LoadBitAsynk(imageView);
		loadBitAsynk.execute(params);
	}

	public class LoadBitAsynk extends AsyncTask<String, Integer, Bitmap> {

		ImageView imageView;

		LoadBitAsynk(ImageView imageView) {
			this.imageView = imageView;
		}

		@Override
		protected Bitmap doInBackground(String... params) {
			Bitmap bitmap = null;
			try {
				if (params != null) {
					for (int i = 0; i < params.length; i++) {
						if(params[i].endsWith("mp4") || params[i].endsWith("3gp")){
							bitmap = ThumbnailUtils.createVideoThumbnail(params[i], Images.Thumbnails.MICRO_KIND);
						} else {
							bitmap = getPathBitmap(Uri.fromFile(new File(params[i])), 200, 200);
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
				imageView.setImageBitmap(result);
			}
		}
	}

	public Bitmap getPathBitmap(Uri imageFilePath, int dw, int dh) throws FileNotFoundException {

		Bitmap pic = BitmapFactory.decodeStream(mContext.getContentResolver().openInputStream(imageFilePath));
//
//		int wRatio = (int) Math.ceil(op.outWidth / (float) dw);
//		int hRatio = (int) Math.ceil(op.outHeight / (float) dh);
//
//		if (wRatio > 1 && hRatio > 1) {
//			if (wRatio > hRatio) {
//				op.inSampleSize = wRatio;
//			} else {
//				op.inSampleSize = hRatio;
//			}
//		}
//		op.inJustDecodeBounds = false;
//		pic = BitmapFactory.decodeStream(mContext.getContentResolver().openInputStream(imageFilePath), null, op);

		pic = ThumbnailUtils.extractThumbnail(pic, dw, dh);
		
		return pic;
	}
	
}
