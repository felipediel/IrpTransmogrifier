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

import com.beust.jcommander.IStringConverter;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.Parameters;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import org.antlr.v4.gui.TreeViewer;
import org.antlr.v4.runtime.ParserRuleContext;
import org.harctoolbox.ircore.IncompatibleArgumentException;
import org.harctoolbox.ircore.XmlUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 * This class contains a command line main routine, allowing command line access to most things in the package.
 */
public class IrpTransmogrifier {
    private static final Logger logger = Logger.getLogger(IrpDatabase.class.getName());
    private static JCommander argumentParser;
    private static CommandLineArgs commandLineArgs = new CommandLineArgs();

    private static void usage(int exitcode) {
        StringBuilder str = new StringBuilder();
        argumentParser.usage(str);

        //str.append("\n"
        //        + "TODO");

        (exitcode == IrpUtils.exitSuccess ? System.out : System.err).println(str);
        doExit(exitcode);
    }

    private static void doExit(int exitcode) {
        System.exit(exitcode);
    }

    private static void doList(IrpDatabase irpDatabase, CommandList commandList) throws IrpSyntaxException, IrpSemanticException, ArithmeticException, IncompatibleArgumentException, InvalidRepeatException, UnknownProtocolException {
        List<String> list = commandList.protocols == null ? new ArrayList<>(irpDatabase.getNames())
                : commandList.regexp ? irpDatabase.getMatchingNames(commandList.protocols)
                : commandList.protocols;
        if (commandList.sort)
            Collections.sort(list);

        for (String proto : list) {
            if (!irpDatabase.isKnown(proto))
                throw new UnknownProtocolException(proto);

            System.out.println(proto);

            if (commandList.irp)
                System.out.println(irpDatabase.getIrp(proto));
            if (commandList.documentation)
                System.out.println(irpDatabase.getDocumentation(proto));
            if (commandList.stringTree) {
                Protocol protocol = new Protocol(irpDatabase.getIrp(proto));
                System.out.println(protocol.toStringTree());
            }
            if (commandList.is) {
                Protocol protocol = new Protocol(irpDatabase.getIrp(proto));
                System.out.println(protocol.toIrpString());
            }
            if (commandList.gui) {
                IrpParser parser = new ParserDriver(irpDatabase.getIrp(proto)).getParser();
                //parser = new ParserDriver(irpDatabase.getIrp(proto)).getParser();
                Protocol protocol = new Protocol(parser.protocol());
                showTreeViewer(parser, protocol.getParseTree(), "Parse tree for " + proto);
            }
            if (commandList.parse)
                try {
                    new Protocol(irpDatabase.getIrp(proto));
                    System.out.println("Parsing succeeded");
                } catch (IrpSyntaxException ex) {
                    logger.log(Level.WARNING, "Unparsable protocol {0}", proto);
                }
        }
    }

    private static void doCode(IrpDatabase irpDatabase, CommandCode commandCode) throws IrpSyntaxException, IrpSemanticException, ArithmeticException, IncompatibleArgumentException, InvalidRepeatException, UnknownProtocolException, FileNotFoundException, IOException, SAXException {
        PrintStream out = commandCode.output != null
                ? IrpUtils.getPrintSteam(commandCode.output)
                : System.out;
        for (String proto : commandCode.protocols) {
            NamedProtocol protocol = irpDatabase.getNamedProtocol(proto);
            if (commandCode.irp)
                out.println(protocol.getIrp());
            if (commandCode.documentation)
                out.println(protocol.getDocumentation());
            if (commandCode.xml) {
                Document doc = protocol.toDocument();
                XmlUtils.printDOM(out, doc, commandCode.encoding, "irp documentation");
            }
            if (commandCode.xslt != null) {
                Document doc = protocol.toDocument();
                CodeGenerator codeGenerator = new CodeGenerator(doc);
                Document stylesheet = XmlUtils.openXmlFile(new File(commandCode.xslt));
                codeGenerator.printDOM(out, stylesheet, commandCode.encoding);
           }
        }
    }

