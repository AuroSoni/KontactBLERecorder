//
// Created by aurok on 28-09-2021.
//

#ifndef KONTACTBLERECORDER_KONTACTBLERECORDER_H
#define KONTACTBLERECORDER_KONTACTBLERECORDER_H

#include <jni.h>
#include <string>


void initialize();

void start_scan();
void stop_scan();

void add_beacon(std::string id, jint rssi);
void remove_beacon(std::string id);

void update_beacons(JNIEnv *env, jobjectArray beacons);

extern "C"
JNIEXPORT void JNICALL
Java_com_example_kontactblerecorder_MainActivity_initialize(JNIEnv *env, jobject thiz) {
    initialize();
}

extern "C"
JNIEXPORT void JNICALL
Java_com_example_kontactblerecorder_MainActivity_start_1scan(JNIEnv *env, jobject thiz) {
    start_scan();
}
extern "C"
JNIEXPORT void JNICALL
Java_com_example_kontactblerecorder_MainActivity_stop_1scan(JNIEnv *env, jobject thiz) {
    stop_scan();
}

extern "C"
JNIEXPORT void JNICALL
Java_com_example_kontactblerecorder_MainActivity_update_1beacons(JNIEnv *env, jobject thiz,
        jobjectArray ibeacons) {
    //TODO: I can parse the ibeacons array into C++ style beacon object array here and pass it on to the update_beacons function.
    update_beacons(env, ibeacons);
}
extern "C"
JNIEXPORT void JNICALL
Java_com_example_kontactblerecorder_MainActivity_add_1beacon(JNIEnv *env, jobject thiz, jstring id, jint rssi) {
    //Convert the jstring into a c-style string.
    //This memory needs to be deallocated.
    const char *str = env->GetStringUTFChars(id,0);

    //Get a C++ style string from the c-style string. Later on we may need to do string comparison.
    //I don't need to de-allocate this memory because it will be statically-allocated.
    //See here: https://stackoverflow.com/questions/8843604/string-c-str-deallocation-necessary
    std::string str_id = std::string(str);

    add_beacon(str_id, rssi);

    //Now de-allocate the memory for c-style string.
    delete str;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_example_kontactblerecorder_MainActivity_remove_1beacon(JNIEnv *env, jobject thiz, jstring id) {
    remove_beacon(env, id);
}

#endif //KONTACTBLERECORDER_KONTACTBLERECORDER_H

