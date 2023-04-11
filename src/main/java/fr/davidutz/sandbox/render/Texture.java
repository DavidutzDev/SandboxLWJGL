package fr.davidutz.sandbox.render;

import org.lwjgl.opengl.GL11;

import java.nio.ByteBuffer;

public class Texture {

    private int textureId;
    private final int textureWidth;
    private final int textureHeight;
    private final ByteBuffer inputBuffer;

    public Texture(int textureWidth, int textureHeight, ByteBuffer inputBuffer) {
        this.textureWidth = textureWidth;
        this.textureHeight = textureHeight;
        this.inputBuffer = inputBuffer;

        //Creation de la texture dans OpenGl
        int[] textureId = new int[1];
        GL11.glGenTextures(textureId);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureId[0]);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, this.textureWidth, this.textureHeight, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, this.inputBuffer);

        this.textureId = textureId[0];
    }

    public int getTextureId() {
        return textureId;
    }

    public int getTextureWidth() {
        return textureWidth;
    }

    public int getTextureHeight() {
        return textureHeight;
    }
}
