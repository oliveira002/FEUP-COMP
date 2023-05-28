package pt.up.fe.comp2023;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import pt.up.fe.comp.TestUtils;
import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.jasmin.JasminBackend;
import pt.up.fe.comp.jmm.jasmin.JasminResult;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.comp.jmm.parser.JmmParserResult;
import pt.up.fe.comp2023.analysis.JmmSimpleAnalysis;
import pt.up.fe.comp2023.jasmin.Jasmin;
import pt.up.fe.comp2023.ollir.JmmOptimizer;
import pt.up.fe.comp2023.ollir.optimization.RegisterAllocation;
import pt.up.fe.specs.util.SpecsIo;
import pt.up.fe.specs.util.SpecsLogs;
import pt.up.fe.specs.util.SpecsSystem;

public class Launcher {

    private static int phase = 1;

    public static void main(String[] args) {
        // Setups console logging and other things
        SpecsSystem.programStandardInit();

        // Parse arguments as a map with predefined options
        var config = parseArgs(args);

        // Get input file
        File inputFile = new File(config.get("inputFile"));

        // Check if file exists
        if (!inputFile.isFile()) {
            throw new RuntimeException("Expected a path to an existing input file, got '" + inputFile + "'.");
        }

        // Read contents of input file
        String code = SpecsIo.read(inputFile);

        // Instantiate JmmParser
        SimpleParser parser = new SimpleParser();

        // Parse stage
        JmmParserResult parserResult = parser.parse(code, config);

        // Check if there are parsing errors
        TestUtils.noErrors(parserResult.getReports());

        // Instantiate JmmAnalysis
        JmmSimpleAnalysis analysis = new JmmSimpleAnalysis();

        // Analysis stage
        JmmSemanticsResult analysisResult = analysis.semanticAnalysis(parserResult);

        // Check if there are analysis errors
        TestUtils.noErrors(analysisResult.getReports());
        System.out.println("Phase "+ phase++ +": AST generation");
        if(parserResult.getConfig().getOrDefault("debug", "false").equals("true")) {
            System.out.println("\n!--AST--!\n"+parserResult.getRootNode().toTree());
        }

        System.out.println("Phase "+ phase++ +": Symbol Table generation");
        if(parserResult.getConfig().getOrDefault("debug", "false").equals("true")) {
            System.out.println("\n!--Symbol table--!\n"+analysisResult.getSymbolTable());
        }

        //Jmm optimization
        JmmOptimizer jmmOptimizer = new JmmOptimizer();
        analysisResult = jmmOptimizer.optimize(analysisResult);
        if(parserResult.getConfig().getOrDefault("optimize", "false").equals("true")) {
            System.out.println("Phase "+ phase++ +": Code optimization");

            if(parserResult.getConfig().getOrDefault("debug", "false").equals("true")) {
                System.out.println("\n!-- OPTIMIZED AST--!\n"+analysisResult.getRootNode().toTree());
            }
        }

        //Ollir generation
        OllirResult ollir = jmmOptimizer.toOllir(analysisResult);
        System.out.println("Phase "+ phase++ +": OLLIR generation");
        if(parserResult.getConfig().getOrDefault("debug", "false").equals("true")) {
            System.out.println("\n!--Ollir code--!\n"+ollir.getOllirCode());
        }

        //Register allocation
        System.out.println("Phase "+ phase++ +": Register allocation");
        // check for errors on reg alloc
        TestUtils.noErrors(ollir.getReports());

        //Jasmin generation
        JasminBackend jasmin = new Jasmin();
        JasminResult jasminResult = jasmin.toJasmin(ollir);
        System.out.println("Phase "+ phase++ +": Jasmin generation");
        if(parserResult.getConfig().getOrDefault("debug", "false").equals("true")) {
            System.out.println("\n!--Jasmin code--!\n"+jasminResult.getJasminCode());
            var output = TestUtils.runJasmin(jasminResult.getJasminCode());
            System.out.println(output);
        }

        System.out.println("Compilation finished");
    }

    private static Map<String, String> parseArgs(String[] args) {
        SpecsLogs.info("Executing with args: " + Arrays.toString(args));

        // Check if there is at least one argument
        if (args.length < 1) {
            throw new RuntimeException("Expected at least an argument, a path to an existing input file.");
        }

        // Create config with default values
        Map<String, String> config = new HashMap<>();
        config.put("inputFile", args[0]);
        config.put("optimize", "false");
        config.put("registerAllocation", "-1");
        config.put("debug", "false");


        // Change config based on command line arguments
        for(String option : args){
            String[] option_split = option.split("=");
            String flag = option_split[0];

            switch (flag){
                case "-i" -> config.put("inputFile", option_split[1]);
                case "-o" -> config.put("optimize", "true");
                case "-r" -> config.put("registerAllocation", option_split[1]);
                case "-d" -> config.put("debug", "true");
                default -> throw new IllegalArgumentException("Unexpected argument: " + flag);
            }
        }

        return config;
    }
}
