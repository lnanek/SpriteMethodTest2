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

/**
 * Methods for working with fixed point numbers. Very little was needed for 
 * this benchmark program. If you need more there are good libraries for 
 * this:<br />
 * http://www.beartronics.com/imode/fplib/ <br />
 * http://manlyignition.blogspot.com/2008/11/arctan-in-j2me.html
S * 
 * @author Lance Nanek
 *
 */
public class FixedPointUtil {

	public static final int fix(final float input) {
		return (int) (input * (1 << 16));
	}
	
}
