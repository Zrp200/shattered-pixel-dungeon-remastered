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

package com.shatteredpixel.shatteredpixeldungeon.ios;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.iosrobovm.objectal.OALSimpleAudio;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.PixmapPacker;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.shatteredpixel.shatteredpixeldungeon.SPDSettings;
import com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon;
import com.watabou.noosa.Game;
import com.watabou.utils.PlatformSupport;
import org.robovm.apple.audiotoolbox.AudioServices;
import org.robovm.apple.systemconfiguration.SCNetworkReachability;
import org.robovm.apple.systemconfiguration.SCNetworkReachabilityFlags;
import org.robovm.apple.uikit.UIApplication;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.watabou.utils.FileUtilsKt.getAsset;

public class IOSPlatformSupport extends PlatformSupport {
	@Override
	public void updateDisplaySize() {
		//non-zero safe insets on left/top/right means device has a notch, show status bar
		if (Gdx.graphics.getSafeInsetTop() != 0
				|| Gdx.graphics.getSafeInsetLeft() != 0
				|| Gdx.graphics.getSafeInsetRight() != 0){
			UIApplication.getSharedApplication().setStatusBarHidden(false);
		} else {
			UIApplication.getSharedApplication().setStatusBarHidden(true);
		}

		if (!SPDSettings.fullscreen()) {
			int insetChange = Gdx.graphics.getSafeInsetBottom() - Game.INSTANCE.bottomInset;
			Game.INSTANCE.bottomInset = Gdx.graphics.getSafeInsetBottom();
			Game.INSTANCE.height -= insetChange;
			Game.INSTANCE.displayHeight = Game.INSTANCE.height;
		} else {
			Game.INSTANCE.height += Game.INSTANCE.bottomInset;
			Game.INSTANCE.displayHeight = Game.INSTANCE.height;
			Game.INSTANCE.bottomInset = 0;
		}
		Gdx.gl.glViewport(0, Game.INSTANCE.bottomInset, Game.INSTANCE.width, Game.INSTANCE.height);
	}

	@Override
	public void updateSystemUI() {
		int prevInset = Game.INSTANCE.bottomInset;
		updateDisplaySize();
		if (prevInset != Game.INSTANCE.bottomInset) {
			ShatteredPixelDungeon.INSTANCE.resetSceneNoFade();
		}
	}

	@Override
	public boolean connectedToUnmeteredNetwork() {
		SCNetworkReachability test = new SCNetworkReachability("www.apple.com");
		return !test.getFlags().contains(SCNetworkReachabilityFlags.IsWWAN);
	}

	@Override
	public void vibrate( int millis ){
		//gives a short vibrate on iPhone 6+, no vibration otherwise
		AudioServices.playSystemSound(1520);
	}

	@Override
	public void setHonorSilentSwitch( boolean value ) {
		OALSimpleAudio.sharedInstance().setHonorSilentSwitch(value);
	}

	/* FONT SUPPORT */

	//custom pixel font, for use with Latin and Cyrillic languages
	private static FreeTypeFontGenerator basicFontGenerator;
	//droid sans fallback, for asian fonts
	private static FreeTypeFontGenerator asianFontGenerator;

	@Override
	public void setupFontGenerators(int pageSize, boolean systemfont) {
		//don't bother doing anything if nothing has changed
		if (fonts != null && this.pageSize == pageSize && this.systemfont == systemfont){
			return;
		}
		this.pageSize = pageSize;
		this.systemfont = systemfont;

		resetGenerators(false);
		fonts = new HashMap<>();

		if (systemfont) {
			basicFontGenerator = asianFontGenerator = new FreeTypeFontGenerator(getAsset("fonts/droid_sans.ttf"));
		} else {
			basicFontGenerator = new FreeTypeFontGenerator(getAsset("fonts/pixel_font.ttf"));
			asianFontGenerator = new FreeTypeFontGenerator(getAsset("fonts/droid_sans.ttf"));
		}

		fonts.put(basicFontGenerator, new HashMap<>());
		fonts.put(asianFontGenerator, new HashMap<>());

		packer = new PixmapPacker(pageSize, pageSize, Pixmap.Format.RGBA8888, 1, false);
	}

	private static final Matcher asianMatcher = Pattern.compile("\\p{InHangul_Syllables}|" +
			"\\p{InCJK_Unified_Ideographs}|\\p{InCJK_Symbols_and_Punctuation}|\\p{InHalfwidth_and_Fullwidth_Forms}|" +
			"\\p{InHiragana}|\\p{InKatakana}").matcher("");

	@Override
	protected FreeTypeFontGenerator getGeneratorForString( String input ){
		if (asianMatcher.reset(input).find()){
			return asianFontGenerator;
		} else {
			return basicFontGenerator;
		}
	}

	//splits on newlines, underscores, and chinese/japaneses characters
	private final Pattern regularSplitter = Pattern.compile(
			"(?<=\n)|(?=\n)|(?<=_)|(?=_)|" +
					"(?<=\\p{InHiragana})|(?=\\p{InHiragana})|" +
					"(?<=\\p{InKatakana})|(?=\\p{InKatakana})|" +
					"(?<=\\p{InCJK_Unified_Ideographs})|(?=\\p{InCJK_Unified_Ideographs})|" +
					"(?<=\\p{InCJK_Symbols_and_Punctuation})|(?=\\p{InCJK_Symbols_and_Punctuation})");

	//additionally splits on words, so that each word can be arranged individually
	private final Pattern regularSplitterMultiline = Pattern.compile(
			"(?<= )|(?= )|(?<=\n)|(?=\n)|(?<=_)|(?=_)|" +
					"(?<=\\p{InHiragana})|(?=\\p{InHiragana})|" +
					"(?<=\\p{InKatakana})|(?=\\p{InKatakana})|" +
					"(?<=\\p{InCJK_Unified_Ideographs})|(?=\\p{InCJK_Unified_Ideographs})|" +
					"(?<=\\p{InCJK_Symbols_and_Punctuation})|(?=\\p{InCJK_Symbols_and_Punctuation})");

	@Override
	public String[] splitForTextBlock(String text, boolean multiline) {
		if (multiline) {
			return regularSplitterMultiline.split(text);
		} else {
			return regularSplitter.split(text);
		}
	}
}
