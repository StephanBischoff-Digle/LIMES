package org.aksw.limes.core.ml.algorithm;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import org.aksw.limes.core.evaluation.qualititativeMeasures.FMeasure;
import org.aksw.limes.core.evaluation.qualititativeMeasures.IQualitativeMeasure;
import org.aksw.limes.core.evaluation.qualititativeMeasures.PseudoFMeasure;
import org.aksw.limes.core.exceptions.NotYetImplementedException;
import org.aksw.limes.core.exceptions.UnsupportedMLImplementationException;
import org.aksw.limes.core.io.cache.Cache;
import org.aksw.limes.core.io.ls.LinkSpecification;
import org.aksw.limes.core.io.mapping.AMapping;
import org.aksw.limes.core.io.mapping.MappingFactory;
import org.aksw.limes.core.ml.algorithm.eagle.core.ALDecider;
import org.aksw.limes.core.ml.algorithm.eagle.core.ExpressionFitnessFunction;
import org.aksw.limes.core.ml.algorithm.eagle.core.ExpressionProblem;
import org.aksw.limes.core.ml.algorithm.eagle.core.IGPFitnessFunction;
import org.aksw.limes.core.ml.algorithm.eagle.core.LinkSpecGeneticLearnerConfig;
import org.aksw.limes.core.ml.algorithm.eagle.core.PseudoFMeasureFitnessFunction;
import org.aksw.limes.core.ml.algorithm.eagle.util.PropertyMapping;
import org.aksw.limes.core.ml.algorithm.eagle.util.TerminationCriteria;
import org.aksw.limes.core.ml.setting.LearningParameter;
import org.apache.log4j.Logger;
import org.jgap.InvalidConfigurationException;
import org.jgap.gp.GPProblem;
import org.jgap.gp.IGPProgram;
import org.jgap.gp.impl.GPGenotype;
import org.jgap.gp.impl.GPPopulation;
import org.jgap.gp.impl.ProgramChromosome;

/**
 * @author Tommaso Soru <tsoru@informatik.uni-leipzig.de>
 *
 */
public class Eagle extends ACoreMLAlgorithm {
	
	//======================= COMMON VARIABLES ======================
    private IGPProgram allBest = null;
    private IGPFitnessFunction fitness;
    private GPGenotype gp;
	
	//================ SUPERVISED-LEARNING VARIABLES ================
    private int turn = 0;
    private List<IGPProgram> bestSolutions = new LinkedList<IGPProgram>();
    private ALDecider alDecider = new ALDecider();

    //=============== UNSUPERVISED-LEARNING VARIABLES ===============
    private List<LinkSpecification> specifications;

    //======================= PARAMETER NAMES =======================
	
    protected static final String ALGORITHM_NAME = "Eagle";
    
    public static final String GENERATIONS = "generations";
    public static final String PRESERVE_FITTEST = "preserve_fittest";
    public static final String MAX_DURATION = "max_duration";
    public static final String INQUIRY_SIZE = "inquiry_size";
    public static final String MAX_ITERATIONS = "max_iterations";
    public static final String MAX_QUALITY = "max_quality";
    public static final String TERMINATION_CRITERIA = "termination_criteria";
    public static final String TERMINATION_CRITERIA_VALUE = "termination_criteria_value";
    public static final String BETA = "beta";
    public static final String POPULATION = "population";
    public static final String MUTATION_RATE = "mutation_rate";
    public static final String REPRODUCTION_RATE = "reproduction_rate";
    public static final String CROSSOVER_RATE = "crossover_rate";
    public static final String PSEUDO_FMEASURE = "pseudo_fmeasure";
    
    protected static final String GAMMA_SCORE = "gamma_score";
    protected static final String EXPANSION_PENALTY = "expansion_penalty";
    protected static final String REWARD = "reward";
    protected static final String PRUNE = "prune";

    public static final String MEASURE = "measure";
    public static final String PROPERTY_MAPPING = "property_mapping";
    
    
    // ========================================================================
    
    
    protected static Logger logger = Logger.getLogger(Eagle.class);
    
    protected Eagle() {
    	super();
    	setDefaultParameters();
    }

    @Override
    protected String getName() {
        return ALGORITHM_NAME;
    }

    @Override
    protected void init(List<LearningParameter> lp, Cache source, Cache target) {
        super.init(lp, source, target);
        this.turn = 0;
        this.bestSolutions = new LinkedList<IGPProgram>();
    }
    
	@Override
    protected MLResults learn(AMapping trainingData) {
		
		try {
			setUp(trainingData);
		} catch (InvalidConfigurationException e) {
			logger.error(e.getMessage());
			return null;
		}
    	
    	turn++;
        fitness.addToReference(extractPositiveMatches(trainingData));
        fitness.fillCachesIncrementally(trainingData);

        Integer nGen = (Integer) getParameter(GENERATIONS);
        
        for (int gen = 1; gen <= nGen; gen++) {
            gp.evolve();
            bestSolutions.add(determineFittest(gp, gen));
        }

        MLResults result = createSupervisedResult();
        return result;
        
    }

