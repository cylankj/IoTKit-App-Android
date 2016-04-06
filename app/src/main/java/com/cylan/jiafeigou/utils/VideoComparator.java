package com.cylan.jiafeigou.utils;

import java.util.Comparator;

public class VideoComparator implements Comparator<String> {

	@Override
	public int compare(String lhs, String rhs) {

		return lhs.compareTo(rhs);
	}

}
