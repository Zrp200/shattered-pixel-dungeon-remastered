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

package com.watabou.noosa;

import com.watabou.glwrap.QuadKt;
import com.watabou.glwrap.Texture;
import com.watabou.glwrap.VertexDataset;
import com.watabou.utils.RectF;

import java.nio.Buffer;
import java.nio.FloatBuffer;

public class NinePatch extends Visual {
	
	public Texture texture;

	protected FloatBuffer quads;
	protected VertexDataset buffer;
	
	protected RectF outterF;
	protected RectF innerF;
	
	protected int marginLeft;
	protected int marginRight;
	protected int marginTop;
	protected int marginBottom;

	protected boolean flipHorizontal;
	protected boolean flipVertical;

	protected boolean dirty;
	
	public NinePatch( Object tx, int margin ) {
		this( tx, margin, margin, margin, margin );
	}
	
	public NinePatch( Object tx, int left, int top, int right, int bottom ) {
		this( tx, 0, 0, 0, 0, left, top, right, bottom );
	}
	
	public NinePatch( Object tx, int x, int y, int w, int h, int margin ) {
		this( tx, x, y, w, h, margin, margin, margin, margin );
	}
	
	public NinePatch( Object tx, int x, int y, int w, int h, int left, int top, int right, int bottom ) {
		super( 0, 0, 0, 0 );
		
		texture = Texture.Companion.get( tx );
		w = w == 0 ? texture.getWidth() : w;
		h = h == 0 ? texture.getHeight() : h;

		quads = QuadKt.createSet( 9 );

		marginLeft	= left;
		marginRight	= right;
		marginTop	= top;
		marginBottom= bottom;
		
		outterF = texture.uvRect( x, y, x + w, y + h );
		innerF = texture.uvRect( x + left, y + top, x + w - right, y + h - bottom );

		updateVertices();
	}
	
	protected void updateVertices() {

		((Buffer)quads).position( 0 );
		
		float right = width - marginRight;
		float bottom = height - marginBottom;

		float outleft   = flipHorizontal ? outterF.right : outterF.left;
		float outright  = flipHorizontal ? outterF.left : outterF.right;
		float outtop    = flipVertical ? outterF.bottom : outterF.top;
		float outbottom = flipVertical ? outterF.top : outterF.bottom;

		float inleft    = flipHorizontal ? innerF.right : innerF.left;
		float inright   = flipHorizontal ? innerF.left : innerF.right;
		float intop     = flipVertical ? innerF.bottom : innerF.top;
		float inbottom  = flipVertical ? innerF.top : innerF.bottom;

		quads.put(QuadKt.fill(0, marginLeft, 0, marginTop, outleft, inleft, outtop, intop));
		quads.put(QuadKt.fill(marginLeft, right, 0, marginTop, inleft, inright, outtop, intop));
		quads.put(QuadKt.fill(right, width, 0, marginTop, inright, outright, outtop, intop));
		quads.put(QuadKt.fill(0, marginLeft, marginTop, bottom, outleft, inleft, intop, inbottom));
		quads.put(QuadKt.fill(marginLeft, right, marginTop, bottom, inleft, inright, intop, inbottom));
		quads.put(QuadKt.fill(right, width, marginTop, bottom, inright, outright, intop, inbottom));
		quads.put(QuadKt.fill(0, marginLeft, bottom, height, outleft, inleft, inbottom, outbottom));
		quads.put(QuadKt.fill(marginLeft, right, bottom, height, inleft, inright, inbottom, outbottom));
		quads.put(QuadKt.fill(right, width, bottom, height, inright, outright, inbottom, outbottom));

		dirty = true;
	}
	
	public int marginLeft() {
		return marginLeft;
	}
	
	public int marginRight() {
		return marginRight;
	}
	
	public int marginTop() {
		return marginTop;
	}
	
	public int marginBottom() {
		return marginBottom;
	}
	
	public int marginHor() {
		return marginLeft + marginRight;
	}
	
	public int marginVer() {
		return marginTop + marginBottom;
	}
	
	public float innerWidth() {
		return width - marginLeft - marginRight;
	}
	
	public float innerHeight() {
		return height - marginTop - marginBottom;
	}
	
	public float innerRight() {
		return width - marginRight;
	}
	
	public float innerBottom() {
		return height - marginBottom;
	}

	public void flipHorizontal(boolean value) {
		if (flipHorizontal != value){
			flipHorizontal = value;
			updateVertices();
		}
	}

	public void flipVertical(boolean value) {
		if (flipVertical != value) {
			flipVertical = value;
			updateVertices();
		}
	}
	
	public void size( float width, float height ) {
		this.width = width;
		this.height = height;
		updateVertices();
	}
	
	@Override
	public void draw() {
		
		super.draw();

		if (dirty){
			if (buffer == null)
				buffer = new VertexDataset(quads);
			else
				buffer.markForUpdate(quads);
			dirty = false;
		}

		Script script = Script.get();
		
		texture.bind();
		
		script.setCamera( getCamera() );
		
		script.getUModel().set( matrix );
		script.lighting(rm, gm, bm, am, ra, ga, ba, aa);
		
		script.drawQuadSet( buffer, 9, 0 );
		
	}

	@Override
	public void destroy() {
		super.destroy();
		if (buffer != null)
			buffer.delete();
	}
}