    @Override
    protected MLResults learn(PseudoFMeasure pfm) {

    	parameters.add(new LearningParameter(PSEUDO_FMEASURE, pfm, PseudoFMeasure.class, 
    			Double.NaN, Double.NaN, Double.NaN, PSEUDO_FMEASURE));
    	
		try {
			setUp(null);
		} catch (InvalidConfigurationException e) {
			logger.error(e.getMessage());
			return null;
		}
		
		Integer nGen = (Integer) getParameter(GENERATIONS);
		
        specifications = new LinkedList<LinkSpecification>();
        logger.info("Start learning");
        for (int gen = 1; gen <= nGen; gen++) {
            gp.evolve();
            IGPProgram currentBest = determineFittestUnsup(gp, gen);
            LinkSpecification currentBestMetric = getLinkSpecification(currentBest);
            //TODO: save the best LS of each generation
            specifications.add(currentBestMetric);
        }

        allBest = determineFittestUnsup(gp, nGen);
        return createUnsupervisedResult();
        
    }

    @Override
    protected AMapping predict(Cache source, Cache target, MLResults mlModel) {
        if (allBest != null)
            return fitness.getMapping(mlModel.getLinkSpecification(), true);
        logger.error("No link specification calculated so far.");
        return MappingFactory.createDefaultMapping();
    }

    @Override
    protected boolean supports(MLImplementationType mlType) {
        return mlType == MLImplementationType.SUPERVISED_BATCH || mlType == MLImplementationType.UNSUPERVISED;
    }

    @Override
    protected AMapping getNextExamples(int size) throws UnsupportedMLImplementationException {
        throw new UnsupportedMLImplementationException(this.getName());
    }

    @Override
    protected MLResults activeLearn(AMapping oracleMapping) throws UnsupportedMLImplementationException {
        throw new UnsupportedMLImplementationException(this.getName());
    }

    @Override
    public void setDefaultParameters() {
        
    	parameters.add(new LearningParameter(GENERATIONS, 10, Integer.class, 1, Integer.MAX_VALUE, 1, GENERATIONS));
    	parameters.add(new LearningParameter(PRESERVE_FITTEST, true, Boolean.class, Double.NaN, Double.NaN, Double.NaN, PRESERVE_FITTEST));
    	parameters.add(new LearningParameter(MAX_DURATION, 60, Long.class, 0, Long.MAX_VALUE, 1, MAX_DURATION));
    	parameters.add(new LearningParameter(INQUIRY_SIZE, 10, Integer.class, 1, Integer.MAX_VALUE, 1, INQUIRY_SIZE));
    	parameters.add(new LearningParameter(MAX_ITERATIONS, 500, Integer.class, 1, Integer.MAX_VALUE, 1, MAX_ITERATIONS));
    	parameters.add(new LearningParameter(MAX_QUALITY, 0.5, Double.class, 0d, 1d, Double.NaN, MAX_QUALITY));
    	parameters.add(new LearningParameter(TERMINATION_CRITERIA, TerminationCriteria.iteration, TerminationCriteria.class, Double.NaN, Double.NaN, Double.NaN, TERMINATION_CRITERIA));
    	parameters.add(new LearningParameter(TERMINATION_CRITERIA_VALUE, 0, Double.class, 0d, Double.MAX_VALUE, Double.NaN, TERMINATION_CRITERIA_VALUE));
    	parameters.add(new LearningParameter(BETA, 1.0, Double.class, 0d, 1d, Double.NaN, BETA));
    	parameters.add(new LearningParameter(POPULATION, 20, Integer.class, 1, Integer.MAX_VALUE, 1, POPULATION));
    	parameters.add(new LearningParameter(MUTATION_RATE, 0.4f, Float.class, 0f, 1f, Double.NaN, MUTATION_RATE));
    	parameters.add(new LearningParameter(REPRODUCTION_RATE, 0.4f, Float.class, 0f, 1f, Double.NaN, REPRODUCTION_RATE));
    	parameters.add(new LearningParameter(CROSSOVER_RATE, 0.3f, Float.class, 0f, 1f, Double.NaN, CROSSOVER_RATE));
    	parameters.add(new LearningParameter(MEASURE, new FMeasure(), IQualitativeMeasure.class, Double.NaN, Double.NaN, Double.NaN, MEASURE));
    	parameters.add(new LearningParameter(PROPERTY_MAPPING, new PropertyMapping(), PropertyMapping.class, Double.NaN, Double.NaN, Double.NaN, PROPERTY_MAPPING));
    	
    	// LION parameters (?)
    	parameters.add(new LearningParameter(GAMMA_SCORE, 0.15d, Double.class, 0d, Double.MAX_VALUE, Double.NaN, GAMMA_SCORE));
    	parameters.add(new LearningParameter(EXPANSION_PENALTY, 0.7d, Double.class, 0d, Double.MAX_VALUE, Double.NaN, EXPANSION_PENALTY));
    	parameters.add(new LearningParameter(REWARD, 1.2, Double.class, 0d, Double.MAX_VALUE, Double.NaN, REWARD));
    	parameters.add(new LearningParameter(PRUNE, true, Boolean.class, Double.NaN, Double.NaN, Double.NaN, PRUNE));
    	
        
    }

