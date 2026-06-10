#ifndef INC_JFIELD_C_STRUCT_H
#define INC_JFIELD_C_STRUCT_H

#include <jni.h>

#ifdef __cplusplus
extern "C" {
#endif

struct JClassDef
{
	char *className;
	jmethodID constructor;
	bool init;

	JClassDef(char *_className) {
		className = _className;
	}
};

struct ImageDataFieldID : JClassDef
{
	jfieldID data;
	jfieldID width;
	jfieldID height;
	jfieldID channels;

	ImageDataFieldID(char *_className):JClassDef(_className){}

};

struct RectFieldID : JClassDef
{
	jfieldID x;
	jfieldID y;
	jfieldID width;
	jfieldID height;
	jfieldID score;
	RectFieldID(char *_className) :JClassDef(_className) {}
};

struct PointFieldID : JClassDef
{
	jfieldID x;
	jfieldID y;
	PointFieldID(char *_className) :JClassDef(_className) {}
};

struct RecognizeFieldID : JClassDef
{
	jfieldID index;
	jfieldID similar;
	RecognizeFieldID(char *_className) :JClassDef(_className) {}
};

#ifdef __cplusplus
}
#endif
#endif
