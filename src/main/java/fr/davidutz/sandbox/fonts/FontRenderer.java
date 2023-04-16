package fr.davidutz.sandbox.fonts;

import fr.davidutz.sandbox.render.Shader;
import org.joml.Matrix4f;
import org.lwjgl.opengl.*;

import static org.lwjgl.opengl.GL11C.GL_FLOAT;

public class FontRenderer {

    private final int[] indices = {
            0, 1, 3,
            1, 2, 3
    };

    public static int BATCH_SIZE = 100;
    public static int VERTEX_SIZE = 7;
    public static float DESCENDER_CORRECTION_VALUE = 0.01f;
    private final float[] vertices = new float[BATCH_SIZE * VERTEX_SIZE];
    private int size = 0;
    private final Matrix4f projection = new Matrix4f();

    private int vao;
    private int vbo;
    private Shader shader;
    private FontAtlas atlas;

    public void initRenderer(Shader shader, FontAtlas atlas) {
        this.shader = shader;
        this.atlas = atlas;

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

    public void addCharacter(float x, float y, float scale, FontCharInfo charInfo, int rgb) {
        if (size >= BATCH_SIZE - 4) this.flushRenderer();

        float r = (float) ((rgb >> 16) & 0xFF) / 255.0f;
        float g = (float) ((rgb >> 8) & 0xFF) / 255.0f;
        float b = (float) ((rgb) & 0xFF) / 255.0f;

        float x0 = x;
        float y0 = y;
        float x1 = x + scale * charInfo.getWidth();
        float y1 = y + scale * charInfo.getHeight();

        float ux0 = charInfo.getTextureCoordinates()[0].x(); float uy0 = charInfo.getTextureCoordinates()[0].y() + (charInfo.hasDescender() ? DESCENDER_CORRECTION_VALUE : 0f);
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

    public void flushRenderer() {
        //Nettoyage du tampon sur le GPU et upload le contenu CPU + affichage
        GL30.glBindBuffer(GL30.GL_ARRAY_BUFFER, this.vbo);
        GL30.glBufferData(GL30.GL_ARRAY_BUFFER, (long) Float.BYTES *  VERTEX_SIZE * BATCH_SIZE, GL30.GL_DYNAMIC_DRAW);
        GL30.glBufferSubData(GL30.GL_ARRAY_BUFFER, 0, this.vertices);

        //Affichage du nouveau tampon
        this.shader.use();
        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        GL11.glBindTexture(GL31.GL_TEXTURE_BUFFER, this.atlas.getAtlasTextureId());
        this.shader.uploadTexture("uFontTexture", 0);
        this.shader.uploadMat4f("uProjection", this.projection);

        GL30.glBindVertexArray(this.vao);

        GL30.glDrawElements(GL30.GL_TRIANGLES, this.size * 6, GL30.GL_UNSIGNED_INT, 0);

        //Reset du batch pour un prochain call
        this.size = 0;
    }
}
