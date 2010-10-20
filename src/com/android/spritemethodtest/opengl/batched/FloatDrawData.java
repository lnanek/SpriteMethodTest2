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

import static com.android.spritemethodtest.opengl.batched.BufferUtil.*;
import static com.android.spritemethodtest.opengl.batched.SharedBuffers.*;

/**
 * Quick copy-paste-edit hack of {@link DrawData} to use floating point
 * instead of fixed point math. This is too slow for my own needs on phones 
 * without hardware floating point support like the G1. It is interesting 
 * to benchmark, however, and runs OK on newer phones like the Nexus One.
 *
 */
public class FloatDrawData {
	/* TODO investigate interleaving the vertex position and texture data.
	 * Qualcomm recommends against it for best performance, while ImgTec
	 * recommends doing it. One disadvantage in this implementation is that 
	 * it wouldn't allow setting the texture coordinates for each quad via 
	 * the fast system.arraycopy method, unless blanks were inserted in the 
	 * texture data and then the position data was set after, but that
	 * might cost more performance than it gained.
	 */
			
	public float[] dims;
	
	public float[] texCoords;
	
	public int vertCount = 0;
		
	private int mVertCapacity;
	
	public FloatDrawData(final int vertCapacity) {
		createArrays(vertCapacity);
	}
	
	private void createArrays(final int vertCapacity) {
	
		mVertCapacity = vertCapacity;
		dims = createArray(toDims(vertCapacity), dims, toDims(vertCount));
		texCoords = createArray(
			toTexCoords(vertCapacity), texCoords, toTexCoords(vertCount));
	}
		
	private float[] createArray(
			final int capacity, final float[] previous, final int used) {
		final float[] created = new float[capacity];
		if ( null != previous && 0 != used ) {
			System.arraycopy(previous, 0, created, 0, used);
		}
		return created;
	}
			
	public void quad(final float left, final float top, 
			final float width, final float height, 
			final float z, final float[] texture) {
		
		//Ensure arrays are big enough.
		{
			final int neededCapacity = vertCount + VERTS_PER_QUAD;
			boolean needCapacityIncrease = false;
			int newCapacity = mVertCapacity;
			while ( newCapacity < neededCapacity ) {
				needCapacityIncrease = true;
				newCapacity += VERT_CAPACITY_INCREASE_STEP;
			}
			if ( needCapacityIncrease ) {
				createArrays(newCapacity);
			}
		}
		
		//Calculate remaining vertex position information.
		int dimsOffset = vertCount * DIMS_PER_VERT;
		final float right = left + width;
		final float bottom = top + height;
		
		//Set vertex position information.
		
		//Left top.
		dims[dimsOffset++] = left;
		dims[dimsOffset++] = top;
		dims[dimsOffset++] = z;
		
		//Left bottom.
		dims[dimsOffset++] = left;
		dims[dimsOffset++] = bottom;
		dims[dimsOffset++] = z;
		
		//Right top.
		dims[dimsOffset++] = right;
		dims[dimsOffset++] = top;
		dims[dimsOffset++] = z;
		
		//Right bottom.
		dims[dimsOffset++] = right;
		dims[dimsOffset++] = bottom;
		dims[dimsOffset++] = z;
		
		//Set texture coordinates.
		final int texCoordsOffset = vertCount * TEX_COORDS_PER_VERT;
		System.arraycopy(
				texture, 0, texCoords, texCoordsOffset, TEX_COORDS_PER_QUAD);
		
		vertCount += VERTS_PER_QUAD;
	}
	
	public void reset() {
		vertCount = 0;
	}
		
}
