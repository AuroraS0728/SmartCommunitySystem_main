#include <com_seetaface_SeetaFace6JNI.h>
#include <JniUtils.h>
#include <CJFieldStruct.h>

#include <string>
#include <memory>

#include <seeta/FaceDetector.h>
#include <seeta/FaceLandmarker.h>
#include <seeta/FaceRecognizer.h>
#include <seeta/FaceDatabase.h>
#include <seeta/FaceAntiSpoofing.h>

using namespace std;

#define LOGD printf //debug日志
#define LOGE printf //error日志

//是否初始化成功
static bool is_initialized = false;
const int landmark_num = 5;
const int crop_face_size = 256 * 256 * 3;

const int REAL = 0;   //真实人脸
const int SPOOF = 1;  // 攻击人脸（假人脸）
const int FUZZY = 2;  // 无法判断（人脸成像质量不好）
const int DETECTING = 3;  // 正在检测

seeta::FaceDetector* FD;//人脸检测器
seeta::FaceRecognizer* FR;//人脸识别器
seeta::FaceLandmarker* FL;//人脸定位器
seeta::FaceDatabase* DB; //人脸搜索数据库模块
seeta::FaceAntiSpoofing* FAS; //活体检测模块

/*
* Class:     com_seetaface_SeetaFace6JNI
* Method:    initModel
* Signature: (Ljava/lang/String;)Z
*/
JNIEXPORT jboolean JNICALL Java_com_seetaface_SeetaFace6JNI_initModel
(JNIEnv *env, jobject obj, jstring model_dir) {
	//如果已初始化则直接返回
	if (is_initialized) {
		return true;
	}

	if (NULL == model_dir) {
		return false;
	}
	string dir = env->GetStringUTFChars(model_dir, 0);
	if (dir.empty()) {
		return false;
	}
	string last_char = dir.substr(dir.length() - 1, 1);
	if ("\\" == last_char) {
		dir = dir.substr(0, dir.length() - 1) + "/";
	}
	else if (last_char != "/") {
		//目录补齐/
		dir += "/";
	}

	//检测模型路径
	string detect_model_path = dir + "face_detector.csta";
	//识别模型路径
	string recognizer_model_path = dir + "face_recognizer.csta";
	//关键点定位模型路径
	string landmarker_model_path = dir + "face_landmarker_pts5.csta";
	//活体检测模型路径
	string fas_first_model_path = dir + "fas_first.csta";
	string fas_second_model_path = dir + "fas_second.csta";

	// Initialize face detection model
	LOGD("Initialize face detection model: %s\n", detect_model_path.c_str());
	seeta::ModelSetting fd_setting;
	fd_setting.append(detect_model_path);
	FD = new seeta::FaceDetector(fd_setting);

	LOGD("Initialize face recognizer model: %s\n", recognizer_model_path.c_str());
	seeta::ModelSetting fr_setting;
	fr_setting.append(recognizer_model_path);
	FR = new seeta::FaceRecognizer(fr_setting);
	DB = new seeta::FaceDatabase(fr_setting);

	LOGD("Initialize face landmarker model: %s\n", landmarker_model_path.c_str());
	seeta::ModelSetting fl_setting;
	fl_setting.append(landmarker_model_path);
	FL = new seeta::FaceLandmarker(fl_setting);

	LOGD("Initialize face antiSpoofing model: %s, %s\n", fas_first_model_path.c_str(), fas_second_model_path.c_str());
	seeta::ModelSetting fas_setting;
	fas_setting.append(fas_first_model_path);
	fas_setting.append(fas_second_model_path);
	FAS = new seeta::FaceAntiSpoofing(fas_setting);

	LOGD("Initialized successfully!\n");
	is_initialized = true;
	return true;
}

