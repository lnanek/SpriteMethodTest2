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

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.DisplayMetrics;

import com.android.spritemethodtest.Mover;
import com.android.spritemethodtest.ProfileRecorder;
import com.android.spritemethodtest.R;
import com.android.spritemethodtest.Renderable;
import com.android.spritemethodtest.opengl.batched.DrawData;
import com.android.spritemethodtest.opengl.batched.FloatDrawData;

/**
 * Activity for testing OpenGL ES drawing speed.  This activity sets up sprites 
 * and passes them off to an OpenGLSurfaceView for rendering and movement.
 */
public class OpenGLTestActivity extends Activity {
	
    private final static int SPRITE_WIDTH = 64;
    
    private final static int SPRITE_HEIGHT = 64;
    
    private GLSurfaceView mGLSurfaceView;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mGLSurfaceView = new GLSurfaceView(this);
        setContentView(mGLSurfaceView);
       
        // Clear out any old profile results.
        ProfileRecorder.sSingleton.resetAll();
        
        final Intent callingIntent = getIntent();
        final int robotCount = callingIntent.getIntExtra("spriteCount", 10);
        final boolean animate = callingIntent.getBooleanExtra("animate", true);
        final DrawMethod method = 
        	DrawMethod.values()[callingIntent.getIntExtra("drawMethod", 0)];

        // Allocate space for the robot sprites + one background sprite.
        GLSprite[] sprites = new GLSprite[robotCount + 1];    
        SimpleGLRenderer spriteRenderer = 
        	new SimpleGLRenderer(this, sprites, method);
        mGLSurfaceView.setRenderer(spriteRenderer);
        
        // We need to know the width and height of the display pretty soon,
        // so grab the information now.
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        final GLSprite background = new GLSprite(R.drawable.background, method);
        sprites[0] = background;
        BitmapDrawable backgroundImage = (BitmapDrawable)getResources().getDrawable(R.drawable.background);
        Bitmap backgoundBitmap = backgroundImage.getBitmap();
        background.setWidth(backgoundBitmap.getWidth());
        background.setHeight(backgoundBitmap.getHeight());
        
        Grid spriteGrid = null;
        DrawData spriteDrawData = null;
        FloatDrawData spriteFloatDrawData = null;
        if ( DrawMethod.BASIC_VERT == method || DrawMethod.VBO == method ) {
            // Setup the background grid.  This is just a quad.
            Grid backgroundGrid = new Grid(2, 2, false);
            backgroundGrid.set(0, 0,  0.0f, 0.0f, 0.0f, 0.0f, 1.0f, null);
            backgroundGrid.set(1, 0, background.getWidth(), 0.0f, 0.0f, 1.0f, 1.0f, null);
            backgroundGrid.set(0, 1, 0.0f, background.getHeight(), 0.0f, 0.0f, 0.0f, null);
            backgroundGrid.set(1, 1, background.getWidth(), background.getHeight(), 0.0f, 
                    1.0f, 0.0f, null );
            background.setGrid(backgroundGrid);
            
            // Setup a quad for the sprites to use.  All sprites will use the
            // same sprite grid instance.
            spriteGrid = new Grid(2, 2, false);
            spriteGrid.set(0, 0,  0.0f, 0.0f, 0.0f, 0.0f , 1.0f, null);
            spriteGrid.set(1, 0, SPRITE_WIDTH, 0.0f, 0.0f, 1.0f, 1.0f, null);
            spriteGrid.set(0, 1, 0.0f, SPRITE_HEIGHT, 0.0f, 0.0f, 0.0f, null);
            spriteGrid.set(1, 1, SPRITE_WIDTH, SPRITE_HEIGHT, 0.0f, 1.0f, 0.0f, null);
        } else if ( DrawMethod.BATCHED_VERT_FIXED == method 
        		|| DrawMethod.BATCHED_VERT_FLOAT == method ) {
        	int backgroundVertCapacity = 4;
        	DrawData backgroundDrawData = new DrawData(4);
        	FloatDrawData backgroundFloatDrawData = new FloatDrawData(4);
        	int totalDrawDataCapacity = backgroundVertCapacity;
        	background.setDrawData(backgroundDrawData, backgroundFloatDrawData);
        	
        	int foregroundVertCapacity = robotCount * 4;
        	spriteDrawData = new DrawData(foregroundVertCapacity);
        	spriteFloatDrawData = new FloatDrawData(foregroundVertCapacity);
        	totalDrawDataCapacity += foregroundVertCapacity;
        	
            DrawData[] drawDataArray = new DrawData[] 
                    {backgroundDrawData, spriteDrawData};
            FloatDrawData[] floatDrawDataArray = new FloatDrawData[] 
                    {backgroundFloatDrawData, spriteFloatDrawData};
            spriteRenderer.setDrawData(
            		drawDataArray, floatDrawDataArray, totalDrawDataCapacity);
        }  
        
        // Allocate our sprites and add them to an array.
        
        // This list of things to move. It points to the same content as the
        // sprite list except for the background.
        Renderable[] renderableArray = new Renderable[robotCount]; 
        
        final int robotBucketSize = robotCount / 3;
        for (int x = 0; x < robotCount; x++) {
            GLSprite robot;
            // Our robots come in three flavors.  Split them up accordingly.
            int resourceId = x < robotBucketSize ? R.drawable.skate1 : 
            	x < robotBucketSize * 2 ? R.drawable.skate2 : 
            		R.drawable.skate3;
            robot = new GLSprite(resourceId, method);            
        
            robot.setWidth(SPRITE_WIDTH);
            robot.setHeight(SPRITE_HEIGHT);
            
            // Pick a random location for this sprite.
            robot.setX((float)(Math.random() * dm.widthPixels));
            robot.setY((float)(Math.random() * dm.heightPixels));
            
            // All sprites can reuse the same grid.  If we're running the
            // DrawTexture extension or batched verts test, this is null.
            robot.setGrid(spriteGrid);
            
            // All sprites put their data into the same array to be processed 
            // all at once if we're running the batched verts tests.
            robot.setDrawData(spriteDrawData, spriteFloatDrawData);
            
            // Add this robot to the spriteArray so it gets drawn and to the
            // renderableArray so that it gets moved.
            sprites[x + 1] = robot;
            renderableArray[x] = robot;
        }
        
        if (animate) {
            Mover simulationRuntime = new Mover();
            simulationRuntime.setRenderables(renderableArray);
            
            simulationRuntime.setViewSize(dm.widthPixels, dm.heightPixels);
            mGLSurfaceView.setEvent(simulationRuntime);
        }
    }
}
