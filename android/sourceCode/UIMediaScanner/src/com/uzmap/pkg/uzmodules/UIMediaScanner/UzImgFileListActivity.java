/**
 * APICloud Modules
 * Copyright (c) 2014-2015 by APICloud, Inc. All Rights Reserved.
 * Licensed under the terms of the The MIT License (MIT).
 * Please see the license.html included with this distribution for details.
 */
package com.uzmap.pkg.uzmodules.UIMediaScanner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.uzmap.pkg.uzcore.UZResourcesIDFinder;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class UzImgFileListActivity extends BaseActivity implements OnItemClickListener {

	public static final int REQUEST_CODE = 0x200;
	public static final int RESULT_CODE = 0x201;
	
	public static final String TRANS_TAG = "hasData";
	
	private ListView mListView;
	private UzImgFileListAdapter mListAdapter;

	private Util mUtil;
	private List<UzFileTraversal> mLocallist;
	private ConfigInfo mConfig;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		

		setFullScreen();
		setContentView(UZResourcesIDFinder.getResLayoutID("uz_media_scanner_imgfilelist"));
		findViewById(UZResourcesIDFinder.getResIdID("relativeLayout1")).setVisibility(View.VISIBLE);
		
		
		mListView = (ListView) findViewById(UZResourcesIDFinder.getResIdID("listView1"));
		mUtil = new Util(this);
		
		mConfig = (ConfigInfo) getIntent().getSerializableExtra(UIMediaScanner.CONFIG_TAG);
		
		if(ConfigInfo.FILTER_ALL.equals(mConfig.filterType)){
			mLocallist = mUtil.LocalImgFileList(Util.ALL_TYPE);
		}
		if(ConfigInfo.FILTER_PICTURE.equals(mConfig.filterType)){
			mLocallist = mUtil.LocalImgFileList(Util.IMAGE_TYPE);
		}
		if(ConfigInfo.FILTER_VIDEO.equals(mConfig.filterType)){
			mLocallist = mUtil.LocalImgFileList(Util.VIDEO_TYPE);
		}

		List<HashMap<String, String>> listdata = new ArrayList<HashMap<String, String>>();

		
		findViewById(UZResourcesIDFinder.getResIdID("relativeLayout1")).setBackgroundColor(0xFF3D3D3D);
		TextView naviTitle = (TextView) findViewById(UZResourcesIDFinder.getResIdID("navi_title"));
		naviTitle.setText("照片");
		naviTitle.setTextSize(22);
		
		TextView cancelTxt = (TextView) findViewById(UZResourcesIDFinder.getResIdID("button2"));
		cancelTxt.setText("取消");
		cancelTxt.setTextSize(20);

		if (mLocallist != null) {
			for (int i = 0; i < mLocallist.size(); i++) {
				HashMap<String, String> map = new HashMap<String, String>();
				map.put("filecount", "("+mLocallist.get(i).filecontent.size() + ")");
				map.put("imgpath", mLocallist.get(i).filecontent.get(0) == null ? null : (mLocallist.get(i).filecontent.get(0)));
				map.put("filename", mLocallist.get(i).filename);
				listdata.add(map);
			}
		}

		mListAdapter = new UzImgFileListAdapter(this, listdata);
		mListView.setAdapter(mListAdapter);
		mListView.setOnItemClickListener(this);
		
		super.onCreate(savedInstanceState);
		
	}

	public void tobreak(View view) {
		finish();
	}

	@Override
	public boolean isPortrait() {
		// TODO Auto-generated method stub
		return mConfig.rotation;
	}

	public void setFullScreen() {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		setResult(UIMediaScanner.RESULT_CODE, data);
		finish();
	}
	
	public static UzFileTraversal fileTraversal;

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {

		Intent intent = new Intent(this, UzImgsActivity.class);
		Bundle bundle = new Bundle();
		fileTraversal = mLocallist.get(arg2);
		bundle.putBoolean(TRANS_TAG, true);
		intent.putExtras(bundle);
		intent.putExtra(UIMediaScanner.CONFIG_TAG, mConfig);
		startActivityForResult(intent, REQUEST_CODE);

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		fileTraversal = null;
	}
}
