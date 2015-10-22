package data.cleaning.core.utils.search;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;

import data.cleaning.core.service.dataset.DatasetService;
import data.cleaning.core.service.dataset.impl.Constraint;
import data.cleaning.core.service.dataset.impl.InfoContentTable;
import data.cleaning.core.service.dataset.impl.MasterDataset;
import data.cleaning.core.service.dataset.impl.TargetDataset;
import data.cleaning.core.service.matching.impl.Match;
import data.cleaning.core.service.repair.impl.Candidate;
import data.cleaning.core.service.repair.impl.Recommendation;
import data.cleaning.core.utils.Pair;
import data.cleaning.core.utils.ProdLevel;
import data.cleaning.core.utils.objectives.IndNormStrategy;
import data.cleaning.core.utils.objectives.Objective;

/**
 * @author thomas
 * This class is used for search all the possible solutions w.r.t the matches
 * for small datasets using Brute Force Searching Algorithm
 */
public class BruteForceSearch extends Search{
	
	private List<Objective> weightedFns;
	private IndNormStrategy indNormStrat;
	
	public BruteForceSearch (List<Objective> weightedFns, IndNormStrategy indNormStrat) {
		this.weightedFns = weightedFns;
		this.indNormStrat = indNormStrat;
	}

	@Override
	public Set<Candidate> calcOptimalSolns(Constraint constraint,
			List<Match> tgtMatches, TargetDataset tgtDataset,
			MasterDataset mDataset, InfoContentTable table,
			boolean shdReturnInit) {
		
		List<Candidate> initCandidates = getInitCandidates (
				constraint, tgtMatches, tgtDataset, mDataset);
		
		//TODO: for test, delete later
		for (Candidate c: initCandidates) {
			Candidate cTemp = getCandidateWithObjectiveScores(c, tgtDataset, mDataset, table, constraint, tgtMatches);
			System.out.println("pvt: " + cTemp.getPvtOut() + "/" + cTemp.getPvtOutUnorm()
					+ " util: " + cTemp.getIndOut() + "/" + cTemp.getIndOutUnorm()
					+ " upd: " + cTemp.getChangesOut() + "/" + cTemp.getChangesOutUnorm());
		}
		
		initCandidates = getCandidatesWithObjectiveScores(initCandidates, tgtDataset, mDataset, table, constraint, tgtMatches);
		
		initCandidates = getSortedCandidates(initCandidates);
		
		//TODO: this is for debug, delete later
		printResultDetails(initCandidates);
		
		Set<Candidate> candidatesSet = getSetCandidates (initCandidates);
		return candidatesSet;
	}
	
	/**
	 * get sorted solns on overal scores
	 * @param constraint
	 * @param tgtMatches
	 * @param tgtDataset
	 * @param mDataset
	 * @param table
	 * @param shdReturnInit
	 * @return
	 */
	public List<Candidate> getSortedSolns(Constraint constraint,
			List<Match> tgtMatches, TargetDataset tgtDataset,
			MasterDataset mDataset, InfoContentTable table,
			boolean shdReturnInit) {
		
		List<Candidate> initCandidates = getInitCandidates (
				constraint, tgtMatches, tgtDataset, mDataset);
		
		initCandidates = getCandidatesWithObjectiveScores(initCandidates, tgtDataset, mDataset, table, constraint, tgtMatches);
		initCandidates = getSortedCandidates(initCandidates);
		
		return initCandidates;
	}
	
	// get all the possible candidates according to the matches
	private List<Candidate> getInitCandidates (Constraint constraint,
			List<Match> tgtMatches, TargetDataset tgtDataset,
			MasterDataset mDataset) {
		List<Candidate> candidates = matchesToCandidates (
				tgtMatches,
				constraint,
				tgtDataset,
				mDataset);
		return candidates;
	}
	
