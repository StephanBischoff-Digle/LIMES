package org.aksw.limes.core.measures.measure.temporal;

import org.aksw.limes.core.measures.measure.Measure;
/**
 * Implements the temporal measure abstract class.
 *
 * @author Kleanthi Georgala (georgala@informatik.uni-leipzig.de)
 * @version 1.0
 */
public abstract class TemporalMeasure extends Measure implements ITemporalMeasure {
    /**
     * Extract first property (beginDate) from metric expression.
     *
     * @param expression,
     *         metric expression
     * @return first property of metric expression as string
     */
    public String getFirstProperty(String expression) throws IllegalArgumentException {
        int plusIndex = expression.indexOf("|");
        if (expression.indexOf("|") != -1) {
            String p1 = expression.substring(0, plusIndex);
            return p1;
        } else
            return expression;
    }

    /**
     * Extract second property (endDate or machineID) from metric expression.
     *
     * @param expression,
     *         the metric expression
     * @return second property of metric expression as string
     */
    public String getSecondProperty(String expression) throws IllegalArgumentException {
        int plusIndex = expression.indexOf("|");
        if (expression.indexOf("|") != -1) {
            String p1 = expression.substring(plusIndex + 1, expression.length());
            return p1;
        } else
            throw new IllegalArgumentException();
    }
}