    private static void doRender(IrpDatabase irpDatabase, CommandRender commandRenderer) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private static void doRecognize(IrpDatabase irpDatabase, CommandRecognize commandRecognize) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private static void doExpression(CommandExpression commandExpression) throws IrpSyntaxException, UnassignedException, IncompatibleArgumentException {
        String nameEngineString = commandExpression.nameEngine;
        if (!nameEngineString.startsWith("{"))
            nameEngineString = "{" + nameEngineString;
        if (!nameEngineString.endsWith("{"))
            nameEngineString += "}";
        NameEngine nameEngine = new NameEngine(nameEngineString);
        for (String text : commandExpression.expressions) {

            IrpParser parser = new ParserDriver(text).getParser();
            Expression expression = new Expression(parser.expression());
//            if (!parser.isMatchedEOF()) {
//                System.err.println("WARNING: Did not match all input");
//                //System.exit(IrpUtils.exitFatalProgramFailure);
//            }

            long result = expression.toNumber(nameEngine);
            System.out.println(result);
            if (commandExpression.stringTree)
                System.out.println(expression.getParseTree().toStringTree(parser));
            if (commandExpression.xml) {
                Document doc = XmlUtils.newDocument();
                Element root = expression.toElement(doc);
                doc.appendChild(root);
                XmlUtils.printDOM(doc);
            }

            if (commandExpression.gui)
                IrpTransmogrifier.showTreeViewer(parser, expression.getParseTree(), text+"="+result);
        }
    }

    private static void doBitfield(CommandBitfield commandBitField) throws IrpSyntaxException, UsageException, IrpSemanticException, ArithmeticException, IncompatibleArgumentException, UnassignedException {
        String nameEngineString = commandBitField.nameEngine;
        if (!nameEngineString.startsWith("{"))
            nameEngineString = "{" + nameEngineString;
        if (!nameEngineString.endsWith("{"))
            nameEngineString += "}";
        NameEngine nameEngine = new NameEngine(nameEngineString);

//        String generalSpecString = commandExpression.generalspec;
//        if (commandExpression.msb) {
//            if (commandExpression.generalspec != null)
//                throw new UsageException("The options --generalspec and --msb are exclusive");
//            generalSpecString = "{msb}";
//        }
//        GeneralSpec generalSpec = new GeneralSpec(generalSpecString);

        for (String text : commandBitField.bitfields) {

            IrpParser parser = new ParserDriver(text).getParser();
            BitField bitfield = BitField.newBitField(parser.bitfield());
//            if (!parser.isMatchedEOF()) {
//                System.err.println("WARNING: Did not match all input");
//                //System.exit(IrpUtils.exitFatalProgramFailure);
//            }

            long result = bitfield.toNumber(nameEngine);
            System.out.print(result);
            if (bitfield instanceof FiniteBitField) {
                FiniteBitField fbf = (FiniteBitField) bitfield;
                System.out.print("\t" + fbf.toBinaryString(nameEngine, commandBitField.lsb));
            }
            System.out.println();

//            if (commandExpression.stringTree)
//                System.out.println(bitfield.getParseTree().toStringTree(parser));
            if (commandBitField.xml) {
                Document doc = XmlUtils.newDocument();
                Element root = bitfield.toElement(doc);
                doc.appendChild(root);
                XmlUtils.printDOM(doc);
            }
//
//            if (commandExpression.gui)
//                IrpTransmogrifier.showTreeViewer(parser, bitfield.getParseTree(), text+"="+result);
        }
    }

    /**
     * show the given Tree Viewer
     *
     * @param tv
     * @param title
     */
    public static void showTreeViewer(TreeViewer tv, String title) {
        JPanel panel = new JPanel();
        //tv.setScale(2);
        panel.add(tv);

        JOptionPane.showMessageDialog(null, panel, title, JOptionPane.PLAIN_MESSAGE);
    }

    public static void showTreeViewer(IrpParser parser, ParserRuleContext parserRuleContext, String title) {
        List<String> ruleNames = Arrays.asList(parser.getRuleNames());

        // http://stackoverflow.com/questions/34832518/antlr4-dotgenerator-example
        TreeViewer tv = new TreeViewer(ruleNames, parserRuleContext);
        showTreeViewer(tv, title);
    }

    public static class LevelParser implements IStringConverter<Level> { // MUST be public

        @Override
        public Level convert(String value) {
            try {
                return Level.parse(value.toUpperCase(Locale.US));
            } catch (IllegalArgumentException ex) {
                throw new ParameterException(ex);
            }
        }
    }

    private final static class CommandLineArgs {

        @Parameter(names = {"-c", "--configfile"}, description = "Pathname of IRP database file in XML format")
        private String configFile = "src/main/config/IrpProtocols.xml";

