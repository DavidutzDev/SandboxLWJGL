package fr.davidutz.sandbox.fonts;

import com.mlomb.freetypejni.Face;
import com.mlomb.freetypejni.FreeType;
import com.mlomb.freetypejni.FreeTypeConstants;
import com.mlomb.freetypejni.Library;
import org.lwjgl.opengl.GL11;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

public class Sdf {

    private static int textureId = -1;

    public static void generateCodepointBitmap(int codepoint, String fontFile, int fontSize) {
        int padding = 15;
        int upscaleResolution = 4096;
        int spread = upscaleResolution / 2;

        Library library = FreeType.newLibrary();
        assert(library != null);

        Face font = library.newFace(fontFile, 0);
        FreeType.FT_Set_Pixel_Sizes(font.getPointer(), 0, upscaleResolution);
        if (FreeType.FT_Load_Char(font.getPointer(), (char)codepoint, FreeTypeConstants.FT_LOAD_RENDER)) {
            System.out.println("FreeType could not generate character.");
            free(library, font);
            return;
        }

        int glyphWidth = font.getGlyphSlot().getBitmap().getWidth();
        int glyphHeight = font.getGlyphSlot().getBitmap().getRows();
        byte[] glyphBitmap = new byte[glyphHeight * glyphWidth];
        font.getGlyphSlot().getBitmap().getBuffer()
                .get(glyphBitmap, 0, glyphWidth * glyphHeight);

        System.out.println("Glyph width " + glyphWidth);
        System.out.println("Glyph Height " + glyphHeight);

        float widthScale = (float)glyphWidth / (float)upscaleResolution;
        float heightScale = (float)glyphHeight / (float)upscaleResolution;
        int characterWidth = (int)((float)fontSize * widthScale);
        int characterHeight = (int)((float)fontSize * heightScale);
        int bitmapWidth = characterWidth + padding * 2;
        int bitmapHeight = characterHeight + padding * 2;
        float bitmapScaleX = (float)glyphWidth / (float)characterWidth;
        float bitmapScaleY = (float)glyphHeight / (float)characterHeight;
        int[] bitmap = new int[bitmapWidth * bitmapHeight];
        for (int y = -padding; y < characterHeight + padding; y++) {
            for (int x = -padding; x < characterWidth + padding; x++) {
                int pixelX = (int)mapRange(x, -padding, characterWidth + padding,
                        -padding * bitmapScaleX, (characterWidth + padding) * bitmapScaleX);
                int pixelY = (int)mapRange(y, -padding, characterHeight + padding,
                        -padding * bitmapScaleY, (characterHeight + padding) * bitmapScaleY);
                float val = findNearestPixel(pixelX, pixelY, glyphBitmap,
                        glyphWidth, glyphHeight, spread);
                bitmap[(x + padding) + ((y + padding) * bitmapWidth)] = (int)(val * 255.0f);
            }
        }

        BufferedImage testImage = new BufferedImage(bitmapWidth, bitmapHeight, BufferedImage.TYPE_INT_ARGB);
        int x = 0;
        int y = 0;
        for (int byteAsInt : bitmap) {
            int argb = (255 << 24) | (byteAsInt << 16) | (byteAsInt << 8) | byteAsInt;
            testImage.setRGB(x, y, argb);
            x++;
            if (x >= bitmapWidth) {
                x = 0;
                y++;
            }
            if (y >= bitmapHeight) {
                break;
            }
        }

        try {
            File output = new File("test.png");
            ImageIO.write(testImage, "png", output);
        } catch (IOException e) {
            e.printStackTrace();
        }

        uploadTexture(testImage);
        free(library, font);
    }

    private static void free(Library library, Face font) {
        FreeType.FT_Done_Face(font.getPointer());
        FreeType.FT_Done_FreeType(library.getPointer());
    }

    private static float findNearestPixel(int pixelX, int pixelY, byte[] bitmap, int width, int height, int spread) {
        int state = getPixel(pixelX, pixelY, bitmap, width, height);
        int minX = pixelX - spread;
        int maxX = pixelX + spread;
        int minY = pixelY - spread;
        int maxY = pixelY + spread;

        float minDistance = spread * spread;
        for (int y = minY; y < maxY; y++) {
            for (int x = minX; x < maxX; x++) {
                int pixelState = getPixel(x, y, bitmap, width, height);
                float dxSquared = (x - pixelX) * (x - pixelX);
                float dySquared = (y - pixelY) * (y - pixelY);
                float distanceSquared = dxSquared + dySquared;
                if (pixelState != state) {
                    minDistance = Math.min(distanceSquared, minDistance);
                }
            }
        }

        minDistance = (float)Math.sqrt(minDistance);
        float output = (minDistance - 0.5f) / (spread - 0.5f);
        output *= state == 0 ? -1 : 1;

        // Map from [-1, 1] to [0, 1]
        return (output + 1) * 0.5f;
    }

    private static int getPixel(int x, int y, byte[] bitmap, int width, int height) {
        if (x >= 0 && x < width && y >= 0 && y < height) {
            return (bitmap[x + y * width] & 0xFF) == 0 ? 0 : 1;
        }
        return 0;
    }

    private static float mapRange(float val, float in_min, float in_max,
                                  float out_min, float out_max) {
        return (val - in_min) * (out_max - out_min) / (in_max - in_min) + out_min;
    }

    private static void uploadTexture(BufferedImage image) {
        ByteBuffer buffer = ByteBuffer.allocateDirect(image.getWidth() * image.getHeight() * 4);
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int rgba = image.getRGB(x, y);
                byte r = (byte) ((rgba >> 16) & 0xFF);
                buffer.put(r);
                buffer.put(r);
                buffer.put(r);
                buffer.put(r);
            }
        }
        buffer.flip();

        textureId = GL11.glGenTextures();

        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);

        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8, image.getWidth(), image.getHeight(), 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);
        buffer.clear();

    }

    public static int getTextureId() {
        return textureId;
    }
}
