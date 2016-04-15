/**
 * APICloud Modules
 * Copyright (c) 2014-2015 by APICloud, Inc. All Rights Reserved.
 * Licensed under the terms of the The MIT License (MIT).
 * Please see the license.html included with this distribution for details.
 */
package com.uzmap.pkg.uzmodules.UIMediaScanner;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.uzmap.pkg.uzcore.UZResourcesIDFinder;

public class UzImgsActivity extends BaseActivity {

	private Bundle mBundle;
	private GridView mImgGridView;
	private UzImgsAdapter mImgsAdapter;
	private RelativeLayout mLayout;
	private HashMap<Integer, ImageView> mHashImages;
	private ArrayList<FileInfo> mFilelist;
	private Util mUtil;
	private ArrayList<FileInfo> mAllImgList;
	private ConfigInfo mConfig;
	private String mReplaceTxt;
	private int mScreenWidth;
	private Bitmap mSelectedIconBmp;
	
	private UzImgCallBack imgCallBack = new UzImgCallBack() {
		public void resultImgCall(ImageView imageView, Bitmap bitmap) {
			imageView.setImageBitmap(bitmap);
		}
	};

	UzImgsAdapter.OnItemClickClass onItemClickClass = new UzImgsAdapter.OnItemClickClass() {
		public void OnItemClick(View v, int Position, CheckBox checkBox, ImageView selectedImage) {
			FileInfo fileInfo = (FileInfo) UzImgsActivity.this.mAllImgList.get(Position);
			if (fileInfo.isChecked)
				fileInfo.isChecked = false;
			else {
				fileInfo.isChecked = true;
			}

			if (checkBox.isChecked()) {
				checkBox.setChecked(false);
				UzImgsActivity.this.mFilelist.remove(fileInfo);
			} else {
				try {
					checkBox.setChecked(true);
					ImageView imageView = UzImgsActivity.this.iconImage(fileInfo, Position, checkBox);
					if (imageView != null) {
						UzImgsActivity.this.mHashImages.put(Integer.valueOf(Position), imageView);
						UzImgsActivity.this.mFilelist.add(fileInfo);
					}
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
			}

			if ((UzImgsActivity.this.mImgsAdapter != null) && (UzImgsActivity.this.mSelectedIconBmp != null)) {
				UzImgsActivity.this.mImgsAdapter.notifyDataSetChanged();
			}

			if (!TextUtils.isEmpty(UzImgsActivity.this.mReplaceTxt)) {
				TextView naviTitle = (TextView) UzImgsActivity.this.findViewById(UZResourcesIDFinder.getResIdID("navi_title"));
				naviTitle.setText(UzImgsActivity.this.mReplaceTxt.replace("*", UzImgsActivity.this.mFilelist.size() + ""));

				if (UzImgsActivity.this.mFilelist.size() == 0)
					naviTitle.setText(UzImgsActivity.this.mReplaceTxt.replace("*", "*"));
			}
		}
	};

	@SuppressWarnings("deprecation")
	@SuppressLint({ "UseSparseArrays" })
	protected void onCreate(Bundle savedInstanceState) {
		
		setFullScreen();

		mBundle = getIntent().getExtras();
		boolean hasData = mBundle.getBoolean(UzImgFileListActivity.TRANS_TAG);
		this.mConfig = ((ConfigInfo) getIntent().getSerializableExtra("configData"));
		
		
		if (hasData && UzImgFileListActivity.fileTraversal != null) {
			this.mAllImgList = UzImgFileListActivity.fileTraversal.fileInfos;
		} else {
			
			this.mUtil = new Util(this);
			if(ConfigInfo.FILTER_ALL.equals(mConfig.filterType)){
				this.mAllImgList = this.mUtil.listAlldir(Util.ALL_TYPE);
			}
			if(ConfigInfo.FILTER_PICTURE.equals(mConfig.filterType)){
				this.mAllImgList = this.mUtil.listAlldir(Util.IMAGE_TYPE);
			}
			if(ConfigInfo.FILTER_VIDEO.equals(mConfig.filterType)){
				this.mAllImgList = this.mUtil.listAlldir(Util.VIDEO_TYPE);
			}
			
		}

		if (this.mConfig != null) {
			if ("time".equals(this.mConfig.key))
				FileInfo.SORT_FLAG = FileInfo.SORT_BY_TIME;
			else if ("size".equals(this.mConfig.key)) {
				FileInfo.SORT_FLAG = FileInfo.SORT_BY_SIZE;
			}

			if ("asc".equals(this.mConfig.order))
				FileInfo.SORT_LAW = FileInfo.SORT_BY_ASC;
			else if ("desc".equals(this.mConfig.order)) {
				FileInfo.SORT_LAW = FileInfo.SORT_BY_DESC;
			}

			if (FileInfo.SORT_BY_TIME == FileInfo.SORT_FLAG) {
				if (FileInfo.SORT_LAW == FileInfo.SORT_BY_ASC) {
					SortUtils.ascSortByTime(this.mAllImgList);
				}

				if (FileInfo.SORT_LAW == FileInfo.SORT_BY_DESC) {
					SortUtils.dascSortByTime(this.mAllImgList);
				}
			}

			if (FileInfo.SORT_BY_SIZE == FileInfo.SORT_FLAG) {
				if (FileInfo.SORT_LAW == FileInfo.SORT_BY_ASC) {
					SortUtils.ascSortBySize(this.mAllImgList);
				}

				if (FileInfo.SORT_LAW == FileInfo.SORT_BY_DESC) {
					SortUtils.dascSortBySize(this.mAllImgList);
				}
			}
		}

		this.mSelectedIconBmp = UIMediaScanner.getBitmap(this.mConfig.mark_icon);

		if (this.mSelectedIconBmp == null) {
			int media_scanner_item_select_icon = UZResourcesIDFinder.getResDrawableID("mediascanner_item_select_icon");
			this.mSelectedIconBmp = BitmapFactory.decodeResource(getResources(), media_scanner_item_select_icon);
		}

		this.mScreenWidth = getWindowManager().getDefaultDisplay().getWidth();
		setContentView(UZResourcesIDFinder.getResLayoutID("uz_media_scanner_photogrally"));
		this.mImgGridView = ((GridView) findViewById(UZResourcesIDFinder.getResIdID("gridView1")));

		this.mBundle = getIntent().getExtras();

		this.mImgsAdapter = new UzImgsAdapter(this, this.mAllImgList, this.onItemClickClass);
		this.mImgsAdapter.setScreenWidth(this.mScreenWidth);

		this.mImgsAdapter.setRow(3);
		this.mImgGridView.setAdapter(this.mImgsAdapter);
		mImgGridView.setSelector(new ColorDrawable(0x00000000));

		this.mImgGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {

				Holder holder = (Holder) ((View) UzImgsActivity.this.mImgsAdapter.getHolderlist().get(arg2)).getTag();

				FileInfo fileInfo = (FileInfo) UzImgsActivity.this.mAllImgList.get(arg2);
				if (fileInfo.isChecked)
					fileInfo.isChecked = false;
				else {
					if (mFilelist.size() < mConfig.selectedMax) {
						fileInfo.isChecked = true;
					}
				}

				if (holder.itemCheckBox.isChecked()) {
					holder.itemCheckBox.setChecked(false);
					UzImgsActivity.this.mFilelist.remove(fileInfo);
				} else {
					try {
						if (mFilelist.size() < mConfig.selectedMax) {
							holder.itemCheckBox.setChecked(true);
							ImageView imageView = UzImgsActivity.this.iconImage(fileInfo, arg2, holder.itemCheckBox);
							if (imageView != null) {

								UzImgsActivity.this.mHashImages.put(Integer.valueOf(arg2), imageView);
								UzImgsActivity.this.mFilelist.add(fileInfo);

							}
						} else {
							Toast.makeText(UzImgsActivity.this, "你最多只能选择" + mFilelist.size() + "个资源", Toast.LENGTH_LONG).show();
							return;
						}
					} catch (FileNotFoundException e) {
						e.printStackTrace();
					}
				}

				if ((UzImgsActivity.this.mImgsAdapter != null) && (UzImgsActivity.this.mSelectedIconBmp != null)) {
					UzImgsActivity.this.mImgsAdapter.notifyDataSetChanged();
				}

				if (!TextUtils.isEmpty(UzImgsActivity.this.mReplaceTxt)) {
					TextView naviTitle = (TextView) UzImgsActivity.this.findViewById(UZResourcesIDFinder.getResIdID("navi_title"));
					naviTitle.setText(UzImgsActivity.this.mReplaceTxt.replace("*", UzImgsActivity.this.mFilelist.size() + ""));

					if (UzImgsActivity.this.mFilelist.size() == 0)
						naviTitle.setText(UzImgsActivity.this.mReplaceTxt.replace("*", "*"));
				}
			}
		});
		this.mImgsAdapter.setBitmap(this.mSelectedIconBmp);

