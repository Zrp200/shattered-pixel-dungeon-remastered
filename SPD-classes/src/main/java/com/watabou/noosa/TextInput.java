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

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextArea;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.watabou.glwrap.BlendingKt;
import com.watabou.glwrap.QuadKt;
import com.watabou.glwrap.Texture;
import com.watabou.noosa.ui.Component;
import com.watabou.utils.Point;

import static com.watabou.utils.FileUtilsKt.getAsset;

//essentially contains a libGDX text input field, plus a PD-rendered background
public class TextInput extends Component {

	private Stage stage;
	private Container<TextField> container;
	private TextField textField;

	private Skin skin;

	private NinePatch bg;

	public TextInput( NinePatch bg, boolean multiline, int size ){
		super();
		this.bg = bg;
		add(bg);

		//use a custom viewport here to ensure stage camera matches game camera
		Viewport viewport = new Viewport() {};
		viewport.setWorldSize(Game.INSTANCE.width, Game.INSTANCE.height);
		viewport.setScreenBounds(0, Game.INSTANCE.bottomInset, Game.INSTANCE.width, Game.INSTANCE.height);
		viewport.setCamera(new OrthographicCamera());
		stage = new Stage(viewport);
		Game.INSTANCE.inputHandler.addInputProcessor(stage);

		container = new Container<TextField>();
		stage.addActor(container);
		container.setTransform(true);

		skin = new Skin(getAsset("gdx/textfield.json"));

		TextField.TextFieldStyle style = skin.get(TextField.TextFieldStyle.class);
		style.font = Game.INSTANCE.platform.getFont(size, "", false, false);
		style.background = null;
		textField = multiline ? new TextArea("", style) : new TextField("", style);
		textField.setProgrammaticChangeEvents(true);

		if (!multiline) textField.setAlignment(Align.center);

		textField.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				BitmapFont f = Game.INSTANCE.platform.getFont(size, textField.getText(), false, false);
				TextField.TextFieldStyle style = textField.getStyle();
				if (f != style.font){
					style.font = f;
					textField.setStyle(style);
				}
			}
		});

		if (!multiline){
			textField.setTextFieldListener((textField, c) -> {
				if (c == '\r' || c == '\n'){
					enterPressed();
				}
			});
		}

		container.setActor(textField);
		stage.setKeyboardFocus(textField);
		Gdx.input.setOnscreenKeyboardVisible(true);
	}

	public void enterPressed(){
		//do nothing by default
	};

	public void setText(String text){
		textField.setText(text);
		textField.setCursorPosition(textField.getText().length());
	}

	public void setMaxLength(int maxLength){
		textField.setMaxLength(maxLength);
	}

	public String getText(){
		return textField.getText();
	}

	@Override
	protected void layout() {
		super.layout();

		float contX = x;
		float contY = y;
		float contW = width;
		float contH = height;

		if (bg != null){
			bg.x = x;
			bg.y = y;
			bg.size(width, height);

			contX += bg.marginLeft();
			contY += bg.marginTop();
			contW -= bg.marginHor();
			contH -= bg.marginVer();
		}

		float zoom = Camera.main.zoom;
		Camera c = getCamera();
		if (c != null){
			zoom = c.zoom;
			Point p = c.cameraToScreen(contX, contY);
			contX = p.x/zoom;
			contY = p.y/zoom;
		}

		container.align(Align.topLeft);
		container.setPosition(contX*zoom, (Game.INSTANCE.height-(contY*zoom)));
		container.size(contW*zoom, contH*zoom);
	}

	@Override
	public void update() {
		super.update();
		stage.act(Game.INSTANCE.elapsed);
	}

	@Override
	public void draw() {
		super.draw();
		QuadKt.releaseIndices();
		Script.unuse();
		Texture.Companion.reset();
		stage.draw();
		QuadKt.bindIndices();
		BlendingKt.useDefaultBlending();
	}

	@Override
	public synchronized void destroy() {
		super.destroy();
		if (stage != null) {
			skin.dispose();
			Game.INSTANCE.inputHandler.removeInputProcessor(stage);
			Gdx.input.setOnscreenKeyboardVisible(false);
			Game.INSTANCE.platform.updateSystemUI();
		}
	}
}
