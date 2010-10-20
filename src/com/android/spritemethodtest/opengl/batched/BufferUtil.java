/*
 * Copyright (C) 2010 Lance Nanek
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.spritemethodtest.opengl.batched;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

/**
 * Utility methods for working with graphics buffers containing quads made 
 * out of non-striped triangle pairs. Vertexes are reused via indexes and 
 * drawn using the OpenGL triangles draw mode. 
 *
 * @author Lance Nanek
 */
public class BufferUtil {
	/* TODO This method of reusing vertexes by indices is recommended by ImgTec 
	 * over triangle strips for best performance using their graphics solutions.
	 * Qualcomm, however, recommends using triangle strips and connecting quads
	 * that are separate from each other using colinear triangles, which are 
	 * invisible. It may be worth supporting both methods in the future, or 
	 * switching over to the Qualcomm recommended method if those phones need 
	 * more of a performance boost.
	 */

	/* TODO investigate using 2 vertex coordinates or 4 instead of 3 where 
	 * possible. The former uses less memory and is less data to process, 
	 * so may be faster in cases where Z coordinates are not needed. The later 
	 * might be faster due to better memory alignment.
	 */

	public static final int VERTS_PER_QUAD = 4;
	
	public static final int VERTS_PER_TRIANGLE = 3;

	public static final int BYTES_PER_INT = Integer.SIZE / Byte.SIZE;

	public static final int BYTES_PER_FLOAT = Float.SIZE / Byte.SIZE;
	
	public static final int BYTES_PER_SHORT = Short.SIZE / Byte.SIZE;
	
	public static final int BYTES_PER_COLOR = 4;
	
	public static final int DIMS_PER_VERT = 3;
	
	public static final int TEX_COORDS_PER_VERT = 2;
	
	public static final int TEX_COORDS_PER_QUAD = 
		VERTS_PER_QUAD * TEX_COORDS_PER_VERT;
    
    //2 triangles in non-strip mode require 6 vertexes.
	public static final int INDICES_PER_QUAD = 6;
    
    public static final int toDims(final int verts) {
    	return verts * DIMS_PER_VERT;
    }
    
    public static final int toComponents(final int verts) {
    	return verts * BYTES_PER_COLOR;
    }  
    
    public static final int toTexCoords(final int verts) {
    	return verts * TEX_COORDS_PER_VERT;
    }  
    
    public static final int toIndices(final int verts) {
    	return verts * INDICES_PER_QUAD / VERTS_PER_QUAD;
    }
    
	public static final ByteBuffer createDirectByteBuffer(final int capacity) {
		return ByteBuffer
    		.allocateDirect(capacity)
    		.order(ByteOrder.nativeOrder());
	}
		
	public static final ByteBuffer createDirectByteBuffer(final int capacity, 
			final ByteBuffer previous) {
		final ByteBuffer created = createDirectByteBuffer(capacity);
				
		if ( null != previous ) {
			previous.flip();	
			created.put(previous);
		}
		
		return created;
	}	
    
	public static final IntBuffer createDirectIntBuffer(final int capacity) {
		return createDirectByteBuffer(capacity  * BYTES_PER_INT).asIntBuffer();
	}
	
	public static final IntBuffer createDirectIntBuffer(final int capacity, 
			final IntBuffer previous) {
		final IntBuffer created = createDirectIntBuffer(capacity);
				
		if ( null != previous ) {
			previous.flip();	
			created.put(previous);
		}
		
		return created;
	}
    
	public static final FloatBuffer createDirectFloatBuffer(
			final int capacity) {
		return createDirectByteBuffer(
				capacity * BYTES_PER_FLOAT).asFloatBuffer();
	}	
	
	public static final FloatBuffer createDirectFloatBuffer(final int capacity, 
			final FloatBuffer previous) {
		final FloatBuffer created = createDirectFloatBuffer(capacity);
				
		if ( null != previous ) {
			previous.flip();	
			created.put(previous);
		}
		
		return created;
	}	
	
	public static final ShortBuffer createDirectShortBuffer(
			final int capacity) {
		return createDirectByteBuffer(
				capacity * BYTES_PER_SHORT).asShortBuffer();    	 
	}
	
	public static final ShortBuffer createDirectShortBuffer(final int capacity, 
			final ShortBuffer previous) {
		final ShortBuffer created = createDirectShortBuffer(capacity);
		
		if ( null != previous ) {
			previous.flip();	
			created.put(previous);
		}
		
		return created;
	}	

	public static final ShortBuffer createShortIndicesBuffer(
			final int capacity) {
		final ShortBuffer indices = createDirectShortBuffer(capacity);	 
		
		/* TODO investigate if generating the indices causes any appreciable
		 * load time and generate them at before packaging if so.
		 */		
		fillIndices(indices, 0, capacity);
		
		return indices;
	}
	
	private static final void fillIndices(final ShortBuffer indices, 
			final int start, final int limit) {
		final int capacity = limit - start;
		
		//Benchmarks show filling array, then filling buffer is faster than 
		//filling buffer 1 at a time.
		final short[] indicesArray = new short[capacity];

		short vertexNumber = 
			(short) (start * VERTS_PER_QUAD / INDICES_PER_QUAD);
		for(int i = 0; i < capacity; i += INDICES_PER_QUAD) {

			//Top left corner, only used once, part of first triangle.
			indicesArray[i] = vertexNumber++;//Index 0, vertex 0, for sprite.
			
			//Bottom left corner, used for both triangles.
			indicesArray[i+1] = vertexNumber;//Index 1, vertex 1, for sprite.
			indicesArray[i+4] = vertexNumber++;//Index 4, vertex 1, for sprite.
			
			//Top right corner, used for both triangles.
			indicesArray[i+2] = vertexNumber;//Index 2, vertex 2, for sprite.
			indicesArray[i+3] = vertexNumber++;//Index 3, vertex 2, for sprite.
			
			//Bottom right corner, only used once, part of second triangle.
			indicesArray[i+5] = vertexNumber++;//Index 5, vertex 3, for sprite.
		}		
		
		indices.position(start);
		indices.put(indicesArray);
		indices.rewind();
	}
	
	public static final ShortBuffer createShortIndicesBuffer(
			final int capacity, final ShortBuffer previous) {
		if ( null == previous ) {
			return createShortIndicesBuffer(capacity);
		}
			
		final int oldPosition = previous.position();

		//The index buffer is always completely filled out.
		previous.position(previous.capacity());
		final ShortBuffer created = createDirectShortBuffer(capacity, previous);
		
		fillIndices(created, previous.capacity(), capacity);

		created.position(oldPosition);
		
		return created;
	}
	
}
