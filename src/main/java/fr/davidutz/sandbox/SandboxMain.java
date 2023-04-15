package fr.davidutz.sandbox;

import fr.davidutz.sandbox.render.RendererManager;

import java.io.IOException;

public class SandboxMain {

    private static final Window window = new Window("Sandbox LWJGL", 1280, 720);
    private static final RendererManager rendererManager = new RendererManager(window);

    public static void main(String[] args) throws IOException {
        //Initialisation de la fenetre
        window.create();

        /* Ajouts des elements a afficher*/

        //rendererManager.addRenderableElement(new ImageElement("assets/elon.jpg", 200, 200, 400, 226));

        /*-------------------------------*/

        //Initialisation du systeme de rendu
        rendererManager.initialize();

        //Boucle de rendu
        while (!window.shouldClose()) {
            //Appel de la boucle de rendu
            rendererManager.renderLoop();

            //Mise a jour de la fenetre
            window.update();
        }

        //Nettoyage des resources
        window.destroy();
    }

    public static Window getWindow() {
        return window;
    }
}
