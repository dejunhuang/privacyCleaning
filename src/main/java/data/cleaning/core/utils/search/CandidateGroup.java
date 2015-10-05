package data.cleaning.core.utils.search;

import java.util.Set;

import data.cleaning.core.service.repair.impl.Candidate;

public class CandidateGroup {
	public double getOut() {
		return out;
	}

	public void setOut(double out) {
		this.out = out;
	}

	public Set<Candidate> getCandidates() {
		return candidates;
	}

	public void setCandidates(Set<Candidate> candidates) {
		this.candidates = candidates;
	}

	private double out;
	private Set<Candidate> candidates;
	
	public CandidateGroup(double out, Set<Candidate> c) {
		this.out = out;
		this.candidates = c;
	}

}
