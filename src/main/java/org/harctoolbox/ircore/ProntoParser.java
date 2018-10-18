/*
Copyright (C) 2018 Bengt Martensson.

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

package org.harctoolbox.ircore;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ProntoParser extends RawParser implements IrSignalParser {

    private final static Logger logger = Logger.getLogger(ProntoParser.class.getName());

    public static MultiParser newProntoRawParser(String source) {
        List<IrSignalParser> parsers = new ArrayList<>(1);
        parsers.add(new ProntoParser(source));
        return new MultiParser(parsers, source);
    }

    public static MultiParser newProntoRawParser(Iterable<? extends CharSequence> args) {
        return newProntoRawParser(String.join(" ", args));
    }

    public ProntoParser(String source) {
        super(source);
    }

    public ProntoParser(Iterable<? extends CharSequence> args) {
        super(args);
    }

    /**
     * Tries to interpret the string argument as one of our known formats, and
     * return an IrSignal. First tries to interpret as Pronto. If this fails,
     * falls back to RawParser.toIrSignal().
     *
     * @param fallbackFrequency Modulation frequency to use, if it cannot be
     * inferred from the first parameter.
     * @param dummyGap
     * @return IrSignal, or null on failure.
     * @throws org.harctoolbox.ircore.InvalidArgumentException
     */
    @Override
    public IrSignal toIrSignal(Double fallbackFrequency, Double dummyGap) throws InvalidArgumentException {
        try {
            IrSignal irSignal = Pronto.parse(getSource());
            // If Pronto.NonProntoFormatException is not thrown, the signal is probably
            // an erroneous Pronto wannabe, do not catch other exceptions than Pronto.NonProntoFormatException
            if (fallbackFrequency != null)
                logger.log(Level.WARNING, "Explicit frequency with a Pronto type signal meaningless, thus ignored.");
            return irSignal;
        } catch (Pronto.NonProntoFormatException ex) {
            // Signal does not look like Pronto, give up
            logger.log(Level.FINER, "Tried as Pronto, gave up");
            return null;
        }
    }

    @Override
    public String getName() {
        return "Pronto Hex";
    }
}