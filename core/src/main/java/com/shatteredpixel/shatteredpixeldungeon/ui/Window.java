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

package com.shatteredpixel.shatteredpixeldungeon.ui;

import com.shatteredpixel.shatteredpixeldungeon.Chrome;
import com.shatteredpixel.shatteredpixeldungeon.SPDAction;
import com.shatteredpixel.shatteredpixeldungeon.effects.ShadowBox;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.watabou.input.KeyBindings;
import com.watabou.input.KeyEvent;
import com.watabou.input.PointerEvent;
import com.watabou.noosa.Camera;
import com.watabou.noosa.Game;
import com.watabou.noosa.Group;
import com.watabou.noosa.NinePatch;
import com.watabou.noosa.PointerArea;
import com.watabou.utils.Point;
import com.watabou.utils.Signal;

public class Window extends Group implements Signal.Listener<KeyEvent> {

	protected int width;
	protected int height;

	protected int xOffset;
	protected int yOffset;
	
	protected PointerArea blocker;
	protected ShadowBox shadow;
	protected NinePatch chrome;

	public static final int WHITE = 0xFFFFFF;
	public static final int TITLE_COLOR = 0xFFFF44;
	public static final int SHPX_COLOR = 0x33BB33;
	
	public Window() {
		this( 0, 0, Chrome.get( Chrome.Type.WINDOW ) );
	}
	
	public Window( int width, int height ) {
		this( width, height, Chrome.get( Chrome.Type.WINDOW ) );
	}

	public Window( int width, int height, NinePatch chrome ) {
		super();
		
		blocker = new PointerArea( 0, 0, PixelScene.uiCamera.width, PixelScene.uiCamera.height ) {
			@Override
			protected void onClick( PointerEvent event ) {
				if (Window.this.getParent() != null && !Window.this.chrome.overlapsScreenPoint(
					(int) event.current.x,
					(int) event.current.y )) {
					
					onBackPressed();
				}
			}
		};
		blocker.setCamera(PixelScene.uiCamera);
		add( blocker );
		
		this.chrome = chrome;

		this.width = width;
		this.height = height;

		shadow = new ShadowBox();
		shadow.am = 0.5f;
		shadow.setCamera(PixelScene.uiCamera.getVisible() ?
				PixelScene.uiCamera : Camera.main);
		add( shadow );

		chrome.x = -chrome.marginLeft();
		chrome.y = -chrome.marginTop();
		chrome.size(
			width - chrome.x + chrome.marginRight(),
			height - chrome.y + chrome.marginBottom() );
		add( chrome );
		
		setCamera(new Camera(0, 0,
				(int) chrome.width,
				(int) chrome.height,
				PixelScene.defaultZoom));
		getCamera().x = (int)(Game.INSTANCE.width - getCamera().width * getCamera().zoom) / 2;
		getCamera().y = (int)(Game.INSTANCE.height - getCamera().height * getCamera().zoom) / 2;
		getCamera().y -= yOffset * getCamera().zoom;
		getCamera().scroll.set( chrome.x, chrome.y );
		Camera.add(getCamera());

		shadow.boxRect(
				getCamera().x / getCamera().zoom,
				getCamera().y / getCamera().zoom,
				chrome.width(), chrome.height );

		KeyEvent.addKeyListener( this );
	}
	
	public void resize( int w, int h ) {
		this.width = w;
		this.height = h;
		
		chrome.size(
			width + chrome.marginHor(),
			height + chrome.marginVer() );
		
		getCamera().resize( (int)chrome.width, (int)chrome.height );

		getCamera().x = (int)(Game.INSTANCE.width - getCamera().screenWidth()) / 2;
		getCamera().x += xOffset * getCamera().zoom;

		getCamera().y = (int)(Game.INSTANCE.height - getCamera().screenHeight()) / 2;
		getCamera().y += yOffset * getCamera().zoom;

		shadow.boxRect( getCamera().x / getCamera().zoom, getCamera().y / getCamera().zoom, chrome.width(), chrome.height );
	}

	public Point getOffset(){
		return new Point(xOffset, yOffset);
	}

	public final void offset( Point offset ){
		offset(offset.x, offset.y);
	}

	//windows with scroll panes will likely need to override this and refresh them when offset changes
	public void offset( int xOffset, int yOffset ){
		getCamera().x -= this.xOffset * getCamera().zoom;
		this.xOffset = xOffset;
		getCamera().x += xOffset * getCamera().zoom;

		getCamera().y -= this.yOffset * getCamera().zoom;
		this.yOffset = yOffset;
		getCamera().y += yOffset * getCamera().zoom;

		shadow.boxRect( getCamera().x / getCamera().zoom, getCamera().y / getCamera().zoom, chrome.width(), chrome.height );
	}

	//ensures the window, with offset, does not go beyond a given margin
	public void boundOffsetWithMargin( int margin ){
		float x = getCamera().x / getCamera().zoom;
		float y = getCamera().y / getCamera().zoom;

		Camera sceneCam = PixelScene.uiCamera.getVisible() ? PixelScene.uiCamera : Camera.main;

		int newXOfs = xOffset;
		if (x < margin){
			newXOfs += margin - x;
		} else if (x + getCamera().width > sceneCam.width - margin){
			newXOfs += (sceneCam.width - margin) - (x + getCamera().width);
		}

		int newYOfs = yOffset;
		if (y < margin){
			newYOfs += margin - y;
		} else if (y + getCamera().height > sceneCam.height - margin){
			newYOfs += (sceneCam.height - margin) - (y + getCamera().height);
		}

		offset(newXOfs, newYOfs);
	}
	
	public void hide() {
		remove();
		destroy();
	}
	
	@Override
	public void destroy() {
		super.destroy();
		
		Camera.remove(getCamera());
		KeyEvent.removeKeyListener( this );
	}

	@Override
	public boolean onSignal( KeyEvent event ) {
		if (event.pressed) {
			if (KeyBindings.getActionForKey( event ) == SPDAction.BACK
				|| KeyBindings.getActionForKey( event ) == SPDAction.WAIT){
				onBackPressed();
			}
		}
		
		//TODO currently always eats the key event as windows always take full focus
		// if they are ever made more flexible, might not want to do this in all cases
		return true;
	}
	
	public void onBackPressed() {
		hide();
	}

}
