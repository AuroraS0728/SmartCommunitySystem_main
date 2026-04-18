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

    private final SeetaFaceRuntime runtime;
    private final RedisUtil redisUtil;
    private final UserMapper userMapper;

    // Local cache to reduce repeated redis decode cost.
    private final Map<Long, float[]> featureCache = new ConcurrentHashMap<>();

    public String registerFace(Long workerId, String imageBase64) {
        ensureWorker(workerId);
        float[] feature = extractFeature(imageBase64);
        saveFeature(workerId, feature);
        log.info("Worker face registered. workerId={}", workerId);
        return "registered";
    }

    public FaceVerifyResult verifyFaceWithScore(Long workerId, String imageBase64) {
        ensureWorker(workerId);
        float[] stored = loadFeature(workerId);
        if (stored == null || stored.length == 0) {
            throw new IllegalArgumentException("该维修员尚未注册人脸");
        }
        float[] current = extractFeature(imageBase64);
        float score = runtime.getEngine().calculateSimilarity(stored, current);
        double threshold = runtime.getProperties().getSimilarityThreshold();
        boolean match = score >= threshold;
        log.info("Worker face verify finished. workerId={}, score={}, threshold={}, match={}",
                workerId, score, threshold, match);
        return new FaceVerifyResult(match, score, threshold);
    }

    public boolean hasRegisteredFace(Long workerId) {
        ensureWorker(workerId);
        float[] feature = loadFeature(workerId);
        return feature != null && feature.length > 0;
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
            throw new IllegalArgumentException("维修员不存在");
        }
    }

    private void ensureEngineReady() {
        if (!runtime.isReady() || runtime.getEngine() == null) {
            throw new IllegalStateException("人脸引擎未就绪: " + runtime.getMessage());
        }
    }

    private float[] extractFeature(String imageBase64) {
        try {
            byte[] imageBytes = decodeBase64Image(imageBase64);
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageBytes));
            if (image == null) {
                throw new IllegalArgumentException("图片解码失败");
            }
            // Convert image to SeetaImageData(BGR) then extract one max face feature.
            SeetaImageData imageData = new SeetaImageData(image.getWidth(), image.getHeight(), 3);
            imageData.data = toBgr(image);
            float[] feature = runtime.getEngine().extractMaxFace(imageData);
            if (feature == null || feature.length == 0) {
                throw new IllegalArgumentException("未检测到有效人脸");
            }
            return feature;
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            log.error("Extract face feature failed", e);
            throw new IllegalStateException("提取人脸特征失败: " + e.getMessage());
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

    private void saveFeature(Long workerId, float[] feature) {
        featureCache.put(workerId, feature);
        String encoded = encodeFeature(feature);
        try {
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
}
