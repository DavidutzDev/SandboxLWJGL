package fr.davidutz.sandbox.render.elements;

public abstract class RenderableElement {

    protected String texturePath;
    protected int xPosition;
    protected int yPosition;
    protected int width;
    protected int height;

    public RenderableElement(String texturePath, int xPosition, int yPosition) {
        this.texturePath = texturePath;
        this.xPosition = xPosition;
        this.yPosition = yPosition;
    }

    public RenderableElement(String texturePath, int xPosition, int yPosition, int width, int height) {
        this.texturePath = texturePath;
        this.xPosition = xPosition;
        this.yPosition = yPosition;
        this.width = width;
        this.height = height;
    }

    public abstract void loadTexture();
    public abstract void render();
}