        @Parameter(names = {"-i", "--ini", "--inifile"}, description = "Pathname of IRP database file in ini format")
        private String iniFile = null;//"src/main/config/IrpProtocols.ini";

        //@Parameter(names = {"-D", "--debug"}, description = "Debug code")
        //private int debug = 0;

        @Parameter(names = {"-h", "--help", "-?"}, description = "Display help message")
        private boolean helpRequested = false;

        @Parameter(names = {"-l", "--loglevel"}, converter = LevelParser.class,
                description = "Log level { ALL, CONFIG, FINE, FINER, FINEST, INFO, OFF, SEVERE, WARNING }")
        private Level logLevel = Level.INFO;

        @Parameter(names = {"-v", "--version"}, description = "Report version")
        private boolean versionRequested = false;

        //@Parameter(description = "protocols")
        //private List<String> protocols = new ArrayList<>();
    }

    @Parameters(commandNames = {"list"}, commandDescription = "List the protocols known")
    private static class CommandList {

        @Parameter(names = { "--gui", "--display"}, description = "Display parse diagram")
        private boolean gui = false;

        @Parameter(names = { "--irp"}, description = "List IRP")
        private boolean irp = false;

        @Parameter(names = { "--is"}, description = "test toIrpString")
        private boolean is = false;

        @Parameter(names = { "--documentation"}, description = "List documentation")
        private boolean documentation = false;

        @Parameter(names = {"-p", "--parse"}, description = "Test parse the protocol(s)")
        private boolean parse = false;

        @Parameter(names = {"-r", "--regex", "--regexp"}, description = "Interpret arguments as regular expressions")
        private boolean regexp = false;

        @Parameter(names = {"-s", "--sort"}, description = "Sort the output")
        private boolean sort = false;

        @Parameter(names = { "--stringtree" }, description = "Produce stringtree")
        private boolean stringTree = false;

        @Parameter(description = "List of protocols (default all)")
        private List<String> protocols;
    }

    @Parameters(commandNames = {"code"}, commandDescription = "Generate code")
    private static class CommandCode {

        @Parameter(names = { "--render" }, description = "Generate code for rendering, otherwise for decoding")
        private boolean render = false;

        @Parameter(names = { "--xslt" }, description = "Pathname to XSLT")
        private String xslt = null;

        @Parameter(names = { "-e", "--encoding" }, description = "Encoding used for generating output")
        private String encoding = "UTF-8";

        @Parameter(names = { "--target" }, description = "Target for code generation")
        private String target;

        @Parameter(names = { "-o", "--output" }, description = "Name of output file")
        private String output = null;

        @Parameter(names = { "--xml"}, description = "List XML")
        private boolean xml = false;

        @Parameter(names = { "--documentation"}, description = "List documentation")
        private boolean documentation = false;

        @Parameter(names = {"-i", "--irp"}, description = "List irp")
        private boolean irp = false;

        @Parameter(description = "List of protocols (default all)")
        private List<String> protocols;
    }

    @Parameters(commandNames = {"render"}, commandDescription = "Render signal")
    private static class CommandRender {

        @Parameter(names = { "--pronto" }, description = "Generate Pronto hex")
        private boolean pronto = false;

        @Parameter(names = { "--raw" }, description = "Generate raw form")
        private boolean raw = false;

        @Parameter(description = "protocol, [paramater assignments]*")
        private List<String> args;
    }

    @Parameters(commandNames = { "expression" }, commandDescription = "Evaluate expression")
    private static class CommandExpression {

        @Parameter(names = { "-n", "--nameengine" }, description = "Name Engine to use")
        private String nameEngine = null;

        @Parameter(names = { "--stringtree" }, description = "Produce stringtree")
        private boolean stringTree = false;

        @Parameter(names = { "--gui", "--display"}, description = "Display parse diagram")
        private boolean gui = false;

        @Parameter(names = { "--xml"}, description = "List XML")
        private boolean xml = false;

        @Parameter(description = "expression")
        private List<String> expressions;
    }

    @Parameters(commandNames = { "bitfield" }, commandDescription = "Evaluate bitfield")
    private static class CommandBitfield {

        @Parameter(names = { "-n", "--nameengine" }, description = "Name Engine to use")
        private String nameEngine = null;

//        @Parameter(names = { "-g", "--generalspec" }, description = "Generalspec to use")
//        private String generalspec = null;

