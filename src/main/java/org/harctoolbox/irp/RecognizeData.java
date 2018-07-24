/*
Copyright (C) 2017 Bengt Martensson.

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 3 of the License, or (at
your option) any later version.

This program is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public License along with
this program. If not, see http://www.gnu.org/licenses/.
*/

package org.harctoolbox.irp;

import org.harctoolbox.ircore.IrSequence;
import org.harctoolbox.ircore.ThisCannotHappenException;

public final class RecognizeData extends Traverser implements Cloneable {

    private int position;
    private double hasConsumed;
    private ParameterCollector parameterCollector;
    private final IrSequence irSequence;
    private int extentStart;
    private boolean interleaving;
    private ParameterCollector needsChecking;
    private final double absoluteTolerance;
    private final double relativeTolerance;
    private BitwiseParameter danglingBitFieldData;
    private final double minimumLeadout;

    public RecognizeData(GeneralSpec generalSpec, NameEngine definitions, IrSequence irSequence, boolean interleaving, ParameterCollector nameMap,
            double absoulteTolerance, double relativeTolerance, double minimumLeadout) {
        this(generalSpec, definitions, irSequence, 0, interleaving, nameMap, absoulteTolerance, relativeTolerance, minimumLeadout);
    }

    public RecognizeData(GeneralSpec generalSpec, NameEngine definitions, IrSequence irSequence, int position,
            boolean interleaving, ParameterCollector parameterCollector, double absoluteTolerance, double relativeTolerance, double minimumLeadout) {
        super(generalSpec, definitions);
        danglingBitFieldData = new BitwiseParameter();
        this.position = position;
        this.hasConsumed = 0.0;
        this.irSequence = irSequence;
        this.parameterCollector = parameterCollector;
        this.extentStart = 0;
        this.interleaving = interleaving;
        this.needsChecking = new ParameterCollector();
        this.absoluteTolerance = absoluteTolerance;
        this.relativeTolerance = relativeTolerance;
        this.minimumLeadout = minimumLeadout;
    }

    /**
     * Returns a shallow copy, except for the NameEngine, which is copied with NameEngine.clone().
     * @return
     */
    @Override
    @SuppressWarnings("CloneDeclaresCloneNotSupported")
    public RecognizeData clone() {
        RecognizeData result;
        try {
            result = (RecognizeData) super.clone();
        } catch (CloneNotSupportedException ex) {
            throw new InternalError(ex);
        }
        result.setParameterCollector(getParameterCollector().clone());
        result.nameEngine = this.nameEngine.clone();
        return result;
    }

    /**
     * @return the irSequence
     */
    public IrSequence getIrSequence() {
        return irSequence;
    }

    /**
     * @return the position
     */
    public int getPosition() {
        return position;
    }

    /**
     * @param position the position to set
     */
    public void setPosition(int position) {
        this.position = position;
    }

    /**
     * @return the parameterCollector
     */
    public ParameterCollector getParameterCollector() {
        return parameterCollector;
    }

    /**
     * @param parameterCollector the parameterCollector to set
     */
    public void setParameterCollector(ParameterCollector parameterCollector) {
        this.parameterCollector = parameterCollector;
    }

    void add(String name, BitwiseParameter parameter) throws ParameterInconsistencyException {
        if (getNameEngine().containsKey(name)) {
            Expression expression;
            try {
                expression = getNameEngine().get(name);
            } catch (NameUnassignedException ex) {
                throw new ThisCannotHappenException();
            }
            try {
                long expected = expression.toNumber(parameterCollector.toNameEngine());
                parameter.checkConsistency(name, expected);
            } catch (NameUnassignedException ex) {
                // It has an expression, but is not presently checkable.
                // mark for later checking.
                needsChecking.add(name, parameter);
            }
        } else
            parameterCollector.add(name, parameter);
    }

    void add(String name, long value, long bitmask) throws ParameterInconsistencyException {
        add(name, new BitwiseParameter(value, bitmask));
    }

    void add(String name, long value) throws ParameterInconsistencyException {
        add(name, new BitwiseParameter(value));
    }

    void incrementPosition(int i) {
        position += i;
    }

    public boolean isOn() {
        return Duration.isOn(position);
    }

    public double get() {
        return  Math.abs(irSequence.get(position)) - getHasConsumed();
    }

    public void consume() {
        position++;
        hasConsumed = 0;
    }

    public void consume(double amount) {
        hasConsumed += amount;
    }

    /**
     * @return the extentStart
     */
    public int getExtentStart() {
        return extentStart;
    }

    /**
     */
    public void markExtentStart() {
        extentStart = position + 1;
    }

    /**
     * @return the interleaving
     */
    public boolean isInterleaving() {
        return interleaving;
    }

    /**
     * @return the hasConsumed
     */
    public double getHasConsumed() {
        return hasConsumed;
    }

    /**
     * @param hasConsumed the hasConsumed to set
     */
    public void setHasConsumed(double hasConsumed) {
        this.hasConsumed = hasConsumed;
    }

    public boolean leadoutOk(boolean isLast) {
        return isLast && (get() >= minimumLeadout);
    }

    public boolean check(boolean on) {
        return isOn() == on
                && position < irSequence.getLength();
    }

    public double getExtentDuration() {
        int endPosition = Math.min(position + 1, irSequence.getLength());//IrCoreUtils.approximatelyEquals(hasConsumed, 0.0) ? position : position + 1;
        return irSequence.getTotalDuration(extentStart, endPosition - extentStart);
    }

    public boolean allowChopping() {
        return ! interleaving;
    }

    void checkConsistency() throws NameUnassignedException, ParameterInconsistencyException {
        NameEngine nameEngine = this.toNameEngine();
        needsChecking.checkConsistency(nameEngine, getNameEngine());
        needsChecking = new ParameterCollector();
    }

    /**
     * @return the absoluteTolerance
     */
    public double getAbsoluteTolerance() {
        return absoluteTolerance;
    }

    /**
     * @return the relativeTolerance
     */
    public double getRelativeTolerance() {
        return relativeTolerance;
    }

    NameEngine toNameEngine() {
        NameEngine nameEngine = getNameEngine().clone();
        nameEngine.add(parameterCollector.toNameEngine());
        return nameEngine;
    }

    /**
     * @return the danglingBitFieldData
     */
    BitwiseParameter getDanglingBitFieldData() {
        return danglingBitFieldData;
    }

    /**
     * @param data
     * @param bitmask
     */
    void setDanglingBitFieldData(long data, long bitmask) {
        danglingBitFieldData = new BitwiseParameter(data, bitmask);
    }

    void setDanglingBitFieldData() {
        danglingBitFieldData = new BitwiseParameter();
    }

    @Override
    public boolean isFinished() {
        return position == irSequence.getLength();
    }

    void finish() {
        if (hasConsumed > 0) {
            position++;
            hasConsumed = 0;
        }
    }
}
