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

package com.android.spritemethodtest.opengl;
import static javax.microedition.khronos.opengles.GL10.*;
import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11Ext;

import com.android.spritemethodtest.Renderable;
import com.android.spritemethodtest.opengl.batched.DrawData;
import com.android.spritemethodtest.opengl.batched.FloatDrawData;
import com.android.spritemethodtest.opengl.batched.TextureAtlas;


/**
 * This is the OpenGL ES version of a sprite.  It is more complicated than the
 * CanvasSprite class because it can be used in more than one way.  This class
 * can draw using a grid of verts, a grid of verts stored in VBO objects, or
 * using the DrawTexture extension.
 */
public class GLSprite extends Renderable {
	
    // The OpenGL ES texture handle to draw.
    private int mTextureName;
    
    // The id of the original resource that mTextureName is based on.
    private int mResourceId;
    
    // If drawing with verts or VBO verts, the grid object defining those verts.
    private Grid mGrid;
    
    // If drawing batched verts, objects for setting data to be dumped into the 
    // graphics buffers all at once.
    private DrawData mDrawData;
    private int[] mFixedAtlasCoords;
    private float[] mFloatAtlasCoords;
    private DrawMethod mDrawMethod;
	private FloatDrawData mFloatDrawData;
    
    public GLSprite(int resourceId, DrawMethod drawMethod) {
        mDrawMethod = drawMethod;
        mResourceId = resourceId;
        mFixedAtlasCoords = TextureAtlas.getFixedCoords(resourceId);
        mFloatAtlasCoords = TextureAtlas.getFloatCoords(resourceId);
    }
    
    public void setTextureName(int name) {
        mTextureName = name;
    }
    
    public int getTextureName() {
        return mTextureName;
    }
    
    public void setResourceId(int id) {
        mResourceId = id;
    }
    
    public int getResourceId() {
        return mResourceId;
    }
    
    public void setGrid(Grid grid) {
        mGrid = grid;
    }
    
    public Grid getGrid() {
        return mGrid;
    }
    
    public void setDrawData(DrawData drawData, FloatDrawData floatDrawData) {
    	mDrawData = drawData;
		mFloatDrawData = floatDrawData;
    }
    
    public void draw(GL10 gl) {
    	
        switch ( mDrawMethod ) {
	    	case BASIC_VERT:
	    	case VBO:
	            // Draw using verts or VBO verts.
	            gl.glBindTexture(GL_TEXTURE_2D, mTextureName);
	            gl.glPushMatrix();
	            gl.glLoadIdentity();
	            gl.glTranslatef(x, y, z);
	            
	            mGrid.draw(gl, true, false);
	            
	            gl.glPopMatrix();
	            break;
	            
	    	case BATCHED_VERT_FLOAT:
	    		//Set the data to draw using batched verts.
	    		mFloatDrawData.quad(x, y, width, height, z, mFloatAtlasCoords);
	        	break;
	        	
	    	case BATCHED_VERT_FIXED:
	    		//Set the data to draw using batched verts.
	    		mDrawData.quad(xFP, yFP, widthFP, heightFP, zFP, mFixedAtlasCoords);
	        	break;
	        	
	    	case DRAW_TEXTURE_FLOAT:
	            // Draw using the DrawTexture extension.
	            gl.glBindTexture(GL_TEXTURE_2D, mTextureName);
	            ((GL11Ext) gl).glDrawTexfOES(x, y, z, width, height);
	            break;      	    		
	    		
	    	case DRAW_TEXTURE_FIXED:
	            // Draw using the DrawTexture extension.
	            gl.glBindTexture(GL_TEXTURE_2D, mTextureName);
	            ((GL11Ext) gl).glDrawTexxOES(xFP, yFP, zFP, widthFP, heightFP);    	
	            break;        	
		}
    }
}
