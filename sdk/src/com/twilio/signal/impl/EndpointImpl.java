package com.twilio.signal.impl;

import java.util.Set;
import java.util.UUID;

import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Parcel;
import android.os.Parcelable;

import com.twilio.signal.Conversation;
import com.twilio.signal.ConversationException;
import com.twilio.signal.ConversationListener;
import com.twilio.signal.Endpoint;
import com.twilio.signal.EndpointListener;
import com.twilio.signal.LocalMediaImpl;
import com.twilio.signal.impl.core.CoreError;
import com.twilio.signal.impl.core.EndpointObserver;
import com.twilio.signal.impl.core.EndpointState;
import com.twilio.signal.impl.logging.Logger;
import com.twilio.signal.impl.util.CallbackHandler;

public class EndpointImpl implements Endpoint, NativeHandleInterface, Parcelable, EndpointObserver{

	static final Logger logger = Logger.getLogger(EndpointImpl.class);
	
	class EndpointObserverInternal implements NativeHandleInterface {
		
		private long nativeEndpointObserver;
		
		public EndpointObserverInternal(EndpointObserver observer) {
			//this.listener = listener;
			this.nativeEndpointObserver = wrapNativeObserver(observer, EndpointImpl.this);
		}

		private native long wrapNativeObserver(EndpointObserver observer, Endpoint endpoint);
		//::TODO figure out when to call this - may be Endpoint.release() ??
		private native void freeNativeObserver(long nativeEndpointObserver);

		@Override
		public long getNativeHandle() {
			return nativeEndpointObserver;
		}
		
	}

	private final UUID uuid = UUID.randomUUID();
	private Context context;
	private EndpointListener listener;
	private String userName;
	private PendingIntent incomingIntent = null;
	private EndpointObserverInternal endpointObserver;
	private long nativeEndpointHandle;

	private Handler handler;
	private EndpointState coreState;
	
	public UUID getUuid() {
		return uuid;
	}


	@Override
	public int hashCode() {
		return super.hashCode();
	}

	EndpointImpl(Context context,
			EndpointListener inListener) {
		this.context = context;
		this.listener = inListener;

		this.endpointObserver = new EndpointObserverInternal(this);
		// TODO: throw an exception if the handler returns null
		handler = CallbackHandler.create();
	}

	void setNativeHandle(long nativeEndpointHandle) {
		this.nativeEndpointHandle = nativeEndpointHandle;
	}
	
	long getEndpointObserverHandle() {
		return this.endpointObserver.getNativeHandle();
	}


	@Override
	public void listen() {
		//SignalCore.getInstance(this.context).register();
		if (nativeEndpointHandle != 0) {
			listen(nativeEndpointHandle);
		}
	}


	@Override
	public void unlisten() {
		//SignalCore.getInstance(this.context).unregister(this);
	}




	@Override
	public void setEndpointListener(EndpointListener listener) {
		// TODO Auto-generated method stub

	}

	@Override
	public String getAddress() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public boolean isListening() {
		// TODO Auto-generated method stub
		return false;
	}


	@Override
	public Conversation createConversation(Set<String> participants,
			LocalMediaImpl localMediaImpl, ConversationListener listener) {
		Conversation conv = ConversationImpl.create(this, participants, localMediaImpl, listener);
		return conv;
	}


	@Override /* Parcelable */
	public int describeContents()
	{
        return 0;
    }

	@Override /* Parcelable */
    public void writeToParcel(Parcel out, int flags)
	{
        out.writeSerializable(uuid);
    }

	/* Parcelable */
    public static final Parcelable.Creator<EndpointImpl> CREATOR = new Parcelable.Creator<EndpointImpl>()
    {
    	@Override
        public EndpointImpl createFromParcel(Parcel in)
        {
            UUID uuid = (UUID)in.readSerializable();
            TwilioRTCImpl twImpl = TwilioRTCImpl.getInstance();
            return twImpl.findDeviceByUUID(uuid);
        }

    	@Override
        public EndpointImpl[] newArray(int size)
        {
            throw new UnsupportedOperationException();
        }
    };


	public void onIncomingInvite() {
		logger.d("Received Incoming notification");
		if (incomingIntent != null) {
			logger.d("Received Incoming notification, calling intent");
			Intent intent = new Intent();
			intent.putExtra(Endpoint.EXTRA_DEVICE, this);
			if (intent.hasExtra(Endpoint.EXTRA_DEVICE)) {
				logger.d("Received Incoming notification, calling intent has extra");
			} else {
				logger.d("Received Incoming notification, calling intent do not have extra");
			}
			try {
				incomingIntent.send(context, 0, intent);
			} catch (final CanceledException e) {

			}
		}
	}

	@Override
	public long getNativeHandle() {
		return nativeEndpointHandle;
	}

	//Native implementation
	private native void listen(long nativeEndpoint);

	/**
	 * EndpointObserver methods
	 */
	@Override
	public void onRegistrationDidComplete(CoreError error) {
		logger.d("onRegistrationDidComplete");
		if (error != null) {
			ConversationException e =
					new ConversationException(error.getDomain(),
							error.getCode(), error.getMessage());
			listener.onFailedToStartListening(this, e);
		} else {
			listener.onStartListeningForInvites(this);
		}
	}


	@Override
	public void onUnregistrationDidComplete(CoreError error) {
		logger.d("onUnregistrationDidComplete");
		listener.onStopListeningForInvites(this);
	}


	@Override
	public void onStateDidChange(EndpointState state) {
		logger.d("onStateDidChange");
		coreState = state;
	}


	@Override
	public void onIncomingCallDidReceive(long nativeSession,
			String[] participants) {
		logger.d("onIncomingCallDidReceive");
		
		//TODO - Create incoming Conversation
		//TODO - Create Invite with that Conversation
		//TODO - notify consumer with Invite
		
	}


}
