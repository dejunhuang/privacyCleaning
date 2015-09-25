package data.cleaning.core.service.repair.impl;

import java.util.ArrayList;
import java.util.List;

import data.cleaning.core.service.dataset.impl.MasterDataset;
import data.cleaning.core.service.dataset.impl.TargetDataset;

public class RecommendationDetailed{
	private MasterDataset m;
	private TargetDataset t;
	
	private long mRid;
	private long tRid;
	private String col;
	private String tVal;
	private String mVal;
	
	public RecommendationDetailed(Recommendation r, MasterDataset m, TargetDataset t) {
		this.mRid = r.getmRid();
		this.tRid = r.gettRid();
		this.col = r.getCol();
		this.mVal = r.getVal();
		
		this.m = m;
		this.t = t;
		settVal();
	}
	
	private void settVal() {
		List<String> cols = new ArrayList<>();
		cols.add(col);
		String tValTemp = t.getRecord(tRid).getRecordStr(cols);
		this.tVal = tValTemp;
	}
	
	public MasterDataset getM() {
		return m;
	}

	public void setM(MasterDataset m) {
		this.m = m;
	}

	public TargetDataset getT() {
		return t;
	}

	public void setT(TargetDataset t) {
		this.t = t;
	}

	public long getmRid() {
		return mRid;
	}

	public void setmRid(long mRid) {
		this.mRid = mRid;
	}

	public long gettRid() {
		return tRid;
	}

	public void settRid(long tRid) {
		this.tRid = tRid;
	}

	public String getCol() {
		return col;
	}

	public void setCol(String col) {
		this.col = col;
	}

	public String gettVal() {
		return tVal;
	}

	public void settVal(String tVal) {
		this.tVal = tVal;
	}

	public String getmVal() {
		return mVal;
	}

	public void setmVal(String mVal) {
		this.mVal = mVal;
	}

	@Override
	public String toString() {
		return "RecommendationDetailed [mRid=" + mRid + ", tRid=" + tRid
				+ ", col=" + col + ", tVal=" + tVal + ", mVal=" + mVal + "]";
	}
	
}
