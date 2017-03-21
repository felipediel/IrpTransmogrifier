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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.harctoolbox.ircore.InvalidArgumentException;
import org.harctoolbox.ircore.IrSignal;
import org.harctoolbox.ircore.Pronto;
import org.harctoolbox.ircore.ThisCannotHappenException;
import org.xml.sax.SAXException;

public class Decoder {
    private static final Logger logger = Logger.getLogger(Decoder.class.getName());

    public static void main(String[] args) {
        ParameterSpec.initRandom(1);

        try {
            if (args.length == 0)
                decode("src/main/config/IrpProtocols.xml");
            else {
                IrSignal irSignal = Pronto.parse(args);
                IrpDatabase database = new IrpDatabase("src/main/config/IrpProtocols.xml");
                Decoder decoder = new Decoder(database);
                Map<String, Decode> decodes = decoder.decode(irSignal, true, true);
                decodes.entrySet().forEach((kvp) -> {
                    System.out.println(kvp);
                });
            }
        } catch (IOException | SAXException | IrpException | InvalidArgumentException ex) {
            Logger.getLogger(Decoder.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(1);
        }
    }

    public static void decode(String irpDatabasePath) throws IOException, SAXException, IrpException {
        IrpDatabase irp = new IrpDatabase(irpDatabasePath);
        irp.expand();
        Decoder decoder = new Decoder(irp);

        for (String protocolName : irp.getNames()) {
            NamedProtocol protocol = irp.getNamedProtocol(protocolName);
            if (protocol.isDecodeable()) {
                NameEngine nameEngine = new NameEngine(protocol.randomParameters());
                if (protocol.getName().equals("Velodyne"))
                    System.out.println(nameEngine);
                IrSignal irSignal = protocol.toIrSignal(nameEngine);
                Map<String, Decode> decodes = decoder.decode(irSignal, true, true);
                boolean success = false;
                for (Decode decode : decodes.values()) {
                    System.out.println(decode);
                    if (decode.same(protocolName, nameEngine)) {
                        success = true;
                        break;
                    }
                }
                if (!success) {
                    System.out.println(">>>>>>>>> " + protocol.getName() + "\t" + nameEngine.toString());
                }
            }
        }
    }

    private final Map<String, NamedProtocol> parsedProtocols;

    public Decoder(IrpDatabase irpDatabase) {
        this(irpDatabase, null);
    }

    /**
     * This is the main constructor.
     * @param irpDatabase will be expanded.
     * @param names If non-null and non-empty, include only the protocols with these names.
     */
    public Decoder(IrpDatabase irpDatabase, Collection<String> names) {
        try {
            irpDatabase.expand();
            this.parsedProtocols = new LinkedHashMap<>(irpDatabase.size());
            Collection<String> list = names != null ? names : irpDatabase.getNames();
            list.parallelStream().forEach((protocolName) -> {
                try {
                    NamedProtocol namedProtocol = irpDatabase.getNamedProtocol(protocolName);
                    if (namedProtocol.isDecodeable())
                        parsedProtocols.put(protocolName, namedProtocol);
                } catch (IrpException ex) {
                    throw new ThisCannotHappenException(ex);
                }
            });
        } catch (IrpSyntaxException ex) {
            // Only happens when there are errors in the Irp database
            throw new ThisCannotHappenException(ex);
        }
    }

    /**
     * Deliver a Map of Decodes
     * @param irSignal IrSignal to be decoded.
     * @param allDecodes If true, output all possible decodes. Otherwise, remove decodes according to prefer-over.
     * @param keepDefaultedParameters If false, remove parameters with value equals to their default.
     * @param frequencyTolerance
     * @param absoluteTolerance
     * @param relativeTolerance
     * @return Map of decodes with protocol name as key.
     */
    public Map<String, Decode> decode(IrSignal irSignal, boolean allDecodes, boolean keepDefaultedParameters,
            Double frequencyTolerance, Double absoluteTolerance, Double relativeTolerance) {
        Map<String, Decode> output = new HashMap<>(8);
        parsedProtocols.values().forEach((namedProtocol) -> {
            Map<String, Long> parameters;
            try {
                logger.log(Level.FINEST, "Trying protocol {0}", namedProtocol.getName());

                parameters = namedProtocol.recognize(irSignal, keepDefaultedParameters,
                        frequencyTolerance, absoluteTolerance, relativeTolerance);
                if (parameters == null)
                    throw new ThisCannotHappenException(namedProtocol.getName());
                output.put(namedProtocol.getName(), new Decode(namedProtocol, parameters));
            } catch (IrpSignalParseException | DomainViolationException | NameConflictException | UnassignedException | InvalidNameException | IrpSemanticException ex) {
                logger.log(Level.FINE, String.format("Protocol %1$s did not decode: %2$s", namedProtocol.getName(), ex.getMessage()));
            } catch (NamedProtocol.ProtocolNotDecodableException ex) {
            }
        });

        if (!allDecodes) {
            List<String> protocols = new ArrayList<>(output.keySet());
            protocols.forEach((name) -> {
                Decode decode = output.get(name);
                if (decode != null) {
                    NamedProtocol prot = output.get(name).getNamedProtocol();
                    if (prot != null) {
                        List<String> preferOvers = prot.getPreferOver();
                        if (preferOvers != null)
                            preferOvers.forEach((protName) -> {
                                output.remove(protName);
                            });
                    }
                }
            });
        }

        return output;
    }

    public Map<String, Decode> decode(IrSignal irSignal) {
        return decode(irSignal, false, false, null, null, null);
    }

    public Map<String, Decode> decode(IrSignal irSignal, boolean allDecodes, boolean keepDefaultedParameters) {
        return decode(irSignal, allDecodes, keepDefaultedParameters, null, null, null);
    }


    public static class Decode {
        private final NamedProtocol namedProtocol;
        private final NameEngine nameEngine;
        private final String notes;

        public Decode(NamedProtocol namedProtocol, Map<String, Long> map) {
            this(namedProtocol, new NameEngine(map), null);
        }

        public Decode(NamedProtocol namedProtocol, NameEngine nameEngine) {
            this(namedProtocol, nameEngine, null);
        }

        public Decode(NamedProtocol namedProtocol, NameEngine nameEngine, String notes) {
            this.namedProtocol = namedProtocol;
            this.nameEngine = nameEngine;
            this.notes = notes == null ? "" : notes;
        }

        public boolean same(String protocolName, NameEngine nameEngine) {
            return namedProtocol.getName().equalsIgnoreCase(protocolName)
                    && nameEngine.numericallyEquals(nameEngine);
        }

        @Override
        public String toString() {
            return namedProtocol.getName() + ": " + nameEngine.toString()
                    + (notes.isEmpty() ? "" : " (" + notes + ")");
        }

        /**
         * @return the namedProtocol
         */
        public NamedProtocol getNamedProtocol() {
            return namedProtocol;
        }

        /**
         * @return the nameEngine
         */
        public NameEngine getNameEngine() {
            return nameEngine;
        }

        /**
         * @return the notes
         */
        public String getNotes() {
            return notes;
        }

        public String getName() {
            return namedProtocol.getName();
        }
    }
}
