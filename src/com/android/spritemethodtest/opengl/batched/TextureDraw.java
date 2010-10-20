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

import static javax.microedition.khronos.opengles.GL10.*;

import java.nio.ShortBuffer;

import javax.microedition.khronos.opengles.GL10;

/**
 * Performs a draw call using a specified texture and blend mode.
 * 
 * @author Lance Nanek
 *
 */
public class TextureDraw extends Draw {
	
	private int textureName;
	
	private boolean blend;
	
	/**
	 * Creates a TextureDraw instance.
	 * 
	 * @param textureName int texture to bind
	 * @param blend boolean true to enable blending, false to disable
	 */
	public TextureDraw(int textureName, boolean blend) {
		this.textureName = textureName;
		this.blend = blend;
	}
	
	@Override
	public void render(GL10 gl, ShortBuffer indices) {
		if (blend) {
			gl.glEnable(GL_BLEND);
		} else {
			gl.glDisable(GL_BLEND);
		}
		gl.glBindTexture(GL_TEXTURE_2D, textureName);
		super.render(gl, indices);
	}
}