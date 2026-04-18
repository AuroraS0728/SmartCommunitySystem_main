package com.smartcommunity.config;

import com.seetaface.SeetaFace6JNI;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Configuration
@EnableConfigurationProperties(SeetaFaceProperties.class)
public class SeetaFaceConfig {

    @Bean
    public SeetaFaceRuntime seetaFaceRuntime(SeetaFaceProperties properties) {
        if (!properties.isEnabled()) {
            return SeetaFaceRuntime.failed(properties, "seetaface disabled by config");
        }
        try {
            String modelDir = prepareModelDir(properties);
            loadNativeLibraries(properties);
            SeetaFace6JNI engine = new SeetaFace6JNI();
            boolean ok = engine.initModel(modelDir);
            if (!ok) {
                String msg = "SeetaFace initModel failed, modelDir=" + modelDir;
                log.error(msg);
                return SeetaFaceRuntime.failed(properties, msg);
            }
            String message = "SeetaFace initialized, modelDir=" + modelDir;
            log.info(message);
            return SeetaFaceRuntime.ready(properties, engine, message);
        } catch (Exception ex) {
            log.error("SeetaFace runtime init failed", ex);
            return SeetaFaceRuntime.failed(properties, ex.getMessage());
        }
    }

    private String prepareModelDir(SeetaFaceProperties properties) {
        Path detector = requireExistingModel(properties.getDetectorModelPath(), "detector");
        Path recognizer = requireExistingModel(properties.getRecognizerModelPath(), "recognizer");
        Path landmarker = requireExistingModel(properties.getLandmarkerModelPath(), "landmarker");
        Path detectorDir = detector.getParent();
        if (detectorDir == null || !detectorDir.equals(recognizer.getParent()) || !detectorDir.equals(landmarker.getParent())) {
            throw new IllegalArgumentException("three model files must be in the same directory");
        }
        return detectorDir.toAbsolutePath().toString();
    }

    private Path requireExistingModel(String pathText, String modelType) {
        if (!StringUtils.hasText(pathText)) {
            throw new IllegalArgumentException("seetaface " + modelType + " model path is empty");
        }
        Path path = Paths.get(pathText);
        if (!Files.exists(path)) {
            throw new IllegalArgumentException("seetaface " + modelType + " model not found: " + path);
        }
        return path;
    }

    private void loadNativeLibraries(SeetaFaceProperties properties) throws IOException {
        List<String> libs = properties.getNativeLibraries();
        if (libs == null || libs.isEmpty()) {
            throw new IllegalArgumentException("seetaface nativeLibraries is empty");
        }
        Path extractDir = Paths.get(properties.getNativeExtractDir());
        Files.createDirectories(extractDir);
        String resourceRoot = trimSlashes(properties.getNativeResourceDir());
        Set<String> loaded = new HashSet<>();
        for (String lib : libs) {
            String libName = lib == null ? "" : lib.trim();
            if (libName.isEmpty() || loaded.contains(libName)) {
                continue;
            }
            Path dllPath = extractNativeDll(resourceRoot, libName, extractDir);
            System.load(dllPath.toAbsolutePath().toString());
            loaded.add(libName);
            log.info("Loaded native dll: {}", dllPath);
        }
    }

    private Path extractNativeDll(String resourceRoot, String libName, Path extractDir) throws IOException {
        String resourcePath = resourceRoot + "/" + libName + ".dll";
        ClassPathResource resource = new ClassPathResource(resourcePath);
        if (!resource.exists()) {
            throw new IllegalArgumentException("native resource not found: " + resourcePath);
        }
        Path target = extractDir.resolve(libName + ".dll");
        try (InputStream in = resource.getInputStream()) {
            Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
        }
        return target;
    }

    private String trimSlashes(String value) {
        String text = value == null ? "" : value.trim();
        while (text.startsWith("/")) {
            text = text.substring(1);
        }
        while (text.endsWith("/")) {
            text = text.substring(0, text.length() - 1);
        }
        return text;
    }
}
