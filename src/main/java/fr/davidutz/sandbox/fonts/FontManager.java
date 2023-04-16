package fr.davidutz.sandbox.fonts;

import fr.davidutz.sandbox.render.Shader;

import java.io.InputStream;

public class FontManager {

    private final FontRenderer fontRenderer = new FontRenderer();
    private final FontAtlas fontAtlas;

    public FontManager(String fontPath, int fontSize) {
        this.fontAtlas = new FontAtlas(fontPath, fontSize);
    }

    public FontManager(InputStream fontStream, int fontSize) {
        this.fontAtlas = new FontAtlas(fontStream, fontSize);
    }

    public void initialize(Shader fontShader) {
        this.fontAtlas.generateAtlas();
        this.fontRenderer.initRenderer(fontShader, this.fontAtlas);
    }

    public void drawString(String text, int x, int y, float scale, int rgb) {
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);

            FontCharInfo charInfo = this.fontAtlas.getCharacter(c);
            if (charInfo.getWidth() == 0) {
                System.out.println("Unknown character \"" + c + "\" !");
                continue;
            }

            float xPos = x;
            this.fontRenderer.addCharacter(xPos, (float) y, scale, charInfo, rgb);
            x += charInfo.getWidth() * scale;
        }

        this.fontRenderer.flushRenderer();
    }

    public void setAtlasMarginCorrection(int correctionValue) {
        this.fontAtlas.setMarginCorrectionValue(correctionValue);
    }
}
