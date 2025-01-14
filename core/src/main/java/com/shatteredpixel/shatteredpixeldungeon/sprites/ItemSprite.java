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

package com.shatteredpixel.shatteredpixeldungeon.sprites;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.items.Gold;
import com.shatteredpixel.shatteredpixeldungeon.items.Heap;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonTilemap;
import com.watabou.glwrap.Matrix;
import com.watabou.glwrap.Texture;
import com.watabou.glwrap.VertexDataset;
import com.watabou.noosa.Camera;
import com.watabou.noosa.Game;
import com.watabou.noosa.MovieClip;
import com.watabou.noosa.Script;
import com.watabou.noosa.audio.Sample;
import com.watabou.noosa.particles.Emitter;
import com.watabou.utils.PointF;
import com.watabou.utils.Random;

import java.nio.Buffer;

public class ItemSprite extends MovieClip {

	public static final int SIZE	= 16;
	
	private static final float DROP_INTERVAL = 0.4f;
	
	public Heap heap;
	
	private Glowing glowing;
	//FIXME: a lot of this emitter functionality isn't very well implemented.
	//right now I want to ship 0.3.0, but should refactor in the future.
	protected Emitter emitter;
	private float phase;
	private boolean glowUp;
	
	private float dropInterval;

	//the amount the sprite is raised from flat when viewed in a raised perspective
	protected float perspectiveRaise    = 5 / 16f; //5 pixels

	//the width and height of the shadow are a percentage of sprite size
	//offset is the number of pixels the shadow is moved down or up (handy for some animations)
	protected boolean renderShadow  = false;
	protected float shadowWidth     = 1f;
	protected float shadowHeight    = 0.25f;
	protected float shadowOffset    = 0.5f;
	
	public ItemSprite() {
		this( ItemSpriteSheet.SOMETHING, null );
	}
	
	public ItemSprite( Heap heap ){
		super(Assets.Sprites.ITEMS);
		view( heap );
	}
	
	public ItemSprite( Item item ) {
		super(Assets.Sprites.ITEMS);
		view( item );
	}
	
	public ItemSprite( int image ){
		this( image, null );
	}
	
	public ItemSprite( int image, Glowing glowing ) {
		super( Assets.Sprites.ITEMS );
		
		view(image, glowing);
	}
	
	public void link() {
		link(heap);
	}
	
	public void link( Heap heap ) {
		this.heap = heap;
		view(heap);
		renderShadow = true;
		place(heap.pos);
	}
	
	@Override
	public void revive() {
		super.revive();
		
		speed.set( 0 );
		acc.set( 0 );
		dropInterval = 0;
		
		heap = null;
		if (emitter != null) {
			emitter.remove();
			emitter = null;
		}
	}

	public void visible(boolean value){
		this.setVisible(value);
		if (emitter != null && !getVisible()){
			emitter.remove();
			emitter = null;
		}
	}
	
	public PointF worldToCamera( int cell ) {
		final int csize = DungeonTilemap.SIZE;
		
		return new PointF(
				PixelScene.align(Camera.main, ((cell % Dungeon.level.width()) + 0.5f) * csize - width() * 0.5f),
				PixelScene.align(Camera.main, ((cell / Dungeon.level.width()) + 1.0f) * csize - height() - csize * perspectiveRaise)
		);
	}
	
	public void place( int p ) {
		if (Dungeon.level != null) {
			point(worldToCamera(p));
			shadowOffset = 0.5f;
		}
	}
	
	public void drop() {

		if (heap.isEmpty()) {
			return;
		} else if (heap.size() == 1){
			// normally this would happen for any heap, however this is not applied to heaps greater than 1 in size
			// in order to preserve an amusing visual bug/feature that used to trigger for heaps with size > 1
			// where as long as the player continually taps, the heap sails up into the air.
			place(heap.pos);
		}
			
		dropInterval = DROP_INTERVAL;
		
		speed.set( 0, -100 );
		acc.set(0, -speed.y / DROP_INTERVAL * 2);
		
		if (heap != null && heap.seen && heap.peek() instanceof Gold) {
			CellEmitter.center( heap.pos ).burst( Speck.factory( Speck.COIN ), 5 );
			Sample.INSTANCE.play( Assets.Sounds.GOLD, 1, 1, Random.Float( 0.9f, 1.1f ) );
		}
	}
	
	public void drop( int from ) {

		if (heap.pos == from) {
			drop();
		} else {
			
			float px = x;
			float py = y;
			drop();
			
			place(from);
	
			speed.offset((px - x) / DROP_INTERVAL, (py - y) / DROP_INTERVAL);
		}
	}

	public ItemSprite view( Item item ){
		view(item.image(), item.glowing());
		Emitter emitter = item.emitter();
		if (emitter != null && getParent() != null) {
			emitter.pos( this );
			getParent().add( emitter );
			this.emitter = emitter;
		}
		return this;
	}
	
	public ItemSprite view( Heap heap ){
		if (heap.size() <= 0 || heap.items == null){
			return view( 0, null );
		}
		
		switch (heap.type) {
			case HEAP: case FOR_SALE:
				return view( heap.peek() );
			case CHEST:
				return view( ItemSpriteSheet.CHEST, null );
			case LOCKED_CHEST:
				return view( ItemSpriteSheet.LOCKED_CHEST, null );
			case CRYSTAL_CHEST:
				return view( ItemSpriteSheet.CRYSTAL_CHEST, null );
			case TOMB:
				return view( ItemSpriteSheet.TOMB, null );
			case SKELETON:
				return view( ItemSpriteSheet.BONES, null );
			case REMAINS:
				return view( ItemSpriteSheet.REMAINS, null );
			default:
				return view( 0, null );
		}
	}
	
