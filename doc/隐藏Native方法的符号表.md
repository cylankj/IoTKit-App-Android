####隐藏JNI函数符号
相当于java的`混淆`技术一样.

```
#1.在CMakeList.txt文件中添加这两行,就可以实现`native函数名称混淆`
SET(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -fvisibility=hidden")
SET(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -fvisibility=hidden")

#或者在Android.mk文件中
LOCAL_CFLAGS := -fvisibility=hidden

#2.在jni函数头部使用 extern "C" return_type JNICALL
```
####关于JNIEXPORT JNICALL
http://stackoverflow.com/questions/19422660/when-to-use-jniexport-and-jnicall-in-android-ndk
