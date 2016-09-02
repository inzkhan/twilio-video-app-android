package com.twilio.video;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A video track represents a single local or remote video source
 */
public class VideoTrack {
    private static final String WARNING_NULL_RENDERER = "Attempted to add a null renderer.";
    private static final Logger logger = Logger.getLogger(VideoTrack.class);

    private org.webrtc.VideoTrack webrtcVideoTrack;
    private String trackId;
    private Map<VideoRenderer, org.webrtc.VideoRenderer> videoRenderersMap = new HashMap<>();
    private boolean isEnabled;
    private long nativeVideoTrackContext;
    private boolean isReleased = false;

    VideoTrack(long nativeVideoTrackContext,
               String trackId,
               boolean isEnabled,
               long nativeWebrtcTrack) {
        this.nativeVideoTrackContext = nativeVideoTrackContext;
        this.trackId = trackId;
        this.isEnabled = isEnabled;
        this.webrtcVideoTrack = new org.webrtc.VideoTrack(nativeWebrtcTrack);
    }

    VideoTrack(org.webrtc.VideoTrack webRtcVideoTrack) {
        this.webrtcVideoTrack = webRtcVideoTrack;
    }

    /**
     * Add a video renderer to get video from the video track
     *
     * @param videoRenderer video renderer that receives video
     */
    public synchronized void addRenderer(VideoRenderer videoRenderer) {
        checkReleased();
        if (videoRenderer != null) {
            org.webrtc.VideoRenderer webrtcVideoRenderer =
                    createWebRtcVideoRenderer(videoRenderer);
            videoRenderersMap.put(videoRenderer, webrtcVideoRenderer);
            webrtcVideoTrack.addRenderer(webrtcVideoRenderer);
        } else {
            logger.w(WARNING_NULL_RENDERER);
        }
    }

    /**
     * Remove a video renderer to stop receiving video from the video track
     *
     * @param videoRenderer the video renderer that should no longer receives video
     */
    public synchronized void removeRenderer(VideoRenderer videoRenderer) {
        if (!isReleased && videoRenderer != null) {
            org.webrtc.VideoRenderer webrtcVideoRenderer =
                    videoRenderersMap.remove(videoRenderer);
            if (webrtcVideoTrack != null && webrtcVideoRenderer != null) {
                webrtcVideoTrack.removeRenderer(webrtcVideoRenderer);
            }
        }
    }

    /**
     * The list of renderers receiving video from this video track
     */
    public List<VideoRenderer> getRenderers() {
        return new ArrayList<>(videoRenderersMap.keySet());
    }

    /**
     * This video track id
     * @return track id
     */
    public String getTrackId() {
        return trackId;
    }

    /**
     * Check if this video track is enabled
     * @return true if track is enabled
     */
    public boolean isEnabled() {
        return isEnabled;
    }

    private org.webrtc.VideoRenderer createWebRtcVideoRenderer(VideoRenderer videoRenderer) {
        return new org.webrtc.VideoRenderer(new VideoRendererCallbackAdapter(videoRenderer));
    }

    void setEnabled(boolean isEnabled) {
        this.isEnabled = isEnabled;
    }

    synchronized void release() {
        if (nativeVideoTrackContext != 0) {
            isEnabled = false;
            videoRenderersMap.clear();
            webrtcVideoTrack = null;
            nativeRelease(nativeVideoTrackContext);
            nativeVideoTrackContext = 0;
            isReleased = true;
        }
    }

    private class VideoRendererCallbackAdapter implements org.webrtc.VideoRenderer.Callbacks {
        private final VideoRenderer videoRenderer;

        public VideoRendererCallbackAdapter(VideoRenderer videoRenderer) {
            this.videoRenderer = videoRenderer;
        }

        @Override
        public void renderFrame(org.webrtc.VideoRenderer.I420Frame frame) {
            videoRenderer.renderFrame(transformWebRtcFrame(frame));
        }

        private I420Frame transformWebRtcFrame(org.webrtc.VideoRenderer.I420Frame frame) {
            long frameNativePointer;
            try {
                Field nativeFramePointField = frame.getClass().getDeclaredField("nativeFramePointer");
                nativeFramePointField.setAccessible(true);
                frameNativePointer = nativeFramePointField.getLong(frame);
            } catch (NoSuchFieldException e) {
                throw new RuntimeException("Unable to retrieve I420 Frame native pointer");
            } catch( IllegalAccessException e) {
                throw new RuntimeException("Unable to retrieve I420 Frame native pointer");
            }
            return new I420Frame(frame.width,
                    frame.height,
                    frame.rotationDegree,
                    frame.yuvStrides,
                    frame.yuvPlanes,
                    frameNativePointer);
        }
    }

    org.webrtc.VideoTrack getWebrtcVideoTrack() {
        return webrtcVideoTrack;
    }

    private synchronized void checkReleased() {
        if (isReleased) {
            throw new IllegalStateException("The video track has been destroyed.");
        }
    }

    private native void nativeRelease(long nativeVideoTrackContext);
}

