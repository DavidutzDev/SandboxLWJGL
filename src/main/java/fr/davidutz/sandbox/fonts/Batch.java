package fr.davidutz.sandbox.fonts;

import fr.davidutz.sandbox.render.Shader;
import org.joml.Matrix4f;
import org.lwjgl.opengl.*;

import static org.lwjgl.opengl.GL11C.GL_FLOAT;

public class Batch {

    /*private float[] vertices = {
            // x, y,        r, g, b              ux, uy
            0.5f, 0.5f,     1.0f, 0.2f, 0.11f,   1.0f, 0.0f,
            0.5f, -0.5f,    1.0f, 0.2f, 0.11f,   1.0f, 1.0f,
            -0.5f, -0.5f,   1.0f, 0.2f, 0.11f,   0.0f, 1.0f,
            -0.5f, 0.5f,    1.0f, 0.2f, 0.11f,   0.0f, 0.0f
    };*/

    private final int[] indices = {
            0, 1, 3,
            1, 2, 3
    };

    public static int BATCH_SIZE = 100;
    public static int VERTEX_SIZE = 7;
    private final float[] vertices = new float[BATCH_SIZE * VERTEX_SIZE];
    private int size = 0;
    private final Matrix4f projection = new Matrix4f();

    private int vao;
    private int vbo;
    private Shader shader;
    private Shader sdfShader;
    private FontRenderer fontRenderer;

    private void generateEbo() {
        int elementSize = BATCH_SIZE * 3;
        int[] elementBuffer = new int[elementSize];

        for (int i = 0; i < elementSize; i++) {
            elementBuffer[i] = indices[(i % 6)] + ((i / 6) * 4);
        }

        int ebo = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, ebo);
        GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, elementBuffer, GL15.GL_STATIC_DRAW);
    }

    public void initBatch() {
        this.projection.identity();
        this.projection.ortho(0, 800, 0, 600, 1f, 100f);

        vao = GL30.glGenVertexArrays();
        GL30.glBindVertexArray(vao);

        vbo = GL30.glGenBuffers();
        GL30.glBindBuffer(GL30.GL_ARRAY_BUFFER, vbo);
        GL30.glBufferData(GL30.GL_ARRAY_BUFFER, (long) Float.BYTES * VERTEX_SIZE * BATCH_SIZE, GL30.GL_DYNAMIC_DRAW);

        this.generateEbo();

        int stride = 7 * Float.BYTES;
        GL30.glVertexAttribPointer(0, 2, GL_FLOAT, false, stride, 0);
        GL30.glEnableVertexAttribArray(0);

        GL30.glVertexAttribPointer(1, 3, GL_FLOAT, false, stride, 2 * Float.BYTES);
        GL30.glEnableVertexAttribArray(1);

        GL30.glVertexAttribPointer(2, 2, GL_FLOAT, false, stride, 5 * Float.BYTES);
        GL30.glEnableVertexAttribArray(2);
    }

    public void addString(String text, int x, int y, float scale, int rgb) {
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);

            CharInfo charInfo = this.fontRenderer.getCharacter(c);
            if (charInfo.getWidth() == 0) {
                System.out.println("Unknown character \"" + c + "\" !");
                continue;
            }

            float xPos = x;
            float yPos = y;
            this.addCharacter(xPos, yPos, scale, charInfo, rgb);
            x += charInfo.getWidth() * scale;
        }
    }

    public void addCharacter(float x, float y, float scale, CharInfo charInfo, int rgb) {
        if (size >= BATCH_SIZE - 4) this.flushBatch();

        float r = (float) ((rgb >> 16) & 0xFF) / 255.0f;
        float g = (float) ((rgb >> 8) & 0xFF) / 255.0f;
        float b = (float) ((rgb >> 0) & 0xFF) / 255.0f;

        float x0 = x;
        float y0 = y;
        float x1 = x + scale * charInfo.getWidth();
        float y1 = y + scale * charInfo.getHeight();

        float ux0 = charInfo.getTextureCoordinates()[0].x(); float uy0 = charInfo.getTextureCoordinates()[0].y();
        float ux1 = charInfo.getTextureCoordinates()[1].x(); float uy1 = charInfo.getTextureCoordinates()[1].y();

        int index = this.size * 7;
        vertices[index] = x1;       vertices[index + 1] = y0;
        vertices[index + 2] = r;    vertices[index + 3] = g;    vertices[index + 4] = b;
        vertices[index + 5] = ux1;  vertices[index + 6] = uy0;

        index += 7;
        vertices[index] = x1;       vertices[index + 1] = y1;
        vertices[index + 2] = r;    vertices[index + 3] = g;    vertices[index + 4] = b;
        vertices[index + 5] = ux1;  vertices[index + 6] = uy1;

        index += 7;
        vertices[index] = x0;       vertices[index + 1] = y1;
        vertices[index + 2] = r;    vertices[index + 3] = g;    vertices[index + 4] = b;
        vertices[index + 5] = ux0;  vertices[index + 6] = uy1;

        index += 7;
        vertices[index] = x0;       vertices[index + 1] = y0;
        vertices[index + 2] = r;    vertices[index + 3] = g;    vertices[index + 4] = b;
        vertices[index + 5] = ux0;  vertices[index + 6] = uy0;

        this.size += 4;
    }

    public void flushBatch() {
        //Nettoyage du tampon sur le GPU et upload le contenu CPU + affichage
        GL30.glBindBuffer(GL30.GL_ARRAY_BUFFER, this.vbo);
        GL30.glBufferData(GL30.GL_ARRAY_BUFFER, (long) Float.BYTES *  VERTEX_SIZE * BATCH_SIZE, GL30.GL_DYNAMIC_DRAW);
        GL30.glBufferSubData(GL30.GL_ARRAY_BUFFER, 0, this.vertices);

        //Affichage du nouveau tampon
        this.shader.use();
        //this.sdfShader.use();
        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        //GL11.glBindTexture(GL31.GL_TEXTURE_BUFFER, this.fontRenderer.getTextureId());
        GL11.glBindTexture(GL31.GL_TEXTURE_BUFFER, Sdf.getTextureId());
        this.shader.uploadTexture("uFontTexture", 0);
        this.shader.uploadMat4f("uProjection", this.projection);

        GL30.glBindVertexArray(this.vao);

        GL30.glDrawElements(GL30.GL_TRIANGLES, this.size * 6, GL30.GL_UNSIGNED_INT, 0);

        //Reset du batch pour un prochain call
        this.size = 0;
    }

    public void setShader(Shader shader) {
        this.shader = shader;
    }

    public void setSdfShader(Shader sdfShader) {
        this.sdfShader = sdfShader;
    }

    public void setFontRenderer(FontRenderer fontRenderer) {
        this.fontRenderer = fontRenderer;
    }
}