/*
* Class:     com_seetaface_SeetaFace6JNI
* Method:    detect
* Signature: (Lcom/seetaface/model/SeetaImageData;)[Lcom/seetaface/model/SeetaRect;
*/
JNIEXPORT jobjectArray JNICALL Java_com_seetaface_SeetaFace6JNI_detect
(JNIEnv *env, jobject obj, jobject jimg) {
	if (!is_initialized) {
		LOGE("model not initialized! Please use initModel() to init.\n");
		return nullptr;
	}
	jclass clazz;
	jobjectArray jrect_array = nullptr;
	//将java的SeetaImageData对象转成C++的SeetaImageData
	SeetaImageData* image = JniUtils::toSeetaImageData(env, jimg);
	//LOGD("SeetaImageData: width=%d, height=%d, channels=%d\n", image->width, image->height, image->channels);
	SeetaFaceInfoArray faces = FD->detect(*image);//检测人脸

	//LOGD("detect face num: %i\n", faces.size);
	if (faces.size > 0) {//是否有检测到人脸
		//获取SeetaRect java对象
		RectFieldID rect_field = JniUtils::getRectFieldID(env);

		//构造一个指向jrect_class类的一维数组对象，该对象数组初始大小为face_num
		clazz = env->FindClass(rect_field.className);
		jrect_array = env->NewObjectArray(faces.size, clazz, nullptr);

		SeetaFaceInfo *face = faces.data;
		for (int i = 0; i < faces.size; ++i, ++face) {
			//创建java对象: new SeetaRect()
			jobject jrect = env->NewObject(clazz, rect_field.constructor);
			//设置人脸范围数据
			SeetaRect rect = face->pos;
			env->SetFloatField(jrect, rect_field.score, face->score);//分值
			env->SetIntField(jrect, rect_field.x, rect.x);//x坐标
			env->SetIntField(jrect, rect_field.y, rect.y);//y坐标
			env->SetIntField(jrect, rect_field.width, rect.width);//宽度
			env->SetIntField(jrect, rect_field.height, rect.height);//高度
			//添加到对象数组
			env->SetObjectArrayElement(jrect_array, i, jrect);
			//删除局部引用
			env->DeleteLocalRef(jrect);
		}
	}
	if (image->data) {
		delete[] image->data;
		image->data = nullptr;
		delete[] image;
		image = nullptr;
	}
	return jrect_array;
}

/*
* Class:     com_seetaface_SeetaFace6JNI
* Method:    mark
* Signature: (Lcom/seetaface/model/SeetaImageData;Lcom/seetaface/model/SeetaRect;)[Lcom/seetaface/model/SeetaPointF;
*/
JNIEXPORT jobjectArray JNICALL Java_com_seetaface_SeetaFace6JNI_mark
(JNIEnv *env, jobject obj, jobject jimg, jobject jface) {
	if (!is_initialized) {
		LOGE("model not initialized! Please use initModel() to init.\n");
		return nullptr;
	}
	
	jclass clazz;
	jobjectArray jpoint_array = nullptr;

	//将java的SeetaImageData对象转成C++的SeetaImageData
	SeetaImageData* image = JniUtils::toSeetaImageData(env, jimg);
	SeetaRect *face = JniUtils::toSeetaRect(env, jface);
	vector<SeetaPointF> points = FL->mark(*image, *face);

	if (points.size() > 0) {
		//获取SeetaPointF java对象
		PointFieldID point_field = JniUtils::getPointFieldID(env);
		int landmark_num = FL->number();
		//构造一个指向jpoint_class类的一维数组对象，该对象数组初始大小为landmark_num
		clazz = env->FindClass(point_field.className);
		jpoint_array = env->NewObjectArray(landmark_num, clazz, nullptr);
		int i = 0;
		for (auto &point : points) {
			//创建java对象: new SeetaPointF()
			jobject jpoint = env->NewObject(clazz, point_field.constructor); 
			//设置特征坐标数据
			env->SetDoubleField(jpoint, point_field.x, point.x);//x坐标
			env->SetDoubleField(jpoint, point_field.y, point.y);//y坐标
			//添加到对象数组
			env->SetObjectArrayElement(jpoint_array, i++, jpoint);
			//删除局部引用
			env->DeleteLocalRef(jpoint);
		}
		// 释放points内存
		points.clear();
		vector<struct SeetaPointF>().swap(points);
	}
	if (image->data) {
		delete[] image->data;
		image->data = nullptr;
		delete[] image;
		image = nullptr;
	}
	delete[] face;
	face = nullptr;

	return jpoint_array;
}

