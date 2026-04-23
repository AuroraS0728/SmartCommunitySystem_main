# SeetaFace JNI Extension Contract

To fully enable mask check, eye state check and quality check, the native `SeetaFace6JNI` library must export these JNI methods:

1. `Java_com_seetaface_SeetaFace6JNI_detectMask`
2. `Java_com_seetaface_SeetaFace6JNI_detectEyeState`
3. `Java_com_seetaface_SeetaFace6JNI_evaluateQuality`

## Java-side signatures

```java
public native int detectMask(SeetaImageData img);
public native int[] detectEyeState(SeetaImageData img);
public native float[] evaluateQuality(SeetaImageData img);
```

## Return value contract

### `detectMask`
- `0`: no mask
- `1`: mask detected
- `2`: unknown/error

### `detectEyeState`
- Return length must be `2`.
- index `0`: left eye state
- index `1`: right eye state
- state value:
  - `0`: close
  - `1`: open
  - `2`: random
  - `3`: unknown

### `evaluateQuality`
- Return format: `[passFlag, score, level]`
- `passFlag`: `1` for pass, `0` for fail
- `score`: quality score (float)
- `level`:
  - `0`: low
  - `1`: medium
  - `2`: high

## Runtime behavior in backend

- If a native method is not exported, backend falls back to `UNSUPPORTED` and does not hard-fail live verify.
- Once native library is rebuilt and replaced, backend will automatically start using the new checks.
