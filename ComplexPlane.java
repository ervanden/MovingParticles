
import java.awt.*;
import java.awt.image.BufferStrategy;

class ComplexPlane extends Canvas {

    private static final long serialVersionUID = 1L;

    Transform t = MovingParticles.transform;

    public void paint(Graphics g) { // never called, we set ignoreRepaint

        int h = this.getSize().height;
        int w = this.getSize().width;

        t.setScreenSpace(0, w, h, 0);
        t.graphics = g;

        MovingParticles.Drawing.draw(t);

    }  // paint 

    public void blitPaint() {

        int h = this.getSize().height;
        int w = this.getSize().width;

        BufferStrategy strategy = this.getBufferStrategy();

        // Render single frame
        do {
            do {
                Graphics g = strategy.getDrawGraphics();
                t.setScreenSpace(0, w, h, 0);
                t.graphics = g;
                try { // sometimes null pointer exception when using graphics, for unknown reason
                    g.clearRect(0, 0, w, h);
                    MovingParticles.Drawing.draw(t);
                } catch (Exception e) {
                    System.out.println("exception in blitPaint");
                }
                g.dispose();
            } while (strategy.contentsRestored());
            strategy.show();
        } while (strategy.contentsLost());
    }

}
