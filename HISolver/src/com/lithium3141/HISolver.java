package com.lithium3141;

import java.util.ArrayList;
import java.util.List;

public class HISolver {
	
	public static int countOccurrencesAt(int snp, int pos, List<? extends SNPSequence> sequences) {
		if(sequences.size() == 0) {
			return 0;
		}
		
		int count = 0;
		for(SNPSequence seq : sequences) {
			if(seq.snps[pos] == snp) {
				count++;
			}
		}
		return count;
	}
	
	public static int countOccurrences(int snp, List<? extends SNPSequence> sequences) {
		if(sequences.size() == 0) {
			return 0;
		}
		
		int count = 0;
		for(SNPSequence seq : sequences) {
			for(int i = 0; i < seq.snps.length; i++) {
				if(seq.snps[i] == snp) {
					count++;
				}
			}
		}
		return count;
	}
	
	/**
	 * Find the haplotype which is likely to have the most in common
	 * with all the provided genotypes.
	 * 
	 * @param genotypes List of genotypes to check
	 * @return The most common haplotype
	 */
	public static Haplotype mostCommon(List<Genotype> genotypes) throws Exception {
		Haplotype best = null;
		
		if(genotypes.size() != 0) {
			int[] snps = new int[genotypes.get(0).snps.length];
			
			for(Genotype genotype : genotypes) {
				for(int i = 0; i < genotype.snps.length; i++) {
					snps[i] += genotype.snps[i];
				}
			}
			
			for(int i = 0; i < snps.length; i++) {
				if(snps[i] == 0) {
					snps[i] = 1; // Right choice?
				}
			}
			best = new Haplotype(snps);
		}
		
		return best;
	}

	/**
	 * Find the number of the given genotypes that can be generated by the
	 * given haplotypes
	 * @param haplotypes The set of haplotypes to check
	 * @param genotypes The set of genotypes to check
	 * @return The number of genotypes that can be generated by haplotypes
	 */
	public static int countGenerated(List<Haplotype> haplotypes, List<Genotype> genotypes) {
		int generates = 0;
		for(Genotype genotype : genotypes) {
			if(generates(haplotypes, genotype)) {
				generates++;
			}
		}
		return generates;
	}
	
	/**
	 * Find whether the given set of haplotypes can create the given 
	 * genotype by brute force.
	 * @param haplotypes The set of haplotypes to check
	 * @param genotypes The genotype to check
	 * @return Whether haplotypes can generate genotype
	 */
	public static boolean generates(List<Haplotype> haplotypes, Genotype genotype) {
		for(int i = 0; i < haplotypes.size(); i++) {
			for(int j = i + 1; j < haplotypes.size(); j++) {
				//System.out.println("generatesAll: checking " + haplotypes.get(i) + " + " + haplotypes.get(j) + " == " + genotype);
				if(haplotypes.get(i).combineWith(haplotypes.get(j)).equals(genotype)) {
					return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * Find whether the given set of haplotypes can create the given set
	 * of genotypes by brute force.
	 * @param haplotypes The set of haplotypes to check
	 * @param genotypes The set of genotypes to check
	 * @return Whether haplotypes can generate genotypes
	 */
	public static boolean generatesAll(List<Haplotype> haplotypes, List<Genotype> genotypes) {
		for(Genotype genotype : genotypes) {
			if(!generates(haplotypes, genotype)) {
				return false;
			}
		}
		return true;
	}
	
	public static void main(String[] args) throws Exception {
		if(args.length != 1) {
			System.err.println("Please provide an input file");
			System.exit(1);
		}
		
		HIDataReader dr = new HIDataReader(args[0]);
		List<Genotype> genotypes = dr.readData();
		
		Haplotype trent = mostCommon(genotypes);
		System.out.println("Initial common haplotype: " + trent);
		
		List<Haplotype> haplotypes = new ArrayList<Haplotype>();
		haplotypes.add(trent);
		
		int iterations = 0;
		int generated = 0;
		int lastGenerated = -1;
		while(!generatesAll(haplotypes, genotypes)) {
			// Build out the set of haplotypes from existing parents
			while(lastGenerated != generated) {
				for(Genotype genotype : genotypes) {
					if(!generates(haplotypes, genotype)) {
						Haplotype toAdd = null;
						inner:
							for(Haplotype haplotype : haplotypes) {
								toAdd = genotype.parentPairOf(haplotype);
								if(toAdd != null) {
									break inner;
								}
							}
						if(toAdd != null) {
							haplotypes.add(toAdd);
						}
					}
				}
				iterations++;
				lastGenerated = generated;
				generated = countGenerated(haplotypes, genotypes);
			}
			
			if(generated != genotypes.size()) {
				// Can't go further - find the next Trent
				List<Genotype> ungenerated = new ArrayList<Genotype>();
				for(Genotype genotype : genotypes) {
					if(!generates(haplotypes, genotype)) {
						ungenerated.add(genotype);
					}
				}
				Haplotype anotherTrent = mostCommon(ungenerated);
				haplotypes.add(anotherTrent);
				lastGenerated = -1;
				System.out.println("Adding another common haplotype (from " + generated + " generated): " + anotherTrent);
			}
		}
		
		System.out.println("After " + iterations + " run(s), haplotype list has " + haplotypes.size() + " entries and generates " + generated + " genotypes; still missing " + (genotypes.size() - generated) + " genotypes");
	}

}
