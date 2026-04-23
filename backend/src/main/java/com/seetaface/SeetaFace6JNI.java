package com.seetaface;

import com.seetaface.model.RecognizeResult;
import com.seetaface.model.SeetaImageData;
import com.seetaface.model.SeetaPointF;
import com.seetaface.model.SeetaRect;

/**
 * Local JNI declaration overlay for SeetaFace.
 * This file mirrors the extended native bridge methods.
 */
public class SeetaFace6JNI {

    public native boolean initModel(String modelDir);

    public native SeetaRect[] detect(SeetaImageData img);

    public native SeetaPointF[] mark(SeetaImageData img, SeetaRect faces);

    public native float compare(SeetaImageData img1, SeetaImageData img2);

    public native float[] extractCroppedFace(byte[] face);

    public native float[] extractMaxFace(SeetaImageData img);

    public native float calculateSimilarity(float[] features1, float[] features2);

    public native long register(SeetaImageData img);

    public native long registerCroppedFace(byte[] bytes);

    public native RecognizeResult query(SeetaImageData img);

    public native RecognizeResult queryByCroppedFace(byte[] bytes);

    public native long delete(long[] index);

    public native byte[][] crop(SeetaImageData img);

    public native int predictImage(SeetaImageData img);

    public native int predictVideo(SeetaImageData img);

    /** @return [clarity, reality] */
    public native float[] getLivenessScores();

    /** @return 0 no mask, 1 mask, 2 unknown/error */
    public native int detectMask(SeetaImageData img);

    /** @return [leftEyeState, rightEyeState] */
    public native int[] detectEyeState(SeetaImageData img);

    /** @return [passFlag(1/0), avgScore, minLevel] */
    public native float[] evaluateQuality(SeetaImageData img);

    /**
     * @return [
     * passFlag,
     * avgScore,
     * minLevel,
     * brightnessLevel, brightnessScore,
     * clarityLevel, clarityScore,
     * integrityLevel, integrityScore,
     * resolutionLevel, resolutionScore,
     * poseExLevel, poseExScore
     * ]
     */
    public native float[] evaluateQualityDetails(SeetaImageData img);

    /** @return age or -1 if failed/unsupported */
    public native int predictAge(SeetaImageData img);

    /** @return 0 male, 1 female, -1 unknown */
    public native int predictGender(SeetaImageData img);

    public native boolean initTracker(int videoWidth, int videoHeight);

    /** @return flattened array: [count, x,y,w,h,score,pid,frameNo,step, ...] */
    public native float[] track(SeetaImageData img);

    public native void resetTracker();
}
