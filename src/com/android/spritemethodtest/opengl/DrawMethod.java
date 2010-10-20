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

package com.android.spritemethodtest.opengl;

import com.android.spritemethodtest.R;

/**
 * Methods of drawing sprites using OpenGL ES.
 * 
 * @author Lance Nanek
 *
 */
public enum DrawMethod {
	
	 BASIC_VERT(R.id.settingVerts),
	 BATCHED_VERT_FLOAT(R.id.settingBatchedVertsFloat),
	 BATCHED_VERT_FIXED(R.id.settingBatchedVertsFixed),
	 DRAW_TEXTURE_FLOAT(R.id.settingDrawTextureFloat),
	 DRAW_TEXTURE_FIXED(R.id.settingDrawTextureFixed),
	 VBO(R.id.settingVBO);
	
	public final int mRadioButtonID;
	
	private DrawMethod(final int radioButtonID) {
		mRadioButtonID = radioButtonID;
	}
	
	public static DrawMethod forRadio(final int selectedRadioButtonID) {
		for ( DrawMethod method : values() ) {
			if ( method.mRadioButtonID == selectedRadioButtonID ) {
				return method;
			}
		}
		throw new IllegalArgumentException("No associated draw method found.");
	}

}
