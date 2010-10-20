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

import java.nio.ShortBuffer;

import javax.microedition.khronos.opengles.GL10;

/**
 * Calls an OpenGL draw call. Often you can fit all your data in one buffer, 
 * but still need separate draw calls in order to change some OpenGL state 
 * in between the different groups of things being drawn. For example, 
 * you may want to disable blending while drawing one group of things and 
 * then enable it for another group, but still draw using data from the same 
 * graphics buffer. Blending is expensive so this can be faster than drawing 
 * everything with blending enabled.
 * 
 * @author Lance Nanek
 *
 */
public class Draw {
	
	int vertOffset;
	
	int vertCount;

	public void render(final GL10 gl, final ShortBuffer indices) {
		if ( 0 != vertCount ) {
			final int indicesCount = 
				vertCount * INDICES_PER_QUAD / VERTS_PER_QUAD;
			final int indicesOffset = 
				vertOffset * INDICES_PER_QUAD / VERTS_PER_QUAD;
			indices.position(indicesOffset);
			gl.glDrawElements(GL_TRIANGLES, 
					indicesCount, GL_UNSIGNED_SHORT, indices);	
		}		
	}
}
