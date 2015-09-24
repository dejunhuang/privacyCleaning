package data.cleaning.core.utils.search;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import data.cleaning.core.service.dataset.impl.Constraint;
import data.cleaning.core.service.dataset.impl.InfoContentTable;
import data.cleaning.core.service.dataset.impl.MasterDataset;
import data.cleaning.core.service.dataset.impl.TargetDataset;
import data.cleaning.core.service.matching.impl.Match;
import data.cleaning.core.service.repair.impl.Candidate;
import data.cleaning.core.service.repair.impl.Recommendation;
import data.cleaning.core.utils.Pair;

/**
 * @author thomas
 * This class is used for search all the possible solutions w.r.t the matches
 * for small datasets using Brute Force Searching Algorithm
 */
public class BruteForceSearch extends Search{

	@Override
	public Set<Candidate> calcOptimalSolns(Constraint constraint,
			List<Match> tgtMatches, TargetDataset tgtDataset,
			MasterDataset mDataset, InfoContentTable table,
			boolean shdReturnInit) {
		
		List<Candidate> initCandidates = getInitCandidates (
				constraint, tgtMatches, tgtDataset, mDataset);
		
		Set<Candidate> candidatesSet = getSetCandidates (initCandidates);
		return candidatesSet;
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
		
		System.out.println("positionToChoices: " + positionToChoices);
		
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

}
