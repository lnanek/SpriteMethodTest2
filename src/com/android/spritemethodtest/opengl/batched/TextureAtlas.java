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

import com.android.spritemethodtest.R;

/**
 * A large composite image containing several smaller images. This allows 
 * drawing any of the smaller images while the larger image is bound as a 
 * texture. This image was so simple that it was composed and the texture 
 * coordinates calculate by hand. Usually you would use a tool do to that like 
 * the mk_atlas.pl script from the Cocos2D game framework:<br />
 * http://github.com/cocos2d/cocos2d-iphone/blob/212f546c9656e0f6b164df89198164ca31311497/tools/mkatlas.pl
 * <p>
 * Generating the atlas at app start time can result in long load times and 
 * high initial memory use, so generating the image before packaging the app 
 * like this is needed for large games.
 * 
 * @author Lance Nanek
 *
 */
public class TextureAtlas {
	
	/**
	 * Resource ID of the large composite image.
	 */
	public static final int RESOURCE_ID = R.raw.skate_atlas; 
	
	/**
	 * Resource ID of the background image. It is handled on its own for now.
	 */
	public static final int BACKGROUND_RESOURCE_ID = R.drawable.background; 
	
	/**
	 * Gets the fixed point texture coordinates for the requested resource ID 
	 * inside the atlas. Normally you do not include the individual resources 
	 * in your app, but because this is a benchmark app it has both the atlas 
	 * and the individual images. This function converts between them.
	 * 
	 * @param resourceId int resource ID
	 * @return int[] or null if not found
	 */
	public static final int[] getFixedCoords(final int resourceId) {
		switch ( resourceId ) {
			case R.drawable.skate1:
				return SKATE_1_FIXED;
			case R.drawable.skate2:
				return SKATE_2_FIXED;
			case R.drawable.skate3:
				return SKATE_3_FIXED;
			case R.drawable.background:
				return BACKGROUND_FIXED;
		}
		return null;
	}
	
	/**
	 * Gets the floating point texture coordinates for the requested resource ID 
	 * inside the atlas. See {@link #getFixedCoords(int)} for why both versions 
	 * of the resource are available.
	 * 
	 * @param resourceId int resource ID
	 * @return float[] or null if not found
	 */	
	public static final float[] getFloatCoords(final int resourceId) {
		switch ( resourceId ) {
			case R.drawable.skate1:
				return SKATE_1_FLOAT;
			case R.drawable.skate2:
				return SKATE_2_FLOAT;
			case R.drawable.skate3:
				return SKATE_3_FLOAT;
			case R.drawable.background:
				return BACKGROUND_FLOAT;
		}
		return null;
	}
	
	private static final int[] fix(final float[] input) {
		final int[] result = new int[input.length];
		for(int i = 0; i < input.length; i++ ) {
			result[i] = FixedPointUtil.fix(input[i]);
		}
		return result;
	}
	
	private static final float[] BACKGROUND_FLOAT = new float[] {
        // U, V
		0f, 1f,
		0f, 0f,
		1f, 1f,
		1f, 0f,
	};
	
	private static final float[] SKATE_1_FLOAT = new float[] {
        // U, V
		0f, 0.25f,
		0f,    0f,
		1f, 0.25f,
		1f,    0f,
	};
	
	private static final float[] SKATE_2_FLOAT = new float[] {
        // U, V
		0f, 0.50f,
		0f, 0.25f,
		1f, 0.50f,
		1f, 0.25f,
	};
	
	private static final float[] SKATE_3_FLOAT = new float[] {
        // U, V
		0f, 0.75f,
		0f, 0.50f,
		1f, 0.75f,
		1f, 0.50f,
	};

	
	private static final int[] BACKGROUND_FIXED = fix(BACKGROUND_FLOAT);

	private static final int[] SKATE_1_FIXED = fix(SKATE_1_FLOAT);

	private static final int[] SKATE_2_FIXED = fix(SKATE_2_FLOAT);

	private static final int[] SKATE_3_FIXED = fix(SKATE_3_FLOAT);
}
