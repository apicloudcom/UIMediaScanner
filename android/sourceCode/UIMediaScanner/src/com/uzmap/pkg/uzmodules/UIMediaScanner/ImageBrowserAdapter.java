/**
 * APICloud Modules
 * Copyright (c) 2014-2015 by APICloud, Inc. All Rights Reserved.
 * Licensed under the terms of the The MIT License (MIT).
 * Please see the license.html included with this distribution for details.
 */
package com.uzmap.pkg.uzmodules.UIMediaScanner;

import java.util.ArrayList;
import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import co.senab.photoview.PhotoView;
import com.uzmap.pkg.uzcore.UZResourcesIDFinder;
import com.uzmap.pkg.uzcore.uzmodule.UZModuleContext;

public class ImageBrowserAdapter extends PagerAdapter {

	private ArrayList<String> mImagePaths;
	private Context mContext;
	private ImageLoader mImageLoader;

	private ViewGroup mViewContainer;

	private boolean zoomEnable = true;

	public void setZoomEnable(boolean zoomable) {
		this.zoomEnable = zoomable;
	}

	public ImageBrowserAdapter(Context context, UZModuleContext uzContext,
			ArrayList<String> imagePaths, ImageLoader imageLoader) {
		this.mImagePaths = imagePaths;
		this.mImageLoader = imageLoader;
		this.mContext = context;
	}

	@Override
	public int getCount() {
		return mImagePaths.size();
	}

	@Override
	public boolean isViewFromObject(View arg0, Object arg1) {
		return arg0 == arg1;
	}

	public ViewGroup getViewContainer() {
		return this.mViewContainer;
	}

	@SuppressWarnings("deprecation")
	@Override
	public Object instantiateItem(ViewGroup container, final int position) {

		mViewContainer = container;

		int item_view_id = UZResourcesIDFinder
				.getResLayoutID("meidascanner_photo_browser_item_layout");
		View itemView = View.inflate(mContext, item_view_id, null);

		itemView.setTag(position);

		int photo_view_id = UZResourcesIDFinder.getResIdID("photoView");
		final PhotoView imageView = (PhotoView) itemView
				.findViewById(photo_view_id);

		imageView.setZoomable(this.zoomEnable);
		imageView.setAdjustViewBounds(true);
		imageView.setMaxScale(15.0f);

		int load_progress_id = UZResourcesIDFinder.getResIdID("loadProgress");
		final ProgressBar progress = (ProgressBar) itemView
				.findViewById(load_progress_id);
		progress.setTag(position);

		mImageLoader.load(imageView, progress, mImagePaths.get(position));

		container.addView(itemView);
		return itemView;
	}

	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		container.removeView((View) object);
	}

	public ArrayList<String> getDatas() {
		return mImagePaths;
	}

}
