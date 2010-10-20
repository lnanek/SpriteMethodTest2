/*
 * Copyright (C) 2008 The Android Open Source Project
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

import java.io.IOException;
import java.io.InputStream;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;
import javax.microedition.khronos.opengles.GL11Ext;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLUtils;
import android.util.Log;

import com.android.spritemethodtest.opengl.batched.Draw;
import com.android.spritemethodtest.opengl.batched.DrawData;
import com.android.spritemethodtest.opengl.batched.FloatDrawData;
import com.android.spritemethodtest.opengl.batched.FloatSharedBuffers;
import com.android.spritemethodtest.opengl.batched.SharedBuffers;
import com.android.spritemethodtest.opengl.batched.TextureAtlas;
import com.android.spritemethodtest.opengl.batched.TextureDraw;

/**
 * An OpenGL ES renderer based on the GLSurfaceView rendering framework.  This
 * class is responsible for drawing a list of renderables to the screen every
 * frame.  It also manages loading of textures and (when VBOs are used) the
 * allocation of vertex buffer objects.
 */
public class SimpleGLRenderer implements GLSurfaceView.Renderer {

	// Specifies the format our textures should be converted to upon load.
    private static BitmapFactory.Options sBitmapOptions
        = new BitmapFactory.Options();
    
    // An array of things to draw every frame.
    private GLSprite[] mSprites;
    
    // Pre-allocated arrays to use at runtime so that allocation during the
    // test can be avoided.
    private int[] mTextureNameWorkspace = new int[1];
    private int[] mCropWorkspace = new int[4];
    
    // A reference to the application context.
    private Context mContext;
    
    private DrawMethod mDrawMethod;
	
	private int mAtlasTextureId;
	
	private int mBackgroundTextureId;
	
	private int mVertCapacity;
	
	private Draw[] mDraws;
    
	private DrawData[] mDrawData;
	
	private SharedBuffers mSharedBuffers;
	
	private FloatDrawData[] mFloatDrawData;
	
	private FloatSharedBuffers mFloatSharedBuffers;
    
    public SimpleGLRenderer(
    		Context context, GLSprite[] sprites, DrawMethod drawMethod) {
        
        // Set our bitmaps to 16-bit, 565 format.
        sBitmapOptions.inPreferredConfig = Bitmap.Config.RGB_565;
        mContext = context;
        mSprites = null != sprites ? sprites : new GLSprite[] {};
    	mDrawMethod = drawMethod;
    }
    
    public int[] getConfigSpec() {
        // We don't need a depth buffer, and don't care about our
        // color depth.
        int[] configSpec = { EGL10.EGL_DEPTH_SIZE, 0, EGL10.EGL_NONE };
        return configSpec;
    }

	public void setDrawData(DrawData[] drawData, FloatDrawData[] floatDrawData, 
			int vertCapacity) {
		mDrawData = drawData;
		mFloatDrawData = floatDrawData;
		mVertCapacity = vertCapacity;
	}

    /** Draws the sprites. */
    public void drawFrame(GL10 gl) {
        switch ( mDrawMethod ) {
        	case BASIC_VERT:
        	case VBO:
                Grid.beginDrawing(gl, true, false);
                drawSprites(gl);
                Grid.endDrawing(gl);
                break;
                
        	case BATCHED_VERT_FLOAT:
                drawSprites(gl);
                mFloatSharedBuffers.update(mFloatDrawData);
                mFloatSharedBuffers.draw(gl);
            	break;
            	
        	case BATCHED_VERT_FIXED:
                drawSprites(gl);
                mSharedBuffers.update(mDrawData);
            	mSharedBuffers.draw(gl);
            	break;
            	
        	case DRAW_TEXTURE_FLOAT:
        	case DRAW_TEXTURE_FIXED:
                drawSprites(gl);       	
                break;        	
    	}
    }
    
