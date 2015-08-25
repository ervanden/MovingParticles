import java.awt.*;

import java.awt.image.BufferStrategy;

class ComplexPlane extends Canvas {

    private static final long serialVersionUID = 1L;
    boolean zPlane = false;

    Transform t = MovingParticles.zPlaneTransform;

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

        {
            BufferStrategy strategy = this.getBufferStrategy();
            Graphics g = strategy.getDrawGraphics();
            
            t.setScreenSpace(0, w, h, 0);
            t.graphics = g;
            
            g.clearRect(0, 0, w, h);
            MovingParticles.Drawing.draw(t);
            
            g.dispose();
            strategy.show();
        }

    } // blitpaint

}  // ComplexPlane

