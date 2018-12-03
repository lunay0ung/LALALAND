package com.example.luna.lalaland.All.Broadcaster;

import com.hannesdorfmann.mosby.mvp.MvpView;

import org.webrtc.EglBase;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoRenderer;

/**
 * Created by nhancao on 7/20/17.
 */

public interface BroadcasterView extends MvpView {

    void logAndToast(String msg);

    void disconnect();

    VideoCapturer createVideoCapturer();

    EglBase.Context getEglBaseContext();

    VideoRenderer.Callbacks getLocalProxyRenderer();
}
