package fr.davidutz.sandbox.fonts;

import fr.davidutz.sandbox.render.Shader;
import org.lwjgl.opengl.GL11;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class FontRenderer {

    private final String fontPath;
    private final int fontSize;
    private final Map<Integer, CharInfo> charMap;

    private int textureId;
    private int width;
    private int height;
    private int lineHeight;

    public FontRenderer(String fontPath, int fontSize) {
        this.fontPath = fontPath;
        this.fontSize = fontSize;
        this.charMap = new HashMap<>();

        this.generateBitmap();
    }

    public CharInfo getCharacter(int codePoint) {
        return this.charMap.getOrDefault(codePoint, new CharInfo(0, 0, 0, 0));
    }

    private void generateBitmap() {
        Font font = new Font(this.fontPath, Font.PLAIN, this.fontSize);

        //Creation d'une fausse image pour recuperer les infos de la font
        BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        g2d.setFont(font);
        FontMetrics fontMetrics = g2d.getFontMetrics();

        int estimatedWidth = (int) (Math.sqrt(font.getNumGlyphs()) * font.getSize()) + 1;
        this.width = 0;
        this.height = fontMetrics.getHeight();
        this.lineHeight = fontMetrics.getHeight();
        int x = 0;
        int y = (int) (fontMetrics.getHeight() * 1.4f);

        for (int i = 0; i < font.getNumGlyphs(); i++) {
           if (font.canDisplay(i)) {
               //Recuperer la taille de chaqque glyphe et maj de la taille de l'image actuelle
               CharInfo charInfo = new CharInfo(x, y, fontMetrics.charWidth(i), fontMetrics.getHeight());

               this.charMap.put(i, charInfo);
               this.width = Math.max(x + fontMetrics.charWidth(i), this.width);

               x += charInfo.getWidth();
               if (x > estimatedWidth) {
                   x = 0;
                   y += fontMetrics.getHeight() * 1.4f;
                   this.height += fontMetrics.getHeight() * 1.4f;
               }
           }
        }
        this.height += fontMetrics.getHeight() * 1.4f;
        g2d.dispose();

        //Creation de la texture
        image = new BufferedImage(this.width, this.height, BufferedImage.TYPE_INT_ARGB);
        g2d = image.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setFont(font);
        g2d.setColor(Color.WHITE);

        for (int i = 0; i < font.getNumGlyphs(); i++) {
            if (font.canDisplay(i)) {
                CharInfo info = this.charMap.get(i);

                info.calculateTextureCoordinates(this.width, this.height);
                g2d.drawString("" + (char) i, info.getSourceX(), info.getSourceY());
            }
        }
        g2d.dispose();

        this.uploadTexture(image);
    }

    private void uploadTexture(BufferedImage image) {
        ByteBuffer buffer = ByteBuffer.allocateDirect(image.getWidth() * image.getHeight() * 4);
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int rgba = image.getRGB(x, y);
                byte alphaComponent = (byte) ((rgba >> 24) & 0xFF);
                buffer.put(alphaComponent);
                buffer.put(alphaComponent);
                buffer.put(alphaComponent);
                buffer.put(alphaComponent);
            }
        }
        buffer.flip();

        this.textureId = GL11.glGenTextures();

        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);

        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8, image.getWidth(), image.getHeight(), 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);
        buffer.clear();

    }

    public int getTextureId() {
        return textureId;
    }
}
