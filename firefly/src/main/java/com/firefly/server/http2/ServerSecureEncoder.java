package com.firefly.server.http2;

import java.nio.ByteBuffer;

import com.firefly.codec.http2.stream.HTTPConnection;
import com.firefly.net.ByteBufferArrayOutputEntry;
import com.firefly.net.ByteBufferOutputEntry;
import com.firefly.net.EncoderChain;
import com.firefly.net.Session;
import com.firefly.utils.concurrent.Callback;

public class ServerSecureEncoder extends EncoderChain {

	@Override
	public void encode(Object message, Session session) throws Throwable {
		HTTPConnection connection = (HTTPConnection) session.getAttachment();

		switch (connection.getHttpVersion()) {
		case HTTP_2: {
			HTTP2ServerConnection http2ServerConnection = (HTTP2ServerConnection) connection;
			if (message instanceof ByteBufferArrayOutputEntry) {
				ByteBufferArrayOutputEntry outputEntry = (ByteBufferArrayOutputEntry) message;
				http2ServerConnection.getSSLSession().write(outputEntry.getData(), outputEntry.getCallback());
			} else if (message instanceof ByteBufferOutputEntry) {
				ByteBufferOutputEntry outputEntry = (ByteBufferOutputEntry) message;
				http2ServerConnection.getSSLSession().write(outputEntry.getData(), outputEntry.getCallback());
			} else {
				throw new IllegalArgumentException(
						"the http2 encoder must receive the ByteBufferOutputEntry and ByteBufferArrayOutputEntry, but this message type is "
								+ message.getClass());
			}
		}
			break;
		case HTTP_1_1:
			if (message instanceof ByteBuffer) {
				HTTP1ServerConnection http1ServerConnection = (HTTP1ServerConnection) connection;
				http1ServerConnection.getSSLSession().write((ByteBuffer) message, Callback.NOOP);
			} else {
				throw new IllegalArgumentException(
						"the http1 encoder must receive the ByteBuffer, but this message type is "
								+ message.getClass());
			}
			break;
		default:
			throw new IllegalStateException("server does not support the http version " + connection.getHttpVersion());
		}
	}

}
