/*
 * Copyright (C) 2017 Twilio, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.twilio.video;

import android.support.test.filters.LargeTest;

import com.twilio.video.base.BaseParticipantTest;
import com.twilio.video.helper.CallbackHelper;
import com.twilio.video.util.FrameCountRenderer;
import com.twilio.video.util.Topology;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeFalse;

@RunWith(Parameterized.class)
@LargeTest
public class RemoteVideoTrackTopologyParameterizedTest extends BaseParticipantTest {
    private static final int VIDEO_TRACK_TEST_DELAY_MS = 3000;

    @Parameterized.Parameters(name = "{0}")
    public static Iterable<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {Topology.P2P},
                {Topology.GROUP}});
    }

    private final Topology topology;

    public RemoteVideoTrackTopologyParameterizedTest(Topology topology) {
        this.topology = topology;
    }

    @Before
    public void setup() throws InterruptedException {
        super.baseSetup(topology);
    }

    @After
    public void teardown() throws InterruptedException {
        super.teardown();
        assertTrue(MediaFactory.isReleased());
    }

    @Test
    public void addRenderer_shouldNotCrashForNullRenderer() throws InterruptedException {
        CallbackHelper.FakeParticipantListener participantListener =
                new CallbackHelper.FakeParticipantListener();
        participantListener.onVideoTrackAddedLatch = new CountDownLatch(1);
        remoteParticipant.setListener(participantListener);
        actor2LocalVideoTrack = LocalVideoTrack.create(mediaTestActivity, true, fakeVideoCapturer);

        assertTrue(actor2LocalParticipant.addVideoTrack(actor2LocalVideoTrack));
        assertTrue(participantListener.onVideoTrackAddedLatch.await(20, TimeUnit.SECONDS));
        List<RemoteVideoTrack> remoteVideoTracks = remoteParticipant.getRemoteVideoTracks();
        assertEquals(1, remoteVideoTracks.size());
        remoteVideoTracks.get(0).addRenderer(null);
    }

    @Test
    public void canAddAndRemoveRenderer() throws InterruptedException {
        /*
         * TODO: GSDK-1152 skipping test for GROUP only because it is flaky.
         * Should be investigated after GA.
         */
        assumeFalse(topology == Topology.GROUP);
        CallbackHelper.FakeParticipantListener participantListener =
                new CallbackHelper.FakeParticipantListener();
        participantListener.onVideoTrackAddedLatch = new CountDownLatch(1);
        remoteParticipant.setListener(participantListener);
        actor2LocalVideoTrack = LocalVideoTrack.create(mediaTestActivity, true, fakeVideoCapturer);

        assertTrue(actor2LocalParticipant.addVideoTrack(actor2LocalVideoTrack));
        assertTrue(participantListener.onVideoTrackAddedLatch.await(20, TimeUnit.SECONDS));
        List<RemoteVideoTrack> remoteVideoTracks = remoteParticipant.getRemoteVideoTracks();
        assertEquals(1, remoteVideoTracks.size());
        FrameCountRenderer frameCountRenderer = new FrameCountRenderer();
        remoteVideoTracks.get(0).addRenderer(frameCountRenderer);
        assertEquals(1, remoteVideoTracks.get(0).getRenderers().size());
        assertTrue(frameCountRenderer.waitForFrame(VIDEO_TRACK_TEST_DELAY_MS));
        remoteVideoTracks.get(0).removeRenderer(frameCountRenderer);
        assertEquals(0, remoteVideoTracks.get(0).getRenderers().size());
    }

    @Test
    public void shouldFailToAddRendererOnRemovedTrack() throws InterruptedException {
        CallbackHelper.FakeParticipantListener participantListener =
                new CallbackHelper.FakeParticipantListener();
        participantListener.onVideoTrackAddedLatch = new CountDownLatch(1);
        remoteParticipant.setListener(participantListener);
        actor2LocalVideoTrack = LocalVideoTrack.create(mediaTestActivity, true, fakeVideoCapturer);

        assertTrue(actor2LocalParticipant.addVideoTrack(actor2LocalVideoTrack));
        assertTrue(participantListener.onVideoTrackAddedLatch.await(20, TimeUnit.SECONDS));
        List<RemoteVideoTrack> remoteVideoTracks = remoteParticipant.getRemoteVideoTracks();
        assertEquals(1, remoteVideoTracks.size());
        FrameCountRenderer frameCountRenderer = new FrameCountRenderer();
        RemoteVideoTrack remoteVideoTrack = remoteVideoTracks.get(0);

        actor1RoomListener.onParticipantDisconnectedLatch = new CountDownLatch(1);
        actor2Room.disconnect();
        assertTrue(actor1RoomListener.onParticipantDisconnectedLatch.await(20, TimeUnit.SECONDS));

        remoteVideoTrack.addRenderer(frameCountRenderer);
        assertFalse(frameCountRenderer.waitForFrame(VIDEO_TRACK_TEST_DELAY_MS));
    }

    @Test
    public void shouldEnableVideoTrackAfterConnectedToRoom() throws InterruptedException {
        CallbackHelper.FakeParticipantListener participantListener =
            new CallbackHelper.FakeParticipantListener();
        participantListener.onVideoTrackAddedLatch = new CountDownLatch(1);
        participantListener.onVideoTrackEnabledLatch = new CountDownLatch(1);
        remoteParticipant.setListener(participantListener);
        actor2LocalVideoTrack = LocalVideoTrack.create(mediaTestActivity, false, fakeVideoCapturer);

        assertTrue(actor2LocalParticipant.addVideoTrack(actor2LocalVideoTrack));
        assertTrue(participantListener.onVideoTrackAddedLatch.await(20, TimeUnit.SECONDS));
        assertFalse(actor1Room.getRemoteParticipants().get(0).getRemoteVideoTracks().get(0).isEnabled());

        actor2LocalVideoTrack.enable(true);
        assertTrue(participantListener.onVideoTrackEnabledLatch.await(20, TimeUnit.SECONDS));
        assertTrue(actor1Room.getRemoteParticipants().get(0).getRemoteVideoTracks().get(0).isEnabled());
    }
}