	// get list of candidates from matches
	// use dhruv's method to calculate Candidates
	private List<Candidate> matchesToCandidates (
			List<Match> tgtMatches,
			Constraint constraint,
			TargetDataset tgtDataset,
			MasterDataset mDataset) {
		List<Candidate> candidates = new ArrayList<>();
		
		PositionalInfo pInfo = calcPositionalInfo(tgtMatches, mDataset,
				constraint);
		Map<Integer, Map<Integer, Choice>> positionToChoices = pInfo
				.getPositionToChoices();
		
//		System.out.println("positionToChoices: " + positionToChoices);
		
		// transfer from positionToChoices to List<Candidate>
		Collection<Map<Integer, Choice>> choicesList = positionToChoices.values();
		
		for (Map<Integer, Choice> choicesMap: choicesList) {
			if (!choicesMap.containsKey(0)) {
				choicesMap.put(0, new Choice());
			}
			
			boolean emptyCandidatesList = false;
			if (candidates.isEmpty()) {
				emptyCandidatesList = true;
			}
			
			Collection<Choice> choices = choicesMap.values();
			if (!choices.isEmpty()) {
				List<Candidate> candidatesTemp = new ArrayList<>();
				
				for (Choice choice: choices) {
					List<Recommendation> recList = choice.getRecs();
					
					if (emptyCandidatesList) {
						Candidate candidateTemp = new Candidate();
						candidateTemp.setRecommendations(recList);
						candidatesTemp.add(candidateTemp);
					}
					else {
						for (Candidate candidateTemp: candidates) {
							List<Recommendation> recListOriginal = candidateTemp.getRecommendations();
							List<Recommendation> temp = new ArrayList<Recommendation>();
							temp.addAll(recListOriginal);
							temp.addAll(recList);
							candidateTemp = new Candidate();
							candidateTemp.setRecommendations(temp);
							candidatesTemp.add(candidateTemp);
						}
					}
				}
				
				candidates = new ArrayList<Candidate>(candidatesTemp);
			}
		}
		
		return candidates;
	}
	
	// to calculate privacy loss, data cleaning utility and changes objective scores for candidates
	private List<Candidate> getCandidatesWithObjectiveScores (
			List<Candidate> can,
			TargetDataset tgtDataset,
			MasterDataset mDataset, 
			InfoContentTable table,
			Constraint constraint,
			List<Match> tgtMatches) {
		
		double maxInd = calcMaxInd(constraint, tgtDataset.getRecords(),
				indNormStrat);
		double maxPvt = table.getMaxInfoContent();
		
		PositionalInfo pInfo = calcPositionalInfo(tgtMatches, mDataset,
				constraint);
		Map<Integer, Map<Integer, Choice>> positionToChoices = pInfo
				.getPositionToChoices();
		List<String> cols = constraint.getColsInConstraint();
		int sigSize = positionToChoices.keySet().size() * cols.size();
		long recSize = sigSize;
		
		for (Candidate c: can) {
			for (Objective weightedFn : weightedFns) {
				double wOut = weightedFn.out(c, tgtDataset, mDataset,
						maxPvt, maxInd, recSize) * weightedFn.getWeight();
				
				if (weightedFn.getClass().getSimpleName()
						.equals("PrivacyObjective")) {
					c.setPvtOut(wOut);
					
				} else if (weightedFn.getClass().getSimpleName()
						.equals("CleaningObjective")) {
					c.setIndOut(wOut);
				} else {
					c.setChangesOut(wOut);
				}
			}
		}
		
		return can;
	}
	
	// to calculate privacy loss, data cleaning utility and changes objective scores for single candidate
	private Candidate getCandidateWithObjectiveScores (
			Candidate c,
			TargetDataset tgtDataset,
			MasterDataset mDataset, 
			InfoContentTable table,
			Constraint constraint,
			List<Match> tgtMatches) {
		double maxInd = calcMaxInd(constraint, tgtDataset.getRecords(),
				indNormStrat);
		double maxPvt = table.getMaxInfoContent();
		
		PositionalInfo pInfo = calcPositionalInfo(tgtMatches, mDataset,
				constraint);
		Map<Integer, Map<Integer, Choice>> positionToChoices = pInfo
				.getPositionToChoices();
		List<String> cols = constraint.getColsInConstraint();
		int sigSize = positionToChoices.keySet().size() * cols.size();
		long recSize = sigSize;
		
		for (Objective weightedFn : weightedFns) {
			double wOut = weightedFn.out(c, tgtDataset, mDataset,
					maxPvt, maxInd, recSize) * weightedFn.getWeight();
			
			if (weightedFn.getClass().getSimpleName()
					.equals("PrivacyObjective")) {
				c.setPvtOut(wOut);
				
			} else if (weightedFn.getClass().getSimpleName()
					.equals("CleaningObjective")) {
				c.setIndOut(wOut);
			} else {
				c.setChangesOut(wOut);
			}
		}
		
		return c;
	}
	
