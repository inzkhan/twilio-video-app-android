package com.twilio.video;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.LargeTest;
import android.support.test.runner.AndroidJUnit4;

import com.twilio.video.base.BaseClientTest;
import com.twilio.video.helper.CallbackHelper;
import com.twilio.video.util.AccessTokenUtils;
import com.twilio.video.util.RandUtils;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static junit.framework.Assert.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class ParticipantTest extends BaseClientTest {
    private Context context;
    private String tokenOne;
    private String tokenTwo;
    private VideoClient videoClient;
    private VideoClient videoClient2;
    private String roomName;

    @Before
    public void setup() throws InterruptedException {
        super.setup();
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        tokenOne = AccessTokenUtils.getAccessToken(RandUtils.generateRandomString(10));
        tokenTwo = AccessTokenUtils.getAccessToken(RandUtils.generateRandomString(10));
        videoClient = new VideoClient(context, tokenOne);
        videoClient2 = new VideoClient(context, tokenTwo);
        roomName = RandUtils.generateRandomString(20);
    }

    @Test
    public void participantCanConnect() throws InterruptedException {
        CallbackHelper.FakeRoomListener roomListener = new CallbackHelper.FakeRoomListener();
        roomListener.onConnectedLatch = new CountDownLatch(1);
        roomListener.onDisconnectedLatch = new CountDownLatch(1);
        roomListener.onParticipantConnectedLatch = new CountDownLatch(1);
        ConnectOptions connectOptions = new ConnectOptions.Builder()
                .roomName(roomName)
                .build();
        Room room = videoClient.connect(connectOptions, roomListener);
        assertTrue(roomListener.onConnectedLatch.await(20, TimeUnit.SECONDS));
        assertEquals(RoomState.CONNECTED, room.getState());

        CallbackHelper.FakeRoomListener roomListener2 = new CallbackHelper.FakeRoomListener();
        Room room2 = videoClient2.connect(connectOptions, roomListener2);
        assertTrue(roomListener.onParticipantConnectedLatch.await(20, TimeUnit.SECONDS));
        assertEquals(1, room.getParticipants().size());

        room.disconnect();
        assertTrue(roomListener.onDisconnectedLatch.await(20, TimeUnit.SECONDS));
        assertEquals(RoomState.DISCONNECTED, room.getState());
        room2.disconnect();
    }

    @Test
    public void participantCanDisconnect() throws InterruptedException {
        CallbackHelper.FakeRoomListener roomListener = new CallbackHelper.FakeRoomListener();
        roomListener.onConnectedLatch = new CountDownLatch(1);
        roomListener.onDisconnectedLatch = new CountDownLatch(1);
        roomListener.onParticipantDisconnectedLatch = new CountDownLatch(1);
        roomListener.onParticipantConnectedLatch = new CountDownLatch(1);
        ConnectOptions connectOptions = new ConnectOptions.Builder()
                .roomName(roomName)
                .build();
        Room room = videoClient.connect(connectOptions, roomListener);
        assertTrue(roomListener.onConnectedLatch.await(20, TimeUnit.SECONDS));
        assertEquals(RoomState.CONNECTED, room.getState());

        CallbackHelper.FakeRoomListener roomListener2 = new CallbackHelper.FakeRoomListener();
        roomListener2.onConnectedLatch = new CountDownLatch(1);
        roomListener2.onDisconnectedLatch = new CountDownLatch(1);
        Room client2room = videoClient2.connect(connectOptions, roomListener2);

        assertTrue(roomListener2.onConnectedLatch.await(20, TimeUnit.SECONDS));

        List<Participant> client2Participants = new ArrayList<>(client2room.getParticipants().values());
        Participant client1Participant = client2Participants.get(0);

        assertEquals(1, client2Participants.size());
        assertTrue(client1Participant.isConnected());
        assertTrue(roomListener.onParticipantConnectedLatch.await(20, TimeUnit.SECONDS));

        List<Participant> client1Participants = new ArrayList<>(room.getParticipants().values());
        Participant client2Participant = client1Participants.get(0);

        assertEquals(1, client1Participants.size());
        assertTrue(client2Participant.isConnected());

        client2room.disconnect();
        assertTrue(roomListener2.onDisconnectedLatch.await(20, TimeUnit.SECONDS));
        assertTrue(roomListener.onParticipantDisconnectedLatch.await(20, TimeUnit.SECONDS));
        assertFalse(client2Participant.isConnected());
        assertTrue(room.getParticipants().isEmpty());

        room.disconnect();
        assertTrue(roomListener.onDisconnectedLatch.await(20, TimeUnit.SECONDS));
        assertFalse(client1Participant.isConnected());
    }
}
