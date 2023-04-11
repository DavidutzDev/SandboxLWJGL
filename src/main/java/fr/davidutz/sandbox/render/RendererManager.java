package fr.davidutz.sandbox.render;

import fr.davidutz.sandbox.Window;
import fr.davidutz.sandbox.render.elements.RenderableElement;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;

import static org.lwjgl.opengl.GL11C.*;

public class RendererManager {

    private final Window window;
    private final ArrayList<RenderableElement> renderableElements = new ArrayList<>();
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
    }

    public void renderLoop() {
        this.prepareToRender();

        //Render Elements on Screen
        this.renderableElements.forEach(RenderableElement::render);
    }

    private void prepareToRender() {
        //Effacement du buffer de couleur et de profondeur
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        //Initialisation du rendu des textures 2D
        GL11.glEnable(GL11.GL_TEXTURE_2D);
    }

    public void addRenderableElement(RenderableElement element) {
        this.renderableElements.add(element);
    }
}
