LOCAL_PATH := $(call my-dir)
include $(LOCAL_PATH)/../../jni/CocosAndroid.mk

LOCAL_LDFLAGS += "-Wl,-z,max-page-size=16384"