        @Parameter(names = { "-l", "--lsb" }, description = "Least significant bit first")
        private boolean lsb = false;

//        @Parameter(names = { "--stringtree" }, description = "Produce stringtree")
//        private boolean stringTree = false;
//
//        @Parameter(names = { "--gui", "--display"}, description = "Display parse diagram")
//        private boolean gui = false;

        @Parameter(names = { "--xml"}, description = "List XML")
        private boolean xml = false;

        @Parameter(description = "bitfield")
        private List<String> bitfields;
    }

    @Parameters(commandNames = {"recognize"}, commandDescription = "Recognize signal")
    private static class CommandRecognize {

        @Parameter(description = "durations, or pronto hex")
        private List<String> args;
    }

    /**
     *
     * @param args
     */
    public static void main(String[] args) {
        commandLineArgs = new CommandLineArgs();
        argumentParser = new JCommander(commandLineArgs);
        argumentParser.setProgramName(Version.appName);

        CommandList commandList = new CommandList();
        argumentParser.addCommand(commandList);

        CommandCode commandCode = new CommandCode();
        argumentParser.addCommand(commandCode);

        CommandRender commandRenderer = new CommandRender();
        argumentParser.addCommand(commandRenderer);

        CommandRecognize commandRecognize = new CommandRecognize();
        argumentParser.addCommand(commandRecognize);

        CommandExpression commandExpression = new CommandExpression();
        argumentParser.addCommand(commandExpression);

        CommandBitfield commandBitfield = new CommandBitfield();
        argumentParser.addCommand(commandBitfield);

        try {
            argumentParser.parse(args);
        } catch (ParameterException ex) {
            System.err.println(ex.getMessage());
            usage(IrpUtils.exitUsageError);
        }

        if (commandLineArgs.helpRequested)
            usage(IrpUtils.exitSuccess);

        if (commandLineArgs.versionRequested) {
            System.out.println(Version.versionString);
            if (commandLineArgs.configFile != null) {
                try {
                    IrpDatabase db = new IrpDatabase(commandLineArgs.configFile);
                    System.out.println("Database: " + commandLineArgs.configFile + " version: "+ db.getConfigFileVersion());
                } catch (IOException | SAXException | IncompatibleArgumentException ex) {
                    logger.log(Level.SEVERE, null, ex);
                }
            }
            System.out.println("JVM: " + System.getProperty("java.vendor") + " " + System.getProperty("java.version") + " " + System.getProperty("os.name") + "-" + System.getProperty("os.arch"));
            System.out.println();
            System.out.println(Version.licenseString);
            System.exit(IrpUtils.exitSuccess);
        }

        ConsoleHandler handler = new ConsoleHandler();
        handler.setLevel(commandLineArgs.logLevel);
        logger.addHandler(handler);
        logger.setUseParentHandlers(false);
        logger.setLevel(commandLineArgs.logLevel/*Level.ALL*/);

        try {
            IrpDatabase irpDatabase = commandLineArgs.iniFile != null
                    ? new IrpDatabase(IrpDatabase.readIni(commandLineArgs.iniFile))
                    : new IrpDatabase(commandLineArgs.configFile);
            String command = argumentParser.getParsedCommand();
            if (command == null) {
                usage(IrpUtils.exitUsageError);
                return;
            }
            switch (command) {
                case "list":
                    doList(irpDatabase, commandList);
                    break;
                case "code":
                    doCode(irpDatabase, commandCode);
                    break;
                case "render":
                    doRender(irpDatabase, commandRenderer);
                    break;
                case "recognize":
                    doRecognize(irpDatabase, commandRecognize);
                    break;
                case "expression":
                    doExpression(commandExpression);
                    break;
                case "bitfield":
                    doBitfield(commandBitfield);
                    break;
                default:
                    System.err.println("Unknown command: " + command);
                    System.exit(IrpUtils.exitSemanticUsageError);
            }
        } catch (UnknownProtocolException | UnassignedException | UsageException ex) {
            System.err.println(ex.getMessage());
        } catch (SAXException | IOException| IncompatibleArgumentException | IrpSyntaxException | IrpSemanticException | ArithmeticException | InvalidRepeatException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
    }

    private static class UsageException extends Exception {

        UsageException(String message) {
            super(message);
        }
    }
}