    @Override
    protected MLResults activeLearn() throws UnsupportedMLImplementationException {
        throw new UnsupportedMLImplementationException(this.getName());
    }
    
    //====================== SPECIFIC METHODS =======================
    
    /**
     * Configures EAGLE.
     * @throws InvalidConfigurationException 
     *
     */
    private void setUp(AMapping trainingData) throws InvalidConfigurationException {
    	
    	logger.info("Setting up EAGLE...");
    	
    	PropertyMapping pm = (PropertyMapping) getParameter(PROPERTY_MAPPING);
    	
        LinkSpecGeneticLearnerConfig jgapConfig = new LinkSpecGeneticLearnerConfig(getConfiguration().getSourceInfo(), getConfiguration().getTargetInfo(), pm);

        jgapConfig.sC = sourceCache;
        jgapConfig.tC = targetCache;
        
        jgapConfig.setPopulationSize((Integer) getParameter(POPULATION));
        jgapConfig.setCrossoverProb((Float) getParameter(CROSSOVER_RATE));
        jgapConfig.setMutationProb((Float) getParameter(MUTATION_RATE));
        jgapConfig.setPreservFittestIndividual((Boolean) getParameter(PRESERVE_FITTEST));
        jgapConfig.setReproductionProb((Float) getParameter(REPRODUCTION_RATE));
        jgapConfig.setPropertyMapping(pm);

        if(trainingData != null) { // supervised
        	
        	FMeasure fm = (FMeasure) getParameter(MEASURE);
        	fitness = ExpressionFitnessFunction.getInstance(jgapConfig, fm, trainingData);
        	org.jgap.Configuration.reset();
        	jgapConfig.setFitnessFunction(fitness);
        	
        } else { // unsupervised
        	
        	PseudoFMeasure pfm = (PseudoFMeasure) getParameter(PSEUDO_FMEASURE);
        	fitness = PseudoFMeasureFitnessFunction.getInstance(jgapConfig, pfm, sourceCache, targetCache);
        	org.jgap.Configuration.reset();
        	jgapConfig.setFitnessFunction(fitness);
        	
        }
        

        GPProblem gpP;

        gpP = new ExpressionProblem(jgapConfig);
        gp = gpP.create();
    }


    /**
     * Returns only positive matches, that are those with a confidence higher then 0.
     *
     * @param trainingData
     * @return
     */
    private AMapping extractPositiveMatches(AMapping trainingData) {
        AMapping positives = MappingFactory.createDefaultMapping();
        for (String sUri : trainingData.getMap().keySet())
            for (String tUri : trainingData.getMap().get(sUri).keySet()) {
                double confidence = trainingData.getConfidence(sUri, tUri);
                if (confidence > 0)
                    positives.add(sUri, tUri, confidence);
            }
        return positives;
    }

    /**
     * Method to compute best individuals by hand.
     *
     * @param gp
     * @param gen
     * @return
     */
    private IGPProgram determineFittest(GPGenotype gp, int gen) {

        GPPopulation pop = gp.getGPPopulation();
        pop.sortByFitness();

        IGPProgram bests[] = {gp.getFittestProgramComputed(), pop.determineFittestProgram(),
                // gp.getAllTimeBest(),
                pop.getGPProgram(0),};
        IGPProgram bestHere = null;
        double fittest = Double.MAX_VALUE;

        for (IGPProgram p : bests) {
            if (p != null) {
                double fitM = fitness.calculateRawFitness(p);
                if (fitM < fittest) {
                    fittest = fitM;
                    bestHere = p;
                }
            }
        }
        /* consider population if necessary */
        if (bestHere == null) {
            logger.debug("Determining best program failed, consider the whole population");
            System.err.println("Determining best program failed, consider the whole population");
            for (IGPProgram p : pop.getGPPrograms()) {
                if (p != null) {
                    double fitM = fitness.calculateRawFitness(p);
                    if (fitM < fittest) {
                        fittest = fitM;
                        bestHere = p;
                    }
                }
            }
        }

        if ((Boolean) getParameter(PRESERVE_FITTEST)) {
            if (allBest == null || fitness.calculateRawFitness(allBest) > fittest) {
                allBest = bestHere;
                logger.info("Generation " + gen + " new fittest (" + fittest + ") individual: " + getLinkSpecification(bestHere));
            }
        }

        return bestHere;
    }

