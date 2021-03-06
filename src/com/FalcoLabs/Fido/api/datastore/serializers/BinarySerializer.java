/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Copyright 2013 Falco Labs LLC
 *
 */

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
