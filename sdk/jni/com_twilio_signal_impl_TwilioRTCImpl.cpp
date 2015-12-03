/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
#include "TSCoreSDKTypes.h"
#include "TSCoreConstants.h"
#include "TSCoreSDK.h"
#include "TSCEndpoint.h"
#include "TSCEndpointObserver.h"
#include "TSCConfiguration.h"
#include "TSCLogger.h"
#include "webrtc/voice_engine/include/voe_base.h"
#include "webrtc/modules/video_capture/video_capture_internal.h"
#include "webrtc/modules/video_render/video_render_internal.h"
#include "talk/app/webrtc/java/jni/androidvideocapturer_jni.h"
#include "webrtc/modules/audio_device/android/audio_manager.h"
#include "webrtc/modules/audio_device/android/opensles_player.h"
#include "webrtc/modules/video_capture/android/device_info_android.h"

#include <string.h>
#include "com_twilio_signal_impl_TwilioRTCImpl.h"

#include "talk/app/webrtc/java/jni/jni_helpers.h"
#include "talk/app/webrtc/java/jni/classreferenceholder.h"

#define TAG  "TwilioSDK(native)"

using namespace webrtc_jni;
using namespace twiliosdk;

static TwilioCommon::AccessManager* extractNativeAccessMgr(JNIEnv* jni, jobject j_am) {
  jfieldID native_am_id = GetFieldID(jni,
      GetObjectClass(jni, j_am), "nativeDataChannel", "J");
  jlong j_d = GetLongField(jni, j_an, native_am_id);
  return reinterpret_cast<TwilioCommon::AccessManager*>(j_d);
}

/*
 * Class:     com_twilio_signal_impl_TwilioRTCImpl
 * Method:    initCore
 * Signature: (Landroid/content/Context;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_twilio_signal_impl_TwilioRTCImpl_initCore(JNIEnv *env, jobject obj, jobject context) {

	bool success = false;
	JavaVM * cachedJVM = NULL;

	env->GetJavaVM(&cachedJVM);

	// Perform webrtc_jni initialization to enable peerconnection_jni.cc JNI bindings.
	jint ret = webrtc_jni::InitGlobalJniVariables(cachedJVM);
	if(ret < 0) {
		TS_CORE_LOG_ERROR("TwilioSDK.InitGlobalJniVariables() failed");
		return success;
	} else {
		webrtc_jni::LoadGlobalClassReferenceHolder();
	}

	TSCSDK* tscSdk = TSCSDK::instance();

	webrtc::videocapturemodule::DeviceInfoAndroid::Initialize(env);
	webrtc::OpenSLESPlayer::SetAndroidAudioDeviceObjects(cachedJVM, context);

	success |= webrtc::SetCaptureAndroidVM(cachedJVM, context);
	success |= webrtc::SetRenderAndroidVM(cachedJVM);

	// Required to setup an external capturer
	success |= webrtc_jni::AndroidVideoCapturerJni::SetAndroidObjects(env, context);

	TS_CORE_LOG_DEBUG("Calling DA Magic formula");
	success |= webrtc::VoiceEngine::SetAndroidObjects(cachedJVM, context);

	// TODO: check success and return appropriately 

	if (tscSdk != NULL && tscSdk->isInitialized())
	{
		return JNI_TRUE;
	}

	return JNI_FALSE;
}


JNIEXPORT jlong JNICALL Java_com_twilio_signal_impl_TwilioRTCImpl_createEndpoint
  (JNIEnv *env, jobject obj, jobject j_am, jlong nativeEndpointObserver) {

	if (token == NULL) {
		TS_CORE_LOG_ERROR("token is null");
		return 0;
	}
	//const char *tokenStr = env->GetStringUTFChars(token, 0);

	TSCOptions options;
	//options.insert(std::make_pair(kTSCTokenKey, tokenStr));

	if (!nativeEndpointObserver)
	{
		TS_CORE_LOG_ERROR("nativeEndpointObserver is null");
		return 0;
	}

	TSCEndpointObserverPtr eObserverPtr =
			TSCEndpointObserverPtr(reinterpret_cast<TSCEndpointObserver*>(nativeEndpointObserver));

	TwilioCommon::AccessManager* accessManager = extractNativeAccessMgr(env, j_am);

	TSCEndpointPtr endpoint = TSCSDK::instance()->createEndpoint(options, accessManager, eObserverPtr);

	return jlongFromPointer(endpoint.get());
}


JNIEXPORT void JNICALL Java_com_twilio_signal_impl_TwilioRTCImpl_setCoreLogLevel
  (JNIEnv *env, jobject obj, jint level) {
	TS_CORE_LOG_DEBUG("setCoreLogLevel");
	TSCoreLogLevel coreLogLevel = static_cast<TSCoreLogLevel>(level);
        TSCLogger::instance()->setLogLevel(coreLogLevel);
}


JNIEXPORT jint JNICALL Java_com_twilio_signal_impl_TwilioRTCImpl_getCoreLogLevel
  (JNIEnv *env, jobject obj) {
	TS_CORE_LOG_DEBUG("getCoreLogLevel");
        return TSCLogger::instance()->getLogLevel();
}
