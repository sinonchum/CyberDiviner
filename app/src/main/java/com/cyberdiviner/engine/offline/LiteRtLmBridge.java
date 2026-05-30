package com.cyberdiviner.engine.offline;

import android.util.Log;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Map;

/**
 * Reflection bridge for LiteRT-LM.
 *
 * Kotlin code must not import com.google.ai.edge.litertlm.* directly, because
 * LiteRT-LM is currently compiled with a newer Kotlin metadata version than
 * this app. Keeping the integration behind Java reflection avoids source-level
 * ABI coupling while still loading the runtime on device.
 */
public final class LiteRtLmBridge {
    private static final String TAG = "LiteRtLmBridge";

    private Object engine;
    private boolean ready = false;

    public synchronized boolean initialize(String modelPath, String cacheDir, int maxTokens) throws Exception {
        if (ready && engine != null) return true;

        Log.i(TAG, "Initializing LiteRT-LM: " + modelPath);

        try {
            initializeWithBackend(modelPath, cacheDir, maxTokens, newGpuBackend(), "GPU");
        } catch (Throwable gpuError) {
            Log.w(TAG, "GPU backend failed, falling back to CPU", gpuError);
            close();
            initializeWithBackend(modelPath, cacheDir, maxTokens, newCpuBackend(), "CPU");
        }

        ready = true;
        Log.i(TAG, "LiteRT-LM initialized");
        return true;
    }

    public synchronized String generate(String prompt, double temperature, int topK) throws Exception {
        if (!ready || engine == null) {
            throw new IllegalStateException("LiteRT-LM is not initialized");
        }

        Object conversation = null;
        try {
            conversation = createConversation(temperature, topK);
            Class<?> conversationClass = conversation.getClass();
            Map<String, Object> extraContext = Collections.emptyMap();

            Object response;
            try {
                Method sendMethod = conversationClass.getMethod("sendMessage", String.class, Map.class);
                response = sendMethod.invoke(conversation, prompt, extraContext);
            } catch (InvocationTargetException e) {
                throw unwrap("sendMessage", e);
            }

            String rendered = renderResponse(conversation, response, extraContext);
            return cleanupMarkers(rendered);
        } finally {
            closeConversation(conversation);
        }
    }

    public synchronized boolean isReady() {
        return ready && engine != null;
    }

    public synchronized void close() {
        try {
            if (engine != null) {
                Method closeMethod = engine.getClass().getMethod("close");
                closeMethod.invoke(engine);
            }
        } catch (Exception e) {
            Log.w(TAG, "Error closing LiteRT-LM", e);
        } finally {
            engine = null;
            ready = false;
        }
    }

    private Object newCpuBackend() throws Exception {
        Class<?> cpuClass = Class.forName("com.google.ai.edge.litertlm.Backend$CPU");
        try {
            return cpuClass.getConstructor(int.class).newInstance(4);
        } catch (NoSuchMethodException ignored) {
            return cpuClass.getConstructor().newInstance();
        }
    }

    private Object newGpuBackend() throws Exception {
        Class<?> gpuClass = Class.forName("com.google.ai.edge.litertlm.Backend$GPU");
        return gpuClass.getConstructor().newInstance();
    }

    private void initializeWithBackend(String modelPath, String cacheDir, int maxTokens, Object backend, String name) throws Exception {
        Class<?> backendClass = Class.forName("com.google.ai.edge.litertlm.Backend");
        Class<?> configClass = Class.forName("com.google.ai.edge.litertlm.EngineConfig");
        Constructor<?> configCtor = configClass.getConstructor(
            String.class,
            backendClass,
            backendClass,
            backendClass,
            Integer.class,
            Integer.class,
            String.class
        );
        Object config = configCtor.newInstance(
            modelPath,
            backend,
            null,
            null,
            Integer.valueOf(maxTokens),
            null,
            cacheDir
        );

        Class<?> engineClass = Class.forName("com.google.ai.edge.litertlm.Engine");
        Constructor<?> engineCtor = engineClass.getConstructor(configClass);
        engine = engineCtor.newInstance(config);

        Method initializeMethod = engineClass.getMethod("initialize");
        initializeMethod.invoke(engine);
        Log.i(TAG, "LiteRT-LM backend active: " + name);
    }

