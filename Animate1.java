
import java.awt.Container;
import java.awt.Dimension;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class Animate1 implements Runnable {

    String animation;
    public boolean suspended = false;
    int sleepMilliseconds = 1000;
    JFrame sFrame;
    JLabel sliderInfo;
    double timeStep;

    public Animate1(String animation) {
        this.animation = animation;
        sFrame = new JFrame("animaton settings");
        Container pane = sFrame.getContentPane();
        pane.setLayout(new BoxLayout(pane, BoxLayout.PAGE_AXIS));

        final JSlider sliderMsec = new JSlider(0, 1000, 1000);
        sliderMsec.addChangeListener((new ChangeListener() {
            public void stateChanged(ChangeEvent changeEvent) {
                sleepMilliseconds = sliderMsec.getValue();
                System.out.println("slider " + sleepMilliseconds);
                sliderInfo.setText("frame delay " + sleepMilliseconds + " msec");
            }
        }));

        sliderInfo = new JLabel("frame delay", JLabel.CENTER);
        pane.add(sliderInfo);
        pane.add(sliderMsec);
        pane.add(Box.createRigidArea(new Dimension(500, 20)));

        for (Point p : MovingParticles.Drawing.getParticles()) {
            JPanel editorPane = new JPanel();
            editorPane.setLayout(new BoxLayout(editorPane, BoxLayout.LINE_AXIS));
            editorPane.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
            editorPane.add(Box.createRigidArea(new Dimension(40, 0)));
            editorPane.add(new JLabel(p.particleName));
            editorPane.add(Box.createRigidArea(new Dimension(20, 0)));
            editorPane.add(new JLabel("Mass"));
            editorPane.add(Box.createRigidArea(new Dimension(20, 0)));
            editorPane.add(new JTextField());
                        editorPane.add(Box.createRigidArea(new Dimension(20, 0)));
            editorPane.add(new JLabel("Velocity"));
                        editorPane.add(Box.createRigidArea(new Dimension(20, 0)));

            editorPane.add(new JTextField());
                        editorPane.add(Box.createRigidArea(new Dimension(20, 0)));
            editorPane.add(new JLabel("Angle"));
            editorPane.add(Box.createRigidArea(new Dimension(20, 0)));
            editorPane.add(new JTextField());

            pane.add(editorPane);
        }
            sFrame.pack();
            sFrame.setVisible(true);

    }

    

    public void run() {

        while (true) {

            if (animation.equals("rotate")) {
                MovingParticles.Drawing.rotate(Math.PI / 200);
            }
            if (animation.equals("gravitate")) {
                MovingParticles.Drawing.gravitate(timeStep);
            }

            try {
                Thread.sleep(sleepMilliseconds);
            } catch (InterruptedException ie) {
            }

            MovingParticles.zPlane.blitPaint();

            while (suspended) {
                try {
                    Thread.sleep(25);
                } catch (InterruptedException ie) {
                }
            }

        }
    }
}
