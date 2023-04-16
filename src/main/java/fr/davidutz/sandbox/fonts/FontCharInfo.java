package fr.davidutz.sandbox.fonts;

import org.joml.Vector2f;

public class FontCharInfo {

    private final int sourceX;
    private final int sourceY;
    private final int width;
    private final int height;
    private final boolean hasDescender;

    private final Vector2f[] textureCoordinates = new Vector2f[4];

    public FontCharInfo(int sourceX, int sourceY, int width, int height, boolean hasDescender) {
        this.sourceX = sourceX;
        this.sourceY = sourceY;
        this.width = width;
        this.height = height;
        this.hasDescender = hasDescender;
    }

    public void calculateTextureCoordinates(int fontWidth, int fontHeight) {
        float x0 = (float) sourceX / (float) fontWidth;
        float x1 = (float) (sourceX + width) / (float) fontWidth;
        float y0 = (float) (sourceY - height) / (float) fontHeight;
        float y1 = (float) (sourceY) / (float) fontHeight;

        this.textureCoordinates[0] = new Vector2f(x0, y1);
        this.textureCoordinates[1] = new Vector2f(x1, y0);
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getSourceX() {
        return sourceX;
    }

    public int getSourceY() {
        return sourceY;
    }

    public boolean hasDescender() {
        return this.hasDescender;
    }

    public Vector2f[] getTextureCoordinates() {
        return textureCoordinates;
    }
}
