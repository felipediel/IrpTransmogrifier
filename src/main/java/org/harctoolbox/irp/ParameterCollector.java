/*
Copyright (C) 2016 Bengt Martensson.

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

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.harctoolbox.ircore.IncompatibleArgumentException;

public class ParameterCollector implements Cloneable {

    private final static Logger logger = Logger.getLogger(ParameterCollector.class.getName());
    private final static long ALLBITS = -1L;
    public final static long INVALID = -1L;

    private HashMap<String, Parameter> map;

    public ParameterCollector() {
        map = new LinkedHashMap<>(10);
    }

    public void add(String name, long value, long bitmask) throws NameConflictException {
        Parameter parameter = new Parameter(value, bitmask);
        Parameter oldParameter = map.get(name);
        if (oldParameter != null) {
            if (!oldParameter.isConsistent(parameter)) {
                logger.log(Level.FINE, "Name conflict: {0}", name);
                throw new NameConflictException(name);
            }
            oldParameter.complete(parameter);
        } else {
            map.put(name, parameter);
        }
    }

    public void add(String name, long value) throws NameConflictException {
        add(name, value, ALLBITS);
    }

    public long get(String name) {
        return map.containsKey(name) ? map.get(name).value : INVALID;
    }

    public void checkConsistencyWith(NameEngine nameEngine) throws NameConflictException, UnassignedException, IrpSyntaxException, IncompatibleArgumentException {
        NameEngine extended = nameEngine.clone();
        addToNameEngine(extended);
        for (Map.Entry<String, Parameter> kvp : map.entrySet()) {
            String name = kvp.getKey();
            Parameter parameter = kvp.getValue();
            if (!parameter.isConsistent(extended.get(name).toNumber(extended)))
                throw new NameConflictException(name);
        }
    }

    public NameEngine toNameEngine() throws IrpSyntaxException {
        NameEngine result = new NameEngine();
        for (Map.Entry<String, Parameter> kvp : map.entrySet()) {
            result.define(kvp.getKey(), kvp.getValue().value);
        }
        return result;
    }

    public HashMap<String, Long> toHashMap() {
        HashMap<String, Long> hashMap = new HashMap<>(map.size());

        map.entrySet().stream().forEach((kvp) -> {
            hashMap.put(kvp.getKey(), kvp.getValue().value);
        });
        return hashMap;
    }

    public void addToNameEngine(NameEngine nameEngine) throws IrpSyntaxException, NameConflictException {
        for (Map.Entry<String, Parameter> kvp : map.entrySet()) {
            String name = kvp.getKey();
            if (nameEngine.containsKey(name))
                throw new NameConflictException(name);
            else
               nameEngine.define(kvp.getKey(), kvp.getValue().value);
        }
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder(100);
        str.append("{");
        map.entrySet().stream().forEach((kvp) -> {
            if (str.length() > 1)
                str.append(";");
            str.append(kvp.getKey()).append("=").append(kvp.getValue().toString());
        });
        return str.append("}").toString();
    }

    @Override
    @SuppressWarnings("CloneDeclaresCloneNotSupported")
    public ParameterCollector clone() {
        ParameterCollector result;
        try {
            result = (ParameterCollector) super.clone();
        } catch (CloneNotSupportedException ex) {
            throw new InternalError(ex);
        }
        result.map = new LinkedHashMap<>(10);
        map.entrySet().stream().forEach((kvp) -> {
            result.map.put(kvp.getKey(), kvp.getValue().clone());
        });
        return result;
    }

    private static class Parameter implements Cloneable {
        long value;

        /**
         * 1 for bits known
         */
        long bitmask;

        Parameter(long value) {
            this(value, ALLBITS);
        }

        Parameter(long value, long bitmask) {
            this.value = value;
            this.bitmask = bitmask;
        }

        boolean isConsistent(Parameter parameter) {
            return ((value ^ parameter.value) & bitmask & parameter.bitmask) == 0L;
        }

        boolean isConsistent(long val) {
            return ((value ^ val) & bitmask) == 0L;
        }

        void complete(Parameter parameter) {
            value = (value & bitmask) | (parameter.value & parameter.bitmask);
            bitmask |= parameter.bitmask;
        }

        @Override
        public String toString() {
            return Long.toString(value) + "&" + Long.toBinaryString(bitmask);
        }

        @Override
        @SuppressWarnings("CloneDeclaresCloneNotSupported")
        public Parameter clone() {
            try {
                return (Parameter) super.clone();
            } catch (CloneNotSupportedException ex) {
                throw new InternalError(ex);
            }
        }
    }
}