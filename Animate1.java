
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class Animate1 implements Runnable, ActionListener {

    String animation;
    public boolean suspended = true;

    ArrayList<Point> particles;
    int sleepMilliseconds = 1000;
    JFrame sFrame;
    JLabel sliderInfo;
    JPanel pointPane;
    JButton goButton;
    JButton stopButton;
    double timeStep = 0.001;

    private void settingsFrame() {
        sFrame = new JFrame("animaton settings");
        Container pane = sFrame.getContentPane();
        pane.setLayout(new BoxLayout(pane, BoxLayout.PAGE_AXIS));

        final JSlider sliderMsec = new JSlider(0, 1000, 1000);
        sliderMsec.addChangeListener((new ChangeListener() {
            public void stateChanged(ChangeEvent changeEvent) {
                sleepMilliseconds = sliderMsec.getValue();
                sliderInfo.setText("frame delay " + sleepMilliseconds + " msec");
            }
        }));

        sliderInfo = new JLabel("frame delay", JLabel.CENTER);
        pane.add(sliderInfo);
        pane.add(sliderMsec);
        pane.add(Box.createRigidArea(new Dimension(500, 20)));
        particles = MovingParticles.Drawing.getParticles();
        for (int i = 0; i < particles.size(); i++) {
            Point p = particles.get(i);
            pointPane = new JPanel();
            pointPane.setLayout(new BoxLayout(pointPane, BoxLayout.LINE_AXIS));
            pointPane.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
            pointPane.add(Box.createRigidArea(new Dimension(40, 0)));
            pointPane.add(new JLabel(p.particleName));
            addTextField(i, "Mass");
            addTextField(i, "Velocity");
            addTextField(i, "Angle");
            pane.add(pointPane);
        }

        JPanel goPanel = new JPanel();
        goButton = new JButton("Go");
        goButton.addActionListener(this);
        goButton.setActionCommand("Go");
        goPanel.add(goButton);
        stopButton = new JButton("Stop");
        stopButton.addActionListener(this);
        stopButton.setActionCommand("Stop");
        goPanel.add(stopButton);

        pane.add(goPanel);
        sFrame.pack();
        sFrame.setVisible(true);
    }

    public void actionPerformed(ActionEvent e) {
        String action = e.getActionCommand();
        if (e.getSource().equals(goButton)) {
            suspended = false;
        } else if (e.getSource().equals(stopButton)) {
            suspended = true;
        } else {
            JTextField field = (JTextField) e.getSource();
            int particleNr = Integer.parseInt(action.split("[|]")[0]);
            String attribute = action.split("[|]")[1];
            boolean validValue = true;
            double value = 0d;
            try {
                value = Double.valueOf(field.getText());
            } catch (Exception ex) {
                field.setBackground(Color.yellow);
                validValue = false;
            }
            if (validValue) {
                field.setBackground(Color.white);
                System.out.println(particleNr + "." + attribute + "=" + value);
                if (attribute.equals("Mass")) {
                    particles.get(particleNr).mass = value;
                    field.setBackground(Color.green);
                }
                if (attribute.equals("Velocity")) {
                    particles.get(particleNr).velocity = value;
                    field.setBackground(Color.green);
                }
                if (attribute.equals("Angle")) {
                    particles.get(particleNr).angle = value;
                    field.setBackground(Color.green);
                }
                for (Point p : particles) {
                    p.xspeed = p.velocity * Math.cos((p.angle / 180) * Math.PI);
                    p.yspeed = p.velocity * Math.sin((p.angle / 180) * Math.PI);
                }
            }
        }

    }

    private void addTextField(int i, String label) {
        pointPane.add(Box.createRigidArea(new Dimension(20, 0)));
        pointPane.add(new JLabel(label));
        pointPane.add(Box.createRigidArea(new Dimension(20, 0)));
        JTextField field = new JTextField();
        field.addActionListener(this);
        field.setActionCommand(i + "|" + label);
        pointPane.add(field);
    }

    public Animate1(String animation) {
        this.animation = animation;
        settingsFrame();
    }

    public void run() {

        while (true) {
            if (!suspended) {
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
            }
            while (suspended) {
                try {
                    Thread.sleep(25);
                } catch (InterruptedException ie) {
                }
            }

        }
    }
}
