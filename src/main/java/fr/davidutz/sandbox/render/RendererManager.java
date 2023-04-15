package fr.davidutz.sandbox.render;

import fr.davidutz.sandbox.SandboxMain;
import fr.davidutz.sandbox.Window;
import fr.davidutz.sandbox.fonts.Batch;
import fr.davidutz.sandbox.fonts.CharInfo;
import fr.davidutz.sandbox.fonts.FontRenderer;
import fr.davidutz.sandbox.fonts.Sdf;
import fr.davidutz.sandbox.render.elements.RenderableElement;
import org.joml.Vector2f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL31;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

import static org.lwjgl.opengl.GL11C.*;

public class RendererManager {

    private final Window window;
    private final ArrayList<RenderableElement> renderableElements = new ArrayList<>();

    /* Font */
    private FontRenderer fontRenderer;
    private Batch batch;
    private Shader fontShader;
    private Shader sdfShader;
    private CharInfo testCharInfo;

    public RendererManager(Window window) {
        this.window = window;
    }

    //Initialisation du rendu
    public void initialize() {
        //Sélectionne la matrice de
        GL11.glMatrixMode(GL11.GL_PROJECTION);

        //Définit une projection orthographique
        GL11.glOrtho(0, this.window.getWindowWidth(), 0, this.window.getWindowHeight(), -1, 1);

        //Sélectionne à nouveau la matrice de modèle-vue
        GL11.glMatrixMode(GL11.GL_MODELVIEW);

        /* Load Elements Textures*/
        renderableElements.forEach(RenderableElement::loadTexture);

        /* Font */
        this.fontRenderer = new FontRenderer("C:/Windows/Arial.ttf", 128);
        this.fontShader = new Shader("assets/fontShader.glsl");
        this.sdfShader = new Shader("assets/sdfShader.glsl");

        this.batch = new Batch();
        this.batch.setShader(this.fontShader);
        this.batch.setSdfShader(this.sdfShader);
        this.batch.setFontRenderer(this.fontRenderer);
        this.batch.initBatch();

        //Sdf.generateCodepointBitmap('A', "C:/Windows/Fonts/arial.ttf", 32);
    }

    public void renderLoop() {
        this.prepareToRender();
        GL11.glClearColor(1, 1, 1, 1);

        //Render Elements on Screen
        this.renderableElements.forEach(RenderableElement::render);

        //Render text
        this.batch.addString("ABCDEFGHIJKLMNOPQRSTUVWXYZ", 10, 400, 0.2f, 000000);
        this.batch.addString("!\"£$%^&*()_+=-{]", 10, 300, 0.6f, 0xFFAB0);
        this.batch.addString("abcdefghijklmnopqrstuvwxyz", 10, 200, 0.4f, 0xFF00AB0);

        //this.batch.addCharacter(0, 0, 620.0f, this.testCharInfo, 0xEE0102);

        this.batch.flushBatch();

        glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_INT, 0);
    }

    private void prepareToRender() {
        //Effacement du buffer de couleur et de profondeur
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        //Initialisation du rendu des textures 2D
        GL11.glEnable(GL11.GL_TEXTURE_2D);

        //Ajout du blending
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
    }

    public void addRenderableElement(RenderableElement element) {
        this.renderableElements.add(element);
    }
}