    private Object createConversation(double temperature, int topK) throws Exception {
        Class<?> samplerClass = Class.forName("com.google.ai.edge.litertlm.SamplerConfig");
        Object samplerConfig = createSamplerConfig(samplerClass, temperature, topK);

        Class<?> contentsClass = Class.forName("com.google.ai.edge.litertlm.Contents");
        Class<?> convConfigClass = Class.forName("com.google.ai.edge.litertlm.ConversationConfig");
        Constructor<?> convConfigCtor = convConfigClass.getConstructor(
            contentsClass,
            java.util.List.class,
            java.util.List.class,
            samplerClass
        );
        Object convConfig = convConfigCtor.newInstance(
            null,
            Collections.emptyList(),
            Collections.emptyList(),
            samplerConfig
        );

        Method createConversation = engine.getClass().getMethod("createConversation", convConfigClass);
        return createConversation.invoke(engine, convConfig);
    }

    private Object createSamplerConfig(Class<?> samplerClass, double temperature, int topK) throws Exception {
        for (Constructor<?> ctor : samplerClass.getConstructors()) {
            Class<?>[] params = ctor.getParameterTypes();
            if (params.length == 4 &&
                params[0] == int.class &&
                params[1] == double.class &&
                params[2] == double.class &&
                params[3] == int.class) {
                return ctor.newInstance(topK, 0.95d, temperature, 0);
            }
        }
        return samplerClass.getConstructor().newInstance();
    }

    private String renderResponse(Object conversation, Object response, Map<String, Object> extraContext) throws Exception {
        if (response == null) return "";

        try {
            Class<?> messageClass = Class.forName("com.google.ai.edge.litertlm.Message");
            Method renderMethod = conversation.getClass().getMethod("renderMessageIntoString", messageClass, Map.class);
            Object rendered = renderMethod.invoke(conversation, response, extraContext);
            return rendered != null ? rendered.toString() : "";
        } catch (InvocationTargetException e) {
            Log.w(TAG, "renderMessageIntoString failed, falling back to response.toString()", e.getTargetException());
            return response.toString();
        } catch (Exception e) {
            Log.w(TAG, "Falling back to response.toString()", e);
            return response.toString();
        }
    }

    private void closeConversation(Object conversation) {
        if (conversation == null) return;
        try {
            Method closeMethod = conversation.getClass().getMethod("close");
            closeMethod.invoke(conversation);
        } catch (Exception e) {
            Log.w(TAG, "Error closing LiteRT-LM conversation", e);
        }
    }

    private Exception unwrap(String methodName, InvocationTargetException e) {
        Throwable cause = e.getTargetException();
        if (cause == null) {
            return new Exception(methodName + " failed: " + e.getMessage(), e);
        }
        Log.e(TAG, methodName + " failed", cause);
        return new Exception(methodName + " failed: " + cause.getClass().getSimpleName() + ": " + cause.getMessage(), cause);
    }

    private String cleanupMarkers(String response) {
        if (response == null) return "";

        String[] lines = response.replace("\r\n", "\n").replace('\r', '\n').split("\n");
        StringBuilder cleaned = new StringBuilder();
        for (String line : lines) {
            String trimmed = line.trim()
                .replaceAll("(?i)<\\|?\\s*(start_of_)?turn\\s*\\|?>", "")
                .replaceAll("(?i)</?\\s*end_of_turn\\s*>", "")
                .replaceAll("(?i)^\\s*(model|assistant|user|模型|助手|用户)\\s*[:：]?\\s*", "")
                .trim();
            if (trimmed.isEmpty()) continue;
            if (trimmed.equalsIgnoreCase("model") ||
                trimmed.equalsIgnoreCase("assistant") ||
                trimmed.equalsIgnoreCase("user") ||
                trimmed.equals("模型") ||
                trimmed.equals("助手") ||
                trimmed.equals("用户")) {
                continue;
            }
            if (cleaned.length() > 0) cleaned.append('\n');
            cleaned.append(trimmed);
        }
        return cleaned.toString().trim();
    }
}
