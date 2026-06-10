#include <seeta/CStruct.h>
#include <jni.h>
#include "CJFieldStruct.h"

class JniUtils
{
public:
	JniUtils();
	~JniUtils();

	//将java的SeetaImageData对象转成C++的SeetaImageData 
	static SeetaImageData* toSeetaImageData(JNIEnv *env, jobject jimg);

	//将java的SeetaRect[]对象转成C++的SeetaRect[]
	static SeetaRect* toSeetaRect(JNIEnv *env, jobject jface);

	static ImageDataFieldID getImageDataFieldID(JNIEnv *env);

	static RectFieldID getRectFieldID(JNIEnv *env);

	static PointFieldID getPointFieldID(JNIEnv *env);

	static RecognizeFieldID getRecognizeFieldID(JNIEnv *env);

	//创建对象
	static jobject newObject(JNIEnv *env, JClassDef classDef);

	static jfloatArray toJFloatArray(JNIEnv *env, int size, float* floatArray);
};