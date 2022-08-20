/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2022 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.shatteredpixel.shatteredpixeldungeon.scenes;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Badges;
import com.shatteredpixel.shatteredpixeldungeon.SPDSettings;
import com.shatteredpixel.shatteredpixeldungeon.effects.BadgeBanner;
import com.shatteredpixel.shatteredpixeldungeon.messages.Languages;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.Tooltip;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.watabou.glwrap.BlendingKt;
import com.watabou.glwrap.FpsKt;
import com.watabou.glwrap.Texture;
import com.watabou.input.ControllerHandler;
import com.watabou.input.PointerEvent;
import com.watabou.noosa.BitmapText;
import com.watabou.noosa.BitmapText.Font;
import com.watabou.noosa.Camera;
import com.watabou.noosa.ColorBlock;
import com.watabou.noosa.Game;
import com.watabou.noosa.Gizmo;
import com.watabou.noosa.Image;
import com.watabou.noosa.Scene;
import com.watabou.noosa.Visual;
import com.watabou.noosa.ui.Component;
import com.watabou.noosa.ui.Cursor;
import com.watabou.utils.DeviceCompat;
import com.watabou.utils.PointF;
import com.watabou.utils.Reflection;

import java.util.ArrayList;

import static com.watabou.utils.MathKt.ceil;
import static com.watabou.utils.MathKt.clamp;
import static com.watabou.utils.MathKt.pow;
import static java.lang.Math.abs;
import static java.lang.Math.min;
import static java.lang.Math.round;

public class PixelScene extends Scene {

	// Minimum virtual display size for mobile portrait orientation
	public static final float MIN_WIDTH_P = 135;
	public static final float MIN_HEIGHT_P = 225;

	// Minimum virtual display size for mobile landscape orientation
	public static final float MIN_WIDTH_L = 240;
	public static final float MIN_HEIGHT_L = 160;

	// Minimum virtual display size for full desktop UI (landscape only)
	//TODO maybe include another scale for mixed UI? might make it more accessible to mobile devices
	// mixed UI has similar requirements to mobile landscape tbh... Maybe just merge them?
	// mixed UI can possible be used on mobile portrait for tablets though.. Does that happen often?
	public static final float MIN_WIDTH_FULL = 360;
	public static final float MIN_HEIGHT_FULL = 200;

	public static int defaultZoom = 0;
	public static int maxDefaultZoom = 0;
	public static int maxScreenZoom = 0;
	public static float minZoom;
	public static float maxZoom;

	public static Camera uiCamera;

	//stylized 3x5 bitmapped pixel font. Only latin characters supported.
	public static BitmapText.Font pixelFont;

	protected BitmapText fps;

	protected boolean inGameScene = false;

	@Override
	public void create() {

		super.create();

		GameScene.scene = null;

		//flush the texture cache whenever moving from ingame to menu, helps reduce memory load
		if (!inGameScene && InterlevelScene.lastRegion != -1){
			InterlevelScene.lastRegion = -1;
			Texture.Companion.clear();
		}

		float minWidth, minHeight, scaleFactor;
		if (SPDSettings.interfaceSize() > 0){
			minWidth = MIN_WIDTH_FULL;
			minHeight = MIN_HEIGHT_FULL;
			scaleFactor = 3.75f;
		} else if (landscape()) {
			minWidth = MIN_WIDTH_L;
			minHeight = MIN_HEIGHT_L;
			scaleFactor = 2.5f;
		} else {
			minWidth = MIN_WIDTH_P;
			minHeight = MIN_HEIGHT_P;
			scaleFactor = 2.5f;
		}

		maxDefaultZoom = (int) min(Game.INSTANCE.width/minWidth, Game.INSTANCE.height/minHeight);
		maxScreenZoom = (int) min(Game.INSTANCE.displayWidth /minWidth, Game.INSTANCE.displayHeight /minHeight);
		defaultZoom = SPDSettings.scale();

		if (defaultZoom < ceil( Game.INSTANCE.density * 2 ) || defaultZoom > maxDefaultZoom){
			defaultZoom = clamp(2, ceil( Game.INSTANCE.density * scaleFactor ), maxDefaultZoom);

			if (SPDSettings.interfaceSize() > 0 && defaultZoom < (maxDefaultZoom+1)/2){
				defaultZoom = (maxDefaultZoom+1)/2;
			}
		}

		minZoom = 1;
		maxZoom = defaultZoom * 2;

		Camera.reset( new PixelCamera( defaultZoom ) );

		float uiZoom = defaultZoom;
		uiCamera = Camera.createFullscreen( uiZoom );
		Camera.add( uiCamera );

		// 3x5 (6)
		pixelFont = Font.colorMarked(
			Texture.Companion.get( Assets.Fonts.PIXELFONT), 0x00000000, BitmapText.Font.LATIN_FULL );
		pixelFont.baseLine = 6;
		pixelFont.tracking = -1;

		fps = new BitmapText( pixelFont) {
			@Override
			public void update() {
				super.update();
				text("FPS: " + FpsKt.fps());
			}
		};
		fps.setVisible(DeviceCompat.isDebug());
		add( fps );
		
		//set up the texture size which rendered text will use for any new glyphs.
		int renderedTextPageSize;
		if (defaultZoom <= 3){
			renderedTextPageSize = 256;
		} else if (defaultZoom <= 8){
			renderedTextPageSize = 512;
		} else {
			renderedTextPageSize = 1024;
		}
		//asian languages have many more unique characters, so increase texture size to anticipate that
		if (Messages.lang() == Languages.KOREAN ||
				Messages.lang() == Languages.CHINESE ||
				Messages.lang() == Languages.JAPANESE){
			renderedTextPageSize *= 2;
		}
		Game.INSTANCE.platform.setupFontGenerators(renderedTextPageSize, SPDSettings.systemFont());

		Tooltip.resetLastUsedTime();

		Cursor.setCustomCursor(Cursor.Type.DEFAULT, defaultZoom);

	}

