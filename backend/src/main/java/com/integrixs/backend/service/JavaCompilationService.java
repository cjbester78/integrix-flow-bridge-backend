package com.integrixs.backend.service;

import com.integrixs.backend.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.tools.*;
import java.io.StringWriter;
import java.net.URI;
import java.util.*;

/**
 * Service for compiling and validating Java transformation functions
 */
@Slf4j
@Service
public class JavaCompilationService {
    
    private final JavaCompiler compiler;
    
    public JavaCompilationService() {
        this.compiler = ToolProvider.getSystemJavaCompiler();
        if (this.compiler == null) {
            log.warn("Java compiler not available. Ensure JDK is installed. Function compilation will not work.");
        }
    }
    
    /**
     * Compile and validate Java function code
     */
    public CompilationResult compileFunction(String functionName, String functionCode) {
        if (compiler == null) {
            throw new BusinessException("Java compiler not available. Ensure JDK is installed.");
        }
        
        try {
            // Create diagnostic collector
            DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
            
            // Create string writer for compiler output
            StringWriter writer = new StringWriter();
            
            // Get standard file manager
            StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, null, null);
            
            // Create source code object
            JavaSourceFromString source = new JavaSourceFromString(functionName, wrapFunctionCode(functionName, functionCode));
            
            // Create compilation task
            Iterable<? extends JavaFileObject> compilationUnits = Arrays.asList(source);
            JavaCompiler.CompilationTask task = compiler.getTask(
                writer, 
                fileManager, 
                diagnostics, 
                Arrays.asList("-Xlint:all"), 
                null, 
                compilationUnits
            );
            
            // Compile
            boolean success = task.call();
            
            // Close file manager
            fileManager.close();
            
            // Build result
            CompilationResult result = new CompilationResult();
            result.setSuccess(success);
            result.setOutput(writer.toString());
            
            // Add diagnostics
            List<String> errors = new ArrayList<>();
            List<String> warnings = new ArrayList<>();
            
            for (Diagnostic<? extends JavaFileObject> diagnostic : diagnostics.getDiagnostics()) {
                String message = String.format("Line %d: %s", 
                    diagnostic.getLineNumber(), 
                    diagnostic.getMessage(null)
                );
                
                if (diagnostic.getKind() == Diagnostic.Kind.ERROR) {
                    errors.add(message);
                } else if (diagnostic.getKind() == Diagnostic.Kind.WARNING) {
                    warnings.add(message);
                }
            }
            
            result.setErrors(errors);
            result.setWarnings(warnings);
            
            return result;
            
        } catch (Exception e) {
            log.error("Error compiling function: {}", functionName, e);
            throw new BusinessException("Failed to compile function: " + e.getMessage());
        }
    }
    
    /**
     * Wrap function code in a complete class structure
     */
    private String wrapFunctionCode(String functionName, String functionCode) {
        // If already a complete class, return as is
        if (functionCode.trim().startsWith("public class")) {
            return addImports(functionCode);
        }
        
        // Otherwise wrap in class template
        return String.format(
            "import java.util.*;\n" +
            "import java.text.*;\n" +
            "import java.time.*;\n" +
            "import java.time.format.*;\n" +
            "import com.integrixs.engine.transformation.TransformationFunction;\n\n" +
            "%s",
            functionCode
        );
    }
    
    /**
     * Add common imports to existing class
     */
    private String addImports(String code) {
        if (!code.contains("import")) {
            return "import java.util.*;\n" +
                   "import java.text.*;\n" +
                   "import java.time.*;\n" +
                   "import java.time.format.*;\n" +
                   "import com.integrixs.engine.transformation.TransformationFunction;\n\n" +
                   code;
        }
        return code;
    }
    
    /**
     * In-memory Java source code object
     */
    static class JavaSourceFromString extends SimpleJavaFileObject {
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
    
    /**
     * Compilation result DTO
     */
    public static class CompilationResult {
        private boolean success;
        private String output;
        private List<String> errors = new ArrayList<>();
        private List<String> warnings = new ArrayList<>();
        
        public boolean isSuccess() {
            return success;
        }
        
        public void setSuccess(boolean success) {
            this.success = success;
        }
        
        public String getOutput() {
            return output;
        }
        
        public void setOutput(String output) {
            this.output = output;
        }
        
        public List<String> getErrors() {
            return errors;
        }
        
        public void setErrors(List<String> errors) {
            this.errors = errors;
        }
        
        public List<String> getWarnings() {
            return warnings;
        }
        
        public void setWarnings(List<String> warnings) {
            this.warnings = warnings;
        }
        
        public boolean hasErrors() {
            return !errors.isEmpty();
        }
        
        public boolean hasWarnings() {
            return !warnings.isEmpty();
        }
    }
}