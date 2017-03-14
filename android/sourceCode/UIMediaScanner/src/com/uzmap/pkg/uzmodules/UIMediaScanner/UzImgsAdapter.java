/**
 * APICloud Modules
 * Copyright (c) 2014-2015 by APICloud, Inc. All Rights Reserved.
 * Licensed under the terms of the The MIT License (MIT).
 * Please see the license.html included with this distribution for details.
 */
package com.uzmap.pkg.uzmodules.UIMediaScanner;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.util.LruCache;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.uzmap.pkg.uzcore.UZResourcesIDFinder;
import com.uzmap.pkg.uzkit.UZUtility;

public class UzImgsAdapter extends BaseAdapter {
	private Context mContext;
	private ArrayList<FileInfo> mAllImgFiles;
	private Util mUtil;
	private OnItemClickClass onItemClickClass;
	private int index = -1;
	private String markPosition;

	private int mScreenWidth;
	private int mRow;
	private int markSize;
	private LruCache<String, SoftReference<Bitmap>> cacheMap;
	private Bitmap mSelectedIconBmp;
	private List<View> mHolderlist;

	UzImgsActivity mActivity;

	private boolean mShowPreview;

	public void setShowPreview(boolean isShowPreivew) {
		mShowPreview = isShowPreivew;
	}

	public void setBitmap(Bitmap selectedIconBmp) {
		this.mSelectedIconBmp = selectedIconBmp;
	}

	public void setScreenWidth(int screenWidth) {
		this.mScreenWidth = screenWidth;
	}

	public void setRow(int row) {
		this.mRow = row;
	}

	public void setMarkPosition(String mark) {
		this.markPosition = mark;
	}

	public void setMarkSize(int size) {
		this.markSize = UZUtility.dipToPix(size);
	}

	public List<View> getHolderlist() {
		return this.mHolderlist;
	}

	public UzImgsAdapter(Context context, ArrayList<FileInfo> data,
			OnItemClickClass onItemClickClass, UzImgsActivity activity) {
		mActivity = activity;
		this.mContext = context;
		this.mAllImgFiles = data;
		this.onItemClickClass = onItemClickClass;

		long maxMem = Runtime.getRuntime().maxMemory() / 1024L;

		this.cacheMap = new LruCache<String, SoftReference<Bitmap>>(
				(int) (maxMem / 8L));
		for (int i = 0; i < data.size(); i++) {
			this.cacheMap.put(((FileInfo) data.get(i)).path,
					new SoftReference<Bitmap>(null));
		}

		this.mUtil = new Util(context);
		this.mHolderlist = new ArrayList<View>();
	}

	public int getCount() {
		return this.mAllImgFiles.size();
	}

	public Object getItem(int arg0) {
		return this.mAllImgFiles.get(arg0);
	}