	@Override
	public void update() {
		super.update();
		//20% deadzone
		if (!Cursor.isCursorCaptured()) {
			if (abs(ControllerHandler.rightStickPosition.x) >= 0.2f
					|| abs(ControllerHandler.rightStickPosition.y) >= 0.2f) {
				if (!ControllerHandler.controllerPointerActive()) {
					ControllerHandler.setControllerPointer(true);
				}

				int sensitivity = SPDSettings.controllerPointerSensitivity() * 100;

				//cursor moves 100xsens scaled pixels per second at full speed
				//35x at 50% movement, ~9x at 20% deadzone threshold
				float xMove = pow(abs(ControllerHandler.rightStickPosition.x), 1.5f);
				if (ControllerHandler.rightStickPosition.x < 0) xMove = -xMove;

				float yMove = pow(abs(ControllerHandler.rightStickPosition.y), 1.5f);
				if (ControllerHandler.rightStickPosition.y < 0) yMove = -yMove;

				PointF virtualCursorPos = ControllerHandler.getControllerPointerPos();
				virtualCursorPos.x += defaultZoom * sensitivity * Game.INSTANCE.elapsed * xMove;
				virtualCursorPos.y += defaultZoom * sensitivity * Game.INSTANCE.elapsed * yMove;
				virtualCursorPos.x = clamp(0, virtualCursorPos.x, Game.INSTANCE.width);
				virtualCursorPos.y = clamp(0, virtualCursorPos.y, Game.INSTANCE.height);
				ControllerHandler.updateControllerPointer(virtualCursorPos, true);
			}
		}
	}

	private Image cursor = null;

	@Override
	public synchronized void draw() {
		super.draw();

		//cursor is separate from the rest of the scene, always appears above
		if (ControllerHandler.controllerPointerActive()){
			if (cursor == null){
				cursor = new Image(Cursor.Type.CONTROLLER.file);
			}

			PointF virtualCursorPos = ControllerHandler.getControllerPointerPos();
			cursor.x = (virtualCursorPos.x / defaultZoom) - cursor.width()/2f;
			cursor.y = (virtualCursorPos.y / defaultZoom) - cursor.height()/2f;
			cursor.setCamera(uiCamera);
			align(cursor);
			cursor.draw();
		}
	}

	//FIXME this system currently only works for a subset of windows
	private static ArrayList<Class<?extends Window>> savedWindows = new ArrayList<>();
	private static Class<?extends PixelScene> savedClass = null;
	
	public synchronized void saveWindows(){

		savedWindows.clear();
		savedClass = getClass();
		for (Gizmo g : getChildren().toArray(new Gizmo[0])){
			if (g instanceof Window){
				savedWindows.add((Class<? extends Window>) g.getClass());
			}
		}
	}
	
	public synchronized void restoreWindows(){
		if (getClass().equals(savedClass)){
			for (Class<?extends Window> w : savedWindows){
				try{
					add(Reflection.newInstanceUnhandled(w));
				} catch (Exception e){
					//window has no public zero-arg constructor, just eat the exception
				}
			}
		}
		savedWindows.clear();
	}

