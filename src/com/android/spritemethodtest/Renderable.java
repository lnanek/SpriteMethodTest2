/*
 * Copyright (C) 2009 The Android Open Source Project
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

package com.android.spritemethodtest;

import static com.android.spritemethodtest.opengl.batched.FixedPointUtil.*;

/** 
 * Base class defining the core set of information necessary to render (and move
 * an object on the screen.  This is an abstract type and must be derived to
 * add methods to actually draw (see CanvasSprite and GLSprite).
 */
public abstract class Renderable {
	/* XXX This class has been hacked to keep track of both floating point 
	 * and fixed point versions of the data. This is handy for testing different 
	 * OpenGL drawing methods that use one or the other. It isn't useful for 
	 * benchmarking the simulation step, however, or for a real game. You'd 
	 * pick one or the other for those purposes. -Lance
	 */
	
    // Position in floats.
    protected float x;
    protected float y;
    protected float z;
    
    // Position in fixed points.
    protected int xFP;
    protected int yFP;
    protected int zFP;
    
    // Velocity.
    public float velocityX;
    public float velocityY;
    public float velocityZ;
    
    // Size in floats.
    protected float width;
    protected float height;

    // Size in fixed points.
    protected int widthFP;
    protected int heightFP;
    
	public void setX(final float x) {
		this.x = x;
		xFP = fix(x);
	}
	
	public float getX() {
		return x;
	}
	
	public void setY(final float y) {
		this.y = y;
		yFP = fix(y);
	}
	
	public float getY() {
		return y;
	}
	
	public void setZ(final float z) {
		this.z = z;
		zFP = fix(z);
	}
	
	public float getZ() {
		return z;
	}

	public void setWidth(final float width) {
		this.width = width;
		widthFP = fix(width);
	}

	public float getWidth() {
		return width;
	}

	public void setHeight(final float height) {
		this.height = height;
		heightFP = fix(height);
	}

	public float getHeight() {
		return height;
	}
}