shared_ptr<float> extract(const SeetaImageData &image, const SeetaRect &rect) {
	shared_ptr<float> features(new float[FR->GetExtractFeatureSize()], default_delete<float[]>());
	vector<SeetaPointF> points = FL->mark(image, rect);
	FR->Extract(image, points.data(), features.get());
	// 释放points内存
	points.clear();
	vector<struct SeetaPointF>().swap(points);

	return features;
}

/*
* Class:     com_seetaface_SeetaFace6JNI
* Method:    compare
* Signature: (Lcom/seetaface/model/SeetaImageData;Lcom/seetaface/model/SeetaImageData;)F
*/
JNIEXPORT jfloat JNICALL Java_com_seetaface_SeetaFace6JNI_compare
(JNIEnv *env, jobject obj, jobject jimg1, jobject jimg2) {
	if (!is_initialized) {
		LOGE("model not initialized! Please use initModel() to init.\n");
		return -1;
	}
	float result = 0.0f;

	//将java的SeetaImageData对象转成C++的SeetaImageData
	SeetaImageData *image1 = JniUtils::toSeetaImageData(env, jimg1);
	SeetaFaceInfoArray face1 = FD->detect(*image1);
	if (face1.size == 0) {
		LOGD("No face detected in image1.\n");
		delete[] image1->data;
		image1->data = nullptr;

		return -1.1f;
	}

	SeetaImageData* image2 = JniUtils::toSeetaImageData(env, jimg2);
	SeetaFaceInfoArray face2 = FD->detect(*image2);
	if (face2.size == 0) {
		LOGD("No face detected in image2.\n");
		result = -1.2f;
	} else {
		shared_ptr<float> &feat1 = extract(*image1, face1.data->pos);
		shared_ptr<float> &feat2 = extract(*image2, face2.data->pos);
		int size = sizeof(feat1);
		result = FR->CalculateSimilarity(feat1.get(), feat2.get());
	}
	if (image1->data) {
		delete[] image1->data;
		image1->data = nullptr;
		delete[] image1;
		image1 = nullptr;
	}
	if (image2->data) {
		delete[] image2->data;
		image2->data = nullptr;
		delete[] image2;
		image2 = nullptr;
	}
	return result;
}

/*
* Class:     com_seetaface_SeetaFace6JNI
* Method:    extractCroppedFace
* Signature: ([B)[F
*/
JNIEXPORT jfloatArray JNICALL Java_com_seetaface_SeetaFace6JNI_extractCroppedFace
(JNIEnv *env, jobject obj, jbyteArray jbytes) {
	int chars_len = env->GetArrayLength(jbytes);
	if (chars_len != crop_face_size) {
		LOGD("Not croped face bytes, the size should be %d bytes, but it is %d bytes.\n", crop_face_size, chars_len);
		return nullptr;
	}
	SeetaImageData* image = new SeetaImageData();
	image->data = (unsigned char*)env->GetByteArrayElements(jbytes, 0);
	image->width = 256;
	image->height = 256;
	image->channels = 3;

	shared_ptr<float> features(new float[FR->GetExtractFeatureSize()], default_delete<float[]>());
	FR->ExtractCroppedFace(*image, features.get());
	if (image->data) {
		delete[] image->data;
		image->data = nullptr;
		delete[] image;
		image = nullptr;
	}

	return JniUtils::toJFloatArray(env, FR->GetExtractFeatureSize(), features.get());
}

