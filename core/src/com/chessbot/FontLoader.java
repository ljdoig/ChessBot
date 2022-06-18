package com.chessbot;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator
        .FreeTypeFontParameter;

public class FontLoader {
    private final BitmapFont font;

    public FontLoader(String fontFile, int size) {
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(
                Gdx.files.internal(fontFile)
        );
        FreeTypeFontParameter parameter = new FreeTypeFontParameter();
        parameter.size = size;
        font = generator.generateFont(parameter);
        generator.dispose();
    }

    public static BitmapFont load(String fontFile, int size) {
        return new FontLoader(fontFile, size).font;
    }
}