		this.mLayout = ((RelativeLayout) findViewById(UZResourcesIDFinder.getResIdID("relativeLayout2")));
		this.mHashImages = new HashMap<Integer, ImageView>();
		this.mFilelist = new ArrayList<FileInfo>();
		this.mUtil = new Util(this);

		if (this.mConfig != null) {
			this.mImgGridView.setNumColumns(this.mConfig.col);
			this.mImgGridView.setBackgroundColor(this.mConfig.bgColor);

			this.mImgsAdapter.setRow(this.mConfig.col);
			this.mImgsAdapter.setMarkPosition(this.mConfig.mark_position);
			this.mImgsAdapter.notifyDataSetChanged();

			this.mImgsAdapter.setMarkSize(this.mConfig.mark_size);

			findViewById(UZResourcesIDFinder.getResIdID("relativeLayout1")).setBackgroundColor(this.mConfig.navi_bg);
			TextView naviTitle = (TextView) findViewById(UZResourcesIDFinder.getResIdID("navi_title"));

			naviTitle.setText(this.mConfig.navi_title);
			naviTitle.setTextSize(this.mConfig.navi_title_size);
			naviTitle.setTextColor(this.mConfig.navi_title_color);
			if (ConfigInfo.navBgBitmap != null) {
				findViewById(UZResourcesIDFinder.getResIdID("relativeLayout1")).setBackgroundDrawable(new BitmapDrawable(ConfigInfo.navBgBitmap));
			}

			////////// cancel text //////////////
			TextView cancelTxt = (TextView) findViewById(UZResourcesIDFinder.getResIdID("button1"));
			if (ConfigInfo.cancelBgBitmap != null)
				cancelTxt.setBackgroundDrawable(new BitmapDrawable(ConfigInfo.cancelBgBitmap));
			else {
				cancelTxt.setBackgroundColor(this.mConfig.finish_bg);
			}

			cancelTxt.setTextColor(this.mConfig.finish_title_color);
			cancelTxt.setText(this.mConfig.finish_title);
			cancelTxt.setTextSize(this.mConfig.finish_title_size);
			

			
			//////////// finish text //////////////
			TextView finishTxt = (TextView) findViewById(UZResourcesIDFinder.getResIdID("button2"));
			if (ConfigInfo.finishBgBitmap != null)
				finishTxt.setBackgroundDrawable(new BitmapDrawable(ConfigInfo.finishBgBitmap));
			else {
				finishTxt.setBackgroundColor(this.mConfig.cancel_bg);
			}
			finishTxt.setTextColor(this.mConfig.cancel_title_color);
			finishTxt.setText(this.mConfig.cancel_title);
			finishTxt.setTextSize(this.mConfig.cancel_title_size);
			
			
			
			if(mConfig.exchange){
				
				if (ConfigInfo.cancelBgBitmap != null)
					finishTxt.setBackgroundDrawable(new BitmapDrawable(ConfigInfo.cancelBgBitmap));
				else {
					finishTxt.setBackgroundColor(this.mConfig.finish_bg);
				}

				finishTxt.setTextColor(this.mConfig.finish_title_color);
				finishTxt.setText(this.mConfig.finish_title);
				finishTxt.setTextSize(this.mConfig.finish_title_size);
				
				finishTxt.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						closeForResult();
					}
				});
				
				
				if (ConfigInfo.finishBgBitmap != null)
					cancelTxt.setBackgroundDrawable(new BitmapDrawable(ConfigInfo.finishBgBitmap));
				else {
					cancelTxt.setBackgroundColor(this.mConfig.cancel_bg);
				}
				cancelTxt.setTextColor(this.mConfig.cancel_title_color);
				cancelTxt.setText(this.mConfig.cancel_title);
				cancelTxt.setTextSize(this.mConfig.cancel_title_size);
				
				cancelTxt.setOnClickListener(new View.OnClickListener() {
					
					@Override
					public void onClick(View v) {
						finish();
					}
				});
				
			}
			

			this.mReplaceTxt = naviTitle.getText().toString();
			
			if(this.mConfig.intervalTime > 0){
				new Handler(Looper.getMainLooper()).postDelayed(new Runnable(){

					@Override
					public void run() {
						mImgGridView.setSelection(mAllImgList.size() - 1);
					}
				}, mConfig.intervalTime * 1000);
				
			}
		}
		super.onCreate(savedInstanceState);
	}

	public void setFullScreen() {
		requestWindowFeature(1);
		getWindow().setFlags(1024, 1024);
	}

	@Override
	public boolean isPortrait() {
		return mConfig.rotation;
	}

	@SuppressLint({ "NewApi" })
	public ImageView iconImage(FileInfo fileInfo, int index, CheckBox checkBox) throws FileNotFoundException {
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(this.mLayout.getMeasuredHeight() - 10, this.mLayout.getMeasuredHeight() - 10);
		ImageView imageView = new ImageView(this);
		imageView.setLayoutParams(params);
		imageView.setBackgroundResource(UZResourcesIDFinder.getResDrawableID("uz_media_scanner_imgbg"));

		this.mUtil.imgExcute(imageView, this.imgCallBack, new FileInfo[] { fileInfo });
		imageView.setOnClickListener(new ImgOnclick(fileInfo, checkBox));
		return imageView;
	}

	public void tobreak(View view) {
		finish();
	}

	public void sendfiles(View view) {
		closeForResult();
	}

	public void onBackPressed() {
		super.onBackPressed();
	}

	public void closeForResult() {
		Intent intent = new Intent();

		/*
		 * 
		if (FileInfo.SORT_BY_TIME == FileInfo.SORT_FLAG) {
			if (FileInfo.SORT_LAW == FileInfo.SORT_BY_ASC) {
				SortUtils.ascSortByTime(this.mFilelist);
			}

			if (FileInfo.SORT_LAW == FileInfo.SORT_BY_DESC) {
				SortUtils.dascSortByTime(this.mFilelist);
			}
		}

		if (FileInfo.SORT_BY_SIZE == FileInfo.SORT_FLAG) {
			if (FileInfo.SORT_LAW == FileInfo.SORT_BY_ASC) {
				SortUtils.ascSortBySize(this.mFilelist);
			}

			if (FileInfo.SORT_LAW == FileInfo.SORT_BY_DESC) {
				SortUtils.dascSortBySize(this.mFilelist);
			}
		}
		
		*/

		intent.putExtra("files", this.mFilelist);
		setResult(257, intent);
		finish();
	}

	class BottomImgIcon implements AdapterView.OnItemClickListener {
		int index;

		public BottomImgIcon(int index) {
			this.index = index;
		}

		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		}
	}

	class ImgOnclick implements View.OnClickListener {
		FileInfo fileInfo;
		CheckBox checkBox;

		public ImgOnclick(FileInfo fileInfo, CheckBox checkBox) {
			this.fileInfo = fileInfo;
			this.checkBox = checkBox;
		}

		public void onClick(View arg0) {
			this.checkBox.setChecked(false);
			UzImgsActivity.this.mFilelist.remove(this.fileInfo);
		}
	}
}