/*
* Class:     com_seetaface_SeetaFace6JNI
* Method:    extractMaxFace
* Signature: (Lcom/seetaface/model/SeetaImageData;)[F
*/
JNIEXPORT jfloatArray JNICALL Java_com_seetaface_SeetaFace6JNI_extractMaxFace
(JNIEnv *env, jobject obj, jobject jimg) {
	SeetaImageData* image = JniUtils::toSeetaImageData(env, jimg);
	SeetaFaceInfoArray faceArray = FD->detect(*image);
	if (faceArray.size == 0) {
		return nullptr;
	}
	vector<struct SeetaFaceInfo> faces(faceArray.size);
	for (int i = 0; i < faceArray.size; ++i) {
		faces.push_back(faceArray.data[i]);
	}

	partial_sort(faces.begin(), faces.begin()+1, faces.end(), [](SeetaFaceInfo a, SeetaFaceInfo b) {
		return a.pos.width > b.pos.width;
	});
	vector<SeetaPointF> points = FL->mark(*image, faces[0].pos);
	shared_ptr<float> features(new float[FR->GetExtractFeatureSize()], default_delete<float[]>());
	FR->Extract(*image, points.data(), features.get());
	int size = FR->GetExtractFeatureSize();
	if (image->data) {
		delete[] image->data;
		image->data = nullptr;
		delete[] image;
		image = nullptr;
	}

	points.clear();
	vector<struct SeetaPointF>().swap(points);

	faces.clear();
	vector<struct SeetaFaceInfo>().swap(faces);

	return JniUtils::toJFloatArray(env, size, features.get());
}

/*
* Class:     com_seetaface_SeetaFace6JNI
* Method:    calculateSimilarity
* Signature: ([F[F)F
*/
JNIEXPORT jfloat JNICALL Java_com_seetaface_SeetaFace6JNI_calculateSimilarity
(JNIEnv *env, jobject obj, jfloatArray jfeat1, jfloatArray jfeat2) {
	float *feat1 = env->GetFloatArrayElements(jfeat1, 0);
	float *feat2 = env->GetFloatArrayElements(jfeat2, 0);
	float result = FR->CalculateSimilarity(feat1, feat2);
	return result;
}

/*
* Class:     com_seetaface_SeetaFace6JNI
* Method:    register
* Signature: (Lcom/seetaface/model/SeetaImageData;)L
*/
JNIEXPORT jlong JNICALL Java_com_seetaface_SeetaFace6JNI_register
(JNIEnv *env, jobject obj, jobject jimg) {
	if (!is_initialized) {
		LOGE("model not initialized! Please use initModel() to init.\n");
		return -1;
	}
	SeetaImageData* image = JniUtils::toSeetaImageData(env, jimg);
	SeetaFaceInfoArray faces = FD->detect(*image);
	if (faces.size == 0) {
		delete[] image->data;
		image->data = nullptr;

		return -1;
	}
	vector<SeetaPointF> points = FL->mark(*image, faces.data->pos);
	int64_t index = DB->Register(*image, &points[0]);

	if (image != NULL) {
		delete[] image->data;
		image->data = NULL;
		delete[] image;
		image = NULL;
	}
	points.clear();
	vector<struct SeetaPointF>().swap(points);

	return index;
}

/*
* Class:     com_seetaface_SeetaFace6JNI
* Method:    registerCroppedFace
* Signature: ([B)J
*/
JNIEXPORT jlong JNICALL Java_com_seetaface_SeetaFace6JNI_registerCroppedFace
(JNIEnv *env, jobject obj, jbyteArray jbytes) {
	int chars_len = env->GetArrayLength(jbytes);
	if (chars_len != crop_face_size) {
		LOGD("Not croped face bytes, the size should be %d bytes, but it is %d bytes.\n", crop_face_size, chars_len);
		return -1;
	}
	SeetaImageData* image = new SeetaImageData();
	image->data = (unsigned char*)env->GetByteArrayElements(jbytes, 0);
	image->width = 256;
	image->height = 256;
	image->channels = 3;

	int64_t index = DB->RegisterByCroppedFace(*image);
	if (image->data) {
		delete[] image->data;
		image->data = nullptr;
		delete[] image;
		image = nullptr;
	}
	return index;
}

