/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.aksw.limes.core.ml.algorithm.euclid;

import org.aksw.limes.core.io.cache.Cache;
import org.aksw.limes.core.io.cache.Instance;
import org.aksw.limes.core.io.mapping.AMapping;
import org.aksw.limes.core.measures.measure.Measure;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Axel-C. Ngonga Ngomo (ngonga@informatik.uni-leipzig.de)
 */
public class LinearSelfConfigurator implements ISelfConfigurator {

    //	//execution mode. STRICT = true leads to a strong bias towards precision by
//	//ensuring that the initial classifiers are classifiers that have the
//	//maximal threshold that leads to the best pseudo-f-measure. False leads to the
//	// best classifier with the smallest threshold
//	public boolean STRICT = true;
//	public int ITERATIONS_MAX = 1000;
//	public double MIN_THRESHOLD = 0.3;
//	static Logger logger = LoggerFactory.getLogger("LIMES");
//
//	public enum Strategy {
//		FMEASURE, THRESHOLD, PRECISION, RECALL
//	};
//	Strategy strategy = Strategy.FMEASURE;
//	public Cache source; //source cache
//	public Cache target; //target cache
//	Map<String, Double> sourcePropertiesCoverageMap; //coverage map for latter computations
//	Map<String, Double> targetPropertiesCoverageMap;//coverage map for latter computations
//	Map<String, String> sourcePropertyTypeMap; //coverage map for latter computations
//	Map<String, String> targetPropertyTypeMap; //coverage map for latter computations
//	List<SimpleClassifier> buffer; //buffers learning results
//	double beta;
//	Map<String, String> measures;
//	public double learningRate = 0.25;
//	public double kappa = 0.6;
//	/* used to compute qualities for the unsupervised approach*/
//	private IQualitativeMeasure _measure;
//	String MEASURE = "own";
//	/* usupervised approaches need a reference mapping to compute qualities*/
//    boolean supervised = false;
//    Mapping reference = MappingFactory.createDefaultMapping(); // all true instance pairs.
//    public Mapping asked = MappingFactory.createDefaultMapping();// all known instance pairs.
//
//	/**
//	 * Constructor
//	 *
//	 * @param source Source cache
//	 * @param target Target cache
//	 * @param beta Beta value for computing F_beta
//	 * @param minCoverage Minimal coverage for a property to be considered for
//	 * linking
//	 *
//	 */
//	public LinearSelfConfigurator() {
//		if (MEASURE.equals("reference")) {
//			_measure = new PseudoRefFMeasure();
//		} else {
//			_measure =  EvaluatorFactory.create(EvaluatorType.PF_MEASURE);
//		}
//	}
//
//	public void setMeasure(IQualitativeMeasure measure) {
//		_measure = measure;
//	}
//
//	public LinearSelfConfigurator(Cache source, Cache target, double minCoverage, double beta) {
//		this.source = source;
//		this.target = target;
//		this.beta = beta;
//		sourcePropertiesCoverageMap = getPropertyStats(source, minCoverage);
//		targetPropertiesCoverageMap = getPropertyStats(target, minCoverage);
//		measures = new HashMap<String, String>();
//		measures.put("euclidean", "numeric");
//		measures.put("levenshtein", "string");
//		measures.put("jaccard", "string");
//		measures.put("trigrams", "string");
//
//		if (MEASURE.equals("reference")) {
//			_measure = new PseudoRefFMeasure();
//		} else {
//			_measure = new PseudoFMeasure();
//		}
//	}
//
//
//	public void computeMeasure(Cache source, Cache target, String[] parameters) {
//		throw new UnsupportedOperationException("Not supported yet.");
//	}
//
//	public String getMeasure() {
//		return _measure.getName();
//	}
//
//	public String getThreshold() {
//		throw new UnsupportedOperationException("Not supported yet.");
//	}
//
//	public Mapping getResults() {
//		throw new UnsupportedOperationException("Not supported yet.");
//	}
//
//	/**
//	 * Extracts all properties from a cache that have a coverage beyond
//	 * minCoverage TESTED
//	 *
//	 * @param c Input cache
//	 * @param minCoverage Threshold for coverage
//	 * @return Map of property to coverage
//	 */
    public static Map<String, Double> getPropertyStats(Cache c, double minCoverage) {
        Map<String, Double> buffer = new HashMap<String, Double>();
        Map<String, Double> result = new HashMap<String, Double>();

        //first count how often properties appear across instances
        for (Instance i : c.getAllInstances()) {
            for (String p : i.getAllProperties()) {
                if (!buffer.containsKey(p)) {
                    buffer.put(p, 1.0);
                } else {
                    buffer.put(p, buffer.get(p) + 1);
                }
            }
        }

        //then compute their coverage
        double total = (double) c.getAllInstances().size();
        double coverage;
        for (String p : buffer.keySet()) {
            coverage = (double) buffer.get(p) / total;
            if (coverage >= minCoverage) {
                result.put(p, coverage);
            }
        }
        return result;
    }

