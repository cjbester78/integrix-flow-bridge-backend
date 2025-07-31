package com.integrationlab.util;

import com.integrationlab.util.helpers.DateHelper;
import com.integrationlab.util.helpers.StringUtils;

import javax.script.*;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JavaFunctionRunner {

    private static final Logger LOGGER = Logger.getLogger(JavaFunctionRunner.class.getName());

    private static final ScriptEngineManager ENGINE_MANAGER = new ScriptEngineManager();
    private static final ScriptEngine ENGINE = ENGINE_MANAGER.getEngineByName("nashorn");
    private static final Map<String, CompiledScript> FUNCTION_CACHE = new ConcurrentHashMap<>();

    /**
     * Executes a JavaScript function with arguments extracted from sourceData based on sourceFields,
     * with injected helper objects available in the script context.
     * Supports optional parameters by passing null for missing args.
     * Uses a cache keyed by function body + version for better cache invalidation.
     *
     * WARNING: This runs untrusted JS code directly on Nashorn engine, no sandboxing applied.
     * Consider sandboxing or validation if code comes from untrusted sources.
     *
     * @param functionBody The JavaScript function string, e.g. "(a, b) => a + ' ' + b"
     * @param sourceFields The list of field names used as arguments
     * @param sourceData   The map containing source data (JSON parsed)
     * @param helpers      A map of helper object name to instance to inject into script context
     * @param version      Optional function version string to differentiate cache entries
     * @return Result of function execution
     */
    public static Object run(
            String functionBody,
            List<String> sourceFields,
            Map<String, Object> sourceData,
            Map<String, Object> helpers,
            String version
    ) {
        sourceFields = sourceFields != null ? sourceFields : List.of();
        sourceData = sourceData != null ? sourceData : Map.of();
        version = version != null ? version : "default";

        String cacheKey = functionBody + "##" + version;

        try {
            if (!(ENGINE instanceof Compilable)) {
                throw new UnsupportedOperationException("ScriptEngine does not support compilation.");
            }

            // Compile or retrieve compiled script from cache
            CompiledScript compiledScript = FUNCTION_CACHE.computeIfAbsent(cacheKey, fn -> {
                try {
                    return ((Compilable) ENGINE).compile("var transform = " + functionBody + ";");
                } catch (ScriptException e) {
                    LOGGER.log(Level.SEVERE, "Failed to compile JavaScript function: " + functionBody, e);
                    throw new RuntimeException("Failed to compile JavaScript function: " + functionBody, e);
                }
            });

            // Prepare bindings (context variables)
            SimpleBindings bindings = new SimpleBindings();
            if (helpers != null) {
                bindings.putAll(helpers);
            }

            // Evaluate compiled script with bindings
            compiledScript.eval(bindings);

            Invocable invocable = (Invocable) ENGINE;

            // Prepare argument list from sourceData according to sourceFields
            Object[] args = new Object[sourceFields.size()];
            for (int i = 0; i < sourceFields.size(); i++) {
                args[i] = sourceData.getOrDefault(sourceFields.get(i), null);
            }

            // Debug logging inputs
            LOGGER.log(Level.FINE, "Invoking JS function with args: {0}", (Object) args);

            // Invoke the 'transform' function with arguments
            Object result = invocable.invokeFunction("transform", args);

            // Debug logging output
            LOGGER.log(Level.FINE, "JS function result: {0}", result);

            return result;

        } catch (ScriptException | NoSuchMethodException e) {
            LOGGER.log(Level.SEVERE, "JavaScript function execution failed: " + functionBody, e);
            throw new RuntimeException("Failed to execute JavaScript function: " + functionBody, e);
        }
    }

    /**
     * Convenience overload for running without helpers and version.
     */
    public static Object run(String functionBody, List<String> sourceFields, Map<String, Object> sourceData) {
        return run(functionBody, sourceFields, sourceData, Map.of(
                "dateHelper", new DateHelper(),
                "stringUtils", new StringUtils()
        ), "default");
    }

    /**
     * Clears the compiled function cache. Useful for manual cache invalidation.
     */
    public static void clearCache() {
        FUNCTION_CACHE.clear();
        LOGGER.info("JavaFunctionRunner cache cleared.");
    }
}