/*
* Class:     com_seetaface_SeetaFace6JNI
* Method:    query
* Signature: (Lcom/seetaface/model/SeetaImageData;)Lcom/seetaface/model/RecognizeResult;
*/
JNIEXPORT jobject JNICALL Java_com_seetaface_SeetaFace6JNI_query
(JNIEnv *env, jobject obj, jobject jimg) {
	if (!is_initialized) {
		LOGE("model not initialized! Please use initModel() to init.\n");
		return nullptr;
	}

	SeetaImageData* image = JniUtils::toSeetaImageData(env, jimg);
	SeetaFaceInfoArray faces = FD->detect(*image);
	if (faces.size == 0) {
		delete[] image->data;
		image->data = nullptr;

		return nullptr;
	}

	float similar = 0;
	vector<SeetaPointF> points = FL->mark(*image, faces.data->pos);
	int64_t index = DB->Query(*image, &points[0], &similar);
	jobject jrecognize = nullptr;
	if (index >= 0) {
		//获取SeetaRect java对象
		RecognizeFieldID result_field = JniUtils::getRecognizeFieldID(env);
		//创建java对象: new RecognizeResult()
		jrecognize = JniUtils::newObject(env, result_field);
		//设置特征坐标数据
		env->SetLongField(jrecognize, result_field.index, index);//数据库索引
		env->SetFloatField(jrecognize, result_field.similar, similar);//相似度
	}
	if (image->data) {
		delete[] image->data;
		image->data = NULL;
		delete[] image;
		image = NULL;
	}
	points.clear();
	vector<struct SeetaPointF>().swap(points);

	return jrecognize;
}

/*
* Class:     com_seetaface_SeetaFace6JNI
* Method:    queryByCroppedFace
* Signature: ([B)Lcom/seetaface/model/RecognizeResult;
*/
JNIEXPORT jobject JNICALL Java_com_seetaface_SeetaFace6JNI_queryByCroppedFace
(JNIEnv *env, jobject obj, jbyteArray jbytes) {
	int chars_len = env->GetArrayLength(jbytes);
	if (chars_len != crop_face_size) {
		LOGD("Not croped face bytes, the size should be %d bytes, but it is %d bytes.\n", crop_face_size, chars_len);
		return nullptr;
	}
	SeetaImageData* image = new SeetaImageData();
	image->data = (unsigned char*)env->GetByteArrayElements(jbytes, 0);
	image->width = 256;
	image->height = 256;
	image->channels = 3;

	float similar = 0;
	int64_t index = DB->QueryByCroppedFace(*image, &similar);
	jobject jrecognize = nullptr;
	if (index >= 0) {
		//获取SeetaRect java对象
		RecognizeFieldID result_field = JniUtils::getRecognizeFieldID(env);
		//创建java对象: new RecognizeResult()
		jrecognize = JniUtils::newObject(env, result_field);
		//设置特征坐标数据
		env->SetLongField(jrecognize, result_field.index, index);//数据库索引
		env->SetFloatField(jrecognize, result_field.similar, similar);//相似度
	}
	if (image->data) {
		delete[] image->data;
		image->data = NULL;
		delete[] image;
		image = NULL;
	}

	return jrecognize;
}

/*
* Class:     com_seetaface_SeetaFace6JNI
* Method:    delete
* Signature: ([J)J
*/
JNIEXPORT jlong JNICALL Java_com_seetaface_SeetaFace6JNI_delete
(JNIEnv *env, jobject obj, jlongArray jindexes) {
	int len = env->GetArrayLength(jindexes);
	int64_t* indexes = env->GetLongArrayElements(jindexes, 0);
	
	size_t rows = 0;
	if (indexes[0] == -1) {
		rows = DB->Count();
		DB->Clear();
	} else {
		for (int i = 0; i < len; i++) {
			rows += DB->Delete(indexes[i]);
		}
	}
	return rows;
}