	public long getItemId(int arg0) {
		return arg0;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public View getView(final int arg0, View arg1, ViewGroup arg2) {

		Log.i("lyh", "debug ...");
		Holder holder = null;
		if ((arg0 != this.index) && (arg0 > this.index)) {
			// this.index = arg0;
			arg1 = LayoutInflater
					.from(this.mContext)
					.inflate(
							UZResourcesIDFinder
									.getResLayoutID("uz_media_scanner_imgsitem"),
							null);
			holder = new Holder();
			holder.itemIv = ((ImageView) arg1.findViewById(UZResourcesIDFinder
					.getResIdID("imageView1")));
			holder.itemCheckBox = ((CheckBox) arg1
					.findViewById(UZResourcesIDFinder.getResIdID("checkBox1")));
			holder.itemSelectedImage = ((ImageView) arg1
					.findViewById(UZResourcesIDFinder.getResIdID("selectIcon")));
			arg1.setTag(holder);
			this.mHolderlist.add(arg1);
		} else {
			holder = (Holder) ((View) this.mHolderlist.get(arg0 % index))
					.getTag();
			arg1 = (View) this.mHolderlist.get(arg0 % index);
		}
		if (holder.itemSelectedImage != null) {
			if (mShowPreview) {
				holder.itemSelectedImage
						.setOnClickListener(new OnClickListener() {

							@Override
							public void onClick(View v) {
								mActivity.onItemClickEvent(arg0);
							}
						});
			}
		}
		if (holder.itemIv != null) {
			holder.itemIv.getLayoutParams().height = (this.mScreenWidth / this.mRow);

			if (((SoftReference<Bitmap>) this.cacheMap
					.get(((FileInfo) this.mAllImgFiles.get(arg0)).path)).get() == null)
				this.mUtil
						.imgExcute(holder.itemIv, new ImgCallBackLisner(arg0),
								new FileInfo[] { (FileInfo) this.mAllImgFiles
										.get(arg0) });
			else {
				holder.itemIv
						.setImageBitmap((Bitmap) ((SoftReference<Bitmap>) this.cacheMap
								.get(((FileInfo) this.mAllImgFiles.get(arg0)).path))
								.get());
			}
		}

		if (holder.itemCheckBox != null) {
			RelativeLayout.LayoutParams param = (RelativeLayout.LayoutParams) holder.itemCheckBox
					.getLayoutParams();

			if (this.markPosition.trim().equals("top_left")) {
				param.addRule(
						5,
						arg1.findViewById(
								UZResourcesIDFinder.getResIdID("imageView1"))
								.getId());
				param.addRule(
						6,
						arg1.findViewById(
								UZResourcesIDFinder.getResIdID("imageView1"))
								.getId());
				holder.itemCheckBox.setLayoutParams(param);

				RelativeLayout.LayoutParams selectedIconParam = new RelativeLayout.LayoutParams(
						this.markSize, this.markSize);
				selectedIconParam.addRule(
						5,
						arg1.findViewById(
								UZResourcesIDFinder.getResIdID("imageView1"))
								.getId());
				selectedIconParam.addRule(
						6,
						arg1.findViewById(
								UZResourcesIDFinder.getResIdID("imageView1"))
								.getId());

				holder.itemSelectedImage.setLayoutParams(selectedIconParam);
			}

			if (this.markPosition.trim().equals("bottom_left")) {
				param.addRule(
						5,
						arg1.findViewById(
								UZResourcesIDFinder.getResIdID("imageView1"))
								.getId());
				param.addRule(12);
				holder.itemCheckBox.setLayoutParams(param);

				RelativeLayout.LayoutParams selectedIconParam = new RelativeLayout.LayoutParams(
						this.markSize, this.markSize);
				selectedIconParam.addRule(
						5,
						arg1.findViewById(
								UZResourcesIDFinder.getResIdID("imageView1"))
								.getId());
				selectedIconParam.addRule(12);

				holder.itemSelectedImage.setLayoutParams(selectedIconParam);
			}

			if (this.markPosition.trim().equals("bottom_right")) {
				param.addRule(
						7,
						arg1.findViewById(
								UZResourcesIDFinder.getResIdID("imageView1"))
								.getId());
				param.addRule(12);
				holder.itemCheckBox.setLayoutParams(param);

				RelativeLayout.LayoutParams selectedIconParam = new RelativeLayout.LayoutParams(
						this.markSize, this.markSize);
				selectedIconParam.addRule(
						7,
						arg1.findViewById(
								UZResourcesIDFinder.getResIdID("imageView1"))
								.getId());
				selectedIconParam.addRule(12);

				holder.itemSelectedImage.setLayoutParams(selectedIconParam);
			}

			if (this.markPosition.trim().equals("top_right")) {
				param.addRule(
						7,
						arg1.findViewById(
								UZResourcesIDFinder.getResIdID("imageView1"))
								.getId());
				param.addRule(
						6,
						arg1.findViewById(
								UZResourcesIDFinder.getResIdID("imageView1"))
								.getId());
				holder.itemCheckBox.setLayoutParams(param);

				RelativeLayout.LayoutParams selectedIconParam = new RelativeLayout.LayoutParams(
						this.markSize, this.markSize);
				selectedIconParam.addRule(
						7,
						arg1.findViewById(
								UZResourcesIDFinder.getResIdID("imageView1"))
								.getId());
				selectedIconParam.addRule(
						6,
						arg1.findViewById(
								UZResourcesIDFinder.getResIdID("imageView1"))
								.getId());

				holder.itemSelectedImage.setLayoutParams(selectedIconParam);
			}

			if ((this.mSelectedIconBmp != null)
					&& (holder.itemSelectedImage != null)) {
				holder.itemCheckBox.setVisibility(8);
				holder.itemSelectedImage.setVisibility(0);

				if (((FileInfo) this.mAllImgFiles.get(arg0)).isChecked) {
					holder.itemSelectedImage
							.setImageBitmap(this.mSelectedIconBmp);
				} else {
					if(mShowPreview){
						holder.itemSelectedImage
						.setImageResource(UZResourcesIDFinder
								.getResDrawableID("mo_media_scanner_select"));
					}else{
						holder.itemSelectedImage.setImageDrawable(new ColorDrawable(0));
					}
				}
			} else {
				holder.itemCheckBox.setVisibility(0);
				if (holder.itemSelectedImage != null) {
					holder.itemSelectedImage.setVisibility(8);
				}
			}

			if (holder.itemSelectedImage != null) {
				holder.itemSelectedImage.setVisibility(View.VISIBLE);
			}
		}
		return arg1;
	}

	public class ImgCallBackLisner implements UzImgCallBack {
		int num;

		public ImgCallBackLisner(int num) {
			this.num = num;
		}

		public void resultImgCall(ImageView imageView, Bitmap bitmap) {
			UzImgsAdapter.this.cacheMap
					.put(((FileInfo) UzImgsAdapter.this.mAllImgFiles
							.get(this.num)).path, new SoftReference<Bitmap>(
							bitmap));
			imageView
					.setImageBitmap((Bitmap) ((SoftReference<Bitmap>) UzImgsAdapter.this.cacheMap
							.get(((FileInfo) UzImgsAdapter.this.mAllImgFiles
									.get(this.num)).path)).get());
		}
	}

	public static abstract interface OnItemClickClass {
		public abstract void OnItemClick(View paramView, int paramInt,
				CheckBox paramCheckBox, ImageView paramImageView);
	}

	class OnPhotoClick implements View.OnClickListener {
		int position;
		CheckBox checkBox;
		ImageView selectedImage;

		public OnPhotoClick(int position, CheckBox checkBox,
				ImageView selectedImage) {
			this.position = position;
			this.checkBox = checkBox;
			this.selectedImage = selectedImage;
		}

		public void onClick(View v) {
			if ((UzImgsAdapter.this.mAllImgFiles != null)
					&& (UzImgsAdapter.this.onItemClickClass != null)) {
				UzImgsAdapter.this.onItemClickClass.OnItemClick(v,
						this.position, this.checkBox, this.selectedImage);
			}
		}
	}
}