    private void drawSprites(GL10 gl) {
        for (int x = 0; x < mSprites.length; x++) {
            mSprites[x].draw(gl);
        }   	
    }
    
    /* Called when the size of the window changes. */
    public void sizeChanged(GL10 gl, int width, int height) {
        /*
         * Some one-time OpenGL initialization can be made here probably based
         * on features of this particular context
         */
        gl.glEnable(GL10.GL_TEXTURE_2D);
        gl.glEnable(GL10.GL_BLEND);
        gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
        gl.glColor4x(0x10000, 0x10000, 0x10000, 0x10000);
        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);              
        gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
        gl.glDisableClientState(GL10.GL_COLOR_ARRAY);
        
        /*
         * By default, OpenGL enables features that improve quality but reduce
         * performance. One might want to tweak that especially on software
         * renderer.
         */
        gl.glDisable(GL10.GL_DITHER);
        gl.glDisable(GL10.GL_LIGHTING);
		gl.glDisable(GL_CULL_FACE);
    	gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_FASTEST);
        gl.glShadeModel(GL10.GL_FLAT);
        gl.glDisable(GL10.GL_DEPTH_TEST);
		gl.glDepthMask(false);
        
		
        gl.glViewport(0, 0, width, height);
        /*
         * Set our projection matrix. This doesn't have to be done each time we
         * draw, but usually a new projection needs to be set when the viewport
         * is resized.
         */
        gl.glMatrixMode(GL10.GL_PROJECTION);
        gl.glLoadIdentity();
        gl.glOrthof(0.0f, width, 0.0f, height, 0.0f, 1.0f);    	
        gl.glMatrixMode(GL10.GL_MODELVIEW); 
        
		
        // If we are using hardware buffers and the screen lost context
        // then the buffer indexes that we recorded previously are now
        // invalid.  Forget them here and recreate them below.
        if ( DrawMethod.VBO == mDrawMethod ) {
            for (int x = 0; x < mSprites.length; x++) {
                // Ditch old buffer indexes.
                mSprites[x].getGrid().invalidateHardwareBuffers();
            }
        }
        
        if ( DrawMethod.BATCHED_VERT_FIXED == mDrawMethod 
        		|| DrawMethod.BATCHED_VERT_FLOAT == mDrawMethod ) {
        	
        	mBackgroundTextureId = loadBitmap(
        			mContext, gl, TextureAtlas.BACKGROUND_RESOURCE_ID);
            mAtlasTextureId = loadBitmap(
            		mContext, gl, TextureAtlas.RESOURCE_ID);
            mDraws = new Draw[] {
            		new TextureDraw(mBackgroundTextureId, false),
            		new TextureDraw(mAtlasTextureId, true),
            };
        	mSharedBuffers = new SharedBuffers(mVertCapacity, mDraws);
        	mFloatSharedBuffers = new FloatSharedBuffers(mVertCapacity, mDraws);
        } else {
            // Load our texture and set its texture name on all sprites.
            
            // To keep this sample simple we will assume that sprites that share
            // the same texture are grouped together in our sprite list. A real
            // app would probably have another level of texture management, 
            // like a texture hash.
            
            int lastLoadedResource = -1;
            int lastTextureId = -1;
            
            for (int x = 0; x < mSprites.length; x++) {
                int resource = mSprites[x].getResourceId();
                if (resource > 0) {
	                if (resource != lastLoadedResource) {
	                    lastTextureId = loadBitmap(mContext, gl, resource);
	                    lastLoadedResource = resource;
	                }
	                mSprites[x].setTextureName(lastTextureId);
	                if ( DrawMethod.VBO == mDrawMethod ) {
	                    Grid currentGrid = mSprites[x].getGrid();
	                    if (!currentGrid.usingHardwareBuffers()) {
	                        currentGrid.generateHardwareBuffers(gl);
	                    }
	                }
                }
            }
        }
    	
 
        // Now's a good time to run the GC.  Since we won't do any explicit
        // allocation during the test, the GC should stay dormant and not
        // influence our results.
        Runtime r = Runtime.getRuntime();
        r.gc();
    }

    /**
     * Called whenever the surface is created.  This happens at startup, and
     * may be called again at runtime if the device context is lost (the screen
     * goes to sleep, etc).  This function must fill the contents of vram with
     * texture data and (when using VBOs) hardware vertex arrays.
     */
    public void surfaceCreated(GL10 gl) {
    	/* XXX At least with the GLSurfaceView built into Android, this method  
    	 * is not called often enough to always make sure your textures are 
    	 * loaded, particularly if you play around with leaving and coming back 
    	 * or switching to other games and back, things like that. So I just 
    	 * moved the init code to the more reliable sizeChanged method in case 
    	 * this method is broken in this implementation as well. -Lance
    	 */
    }
    
    /**
     * Called when the rendering thread shuts down.  This is a good place to
     * release OpenGL ES resources.
     * @param gl
     */
    public void shutdown(GL10 gl) {
        if ( DrawMethod.BATCHED_VERT_FIXED == mDrawMethod 
        		|| DrawMethod.BATCHED_VERT_FLOAT == mDrawMethod ) {

        	mAtlasTextureId = deleteTexture(gl, mAtlasTextureId);
        	mBackgroundTextureId = deleteTexture(gl, mBackgroundTextureId);
        } else {
            int lastFreedResource = -1;            
            for (int x = 0; x < mSprites.length; x++) {
                int resource = mSprites[x].getResourceId();
                if (resource != lastFreedResource) {                    
                	lastFreedResource = deleteTexture(
                			gl, mSprites[x].getTextureName());
                    mSprites[x].setTextureName(0);
                }
                if ( DrawMethod.VBO == mDrawMethod ) {
                    mSprites[x].getGrid().releaseHardwareBuffers(gl);
                }
            }
        }
    }
    
    private int deleteTexture(GL10 gl, int textureId) {
    	int[] textureToDelete = new int[1];
        textureToDelete[0] = textureId;
        gl.glDeleteTextures(1, textureToDelete, 0);
    	return 0;   	
    }
 
    /** 
     * Loads a bitmap into OpenGL and sets up the common parameters for 
     * 2D texture maps. 
     */
    protected int loadBitmap(Context context, GL10 gl, int resourceId) {
        int textureName = -1;
        if (context != null && gl != null) {
            gl.glGenTextures(1, mTextureNameWorkspace, 0);

            textureName = mTextureNameWorkspace[0];
            gl.glBindTexture(GL_TEXTURE_2D, textureName);

            gl.glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
            gl.glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

            gl.glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
            gl.glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);

            gl.glTexEnvf(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_REPLACE);

            InputStream is = context.getResources().openRawResource(resourceId);
            Bitmap bitmap;
            try {
                bitmap = BitmapFactory.decodeStream(is, null, sBitmapOptions);
            } finally {
                try {
                    is.close();
                } catch (IOException e) {
                    // Ignore.
                	throw new RuntimeException(e);
                }
            }

            GLUtils.texImage2D(GL_TEXTURE_2D, 0, bitmap, 0);

            mCropWorkspace[0] = 0;
            mCropWorkspace[1] = bitmap.getHeight();
            mCropWorkspace[2] = bitmap.getWidth();
            mCropWorkspace[3] = -bitmap.getHeight();
            
            bitmap.recycle();

            ((GL11) gl).glTexParameteriv(GL_TEXTURE_2D, 
                    GL11Ext.GL_TEXTURE_CROP_RECT_OES, mCropWorkspace, 0);

            
            int error = gl.glGetError();
            if (error != GL_NO_ERROR) {
                Log.e("SpriteMethodTest", "Texture Load GLError: " + error);
            }
        
        }

        return textureName;
    }

}
