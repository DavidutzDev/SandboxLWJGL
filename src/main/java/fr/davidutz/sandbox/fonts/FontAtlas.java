package fr.davidutz.sandbox.fonts;

import org.lwjgl.opengl.GL11;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class FontAtlas {
    private int MARGIN_CORRECTION_VALUE = 0;

    private final Font font;
    private final int fontSize;
    private final Map<Integer, FontCharInfo> charMap;

    private int textureId;

    public FontAtlas(String fontPath, int fontSize) {
        this.fontSize = fontSize;
        this.charMap = new HashMap<>();
        this.font = this.registerFont(fontPath);
    }

    public FontAtlas(InputStream fontInputStream, int fontSize) {
        this.fontSize = fontSize;
        this.charMap = new HashMap<>();
        this.font = this.registerFont(fontInputStream);
    }

    public void generateAtlas() {
        Font font = new Font(Objects.requireNonNull(this.font).getName(), Font.PLAIN, this.fontSize);

        //Creation d'une fausse image pour recuperer les infos de la font
        BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        g2d.setFont(font);
        FontMetrics fontMetrics = g2d.getFontMetrics();
        FontRenderContext frc = g2d.getFontRenderContext();

        int estimatedWidth = (int) (Math.sqrt(font.getNumGlyphs()) * font.getSize()) + 1;
        int width = 0;
        int height = fontMetrics.getHeight();
        int x = 0;
        int y = (int) (fontMetrics.getHeight() * 1.4f);

        boolean hasGlobalDescender = (fontMetrics.getMaxDescent() > 0);

        for (int i = 0; i < font.getNumGlyphs(); i++) {
            if (font.canDisplay(i)) {
                //Recuperer la taille de chaqque glyphe et maj de la taille de l'image actuelle
                FontCharInfo charInfo = new FontCharInfo(x, y, fontMetrics.charWidth(i), fontMetrics.getHeight(), hasGlobalDescender || this.hasDescender(font, (char) i, fontMetrics, frc));

                this.charMap.put(i, charInfo);
                width = Math.max(x + fontMetrics.charWidth(i), width);

                x += charInfo.getWidth() + MARGIN_CORRECTION_VALUE;
                if (x > estimatedWidth) {
                    x = 0;
                    y += fontMetrics.getHeight() * 1.4f;
                    height += fontMetrics.getHeight() * 1.4f;
                }
            }
        }
        height += fontMetrics.getHeight() * 1.4f;
        g2d.dispose();

        //Creation de la texture
        image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        g2d = image.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setFont(font);
        g2d.setColor(Color.WHITE);

        for (int i = 0; i < font.getNumGlyphs(); i++) {
            if (font.canDisplay(i)) {
                FontCharInfo info = this.charMap.get(i);

                info.calculateTextureCoordinates(width, height);
                g2d.drawString("" + (char) i, info.getSourceX(), info.getSourceY());
            }
        }
        g2d.dispose();

        try {
            File file = new File("fontAtlas.png");
            ImageIO.write(image, "png", file);
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.uploadAtlas(image);
    }

    private void uploadAtlas(BufferedImage image) {
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

    private Font registerFont(String fontPath) {
        try {
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            Font font = Font.createFont(Font.TRUETYPE_FONT, new File(fontPath));
            ge.registerFont(font);
            return font;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private Font registerFont(InputStream fontInputStream) {
        try {
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            Font font = Font.createFont(Font.TRUETYPE_FONT, fontInputStream);
            ge.registerFont(font);
            return font;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean hasDescender(Font font, char c, FontMetrics fontMetrics, FontRenderContext frc) {
        String text = Character.toString(c);
        TextLayout layout = new TextLayout(text, font, frc);
        Rectangle2D bounds = layout.getBounds();

        return (bounds.getY() + bounds.getHeight() > fontMetrics.getDescent());
    }

    public FontCharInfo getCharacter(int codePoint) {
        return this.charMap.getOrDefault(codePoint, new FontCharInfo(0, 0, 0, 0, false));
    }

    public void setMarginCorrectionValue(int MARGIN_CORRECTION_VALUE) {
        this.MARGIN_CORRECTION_VALUE = MARGIN_CORRECTION_VALUE;
    }

    public int getAtlasTextureId() {
        return textureId;
    }
}
