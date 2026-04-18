package com.smartcommunity.config;

import com.seetaface.SeetaFace6JNI;
import lombok.Getter;

@Getter
public class SeetaFaceRuntime {
    private final SeetaFaceProperties properties;
    private final SeetaFace6JNI engine;
    private final boolean ready;
    private final String message;

    private SeetaFaceRuntime(SeetaFaceProperties properties, SeetaFace6JNI engine, boolean ready, String message) {
        this.properties = properties;
        this.engine = engine;
        this.ready = ready;
        this.message = message;
    }

    public static SeetaFaceRuntime ready(SeetaFaceProperties properties, SeetaFace6JNI engine, String message) {
        return new SeetaFaceRuntime(properties, engine, true, message);
    }

    public static SeetaFaceRuntime failed(SeetaFaceProperties properties, String message) {
        return new SeetaFaceRuntime(properties, null, false, message);
    }
}