	@Override
	public void destroy() {
		super.destroy();
		PointerEvent.clearListeners();
		if (cursor != null){
			cursor.destroy();
		}
	}

	public static boolean landscape(){
		return SPDSettings.interfaceSize() > 0 || Game.INSTANCE.width > Game.INSTANCE.height;
	}


	public static RenderedTextBlock renderTextBlock(int size ){
		return renderTextBlock("", size);
	}

	public static RenderedTextBlock renderTextBlock(String text, int size ){
		RenderedTextBlock result = new RenderedTextBlock( text, size*defaultZoom);
		result.zoom(1/(float)defaultZoom);
		return result;
	}

	/**
	 * These methods align UI elements to device pixels.
	 * e.g. if we have a scale of 3x then valid positions are #.0, #.33, #.67
	 */

	public static float align( float pos ) {
		return round(pos * defaultZoom) / (float)defaultZoom;
	}

	public static float align( Camera camera, float pos ) {
		return round(pos * camera.zoom) / camera.zoom;
	}

	public static void align( Visual v ) {
		v.x = align( v.x );
		v.y = align( v.y );
	}

	public static void align( Component c ){
		c.setPos(align(c.left()), align(c.top()));
	}

	public static boolean noFade = false;
	protected void fadeIn() {
		if (noFade) {
			noFade = false;
		} else {
			fadeIn( 0xFF000000, false );
		}
	}
	
	protected void fadeIn( int color, boolean light ) {
		add( new Fader( color, light ) );
	}
	
	public static void showBadge( Badges.Badge badge ) {
		Game.runOnRenderThread(() -> {
			Scene s = Game.INSTANCE.scene;
			if (s != null) {
				BadgeBanner banner = BadgeBanner.show(badge.image);
				s.add(banner);
				float offset = Camera.main.centerOffset.y;

				int left = uiCamera.width/2 - BadgeBanner.SIZE/2;
				left -= (BadgeBanner.SIZE * BadgeBanner.DEFAULT_SCALE * (BadgeBanner.showing.size()-1))/2;
				for (int i = 0; i < BadgeBanner.showing.size(); i++){
					banner = BadgeBanner.showing.get(i);
					banner.setCamera(uiCamera);
					banner.x = align(banner.getCamera(), left);
					banner.y = align(uiCamera, (uiCamera.height - banner.height) / 2 - banner.height / 2 - 16 - offset);
					left += BadgeBanner.SIZE * BadgeBanner.DEFAULT_SCALE;
				}

			}
		});
	}
	
	protected static class Fader extends ColorBlock {
		
		private static float FADE_TIME = 1f;
		
		private boolean light;
		
		private float time;

		private static Fader INSTANCE;
		
		public Fader( int color, boolean light ) {
			super( uiCamera.width, uiCamera.height, color );
			
			this.light = light;
			
			setCamera(uiCamera);
			
			alpha( 1f );
			time = FADE_TIME;

			if (INSTANCE != null){
				INSTANCE.remove();
			}
			INSTANCE = this;
		}
		
		@Override
		public void update() {
			
			super.update();
			
			if ((time -= Game.INSTANCE.elapsed) <= 0) {
				alpha( 0f );
				remove();
				destroy();
				if (INSTANCE == this) {
					INSTANCE = null;
				}
			} else {
				alpha( time / FADE_TIME );
			}
		}
		
		@Override
		public void draw() {
			if (light) {
				BlendingKt.setLightMode();
				super.draw();
				BlendingKt.setNormalMode();
			} else {
				super.draw();
			}
		}
	}
	
	private static class PixelCamera extends Camera {
		
		public PixelCamera( float zoom ) {
			super(
				(int)(Game.INSTANCE.width - ceil( Game.INSTANCE.width / zoom ) * zoom) / 2,
				(int)(Game.INSTANCE.height - ceil( Game.INSTANCE.height / zoom ) * zoom) / 2,
				ceil( Game.INSTANCE.width / zoom ),
				ceil( Game.INSTANCE.height / zoom ), zoom );
			fullScreen = true;
		}
		
		@Override
		protected void updateMatrix() {
			float sx = align( this, scroll.x + shakeX );
			float sy = align( this, scroll.y + shakeY );
			
			matrix.setValue(0, zoom * invW2);
			matrix.setValue(5, -zoom * invH2);

			matrix.setValue(12, -1 + x * invW2 - sx * matrix.getValues()[0]);
			matrix.setValue(13, 1 - y * invH2 - sy * matrix.getValues()[5]);
			
		}
	}
}
