#include "JniUtils.h"

#include <map>
#include <string>

using namespace std;

static ImageDataFieldID _ImageDataFieldID("com/seetaface/model/SeetaImageData");
static RectFieldID _RectFieldID("com/seetaface/model/SeetaRect");
static PointFieldID _PointFieldID("com/seetaface/model/SeetaPointF");
static RecognizeFieldID _RecognizeFieldID("com/seetaface/model/RecognizeResult");

JniUtils::JniUtils()
{
}


JniUtils::~JniUtils()
{
}

/*
 * 将java的SeetaImageData对象转成C++的SeetaImageData 
 */
SeetaImageData* JniUtils::toSeetaImageData(JNIEnv *env, jobject jimg){
	ImageDataFieldID field = getImageDataFieldID(env);

	//获取人脸图片数据，转为SeetaImageData对象
	SeetaImageData* imgdata = new SeetaImageData();
	jbyteArray jbytes = (jbyteArray)env->GetObjectField(jimg, field.data);
	imgdata->data = (unsigned char*)env->GetByteArrayElements(jbytes, 0);
	imgdata->width = env->GetIntField(jimg, field.width);
	imgdata->height = env->GetIntField(jimg, field.height);
	imgdata->channels = env->GetIntField(jimg, field.channels);
	//printf("img data: width=%i,height=%i,channels=%i\n", imgdata->width, imgdata->height, imgdata->channels);
	//删除局部引用
	env->DeleteLocalRef(jbytes);
	return imgdata;
}

/*
* 将java的SeetaRect对象转成C++的SeetaRect
*/
SeetaRect* JniUtils::toSeetaRect(JNIEnv *env, jobject jface) {
	RectFieldID field = getRectFieldID(env);

	SeetaRect *face = new SeetaRect();
	
	face->x = env->GetIntField(jface, field.x);
	face->y = env->GetIntField(jface, field.y);
	face->width = env->GetIntField(jface, field.width);
	face->height = env->GetIntField(jface, field.height);

	//删除局部引用
	env->DeleteLocalRef(jface);
	
	return face;
}

ImageDataFieldID JniUtils::getImageDataFieldID(JNIEnv *env) {
	//printf("\nget SeetaImageDataFieldID...\n");
	if (!_ImageDataFieldID.init) {
		//printf("init SeetaImageDataFieldID...\n");
		//获取SeetaImageData java对象
		jclass clazz = env->FindClass(_ImageDataFieldID.className);
		//获取SeetaImageData对象的属性
		_ImageDataFieldID.data = env->GetFieldID(clazz, "data", "[B");
		_ImageDataFieldID.width = env->GetFieldID(clazz, "width", "I");
		_ImageDataFieldID.height = env->GetFieldID(clazz, "height", "I");
		_ImageDataFieldID.channels = env->GetFieldID(clazz, "channels", "I");

		//初始化完成
		_ImageDataFieldID.init = true;

		env->DeleteLocalRef(clazz);
	}
	return _ImageDataFieldID;
}

RectFieldID JniUtils::getRectFieldID(JNIEnv *env) {
	//printf("get RectFieldID...\n");
	if (!_RectFieldID.init) {
		//printf("init RectFieldID...\n");
		//获取SeetaRect java对象
		jclass clazz = env->FindClass(_RectFieldID.className);
		//获取默认构造方法
		_RectFieldID.constructor = env->GetMethodID(clazz, "<init>", "()V");
		//获取SeetaRect对象的属性
		_RectFieldID.x = env->GetFieldID(clazz, "x", "I");
		_RectFieldID.y = env->GetFieldID(clazz, "y", "I");
		_RectFieldID.width = env->GetFieldID(clazz, "width", "I");
		_RectFieldID.height = env->GetFieldID(clazz, "height", "I");
		_RectFieldID.score = env->GetFieldID(clazz, "score", "F");

		//初始化完成
		_RectFieldID.init = true;

		env->DeleteLocalRef(clazz);
	}
	return _RectFieldID;
}

PointFieldID JniUtils::getPointFieldID(JNIEnv *env) {
	//printf("get PointFieldID...\n");
	if (!_PointFieldID.init) {
		//printf("init PointFieldID...\n");
		//获取SeetaPointF java对象
		jclass clazz = env->FindClass(_PointFieldID.className);
		//获取默认构造方法
		_PointFieldID.constructor = env->GetMethodID(clazz, "<init>", "()V");
		//获取SeetaPointF对象的属性
		_PointFieldID.x = env->GetFieldID(clazz, "x", "D");
		_PointFieldID.y = env->GetFieldID(clazz, "y", "D");

		//初始化完成
		_PointFieldID.init = true;

		env->DeleteLocalRef(clazz);
	}
	return _PointFieldID;
}

RecognizeFieldID JniUtils::getRecognizeFieldID(JNIEnv * env) {
	if (!_RecognizeFieldID.init) {
		//获取RecognizeResult java对象
		jclass clazz = env->FindClass(_RecognizeFieldID.className);
		//获取默认构造方法
		_RecognizeFieldID.constructor = env->GetMethodID(clazz, "<init>", "()V");
		//获取对象的属性
		_RecognizeFieldID.index = env->GetFieldID(clazz, "index", "I");
		_RecognizeFieldID.similar = env->GetFieldID(clazz, "similar", "F");

		//初始化完成
		_RecognizeFieldID.init = true;

		env->DeleteLocalRef(clazz);
	}
	return _RecognizeFieldID;
}

jobject JniUtils::newObject(JNIEnv * env, JClassDef classDef){
	jclass clazz = env->FindClass(classDef.className);
	jobject obj = env->NewObject(clazz, classDef.constructor);

	env->DeleteLocalRef(clazz);

	return obj;
}

jfloatArray JniUtils::toJFloatArray(JNIEnv *env, int size, float* floatArray) {
	jfloatArray result = env->NewFloatArray(size);
	env->SetFloatArrayRegion(result, 0, size, floatArray);
	return result;
}
