package fr.davidutz.sandbox;

import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GLUtil;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public class Window {

    private String windowTitle;
    private int windowWidth;
    private int windowHeight;
    private long glfwWindow;

    public Window(String windowTitle, int windowWidth, int windowHeight) {
        this.windowTitle = windowTitle;
        this.windowWidth = windowWidth;
        this.windowHeight = windowHeight;
    }

    public void create() {
        //Initialisation de GLFW (La fenetre)
        if (!GLFW.glfwInit()) {
            throw new IllegalStateException("Impossible d'initialiser GLFW");
        }

        //Configuration des options de la fenetre
        GLFW.glfwDefaultWindowHints();
        GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GLFW.GLFW_FALSE);
        GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, GLFW.GLFW_TRUE);
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_DEBUG_CONTEXT, GLFW.GLFW_TRUE);

        //Creation de la fenetre
        this.glfwWindow = GLFW.glfwCreateWindow(this.windowWidth, this.windowHeight, this.windowTitle, 0, 0);
        if (this.glfwWindow == 0) throw new RuntimeException("Impossible de creer la fenetre GLFW");

        //Configuration de la fenetre OpenGL
        GLFW.glfwMakeContextCurrent(this.glfwWindow);
        GLFW.glfwSwapInterval(1);
        GL.createCapabilities();

        // Message de debug
        GLUtil.setupDebugMessageCallback();

        //Affichage de la fenetre
        GLFW.glfwShowWindow(this.glfwWindow);
    }

    public void update() {
        //Echange de buffers
        GLFW.glfwSwapBuffers(this.glfwWindow);

        //Gestion des events
        GLFW.glfwPollEvents();
    }

    public boolean shouldClose() {
        return GLFW.glfwWindowShouldClose(this.glfwWindow);
    }

    public void destroy() {
        //Destruction de la fenetre
        GLFW.glfwDestroyWindow(this.glfwWindow);

        //Terminaison de GLFW
        GLFW.glfwTerminate();
    }

    public int getWindowWidth() {
        return windowWidth;
    }

    public int getWindowHeight() {
        return windowHeight;
    }

    public int getCurrentWindowWidth() {
        IntBuffer widthBuf = BufferUtils.createIntBuffer(1);
        GLFW.glfwGetFramebufferSize(this.glfwWindow, widthBuf, null);
        FloatBuffer scaleXBuf = BufferUtils.createFloatBuffer(1);
        FloatBuffer scaleYBuf = BufferUtils.createFloatBuffer(1);
        GLFW.glfwGetWindowContentScale(this.glfwWindow, scaleXBuf, scaleYBuf);
        float scaleX = scaleXBuf.get();

        return (int)(widthBuf.get() * scaleX);
    }

    public int getCurrentWindowHeight() {
        IntBuffer heightBuf = BufferUtils.createIntBuffer(1);
        GLFW.glfwGetFramebufferSize(this.glfwWindow, null, heightBuf);
        FloatBuffer scaleXBuf = BufferUtils.createFloatBuffer(1);
        FloatBuffer scaleYBuf = BufferUtils.createFloatBuffer(1);
        GLFW.glfwGetWindowContentScale(this.glfwWindow, scaleXBuf, scaleYBuf);
        float scaleY = scaleYBuf.get();

        return (int)(heightBuf.get() * scaleY);
    }
}