    /**
     * Computes for a given jgap Program its corresponding link specification.
     *
     * @param p
     * @return
     */
    private LinkSpecification getLinkSpecification(IGPProgram p) {
        Object[] args = {};
        ProgramChromosome pc = p.getChromosome(0);
        return (LinkSpecification) pc.getNode(0).execute_object(pc, 0, args);
    }

    private MLResults createSupervisedResult() {
        MLResults result = new MLResults();
        result.setLinkSpecification(getLinkSpecification(allBest));
        
        // TODO I don't know why this is turned off...
//		result.setMapping(fitness.getMapping(getLinkSpecification(allBest), true));
        
        result.setQuality(allBest.getFitnessValue());
        result.addDetail("specifiactions", bestSolutions);
        result.addDetail("controversyMatches", calculateOracleQuestions((Integer) getParameter(INQUIRY_SIZE)));
        return result;
    }
    
    /**
     * Constructs the MLResult for this run.
     *
     * @return
     */
    private MLResults createUnsupervisedResult() {
        MLResults result = new MLResults();
        result.setLinkSpecification(getLinkSpecification(allBest));
//		result.setMapping(fitness.calculateMapping(allBest));
        result.setQuality(allBest.getFitnessValue());
        result.addDetail("specifiactions", specifications);
        return result;
    }


    private AMapping calculateOracleQuestions(int size) {
        // first get all Mappings for the current population
        logger.info("Getting mappings for output");
        GPPopulation pop = this.gp.getGPPopulation();
        pop.sortByFitness();
        HashSet<LinkSpecification> metrics = new HashSet<LinkSpecification>();
        List<AMapping> candidateMaps = new LinkedList<AMapping>();
        // and add the all time best

        metrics.add(getLinkSpecification(allBest));

        for (IGPProgram p : pop.getGPPrograms()) {
            LinkSpecification m = getLinkSpecification(p);
            if (m != null && !metrics.contains(m)) {
                //logger.info("Adding metric "+m);
                metrics.add(m);
            }
        }
        // fallback solution if we have too less candidates
        if (metrics.size() <= 1) {
            // TODO implement
        	throw new NotYetImplementedException("Fallback solution if we have too less candidates.");
        }

        // get mappings for all distinct metrics
        logger.info("Getting " + metrics.size() + " full mappings to determine controversy matches...");
        for (LinkSpecification m : metrics) {
            candidateMaps.add(fitness.getMapping(m, true));
        }
        // get most controversy matches
        logger.info("Getting " + size + " controversy match candidates from " + candidateMaps.size() + " maps...");
        ;
        List<ALDecider.Triple> controversyMatches = alDecider.getControversyCandidates(candidateMaps, size);
        // construct answer
        AMapping answer = MappingFactory.createDefaultMapping();
        for (ALDecider.Triple t : controversyMatches) {
            answer.add(t.getSourceUri(), t.getTargetUri(), t.getSimilarity());
        }
        return answer;
    }
    
    
    /**
     * Method to compute best individuals by hand.
     *
     * @param gp
     * @param gen
     * @return
     */
    private IGPProgram determineFittestUnsup(GPGenotype gp, int gen) {

        GPPopulation pop = gp.getGPPopulation();
        pop.sortByFitness();

        IGPProgram bests[] = {gp.getFittestProgramComputed(), pop.determineFittestProgram(),
                // gp.getAllTimeBest(),
                pop.getGPProgram(0),};
        IGPProgram bestHere = null;
        double fittest = Double.MAX_VALUE;

        for (IGPProgram p : bests) {
            if (p != null) {
                double fitM = fitness.calculateRawFitness(p);
                if (fitM < fittest) {
                    fittest = fitM;
                    bestHere = p;
                }
            }
        }
        /* consider population if neccessary */
        if (bestHere == null) {
            logger.debug("Determining best program failed, consider the whole population.");
            for (IGPProgram p : pop.getGPPrograms()) {
                if (p != null) {
                    double fitM = fitness.calculateRawFitness(p);
                    if (fitM < fittest) {
                        fittest = fitM;
                        bestHere = p;
                    }
                }
            }
        }

        if ((Boolean) getParameter(PRESERVE_FITTEST)) {
            if (allBest == null || fitness.calculateRawFitness(allBest) > fittest) {
                allBest = bestHere;
                logger.info("Generation " + gen + " new fittest (" + fittest + ") individual: " + getLinkSpecification(bestHere));
            }
        }

        return bestHere;
    }

	public int getTurn() {
		return turn;
	}



}
