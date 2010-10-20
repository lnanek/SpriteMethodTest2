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
import static javax.microedition.khronos.opengles.GL10.*;

import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.opengles.GL10;

/**
 * Holds buffers for vertex positions and texture data that are used by 
 * multiple entities drawn multiple ways. This allows the vertex and texture
 * pointer set calls to made as infrequently as possible. It also helps drawing
 * more entities per draw call. Batching multiple entities into fewer draw 
 * calls is needed once you get a very large number and it becomes too slow 
 * to call one method per thing you want to draw.
 *
 */
public class SharedBuffers {
		
	public static final int VERT_CAPACITY_INCREASE_STEP = 50 * VERTS_PER_QUAD;
			
	private IntBuffer dims;
	
	private IntBuffer texCoords;

	private ShortBuffer indices;
	
	private int mVertCapacity;
	
	private int mUsedVerts;
	
	private boolean mPointersSet;
	
	private final Draw[] mRenders;
	
	public SharedBuffers(final int initialVertCapacity, final Draw[] renders) {
		mRenders = renders;
		createBuffers(initialVertCapacity);		
	}
	
	public void reset() {
		mPointersSet = false;
		
		for( int i = 0; i < mRenders.length; i++ ) {
			mRenders[i].vertCount = 0;
		}
		
		rewind();
	}
	
	public void rewind() {
		mUsedVerts = 0;
		dims.rewind();
		texCoords.rewind();
	}
		
	private void createBuffers(final int vertCapacity) {
				
		mVertCapacity = vertCapacity;
				
		dims = createDirectIntBuffer(toDims(vertCapacity), dims);
		texCoords = createDirectIntBuffer(toTexCoords(vertCapacity), texCoords);
		indices = createShortIndicesBuffer(toIndices(vertCapacity), indices);	
				
		mPointersSet = false;	
	}
	
	public void update(final DrawData[] updates) {			
		final int count = Math.min(updates.length, mRenders.length);
		for( int i = 0; i < count; i++ ) {
			update(updates[i], mRenders[i]);
		}		
	}
	
	private void update(final DrawData update, final Draw render) {
		if ( null == update || null == render ) return;
		
		render.vertCount = update.vertCount;
		render.vertOffset = dims.position() / DIMS_PER_VERT;
		
		if ( 0 == update.vertCount ) {
			return;
		}
		
		final int pastQuadBoundary = update.vertCount % 4;
		final int endOnQuadAdjustment = 
			0 == pastQuadBoundary ? 0 : 4 - pastQuadBoundary;
		
		mUsedVerts += update.vertCount + endOnQuadAdjustment;
		ensureCapacity();
		
		dims.put(update.dims, 0, update.vertCount * DIMS_PER_VERT);
		texCoords.put(
				update.texCoords, 0, update.vertCount * TEX_COORDS_PER_VERT);

		if ( endOnQuadAdjustment > 0 ) {
			dims.position(dims.position() 
					+ (endOnQuadAdjustment * DIMS_PER_VERT));
			texCoords.position(texCoords.position() 
					+ (endOnQuadAdjustment * TEX_COORDS_PER_VERT));
		}		

		update.reset();
	} 
		
	private void ensureCapacity() {
		if ( mUsedVerts > mVertCapacity ) {

			//Increase is minimum number of increase steps to reach amount.
			int newCapacity = mVertCapacity + VERT_CAPACITY_INCREASE_STEP;
			while ( newCapacity < mUsedVerts ) {
				newCapacity += VERT_CAPACITY_INCREASE_STEP;
			}
			createBuffers(newCapacity);
		}		
	}
	
	public void draw(final GL10 gl) {

		rewind();
		
		if ( !mPointersSet ) {
			gl.glVertexPointer(3, GL_FIXED, 0, dims);
		 	gl.glTexCoordPointer(2, GL_FIXED, 0, texCoords);
			mPointersSet = true;
		}		

		for( int i = 0; i < mRenders.length; i++ ) {
			mRenders[i].render(gl, indices);
		}
	}
	
}