	public ItemSprite view( int image, Glowing glowing ) {
		if (this.emitter != null) this.emitter.remove();
		emitter = null;
		frame( image );
		glow( glowing );
		return this;
	}

	public void frame( int image ){
		frame( ItemSpriteSheet.film.get( image ));

		float height = ItemSpriteSheet.film.height( image );
		//adds extra raise to very short items, so they are visible
		if (height < 8f){
			perspectiveRaise =  (5 + 8 - height) / 16f;
		}
	}
	
	public synchronized void glow( Glowing glowing ){
		this.glowing = glowing;
		if (glowing == null) resetColor();
	}

	@Override
	public void kill() {
		super.kill();
		if (emitter != null) {
			emitter.on = false;
			emitter.autoKill = true;
		}
		emitter = null;
	}

	private final Matrix shadowMatrix = new Matrix();

	@Override
	protected void updateMatrix() {
		super.updateMatrix();
		shadowMatrix.copy(matrix);
		shadowMatrix.translate((width * (1f - shadowWidth)) / 2f, (height * (1f - shadowHeight)) + shadowOffset);
		shadowMatrix.scale(shadowWidth, shadowHeight);
	}

	@Override
	public void draw() {
		if (texture == null || (!dirty && vertexDataset == null))
			return;

		if (renderShadow) {
			if (dirty) {
				((Buffer) vertexBuffer).position(0);
				vertexBuffer.put(vertices);
				if (vertexDataset == null)
					vertexDataset = new VertexDataset(vertexBuffer);
				else
					vertexDataset.markForUpdate(vertexBuffer);
				dirty = false;
			}

			Script script = Script.get();

			texture.bind();

			script.setCamera(getCamera());

			updateMatrix();

			script.getUModel().set(shadowMatrix);
			script.lighting(
					0, 0, 0, am * .6f,
					0, 0, 0, aa * .6f);

			script.drawQuad(vertexDataset);
		}

		super.draw();

	}

	@Override
	public synchronized void update() {
		super.update();

		setVisible((heap == null || heap.seen));

		if (emitter != null){
			emitter.setVisible(getVisible());
		}

		if (dropInterval > 0){
			shadowOffset -= speed.y * Game.elapsed * 0.8f;

			if ((dropInterval -= Game.elapsed) <= 0){

				speed.set(0);
				acc.set(0);
				shadowOffset = 0.25f;
				place(heap.pos);

				if (getVisible()) {

					if (Dungeon.level.water[heap.pos]) {
						GameScene.ripple(heap.pos);
					}

					if (Dungeon.level.water[heap.pos]) {
						Sample.INSTANCE.play( Assets.Sounds.WATER, 0.8f, Random.Float( 1f, 1.45f ) );
					} else if (Dungeon.level.map[heap.pos] == Terrain.EMPTY_SP) {
						Sample.INSTANCE.play( Assets.Sounds.STURDY, 0.8f, Random.Float( 1.16f, 1.25f ) );
					} else if (Dungeon.level.map[heap.pos] == Terrain.GRASS
							|| Dungeon.level.map[heap.pos] == Terrain.EMBERS
							|| Dungeon.level.map[heap.pos] == Terrain.FURROWED_GRASS){
						Sample.INSTANCE.play( Assets.Sounds.GRASS, 0.8f, Random.Float( 1.16f, 1.25f ) );
					} else if (Dungeon.level.map[heap.pos] == Terrain.HIGH_GRASS) {
						Sample.INSTANCE.play( Assets.Sounds.STEP, 0.8f, Random.Float( 1.16f, 1.25f ) );
					} else {
						Sample.INSTANCE.play( Assets.Sounds.STEP, 0.8f, Random.Float( 1.16f, 1.25f ));
					}
				}
			}
		}

		if (getVisible() && glowing != null) {
			if (glowUp && (phase += Game.elapsed) > glowing.period) {
				
				glowUp = false;
				phase = glowing.period;
				
			} else if (!glowUp && (phase -= Game.elapsed) < 0) {
				
				glowUp = true;
				phase = 0;
				
			}
			
			float value = phase / glowing.period * 0.6f;
			
			rm = gm = bm = 1 - value;
			ra = glowing.red * value;
			ga = glowing.green * value;
			ba = glowing.blue * value;
		}
	}

	public static int pick( int index, int x, int y ) {
		Texture tx = Texture.Companion.get( Assets.Sprites.ITEMS );
		int rows = tx.getWidth() / SIZE;
		int row = index / rows;
		int col = index % rows;
		return tx.bitmap.getPixel( col * SIZE + x, row * SIZE + y );
	}
	
	public static class Glowing {
		
		public int color;
		public float red;
		public float green;
		public float blue;
		public float period;
		
		public Glowing( int color ) {
			this( color, 1f );
		}
		
		public Glowing( int color, float period ) {

			this.color = color;

			red = (color >> 16) / 255f;
			green = ((color >> 8) & 0xFF) / 255f;
			blue = (color & 0xFF) / 255f;
			
			this.period = period;
		}
	}
}