    @Override
    public void computeMeasure(Cache source, Cache target, String[] parameters) {
        // TODO Auto-generated method stub

    }

    @Override
    public String getMeasure() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setMeasure(Measure measure) {
        // TODO Auto-generated method stub

    }

    @Override
    public String getThreshold() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public AMapping getResults() {
        // TODO Auto-generated method stub
        return null;
    }
//
//	public static String getPropertyType(Cache c, String p) {
//		for (Instance i : c.getAllInstances()) {
//			if (i.getAllProperties().contains(p)) {
//				for (String value : i.getProperty(p)) {
//					if (!value.matches("[0-9]*\\.*[0-9]*")) {
//						return "string";
//					}
//				}
//			}
//		}
//		return "numeric";
//	}
//
//	/**
//	 * Computes all initial classifiers that compare properties whose coverage
//	 * is beyong the coverage threshold
//	 *
//	 * @return A map of sourceProperty -> targetProperty -> Classifier
//	 */
//	public List<SimpleClassifier> getAllInitialClassifiers() {
//		List<SimpleClassifier> initialClassifiers = new ArrayList<SimpleClassifier>();
//		for (String p : sourcePropertiesCoverageMap.keySet()) {
//			for (String q : sourcePropertiesCoverageMap.keySet()) {
//				SimpleClassifier cp = getInitialClassifier(p, q, "jaccard");
//				initialClassifiers.add(cp);
//			}
//		}
//		return initialClassifiers;
//	}
//
//	/**
//	 * Computes the best initial mapping for each source property
//	 *
//	 * @return List of classifiers that each contain the best initial mappings
//	 */
//	public List<SimpleClassifier> getBestInitialClassifiers() {
//		Set<String> measureList = new HashSet<String>();
//		measureList.add("jaccard");
//		//		measureList.add("levenshtein");
//		measureList.add("trigrams");
//		return getBestInitialClassifiers(measureList);
//	}
//
//	/**
//	 * Computes the best initial mapping for each source property
//	 * @param measureList Define Measures to be used by their name: eg. "jaccard", "levenshtein", "trigrams", "cosine", ...
//	 * @return List of classifiers that each contain the best initial mappings
//	 */
//	public List<SimpleClassifier> getBestInitialClassifiers(Set<String> measureList) {
//		List<SimpleClassifier> initialClassifiers = new ArrayList<SimpleClassifier>();
//		//        logger.info(sourcePropertiesCoverageMap);
//		//        logger.info(targetPropertiesCoverageMap);
//		for (String p : sourcePropertiesCoverageMap.keySet()) {
//			double fMeasure = 0;
//			SimpleClassifier bestClassifier = null;
//			//String bestProperty = "";
//			Map<String, SimpleClassifier> cp = new HashMap<String, SimpleClassifier>();
//			for (String q : targetPropertiesCoverageMap.keySet()) {
//				for (String measure : measureList) {
//					SimpleClassifier cps = getInitialClassifier(p, q, measure);
//					if (cps.fMeasure > fMeasure) {
//						bestClassifier = cps.clone();
//						//bestProperty = q;
//						fMeasure = cps.fMeasure;
//					}
//				}
//			}
//			if (bestClassifier != null) {
//				initialClassifiers.add(bestClassifier);
//			}
//		}
//		return initialClassifiers;
//	}
//	/**
//	 * Gets the best parameter to match the entities contained in the source and
//	 * target via properties p1 and p2 by the means of the similarity measure
//	 * "measure"
//	 *
//	 * @param sourceProperty Source property
//	 * @param targetProperty Target property
//	 * @param measure Similarity measure to be used
//	 * @return Maximal threshold that leads to maximal f-Measure (bias towards
//	 * precision)
//	 */
//	private SimpleClassifier getInitialClassifier(String sourceProperty, String targetProperty, String measure) {
//		double fMax = 0;
//		double theta = 1.0;
//		for (double threshold = 1; threshold > MIN_THRESHOLD; threshold = threshold - learningRate) {
//			Mapping mapping = execute(sourceProperty, targetProperty, measure, threshold);
//			//            double fMeasure = _measure.getPseudoFMeasure(source.getAllUris(), target.getAllUris(), mapping, beta);
//			double fMeasure = computeQuality(mapping);
//			//            System.out.println("Source: " + sourceProperty + ""
//			//                    + " Target: " + targetProperty + " Threshold " + threshold + " leads to F-Measure " + fMeasure);
//
//			if (STRICT) {
//				if (fMeasure > fMax) {
//					fMax = fMeasure;
//					theta = threshold;
//				}
//			} else {
//				if (fMeasure >= fMax) {
//					fMax = fMeasure;
//					theta = threshold;
//				}
//			}
//			if (fMeasure < fMax) {
//				break;
//			}
//		}
//
//		SimpleClassifier cp = new SimpleClassifier(measure, theta);
//		cp.fMeasure = fMax;
//		cp.sourceProperty = sourceProperty;
//		cp.targetProperty = targetProperty;
//		return cp;
//	}
//
//	/**
//	 * Runs classifiers and retrieves the corresponding mappings
//	 *
//	 * @param classifiers List of classifiers
//	 * @return Mapping generated by the list of classifiers
//	 */
//	public Mapping getMapping(List<SimpleClassifier> classifiers) {
//		classifiers = normalizeClassifiers(classifiers);
//		Map<SimpleClassifier, Mapping> mappings = new HashMap<SimpleClassifier, Mapping>();
//		for (int i = 0; i < classifiers.size(); i++) {
//			double threshold = 1 + classifiers.get(i).weight - (1 / kappa);
//			if (threshold > 0) {
//				Mapping m = executeClassifier(classifiers.get(i), threshold);
//				mappings.put(classifiers.get(i), m);
//			}
//		}
//		System.out.println(mappings);
//		return getOverallMapping(mappings, 1.0);
//	}
//
//	/**
//	 * Computes the weighted linear combination of the similarity computed by
//	 * the single classifiers
//	 *
//	 * @param mappings Maps classifiers to their results
//	 * @param threshold Similarity threshold for exclusion
//	 * @return Resulting overall mapping
//	 */
//	public Mapping getOverallMapping(Map<SimpleClassifier, Mapping> mappings, double threshold) {
//		if (mappings.isEmpty()) {
//			return MappingFactory.createDefaultMapping();
//		}
//		Mapping reference = mappings.get(mappings.keySet().iterator().next());
//		Mapping result = MappingFactory.createDefaultMapping();
//		for (String s : reference.getMap().keySet()) {
//			for (String t : reference.getMap().get(s).keySet()) {
//				double score = 0;
//				for (SimpleClassifier cp : mappings.keySet()) {
//					score = score + cp.weight * mappings.get(cp).getConfidence(s, t);
//				}
//				if (score >= threshold) {
//					result.add(s, t, score);
//				}
//			}
//		}
//		return result;
//	}
//
//	public List<SimpleClassifier> normalizeClassifiers(List<SimpleClassifier> classifiers) //weight classifier by f-measure. Assume that overall theta is 1
//	{
//		double total = 0;
//		for (SimpleClassifier cp : classifiers) {
//			total = total + cp.weight;
//		}
//		for (SimpleClassifier cp : classifiers) {
//			cp.weight = cp.weight / (total * kappa);
//		}
//		return classifiers;
//	}
//
//	/**
//	 * Updates the weights of the classifiers such that they map the initial
//	 * conditions for a classifier
//	 *
//	 * @param classifiers Input classifiers
//	 * @return Normed classifiers
//	 */
//	public List<SimpleClassifier> getInitialOverallClassifiers(List<SimpleClassifier> classifiers) {
//		//weight classifier by f-measure. Assume that overall theta is 1
//		if (strategy.equals(Strategy.FMEASURE)) {
//			double total = 0;
//			for (SimpleClassifier cp : classifiers) {
//				total = total + cp.fMeasure;
//			}
//			for (SimpleClassifier cp : classifiers) {
//				cp.weight = cp.fMeasure / (total * kappa);
//			}
//		}
//		return classifiers;
//	}
//
//	/**
//	 * Aims to improve upon a particular classifier by checking whether adding a
//	 * delta to its similarity worsens the total classifer
//	 *
//	 * @param mappings Current classifiers and their mappings
//	 * @param toImprove Classifier that is to be updated
//	 * @return Improved classifiers and their mapping
//	 */
//	public double computeNext(List<SimpleClassifier> classifiers, int index) {
//		classifiers.get(index).weight = classifiers.get(index).weight - learningRate;
//		classifiers = normalizeClassifiers(classifiers);
//		Mapping m = getMapping(classifiers);
//		buffer = classifiers;
//		//        return _measure.getPseudoFMeasure(source.getAllUris(), target.getAllUris(), m, beta);
//		return	computeQuality(m);
//	}
//
//	public List<SimpleClassifier> learnClassifer(List<SimpleClassifier> classifiers) {
//		classifiers = normalizeClassifiers(classifiers);
//		Mapping m = getMapping(classifiers);
//		//      double f = _measure.getPseudoFMeasure(source.getAllUris(), target.getAllUris(), m, beta);
//		double f = computeQuality(m);
//		// no need to update if the classifiers are already perfect
//		if (f == 1.0) {
//			return classifiers;
//		}
//		int dimensions = classifiers.size();
//		int direction = 0;
//		int iterations = 0;
//		List<SimpleClassifier> bestClassifiers = null;
//		double bestF = f;
//
//		while (iterations <= ITERATIONS_MAX) {
//			iterations++;
//			double fMeasure;
//			double index = -1;
//			//evaluate neighbors of current classifier
//			for (int i = 0; i < dimensions; i++) {
//				fMeasure = computeNext(classifiers, i);
//				if (fMeasure > bestF) {
//					bestF = fMeasure;
//					index = i;
//					bestClassifiers = buffer;
//				}
//			}
//			//nothing better found. simply march in the space in direction 
//			//"direction"
//			if (bestF == f) {
//				System.out.println(">>>> Walking along direction " + direction);
//				computeNext(classifiers, direction);
//				bestClassifiers = buffer;
//				direction++;
//				direction = direction % dimensions;
//			} //every classifier is worse
//			else if (bestF < f) {
//				return bestClassifiers;
//			} //found a better classifier
//			classifiers = bestClassifiers;
//			System.out.println(">> Iteration " + iterations + ": " + classifiers + " F-Measure = " + bestF);
//		}
//		return classifiers;
//	}
//
//
//	/**
//	 * Runs a classifier and get the mappings for it
//	 *
//	 * @param c Classifier
//	 * @param threshold Threshold for similarities
//	 * @return Corresponding mapping
//	 */
//	public Mapping executeClassifier(SimpleClassifier c, double threshold) {
//		return execute(c.sourceProperty, c.targetProperty, c.measure, threshold);
//	}
//
//	/**
//	 * Runs measure(sourceProperty, targetProperty) >= threshold
//	 *
//	 * @param sourceProperty Source property
//	 * @param targetProperty Target property
//	 * @param measure Similarity measure
//	 * @param threshold Similarity threshold
//	 * @return Correspoding Mapping
//	 */
//	public Mapping execute(String sourceProperty, String targetProperty, String measure, double threshold) {
//		String measureExpression = measure + "(x." + sourceProperty + ", y." + targetProperty + ")";
//		Instruction inst = new Instruction(Command.RUN, measureExpression, threshold + "", -1, -1, -1);
//		Plan p = new Plan();
//		p.addInstruction(inst);
//		ExecutionEngine ee = ExecutionEngineFactory.getEngine(ExecutionEngineType.DEFAULT, source, target, "?x", "?y");
//		return ee.execute(p);
//	}
//
//	/**
//	 * Gets the best target for each source and returns it
//	 *
//	 * @param m
//	 * @return
//	 */
//	public Mapping getBestOneToOneMapping(Mapping m) {
//		Mapping result = MappingFactory.createDefaultMapping();
//		for (String s : m.getMap().keySet()) {
//			double maxSim = 0;
//			Set<String> target = new HashSet<String>();;
//			for (String t : m.getMap().get(s).keySet()) {
//				if (m.getConfidence(s, t) == maxSim) {
//					target.add(t);
//				}
//				if (m.getConfidence(s, t) > maxSim) {
//					maxSim = m.getConfidence(s, t);
//					target = new HashSet<String>();
//					target.add(t);
//				}
//			}
//			for (String t : target) {
//				result.add(s, t, maxSim);
//			}
//		}
//		return result;
//	}
//	/**  
//	 * Set PFMs based upon name.
//	 * if name.equals("reference") using ReferencePseudoMeasures.class:  Nikolov/D'Aquin/Motta ESWC 2012.
//	 * @param m
//	 */
//	public void setMeasure(String m){	{
//		if (m.equals("reference")) {
//			_measure = new ReferencePseudoMeasures();
//		} else {
//			_measure = new PseudoMeasures();
//		}
//	}
//
//	/**
//	 * Method to compute quality of a mapping. Uses per default the specified PFM _measure.
//	 * TODO: active learning variant.
//	 * @param map
//	 * @return
//	 */
//	public Double computeQuality(Mapping map) {
//		if(!supervised) {
//			// default use pfm
//			return _measure.getPseudoFMeasure(source.getAllUris(), target.getAllUris(), map, beta);
//		} else {
//			//		  logger.info("Computing score of map based upon referenceMap");
//			return new FMeasure().calculate(minimizeToKnow(map), new GoldStandard(reference));
//		}
//	}
//
//	 /** Set caches to trimmed caches according to the given reference mapping.
//	   * @param reference
//	   */
//	  public void setSupervisedBatch(Mapping reference) {
//		 this.supervised = true;
//		 for(String sUri : reference.getMap().keySet()) {
//			 for(String tUri : reference.getMap().get(sUri).keySet()) {
//				 double sim = reference.getConfidence(sUri, tUri);
//				 if(sim > 0)
//					 this.reference.add(sUri, tUri, sim);
//				 this.asked.add(sUri, tUri, sim);
//			 }
//		 }
//	  }
//	  
//	  public void setSupervision(boolean supervised) {
//		  this.supervised = supervised;
//	  }
//	  
//	  public Mapping minimizeToKnow(Mapping map) {
//		  Mapping minimal = MappingFactory.createDefaultMapping();
//		  for(String sUri : map.getMap().keySet()) {
//			  for(String tUri : map.getMap().get(sUri).keySet()) {
//				  if(asked.getMap().containsKey(sUri)) {
//					  //is also tUri known?
//					  for(String knownSuri : asked.getMap().keySet())
//						  if(asked.getMap().get(knownSuri).containsKey(tUri))
//							  minimal.add(sUri, tUri, map.getConfidence(sUri, tUri));
//				  }
//			  }
//		  }
//		  return minimal;
//	  }
//
//	public Cache getSource() {
//		return source;
//	}
//
//	public void setSource(Cache source) {
//		this.source = source;
//	}
//
//	public Cache getTarget() {
//		return target;
//	}
//
//	public void setTarget(Cache target) {
//		this.target = target;
//	}


}