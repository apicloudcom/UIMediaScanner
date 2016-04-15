/**
 * APICloud Modules
 * Copyright (c) 2014-2015 by APICloud, Inc. All Rights Reserved.
 * Licensed under the terms of the The MIT License (MIT).
 * Please see the license.html included with this distribution for details.
 */
package com.uzmap.pkg.uzmodules.UIMediaScanner;

import java.util.ArrayList;

public class SortUtils {

	/**
	 * selected sort by DESC & time
	 */
	public static ArrayList<FileInfo> dascSortByTime(ArrayList<FileInfo> param) {
		int in, out;
		int max;
		FileInfo temp;
		for (out = 0; out < param.size(); out++) {
			max = out;
			for (in = out + 1; in < param.size(); in++) {
				if (param.get(max).time < param.get(in).time) {
					max = in;
				}
			}
			if (out != max) {
				temp = param.get(out);
				param.set(out, param.get(max));
				param.set(max, temp);
			}
		}
		return param;
	}

	/**
	 * selected sort by ASC & time
	 */
	public static ArrayList<FileInfo> ascSortByTime(ArrayList<FileInfo> param) {
		int i, j;
		FileInfo temp;

		for (i = 0; i < param.size() - 1; i++) {
			for (j = i + 1; j < param.size(); j++) {

				if (param.get(j).time < param.get(i).time) {

					temp = param.get(i);
					param.set(i, param.get(j));
					param.set(j, temp);

				}
			}
		}

		return param;
	}

	/**
	 * selected sort by DESC & size
	 */
	public static ArrayList<FileInfo> dascSortBySize(ArrayList<FileInfo> param) {
		int in, out;
		int max;
		FileInfo temp;
		for (out = 0; out < param.size(); out++) {
			max = out;
			for (in = out + 1; in < param.size(); in++) {
				if (param.get(max).size < param.get(in).size) {
					max = in;
				}
			}
			if (out != max) {
				temp = param.get(out);
				param.set(out, param.get(max));
				param.set(max, temp);
			}
		}

		return param;
	}

	/**
	 * selected sort by ASC & size
	 */
	public static ArrayList<FileInfo> ascSortBySize(ArrayList<FileInfo> param) {
		int i, j;
		FileInfo temp;

		for (i = 0; i < param.size() - 1; i++) {
			for (j = i + 1; j < param.size(); j++) {

				if (param.get(j).size < param.get(i).size) {

					temp = param.get(i);
					param.set(i, param.get(j));
					param.set(j, temp);

				}
			}
		}
		return param;
	}

}
