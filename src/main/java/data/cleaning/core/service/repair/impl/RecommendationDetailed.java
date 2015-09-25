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
		this.tVal = r.getVal();
		
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
	
}
