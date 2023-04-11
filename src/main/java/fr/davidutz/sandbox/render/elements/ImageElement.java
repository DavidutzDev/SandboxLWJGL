package fr.davidutz.sandbox.render.elements;

import fr.davidutz.sandbox.SandboxMain;
import fr.davidutz.sandbox.render.Texture;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Objects;

public class ImageElement extends RenderableElement{

    private Texture texture;

    public ImageElement(String texturePath, int xPosition, int yPosition) {
        super(texturePath, xPosition, yPosition);
    }

    public ImageElement(String texturePath, int xPosition, int yPosition, int width, int height) {
        super(texturePath, xPosition, yPosition, width, height);
    }

    @Override
    public void loadTexture() {
        try {
            //Chargement d'une image a partir du disque
            InputStream is = getClass().getClassLoader().getResourceAsStream(this.texturePath);
            BufferedImage image = ImageIO.read(Objects.requireNonNull(is));

            //Convertit l'image en un tampon de texture OpenGL
            ByteBuffer buffer = ByteBuffer.allocateDirect(image.getWidth() * image.getHeight() * 4);
            for (int y = 0; y < image.getHeight(); y++) {
                for (int x = 0; x < image.getWidth(); x++) {
                    int rgba = image.getRGB(x, y);
                    buffer.put((byte) ((rgba >> 16) & 0xFF)); // R
                    buffer.put((byte) ((rgba >>  8) & 0xFF)); // G
                    buffer.put((byte) ((rgba      ) & 0xFF)); // B
                    buffer.put((byte) ((rgba >> 24) & 0xFF)); // A
                }
            }
            buffer.flip();

            this.texture = new Texture(image.getWidth(), image.getHeight(), buffer);
        } catch (IOException e) {
            System.out.println("Failed to load texture: ");
            e.printStackTrace();
        }
    }

    @Override
    public void render() {
        if (texture == null) throw new RuntimeException("Load texture before render the element !");

        //Activation de la texture
        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.texture.getTextureId());

        //Affichage du quadrilatere
        GL11.glBegin(GL11.GL_QUADS);

        if (this.width != 0 && this.height != 0) {

            GL11.glTexCoord2f(0, 0); GL11.glVertex2f(this.xPosition, SandboxMain.getWindow().getWindowHeight() - this.yPosition); //Coin inférieur gauche
            GL11.glTexCoord2f(1, 0); GL11.glVertex2f(this.xPosition + this.width, SandboxMain.getWindow().getWindowHeight() - this.yPosition); //Coin inférieur droit
            GL11.glTexCoord2f(1, 1); GL11.glVertex2f(this.xPosition + this.width, SandboxMain.getWindow().getWindowHeight() - this.yPosition - this.height); //Coin supérieur droit
            GL11.glTexCoord2f(0, 1); GL11.glVertex2f(this.xPosition, SandboxMain.getWindow().getWindowHeight() - this.yPosition - this.height); //Coin supérieur gauche

        } else {

            GL11.glTexCoord2f(0, 0); GL11.glVertex2f(this.xPosition, SandboxMain.getWindow().getWindowHeight() - this.yPosition); //Coin inférieur gauche
            GL11.glTexCoord2f(1, 0); GL11.glVertex2f(this.xPosition + this.texture.getTextureWidth(), SandboxMain.getWindow().getWindowHeight() - this.yPosition); //Coin inférieur droit
            GL11.glTexCoord2f(1, 1); GL11.glVertex2f(this.xPosition + this.texture.getTextureWidth(), SandboxMain.getWindow().getWindowHeight() - this.yPosition - this.texture.getTextureHeight()); //Coin supérieur droit
            GL11.glTexCoord2f(0, 1); GL11.glVertex2f(this.xPosition, SandboxMain.getWindow().getWindowHeight() - this.yPosition - this.texture.getTextureHeight()); //Coin supérieur gauche

        }
        GL11.glEnd();
    }
}