	//TODO: To be finished
	// get sorted list of candidates by the weighted scores
	private List<Candidate> getSortedCandidates (List<Candidate> candidates) {
		List<CandidateWithScore> candidatesWithScore = new ArrayList<CandidateWithScore>();
		
		for (Candidate can: candidates) {
			double overallScore = 0.0;
			for (Objective weightedFn : weightedFns) {
				double temp;
				if (weightedFn.getClass().getSimpleName()
						.equals("PrivacyObjective")) {
					temp = can.getPvtOut() * weightedFn.getWeight();
					
				} else if (weightedFn.getClass().getSimpleName()
						.equals("CleaningObjective")) {
					temp = can.getIndOut() * weightedFn.getWeight();
				} else {
					temp = can.getChangesOut() * weightedFn.getWeight();
				}
				overallScore += temp;
			}
			CandidateWithScore candidateWithScore = new CandidateWithScore();
			candidateWithScore.setCandidate(can);
			candidateWithScore.setOverallScore(overallScore);
			candidatesWithScore.add(candidateWithScore);
		}
		
		Collections.sort(candidatesWithScore, new Comparator<CandidateWithScore>(){
			@Override
		    public int compare(CandidateWithScore c1, CandidateWithScore c2) {
		        if (c1.getOverallScore() > c2.getOverallScore()) {
		        	return 1;
		        }
		        else {
		        	return 0;
		        }
		    }
		});
		
		List<Candidate> candidatesResult = new ArrayList<Candidate>();
		for (CandidateWithScore cTemp: candidatesWithScore) {
			candidatesResult.add(cTemp.getCandidate());
		}
		
		return candidatesResult;
	}
	
	// get list of candidates from matches
	// use customized method to calculate Candidates
	private List<Candidate> matchesToCandidates_Customized (
			List<Match> tgtMatches,
			Constraint constraint,
			TargetDataset tgtDataset,
			MasterDataset mDataset) {
		
		List<Candidate> candidates = new ArrayList<Candidate>();
		
		for (Match m: tgtMatches) {
			Queue<Pair<Long, Float>> queue = m.getMatchRidToDist();
			if (!queue.isEmpty()) {
				boolean emptyCandidatesList = false;
				if (candidates.isEmpty()) {
					emptyCandidatesList = true;
				}
				
				List<Candidate> candidatesTemp = new ArrayList<Candidate>();
				
				for (Pair<Long, Float> pair: queue) {
					// list of recommendation w.r.t a pair of match
					List<Recommendation> recList = matchPairToRecommendation(
							pair, 
							m.getOriginalrId(),
							constraint,
							tgtDataset,
							mDataset);
					
					// add the current recommendations to existing candidates
					if (emptyCandidatesList) {
						Candidate canTemp = new Candidate();
						canTemp.setRecommendations(recList);
						candidatesTemp.add(canTemp);
					}
					else {
						for (Candidate c: candidates) {
							List<Recommendation> recListOrigin = c.getRecommendations();
							recListOrigin.addAll(recList);
							Candidate canTemp = new Candidate();
							canTemp.setRecommendations(recListOrigin);
							candidatesTemp.add(canTemp);
						}
					}
				}
				
				candidates = candidatesTemp;
			}
		}
		
		return candidates;
	}
	
	//TODO
	private List<Recommendation> matchPairToRecommendation (
			Pair<Long, Float> pair, 
			long rId,
			Constraint constraint,
			TargetDataset tgtDataset,
			MasterDataset mDataset) {
		
		return null;
	}
	
	// transfer list of candidates to set of candidates
	private Set<Candidate> getSetCandidates (List<Candidate> candidates) {
		Set<Candidate> candidatesSet = new HashSet<>();
		
		for (Candidate c: candidates) {
			candidatesSet.add(c);
		}
		
		return candidatesSet;
	}
	
	// this class is for sorting Candidates by overallScore
	private class CandidateWithScore {
		private Candidate candidate;
		private double overallScore;
		
		public Candidate getCandidate() {
			return candidate;
		}
		public void setCandidate(Candidate candidate) {
			this.candidate = candidate;
		}
		public double getOverallScore() {
			return overallScore;
		}
		public void setOverallScore(double overallScore) {
			this.overallScore = overallScore;
		}
	}
	
	private void printResultDetails (List<Candidate> c) {
		System.out.println();
		System.out.println("# of Candidates: " + c.size());
		System.out.println("Candidates Details:");
		
		for (Candidate cTemp: c) {
			System.out.println("Pvt: " + cTemp.getPvtOut() + "/" + cTemp.getPvtOutUnorm()
					+ " Ind: " + cTemp.getIndOut() + "/" + cTemp.getIndOutUnorm()
					+ " Upd: " + cTemp.getChangesOut() + "/" + cTemp.getChangesOutUnorm());
		}
		System.out.println();
		
	}

}
