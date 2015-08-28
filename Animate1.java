
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
    JLabel frameDelayInfo;
    JLabel timeStepInfo;
    JPanel pointPane;
    JButton goButton;
    JButton stopButton;
    JButton resetButton;
    double timeStep = 0.001;

    public Animate1(String animation) {
        this.animation = animation;
        particles = MovingParticles.Drawing.getParticles();
        for (Point p : particles) {
            p.x_init = p.x;
            p.y_init = p.y;
            p.trajectory = MovingParticles.Drawing.addShape();
            MovingParticles.Drawing.addPointToShape(p.trajectory, p.x, p.y);
        }

        settingsFrame();
    }

    private void settingsFrame() {
        sFrame = new JFrame("animaton settings");
        Container pane = sFrame.getContentPane();
        pane.setLayout(new BoxLayout(pane, BoxLayout.PAGE_AXIS));

        frameDelayInfo = new JLabel("frame delay", JLabel.CENTER);
        final JSlider sliderMsec = new JSlider(0, 1000, 1000);
        sliderMsec.addChangeListener((new ChangeListener() {
            public void stateChanged(ChangeEvent changeEvent) {
                sleepMilliseconds = sliderMsec.getValue();
                frameDelayInfo.setText("frame delay " + sleepMilliseconds + " msec");
            }
        }));

        timeStepInfo = new JLabel("time step", JLabel.CENTER);
        final JSlider sliderTimeStep = new JSlider(-6000, -2000, -3000);
        sliderTimeStep.addChangeListener((new ChangeListener() {
            public void stateChanged(ChangeEvent changeEvent) {
                timeStep = Math.pow(10.0, (double) sliderTimeStep.getValue() / 1000);
                String timeStepString = String.format("%f", timeStep);
                timeStepInfo.setText("time step " + timeStepString + " sec");
            }
        }));

        pane.add(frameDelayInfo);
        pane.add(sliderMsec);
        pane.add(Box.createRigidArea(new Dimension(500, 20)));
        pane.add(timeStepInfo);
        pane.add(sliderTimeStep);
        pane.add(Box.createRigidArea(new Dimension(500, 20)));

        for (int i = 0; i < particles.size(); i++) {
            Point p = particles.get(i);
            pointPane = new JPanel();
            pointPane.setLayout(new BoxLayout(pointPane, BoxLayout.LINE_AXIS));
            pointPane.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
            pointPane.add(Box.createRigidArea(new Dimension(40, 0)));
            pointPane.add(new JLabel(p.particleName));
            addTextField(i, "Mass", p.mass);
            addTextField(i, "Velocity", p.velocity);
            addTextField(i, "Angle", p.angle);
            pane.add(pointPane);
        }

        JPanel goPanel = new JPanel();
        goButton = new JButton("Go");
        goButton.addActionListener(this);
        goPanel.add(goButton);
        stopButton = new JButton("Stop");
        stopButton.addActionListener(this);
        goPanel.add(stopButton);
        resetButton = new JButton("Reset");
        resetButton.addActionListener(this);
        goPanel.add(resetButton);

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
        } else if (e.getSource().equals(resetButton)) {
            suspended = true;
            for (Point p : particles) {
                // reset initial position
                p.x = p.x_init;
                p.y = p.y_init;
                // reset initial speed
                p.xspeed = p.velocity * Math.cos((p.angle / 180) * Math.PI);
                p.yspeed = p.velocity * Math.sin((p.angle / 180) * Math.PI);
            }
            // remove trajectories and start new ones
            MovingParticles.Drawing.removeTrajectories();
            for (Point p : particles) {
                p.trajectory = MovingParticles.Drawing.addShape();
                MovingParticles.Drawing.addPointToShape(p.trajectory, p.x, p.y);
            }
            MovingParticles.zPlane.blitPaint();
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
                if (attribute.equals("Velocity") || attribute.equals("Angle")) {
                    for (Point p : particles) {
                        p.xspeed = p.velocity * Math.cos((p.angle / 180) * Math.PI);
                        p.yspeed = p.velocity * Math.sin((p.angle / 180) * Math.PI);
                    }
                }
            }
        }

    }

    private void addTextField(int i, String label, double value) {
        pointPane.add(Box.createRigidArea(new Dimension(20, 0)));
        pointPane.add(new JLabel(label));
        pointPane.add(Box.createRigidArea(new Dimension(20, 0)));
        JTextField field = new JTextField(String.format("%.2f", value));
        field.addActionListener(this);
        field.setActionCommand(i + "|" + label);
        pointPane.add(field);
    }

    public void run() {
        boolean redraw = true;
        while (true) {
            if (!suspended) {
                if (animation.equals("rotate")) {
                    redraw = MovingParticles.Drawing.rotate(Math.PI / 200);
                }
                if (animation.equals("gravitate")) {
                    redraw = MovingParticles.Drawing.gravitate(timeStep);
                }

                if (redraw) {
                    try {
                        Thread.sleep(sleepMilliseconds);
                    } catch (InterruptedException ie) {
                    }
                    MovingParticles.zPlane.blitPaint();
                }
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
