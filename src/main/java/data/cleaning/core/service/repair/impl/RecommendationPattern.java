package data.cleaning.core.service.repair.impl;

import java.util.HashSet;
import java.util.Set;

public class RecommendationPattern {
	
	private String col;
	private String mVal;
	private String tVal;
	private Set<Long> tIds = new HashSet<>();
	
	public String getCol() {
		return col;
	}

	public void setCol(String col) {
		this.col = col;
	}

	public String getmVal() {
		return mVal;
	}

	public void setmVal(String mVal) {
		this.mVal = mVal;
	}

	public String gettVal() {
		return tVal;
	}

	public void settVal(String tVal) {
		this.tVal = tVal;
	}

	public void addtId (long id) {
		this.tIds.add(id);
	}
	
	public Set<Long> gettIds () {
		return this.tIds;
	}

	@Override
	public String toString() {
		return "RecommendationPattern [col=" + col + ", mVal=" + mVal
				+ ", tVal=" + tVal + ", tIds=" + tIds + "]";
	}
	
}