/*
* Class:     com_seetaface_SeetaFace6JNI
* Method:    crop
* Signature: (Lcom/seetaface/model/SeetaImageData;)[[B
*/
JNIEXPORT jobjectArray JNICALL Java_com_seetaface_SeetaFace6JNI_crop
(JNIEnv *env, jobject obj, jobject jimg) {
	if (!is_initialized) {
		LOGE("model not initialized! Please use initModel() to init.\n");
		return nullptr;
	}
	jobjectArray data_array = nullptr;
	SeetaImageData* image = JniUtils::toSeetaImageData(env, jimg);
	//LOGD("---width=%i, height=%i, channel=%i, size=%zi\n", image->width, image->height, image->channels, strlen((char *)image->data));
	SeetaFaceInfoArray faces = FD->detect(*image);
	//LOGD("---faces.size=%i\n", faces.size);
	if (faces.size > 0) {
		jclass byteArray = env->FindClass("[B");
		data_array = env->NewObjectArray(faces.size, byteArray, nullptr);

		SeetaFaceInfo *face = faces.data;
		for (int i = 0; i < faces.size; ++i, ++face) {
			vector<SeetaPointF> points = FL->mark(*image, face->pos);
			SeetaImageData cropped_face = FR->CropFaceV2(*image, points.data());
			//LOGD("face-%d: width=%i, height=%i, channel=%i\n", i, cropped_face.width, cropped_face.height, cropped_face.channels);
			char *buf = (char*)cropped_face.data;
			int size = cropped_face.width * cropped_face.height * cropped_face.channels;
			jbyteArray jbytes = env->NewByteArray(size);
			env->SetByteArrayRegion(jbytes, 0, size, (jbyte *)buf);

			//添加到对象数组
			env->SetObjectArrayElement(data_array, i, jbytes);

			//删除局部引用
			env->DeleteLocalRef(jbytes);
			// 释放points内存
			points.clear();
			vector<struct SeetaPointF>().swap(points);
		}
	}
	if (image->data) {
		delete[] image->data;
		image->data = nullptr;
		delete[] image;
		image = nullptr;
	}
	return data_array;
}

/*
* Class:     com_seetaface_SeetaFace6JNI
* Method:    predictImage
* Signature: (Lcom/seetaface/model/SeetaImageData;)I
*/
JNIEXPORT jint JNICALL Java_com_seetaface_SeetaFace6JNI_predictImage
(JNIEnv *env, jobject obj, jobject jimg) {
	if (!is_initialized) {
		LOGE("model not initialized! Please use initModel() to init.\n");
		return FUZZY;
	}

	SeetaImageData* image = JniUtils::toSeetaImageData(env, jimg);
	SeetaFaceInfoArray faces = FD->detect(*image);
	if (faces.size == 0) {
		delete[] image->data;
		image->data = nullptr;

		return FUZZY;
	}

	vector<SeetaPointF> points = FL->mark(*image, faces.data->pos);
	auto status = FAS->Predict(*image, faces.data->pos, &points[0]);

	if (image->data) {
		delete[] image->data;
		image->data = nullptr;
		delete[] image;
		image = nullptr;
	}
	points.clear();
	vector<struct SeetaPointF>().swap(points);
	switch (status) {
		case seeta::FaceAntiSpoofing::REAL:
			return REAL;
			break;
		case seeta::FaceAntiSpoofing::SPOOF:
			return SPOOF;
			break;
		case seeta::FaceAntiSpoofing::FUZZY:
			return FUZZY;
			break;
		case seeta::FaceAntiSpoofing::DETECTING:
			return DETECTING;
			break;
	}
	return FUZZY;
}