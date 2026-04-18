package com.smartcommunity.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@Data
@ConfigurationProperties(prefix = "seetaface")
public class SeetaFaceProperties {
    private boolean enabled = true;
    private String detectorModelPath;
    private String recognizerModelPath;
    private String landmarkerModelPath;
    private double similarityThreshold = 0.7d;
    private String nativeResourceDir = "native";
    private String nativeExtractDir = System.getProperty("java.io.tmpdir") + "/smartcommunity-seetaface";
    private List<String> nativeLibraries = List.of(
            "tennis",
            "tennis_haswell",
            "tennis_pentium",
            "tennis_sandy_bridge",
            "SeetaAuthorize",
            "SeetaFaceAntiSpoofingX600",
            "SeetaFaceDetector600",
            "SeetaFaceLandmarker600",
            "SeetaFaceRecognizer610",
            "SeetaFace6JNI"
    );
}
