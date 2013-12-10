package com.FalcoLabs.Fido.api.datastore.serializers;

import java.io.Serializable;
import java.nio.ByteBuffer;

import org.apache.commons.lang3.SerializationUtils;

public class BinarySerializer<T> {

    public ByteBuffer toByteBuffer(T obj) {
    	byte[] bytes = SerializationUtils.serialize((Serializable) obj);
    	return ByteBuffer.wrap(bytes);
    }
    
    @SuppressWarnings("unchecked")
	public T fromByteBuffer(ByteBuffer byteBuffer) {    	
		byte[] bytes = new byte[byteBuffer.remaining()];
		byteBuffer.get(bytes);
    	Object obj = SerializationUtils.deserialize(bytes);
    	return (T)obj;
    }
}
