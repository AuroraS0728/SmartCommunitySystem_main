package com.smartcommunity.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.seetaface.model.SeetaImageData;
import com.smartcommunity.config.SeetaFaceRuntime;
import com.smartcommunity.entity.User;
import com.smartcommunity.mapper.UserMapper;
import com.smartcommunity.utils.RedisUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.nio.ByteBuffer;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class SeetaFaceService {

    private static final String FACE_REDIS_KEY_PREFIX = "face:worker:feature:";

    // JNI constants from SeetaFace6JNI.cpp
    private static final int LIVENESS_REAL = 0;
    private static final int LIVENESS_SPOOF = 1;
    private static final int LIVENESS_FUZZY = 2;
    private static final int LIVENESS_DETECTING = 3;

    // Mask check status (custom JNI extension)
    private static final int MASK_NO = 0;
    private static final int MASK_YES = 1;
    private static final int MASK_UNKNOWN = 2;

    // Eye state (seeta::EyeStateDetector::EYE_STATE)
    private static final int EYE_CLOSE = 0;
    private static final int EYE_OPEN = 1;
    private static final int EYE_RANDOM = 2;
    private static final int EYE_UNKNOWN = 3;

    // Quality level (seeta::QualityLevel)
    private static final int QUALITY_LOW = 0;
    private static final int QUALITY_MEDIUM = 1;
    private static final int QUALITY_HIGH = 2;

    private final SeetaFaceRuntime runtime;
    private final RedisUtil redisUtil;
    private final UserMapper userMapper;

    // Local cache to reduce repeated redis decode cost.
    private final Map<Long, float[]> featureCache = new ConcurrentHashMap<>();
    private volatile boolean maskNativeUnsupportedLogged = false;
    private volatile boolean eyeNativeUnsupportedLogged = false;
    private volatile boolean qualityNativeUnsupportedLogged = false;

    public String registerFace(Long workerId, String imageBase64) {
        ensureWorker(workerId);
        // 注册时只保存人脸特征，不直接保存原始照片，减少隐私数据落库。
        float[] feature = extractFeature(imageBase64);
        saveFeature(workerId, feature);
        log.info("Worker face registered. workerId={}", workerId);
        return "registered";
    }

    public FaceVerifyResult verifyFaceWithScore(Long workerId, String imageBase64) {
        ensureWorker(workerId);
        float[] stored = loadFeature(workerId);
        if (stored == null || stored.length == 0) {
            throw new IllegalArgumentException("worker face is not registered");
        }

        float[] current = extractFeature(imageBase64);
        float score = runtime.getEngine().calculateSimilarity(stored, current);
        double threshold = runtime.getProperties().getSimilarityThreshold();
        boolean match = score >= threshold;
        log.info("Worker face verify finished. workerId={}, score={}, threshold={}, match={}",
                workerId, score, threshold, match);
        return new FaceVerifyResult(match, score, threshold);
    }

    public FaceLiveVerifyResult verifyLiveFaceWithScore(Long workerId, String imageBase64) {
        ensureWorker(workerId);
        float[] stored = loadFeature(workerId);
        if (stored == null || stored.length == 0) {
            throw new IllegalArgumentException("worker face is not registered");
        }

        SeetaImageData imageData = toSeetaImageData(imageBase64);
        double threshold = runtime.getProperties().getSimilarityThreshold();

        // AI本地模型接入点：通过JNI调用服务器上的SeetaFace6动态库做活体检测。
        boolean livenessCheckEnabled = runtime.getProperties().isEnableLivenessCheck();
        int livenessStatus = livenessCheckEnabled ? runtime.getEngine().predictImage(imageData) : LIVENESS_REAL;
        String livenessLabel = livenessCheckEnabled ? livenessLabel(livenessStatus) : "DISABLED";
        boolean livenessPassed = !livenessCheckEnabled || livenessStatus == LIVENESS_REAL;

        MaskCheck mask = detectMask(imageData);
        EyeStateCheck eyeState = detectEyeState(imageData);
        QualityCheck quality = evaluateQuality(imageData);

        // 活体、口罩、睁眼、图像质量都属于前置门禁，不通过时不继续认可人脸相似度。
        boolean maskGatePassed = !runtime.getProperties().isRequireNoMask()
                || !runtime.getProperties().isEnableMaskCheck()
                || !mask.isSupported()
                || mask.isPassed();
        boolean eyeGatePassed = !runtime.getProperties().isRequireEyesOpen()
                || !runtime.getProperties().isEnableEyeStateCheck()
                || !eyeState.isSupported()
                || eyeState.isPassed();
        boolean qualityGatePassed = !runtime.getProperties().isRequireQualityPass()
                || !runtime.getProperties().isEnableQualityCheck()
                || !quality.isSupported()
                || quality.isPassed();

        if (!livenessPassed || !maskGatePassed || !eyeGatePassed || !qualityGatePassed) {
            log.info("Worker live verify blocked. workerId={}, liveness={}, maskPass={}, eyePass={}, qualityPass={}",
                    workerId, livenessLabel, maskGatePassed, eyeGatePassed, qualityGatePassed);
            return new FaceLiveVerifyResult(
                    false,
                    0D,
                    threshold,
                    livenessPassed,
                    livenessStatus,
                    livenessLabel,
                    mask.isSupported(),
                    mask.isPassed(),
                    mask.getStatus(),
                    mask.getLabel(),
                    eyeState.isSupported(),
                    eyeState.isPassed(),
                    eyeState.getLeftState(),
                    eyeState.getLeftLabel(),
                    eyeState.getRightState(),
                    eyeState.getRightLabel(),
                    quality.isSupported(),
                    quality.isPassed(),
                    quality.getScore(),
                    quality.getLevel(),
                    quality.getLevelLabel()
            );
        }

        // 前置检测都通过后，再做人脸特征相似度比较。
        float[] current = extractFeature(imageData);
        float score = runtime.getEngine().calculateSimilarity(stored, current);
        boolean match = score >= threshold;
        log.info("Worker live verify finished. workerId={}, score={}, threshold={}, match={}, liveness={}",
                workerId, score, threshold, match, livenessLabel);

        return new FaceLiveVerifyResult(
                match,
                score,
                threshold,
                true,
                livenessStatus,
                livenessLabel,
                mask.isSupported(),
                mask.isPassed(),
                mask.getStatus(),
                mask.getLabel(),
                eyeState.isSupported(),
                eyeState.isPassed(),
                eyeState.getLeftState(),
                eyeState.getLeftLabel(),
                eyeState.getRightState(),
                eyeState.getRightLabel(),
                quality.isSupported(),
                quality.isPassed(),
                quality.getScore(),
                quality.getLevel(),
                quality.getLevelLabel()
        );
    }

    public boolean hasRegisteredFace(Long workerId) {
        ensureWorker(workerId);
        float[] feature = loadFeature(workerId);
        return feature != null && feature.length > 0;
    }

    private MaskCheck detectMask(SeetaImageData imageData) {
        if (!runtime.getProperties().isEnableMaskCheck()) {
            return MaskCheck.unsupported(MASK_UNKNOWN, "DISABLED");
        }
        try {
            int status = runtime.getEngine().detectMask(imageData);
            boolean passed = status == MASK_NO;
            return new MaskCheck(true, passed, status, maskLabel(status));
        } catch (UnsatisfiedLinkError ex) {
            if (!maskNativeUnsupportedLogged) {
                log.warn("Mask check native method unavailable. Keep running without mask gate.");
                maskNativeUnsupportedLogged = true;
            }
            return MaskCheck.unsupported(MASK_UNKNOWN, "UNSUPPORTED");
        } catch (Exception ex) {
            log.warn("Mask check failed, keep running without mask gate", ex);
            return MaskCheck.unsupported(MASK_UNKNOWN, "ERROR");
        }
    }

    private EyeStateCheck detectEyeState(SeetaImageData imageData) {
        if (!runtime.getProperties().isEnableEyeStateCheck()) {
            return EyeStateCheck.unsupported(EYE_UNKNOWN, EYE_UNKNOWN, "DISABLED", "DISABLED");
        }
        try {
            int[] states = runtime.getEngine().detectEyeState(imageData);
            if (states == null || states.length < 2) {
                return EyeStateCheck.unsupported(EYE_UNKNOWN, EYE_UNKNOWN, "UNSUPPORTED", "UNSUPPORTED");
            }
            int left = states[0];
            int right = states[1];
            boolean passed = left == EYE_OPEN && right == EYE_OPEN;
            return new EyeStateCheck(true, passed, left, eyeLabel(left), right, eyeLabel(right));
        } catch (UnsatisfiedLinkError ex) {
            if (!eyeNativeUnsupportedLogged) {
                log.warn("Eye state native method unavailable. Keep running without eye gate.");
                eyeNativeUnsupportedLogged = true;
            }
            return EyeStateCheck.unsupported(EYE_UNKNOWN, EYE_UNKNOWN, "UNSUPPORTED", "UNSUPPORTED");
        } catch (Exception ex) {
            log.warn("Eye state check failed, keep running without eye gate", ex);
            return EyeStateCheck.unsupported(EYE_UNKNOWN, EYE_UNKNOWN, "ERROR", "ERROR");
        }
    }

    private QualityCheck evaluateQuality(SeetaImageData imageData) {
        if (!runtime.getProperties().isEnableQualityCheck()) {
            return QualityCheck.unsupported(0D, QUALITY_LOW, "DISABLED");
        }
        try {
            float[] result = runtime.getEngine().evaluateQuality(imageData);
            if (result == null || result.length == 0) {
                return QualityCheck.unsupported(0D, QUALITY_LOW, "UNSUPPORTED");
            }
            boolean passed = result[0] >= 0.5f;
            double score = result.length > 1 ? result[1] : 0D;
            int level = result.length > 2 ? Math.round(result[2]) : QUALITY_LOW;
            return new QualityCheck(true, passed, score, level, qualityLevelLabel(level));
        } catch (UnsatisfiedLinkError ex) {
            if (!qualityNativeUnsupportedLogged) {
                log.warn("Quality native method unavailable. Keep running without quality gate.");
                qualityNativeUnsupportedLogged = true;
            }
            return QualityCheck.unsupported(0D, QUALITY_LOW, "UNSUPPORTED");
        } catch (Exception ex) {
            log.warn("Quality check failed, keep running without quality gate", ex);
            return QualityCheck.unsupported(0D, QUALITY_LOW, "ERROR");
        }
    }

    private String maskLabel(int status) {
        return switch (status) {
            case MASK_NO -> "NO_MASK";
            case MASK_YES -> "MASK";
            case MASK_UNKNOWN -> "UNKNOWN";
            default -> "UNKNOWN";
        };
    }

    private String eyeLabel(int state) {
        return switch (state) {
            case EYE_CLOSE -> "CLOSE";
            case EYE_OPEN -> "OPEN";
            case EYE_RANDOM -> "RANDOM";
            case EYE_UNKNOWN -> "UNKNOWN";
            default -> "UNKNOWN";
        };
    }

    private String qualityLevelLabel(int level) {
        return switch (level) {
            case QUALITY_LOW -> "LOW";
            case QUALITY_MEDIUM -> "MEDIUM";
            case QUALITY_HIGH -> "HIGH";
            default -> "LOW";
        };
    }

    private void ensureWorker(Long workerId) {
        ensureEngineReady();
        if (workerId == null || workerId <= 0) {
            throw new IllegalArgumentException("workerId is invalid");
        }
        User worker = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getId, workerId)
                .eq(User::getRole, 3)
                .last("limit 1"));
        if (worker == null) {
            throw new IllegalArgumentException("worker not found");
        }
    }

    private void ensureEngineReady() {
        if (!runtime.isReady() || runtime.getEngine() == null) {
            throw new IllegalStateException("face engine not ready: " + runtime.getMessage());
        }
    }

    private float[] extractFeature(String imageBase64) {
        SeetaImageData imageData = toSeetaImageData(imageBase64);
        return extractFeature(imageData);
    }

    private float[] extractFeature(SeetaImageData imageData) {
        try {
            float[] feature = runtime.getEngine().extractMaxFace(imageData);
            if (feature == null || feature.length == 0) {
                throw new IllegalArgumentException("no valid face detected");
            }
            return feature;
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            log.error("Extract face feature failed", e);
            throw new IllegalStateException("extract face feature failed: " + e.getMessage());
        }
    }

    private SeetaImageData toSeetaImageData(String imageBase64) {
        try {
            byte[] imageBytes = decodeBase64Image(imageBase64);
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageBytes));
            if (image == null) {
                throw new IllegalArgumentException("image decode failed");
            }
            SeetaImageData imageData = new SeetaImageData(image.getWidth(), image.getHeight(), 3);
            imageData.data = toBgr(image);
            return imageData;
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalArgumentException("image parse failed: " + e.getMessage());
        }
    }

    private byte[] decodeBase64Image(String imageBase64) {
        if (imageBase64 == null || imageBase64.trim().isEmpty()) {
            throw new IllegalArgumentException("imageBase64 is empty");
        }
        String text = imageBase64.trim();
        int comma = text.indexOf(',');
        if (comma > 0 && text.substring(0, comma).contains("base64")) {
            text = text.substring(comma + 1);
        }
        try {
            return Base64.getDecoder().decode(text);
        } catch (Exception e) {
            throw new IllegalArgumentException("imageBase64 format invalid");
        }
    }

    private byte[] toBgr(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        int[] rgb = image.getRGB(0, 0, width, height, null, 0, width);
        byte[] bgr = new byte[width * height * 3];
        for (int i = 0, j = 0; i < rgb.length; i++, j += 3) {
            bgr[j] = (byte) (rgb[i] & 0xff);
            bgr[j + 1] = (byte) ((rgb[i] >> 8) & 0xff);
            bgr[j + 2] = (byte) ((rgb[i] >> 16) & 0xff);
        }
        return bgr;
    }

    private String livenessLabel(int status) {
        return switch (status) {
            case LIVENESS_REAL -> "REAL";
            case LIVENESS_SPOOF -> "SPOOF";
            case LIVENESS_FUZZY -> "FUZZY";
            case LIVENESS_DETECTING -> "DETECTING";
            default -> "UNKNOWN";
        };
    }

    private void saveFeature(Long workerId, float[] feature) {
        featureCache.put(workerId, feature);
        String encoded = encodeFeature(feature);
        try {
            // Redis保存特征后，服务重启也能继续读取维修人员已注册的人脸信息。
            redisUtil.set(redisKey(workerId), encoded);
        } catch (Exception ex) {
            log.warn("Save face feature to redis failed, use local cache only. workerId={}", workerId, ex);
        }
    }

    private float[] loadFeature(Long workerId) {
        float[] cached = featureCache.get(workerId);
        if (cached != null && cached.length > 0) {
            return cached;
        }
        try {
            String encoded = redisUtil.get(redisKey(workerId));
            if (encoded == null || encoded.isBlank()) {
                return null;
            }
            float[] decoded = decodeFeature(encoded);
            if (decoded.length > 0) {
                featureCache.put(workerId, decoded);
            }
            return decoded;
        } catch (Exception ex) {
            log.warn("Load face feature from redis failed, fallback local cache. workerId={}", workerId, ex);
            return featureCache.get(workerId);
        }
    }

    private String encodeFeature(float[] feature) {
        ByteBuffer buf = ByteBuffer.allocate(feature.length * 4);
        for (float val : feature) {
            buf.putFloat(val);
        }
        return Base64.getEncoder().encodeToString(buf.array());
    }

    private float[] decodeFeature(String encoded) {
        byte[] bytes = Base64.getDecoder().decode(encoded);
        if (bytes.length % 4 != 0) {
            throw new IllegalArgumentException("invalid feature bytes");
        }
        float[] feature = new float[bytes.length / 4];
        ByteBuffer buf = ByteBuffer.wrap(bytes);
        for (int i = 0; i < feature.length; i++) {
            feature[i] = buf.getFloat();
        }
        return feature;
    }

    private String redisKey(Long workerId) {
        return FACE_REDIS_KEY_PREFIX + workerId;
    }

    @Data
    @AllArgsConstructor
    public static class FaceVerifyResult {
        private boolean match;
        private double score;
        private double threshold;
    }

    @Data
    @AllArgsConstructor
    public static class FaceLiveVerifyResult {
        private boolean match;
        private double score;
        private double threshold;
        private boolean livenessPassed;
        private int livenessStatus;
        private String livenessLabel;
        private boolean maskCheckSupported;
        private boolean maskPassed;
        private int maskStatus;
        private String maskLabel;
        private boolean eyeStateCheckSupported;
        private boolean eyesOpenPassed;
        private int leftEyeState;
        private String leftEyeLabel;
        private int rightEyeState;
        private String rightEyeLabel;
        private boolean qualityCheckSupported;
        private boolean qualityPassed;
        private double qualityScore;
        private int qualityLevel;
        private String qualityLevelLabel;
    }

    @Data
    @AllArgsConstructor
    private static class MaskCheck {
        private boolean supported;
        private boolean passed;
        private int status;
        private String label;

        private static MaskCheck unsupported(int status, String label) {
            return new MaskCheck(false, true, status, label);
        }
    }

    @Data
    @AllArgsConstructor
    private static class EyeStateCheck {
        private boolean supported;
        private boolean passed;
        private int leftState;
        private String leftLabel;
        private int rightState;
        private String rightLabel;

        private static EyeStateCheck unsupported(int leftState, int rightState, String leftLabel, String rightLabel) {
            return new EyeStateCheck(false, true, leftState, leftLabel, rightState, rightLabel);
        }
    }

    @Data
    @AllArgsConstructor
    private static class QualityCheck {
        private boolean supported;
        private boolean passed;
        private double score;
        private int level;
        private String levelLabel;

        private static QualityCheck unsupported(double score, int level, String levelLabel) {
            return new QualityCheck(false, true, score, level, levelLabel);
        }
    }
}
