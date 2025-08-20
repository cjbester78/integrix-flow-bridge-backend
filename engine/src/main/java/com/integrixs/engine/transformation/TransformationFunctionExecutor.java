package com.integrixs.engine.transformation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.tools.*;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for executing transformation functions dynamically
 */
@Service
public class TransformationFunctionExecutor {
    
    private static final Logger logger = LoggerFactory.getLogger(TransformationFunctionExecutor.class);
    
    // Cache compiled functions for performance
    private final Map<String, TransformationFunction> functionCache = new ConcurrentHashMap<>();
    
    /**
     * Execute a transformation function by name with given arguments
     */
    public Object executeFunction(String functionName, String functionBody, Object... args) {
        try {
            // Check cache first
            String cacheKey = functionName + "_" + functionBody.hashCode();
            TransformationFunction function = functionCache.get(cacheKey);
            
            if (function == null) {
                // Compile and cache the function
                function = compileAndLoadFunction(functionName, functionBody);
                functionCache.put(cacheKey, function);
            }
            
            // Execute the function
            return function.execute(args);
            
        } catch (Exception e) {
            logger.error("Error executing transformation function: {}", functionName, e);
            throw new RuntimeException("Failed to execute transformation function: " + functionName, e);
        }
    }
    
    /**
     * Execute a function by parsing the function call string
     */
    public Object executeFunctionCall(String functionCall, String functionBody, Map<String, Object> context) {
        try {
            // Parse function call: functionName(arg1, arg2, ...)
            int parenIndex = functionCall.indexOf('(');
            if (parenIndex == -1) {
                throw new IllegalArgumentException("Invalid function call format: " + functionCall);
            }
            
            String functionName = functionCall.substring(0, parenIndex).trim();
            String argsString = functionCall.substring(parenIndex + 1, functionCall.lastIndexOf(')')).trim();
            
            // Parse arguments
            List<Object> args = parseArguments(argsString, context);
            
            // Execute function
            return executeFunction(functionName, functionBody, args.toArray());
            
        } catch (Exception e) {
            logger.error("Error executing function call: {}", functionCall, e);
            throw new RuntimeException("Failed to execute function call: " + functionCall, e);
        }
    }
    
    /**
     * Compile and load a function dynamically
     */
    private TransformationFunction compileAndLoadFunction(String functionName, String functionBody) throws Exception {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) {
            throw new RuntimeException("Java compiler not available. Ensure JDK is installed.");
        }
        
        // Create temporary directory for compiled classes
        Path tempDir = Files.createTempDirectory("transformation_functions");
        tempDir.toFile().deleteOnExit();
        
        try {
            // Prepare the source code
            String className = functionName.substring(0, 1).toUpperCase() + functionName.substring(1) + "Function";
            String sourceCode = prepareSourceCode(className, functionBody);
            
            // Compile the source
            DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
            StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, null, null);
            
            JavaFileObject source = new JavaSourceFromString(className, sourceCode);
            Iterable<? extends JavaFileObject> compilationUnits = Arrays.asList(source);
            
            List<String> options = Arrays.asList("-d", tempDir.toString());
            JavaCompiler.CompilationTask task = compiler.getTask(null, fileManager, diagnostics, options, null, compilationUnits);
            
            boolean success = task.call();
            fileManager.close();
            
            if (!success) {
                StringBuilder errors = new StringBuilder();
                for (Diagnostic<? extends JavaFileObject> diagnostic : diagnostics.getDiagnostics()) {
                    errors.append(diagnostic.getMessage(null)).append("\n");
                }
                throw new RuntimeException("Compilation failed: " + errors.toString());
            }
            
            // Load the compiled class
            URLClassLoader classLoader = new URLClassLoader(new java.net.URL[]{tempDir.toUri().toURL()});
            Class<?> clazz = classLoader.loadClass(className);
            
            // Create instance
            return (TransformationFunction) clazz.getDeclaredConstructor().newInstance();
            
        } finally {
            // Clean up temporary directory
            try {
                Files.walk(tempDir)
                    .sorted(Comparator.reverseOrder())
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                        } catch (Exception e) {
                            logger.warn("Failed to delete temporary file: {}", path, e);
                        }
                    });
            } catch (Exception e) {
                logger.warn("Failed to clean up temporary directory: {}", tempDir, e);
            }
        }
    }
    
    /**
     * Prepare source code with necessary imports
     */
    private String prepareSourceCode(String className, String functionBody) {
        if (functionBody.trim().startsWith("public class")) {
            // Already a complete class, just add imports
            return "import com.integrixs.engine.transformation.TransformationFunction;\n" +
                   "import java.util.*;\n" +
                   "import java.time.*;\n" +
                   "import java.time.format.*;\n\n" +
                   functionBody;
        }
        
        // Wrap in a class structure
        return "import com.integrixs.engine.transformation.TransformationFunction;\n" +
               "import java.util.*;\n" +
               "import java.time.*;\n" +
               "import java.time.format.*;\n\n" +
               "public class " + className + " implements TransformationFunction {\n" +
               "    " + functionBody + "\n" +
               "}";
    }
    
    /**
     * Parse function arguments from string
     */
    private List<Object> parseArguments(String argsString, Map<String, Object> context) {
        List<Object> args = new ArrayList<>();
        
        if (argsString.isEmpty()) {
            return args;
        }
        
        // Simple argument parsing (can be enhanced for complex cases)
        String[] argParts = argsString.split(",");
        for (String arg : argParts) {
            arg = arg.trim();
            
            // Check if it's a string literal
            if (arg.startsWith("\"") && arg.endsWith("\"")) {
                args.add(arg.substring(1, arg.length() - 1));
            }
            // Check if it's a number
            else if (arg.matches("-?\\d+(\\.\\d+)?")) {
                if (arg.contains(".")) {
                    args.add(Double.parseDouble(arg));
                } else {
                    args.add(Long.parseLong(arg));
                }
            }
            // Check if it's a boolean
            else if ("true".equalsIgnoreCase(arg) || "false".equalsIgnoreCase(arg)) {
                args.add(Boolean.parseBoolean(arg));
            }
            // Check if it's null
            else if ("null".equalsIgnoreCase(arg)) {
                args.add(null);
            }
            // Otherwise, look it up in context
            else {
                Object value = context.get(arg);
                args.add(value != null ? value : arg);
            }
        }
        
        return args;
    }
    
    /**
     * Clear the function cache
     */
    public void clearCache() {
        functionCache.clear();
    }
    
    /**
     * Get cache size for monitoring
     */
    public int getCacheSize() {
        return functionCache.size();
    }
    
    /**
     * Inner class for Java source from string
     */
    private static class JavaSourceFromString extends SimpleJavaFileObject {
        final String code;
        
        JavaSourceFromString(String name, String code) {
            super(URI.create("string:///" + name.replace('.', '/') + Kind.SOURCE.extension), Kind.SOURCE);
            this.code = code;
        }
        
        @Override
        public CharSequence getCharContent(boolean ignoreEncodingErrors) {
            return code;
        }
    }
}