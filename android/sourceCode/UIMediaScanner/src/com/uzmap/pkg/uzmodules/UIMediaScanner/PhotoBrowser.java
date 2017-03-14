/**
 * APICloud Modules
 * Copyright (c) 2014-2015 by APICloud, Inc. All Rights Reserved.
 * Licensed under the terms of the The MIT License (MIT).
 * Please see the license.html included with this distribution for details.
 */
package com.uzmap.pkg.uzmodules.UIMediaScanner;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

import com.uzmap.pkg.uzcore.UZResourcesIDFinder;
import com.uzmap.pkg.uzcore.uzmodule.UZModuleContext;

public class PhotoBrowser extends Activity {

	private HackyViewPager mBrowserPager;
	private ImageLoader mLoader;
	private ImageBrowserAdapter mAdapter;
	private ArrayList<String> mImgPaths;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(1);
		getWindow().setFlags(1024, 1024);
		setContentView(UZResourcesIDFinder
				.getResLayoutID("meidascanner_photobrowser_main_layout"));
		mLoader = new ImageLoader(this, getCacheDir().getAbsolutePath());
		mImgPaths = getIntent().getStringArrayListExtra("imgPaths");
		open();
	}

	public void open() {
		int browserPagerId = UZResourcesIDFinder.getResIdID("browserPager");
		mBrowserPager = (HackyViewPager) findViewById(browserPagerId);
		mBrowserPager.setBackgroundColor(Color.BLACK);
		mAdapter = new ImageBrowserAdapter(this, null, mImgPaths, mLoader);
		mBrowserPager.setAdapter(mAdapter);
		mAdapter.setZoomEnable(true);
		mBrowserPager.setCurrentItem(0);
		findViewById(UZResourcesIDFinder.getResIdID("back"))
				.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						setResult(1);
						finish();
					}
				});

		findViewById(UZResourcesIDFinder.getResIdID("confirm"))
				.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						setResult(0);
						finish();
					}
				});
	}

	public View getExistChild(int index) {
		if (mAdapter != null && mAdapter.getViewContainer() != null) {
			for (int i = 0; i < mAdapter.getViewContainer().getChildCount(); i++) {
				if ((Integer) (mAdapter.getViewContainer().getChildAt(i)
						.getTag()) == index) {
					return mAdapter.getViewContainer().getChildAt(i);
				}
			}
		}
		return null;
	}

	public void clearCache(UZModuleContext uzContext) {
		if (mLoader != null) {

			new Thread(new Runnable() {
				@Override
				public void run() {
					mLoader.clearCache();
				}
			}).start();
		}
	}
}
