
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class AnimationRunner implements Runnable, ActionListener, ChangeListener, ItemListener {

    Transform transform = MovingParticles.transform;

    String animationType;
    Animation a;
    double time = 0;
    int steps = 0;

    ArrayList<Point> particles = null;
   Marker centerOfGravity = null;

    public boolean suspended = true;
    public boolean exitRequested = false;
    boolean trajectoryOn = false;
    double timeStep = 0.001;
    int resolution = 5;
    int sleepMilliseconds = 1000;
    boolean centerCog = false;

    JFrame sFrame;
    JLabel frameDelayInfo;
    JLabel timeStepInfo;
    JLabel resolutionInfo;
    JButton initializeButton;
    JButton goSuspendButton;
    JButton resetButton;
    JButton followCogButton;
    JSlider sliderFrameDelay;
    JSlider sliderTimeStep;
    JSlider sliderResolution;
    JComboBox animationBox;
    JCheckBox trajectoryBox;
    JCheckBox cogBox;

    public AnimationRunner() {

        sFrame = new JFrame("animaton settings");
        Container pane = sFrame.getContentPane();
        pane.setLayout(new BoxLayout(pane, BoxLayout.PAGE_AXIS));
         Border       blackline = BorderFactory.createLineBorder(Color.black);

         JPanel topPane=new JPanel();
         topPane.setLayout(new BoxLayout(topPane, BoxLayout.PAGE_AXIS));
         topPane.setBorder(blackline);
         pane.add(topPane);

        String[] animationTypes = {"rotation", "gravity", "elastic"};

        animationBox = new JComboBox(animationTypes);
        animationBox.setSelectedIndex(2);
        animationType = (String) animationBox.getSelectedItem();
        animationBox.addActionListener(this);

        frameDelayInfo = new JLabel("frame delay", JLabel.CENTER);
        sliderFrameDelay = new JSlider(0, 1000, 1000);
        sliderFrameDelay.addChangeListener(this);

        timeStepInfo = new JLabel("time step", JLabel.CENTER);
        sliderTimeStep = new JSlider(-6000, -1000, -3000);
        sliderTimeStep.addChangeListener(this);

        resolutionInfo = new JLabel("drawing resolution", JLabel.CENTER);
        sliderResolution = new JSlider(1, 15, 5);
        sliderResolution.addChangeListener(this);

        trajectoryBox = new JCheckBox("Show trajectories");
        cogBox = new JCheckBox("Show center of gravity");
        trajectoryBox.addItemListener(this);
        cogBox.addItemListener(this);
        cogBox.setEnabled(false);
        trajectoryBox.setEnabled(false);

        topPane.add(Box.createRigidArea(new Dimension(500, 20)));
        topPane.add(animationBox);
        topPane.add(Box.createRigidArea(new Dimension(500, 20)));
        topPane.add(frameDelayInfo);
        topPane.add(sliderFrameDelay);
        topPane.add(Box.createRigidArea(new Dimension(500, 20)));
        topPane.add(timeStepInfo);
        topPane.add(sliderTimeStep);
        topPane.add(Box.createRigidArea(new Dimension(500, 20)));
        topPane.add(resolutionInfo);
        topPane.add(sliderResolution);
        topPane.add(Box.createRigidArea(new Dimension(500, 20)));

        JPanel showPanel = new JPanel();
        showPanel.add(trajectoryBox);
        showPanel.add(cogBox);

        JPanel goPanel = new JPanel();
        initializeButton = new JButton("Initialize");
        initializeButton.addActionListener(this);
        goPanel.add(initializeButton);
        goSuspendButton = new JButton("Go/Suspend");
        goSuspendButton.addActionListener(this);
        goPanel.add(goSuspendButton);
        resetButton = new JButton("Reset");
        resetButton.addActionListener(this);
        goPanel.add(resetButton);
        followCogButton = new JButton("Center CoG");
        followCogButton.addActionListener(this);
        goPanel.add(followCogButton);

        resetButton.setEnabled(false);
        followCogButton.setEnabled(false);
        goSuspendButton.setEnabled(false);

        topPane.add(Box.createRigidArea(new Dimension(500, 20)));
        topPane.add(showPanel);
        topPane.add(Box.createRigidArea(new Dimension(500, 20)));
        topPane.add(goPanel);

        sFrame.pack();
        sFrame.setVisible(true);
    }

    public void stateChanged(ChangeEvent e) {
        if (e.getSource().equals(sliderFrameDelay)) {
            sleepMilliseconds = sliderFrameDelay.getValue();
            frameDelayInfo.setText("frame delay " + sleepMilliseconds + " msec");
        } else if (e.getSource().equals(sliderTimeStep)) {
            timeStep = Math.pow(10.0, (double) sliderTimeStep.getValue() / 1000);
            String timeStepString = String.format("%f", timeStep);
            timeStepInfo.setText("time step " + timeStepString + " sec");
        } else if (e.getSource().equals(sliderResolution)) {
            resolution = sliderResolution.getValue();
            String resolutionString = String.format("%d", resolution);
            resolutionInfo.setText("resolution " + resolutionString + " pixels");
        }
    }

    public void itemStateChanged(ItemEvent e) {

        Object source = e.getItemSelectable();

        if (source == trajectoryBox) {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                trajectoryOn = true;
            };
            if (e.getStateChange() == ItemEvent.DESELECTED) {
                trajectoryOn = false;
            };
            System.out.println("trajectories " + trajectoryOn);
            for (Point p : particles) {
                wipeTrajectory(p);
                if (trajectoryOn) {
                    createTrajectory(p);
                }
            }
        }

        if (source == cogBox) {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                if (centerOfGravity == null) {
                    centerOfGravity = MovingParticles.Drawing.addMarker(0d,0d);
                    centerOfGravity.color = Color.BLUE;
                    centerOfGravity.name = "CoG";
                }
            };
            if (e.getStateChange() == ItemEvent.DESELECTED) {
                if (centerOfGravity != null) {
                    MovingParticles.Drawing.deleteMarker(centerOfGravity);
                    centerOfGravity = null;
                }
            };

            if (centerOfGravity != null) {
                System.out.println("cog ON");
            } else {
                System.out.println("cog OFF");
            }

        }

    }

    private void wipeTrajectory(Point p) {                 // trajectory is set to null 
        if (p.trajectory != null) {
            MovingParticles.Drawing.deleteCurve(p.trajectory);
            p.trajectory = null;
        }
    }

    private void createTrajectory(Point p) {
        if (p.trajectory == null) {
            p.trajectory = MovingParticles.Drawing.addCurve();
        } else {
            System.out.println("createTrajectory() : trajectory already exists");
        }
        MovingParticles.Drawing.addPointToCurve(p.trajectory, p.x, p.y);
    }

    public Marker updateCenterOfGravity() {
        double xCenterOfGravity = 0;
        double yCenterOfGravity = 0;
        double totalMass = 0;

        for (Point p : particles) {
            xCenterOfGravity = xCenterOfGravity + p.mass * p.x;
            yCenterOfGravity = yCenterOfGravity + p.mass * p.y;
            totalMass = totalMass + p.mass;
        }
        centerOfGravity.x = xCenterOfGravity / totalMass;
        centerOfGravity.y = yCenterOfGravity / totalMass;
        return centerOfGravity;
    }

    public void reset() {

        for (Point p : particles) {

            wipeTrajectory(p);

            // reset initial position
            p.x = p.x_init;
            p.y = p.y_init;
            // reset initial speed
            p.vx = p.velocity * Math.cos((p.angle / 180) * Math.PI);
            p.vy = p.velocity * Math.sin((p.angle / 180) * Math.PI);

            if (trajectoryOn) {
                createTrajectory(p);
            }

            if (centerOfGravity != null) {
                updateCenterOfGravity();
            }
        }
        time = 0;
        steps = 0;

        MovingParticles.zPlane.blitPaint();

    }

    public void actionPerformed(ActionEvent e) {

        if (e.getSource().equals(animationBox)) {
            animationType = (String) animationBox.getSelectedItem();
        } else if (e.getSource().equals(initializeButton)) {
            if (animationType.equals("rotation")) {
                a = new Rotation();
            }
            if (animationType.equals("gravity")) {
                a = new Gravity();
            }
            if (animationType.equals("elastic")) {
                a = new Elasticity();
            }

            particles = a.getParticles(); // particles can not change once animation is initialized

            suspended = true;
            initializeButton.setEnabled(false);
            resetButton.setEnabled(true);
            followCogButton.setEnabled(true);
            goSuspendButton.setEnabled(true);
            cogBox.setEnabled(true);
            trajectoryBox.setEnabled(true);

            if (a.getPane() != null) {
                sFrame.getContentPane().add(a.getPane());
                sFrame.pack();
                sFrame.setVisible(true);
            }

        } else if (e.getSource().equals(goSuspendButton)) {

            suspended = !suspended;

        } else if (e.getSource().equals(resetButton)) {

            suspended = true;
            reset();

        } else if (e.getSource().equals(followCogButton)) {
            centerCog = !centerCog;
        }
    }

    void moveCenterToCog() {
        Marker cog = updateCenterOfGravity();
        double xmin = transform.uxmin;
        double xmax = transform.uxmax;
        double ymin = transform.uymin;
        double ymax = transform.uymax;
        double xcenter = (xmax + xmin) / 2;
        double ycenter = (ymax + ymin) / 2;
        double xdelta = cog.x - xcenter;
        double ydelta = cog.y - ycenter;
//System.out.printf("%5f %5f %5f %5f\n", xmin, xmax, ymin, ymax);
//System.out.printf("xcenter=%5f ycenter=%5f cogx=%5f cogy=%5f xdelta=%5f ydelta=%5f\n",xcenter, ycenter,cog.x,cog.y,xdelta,ydelta);
        transform.setUserSpace(xmin + xdelta, xmax + xdelta, ymin + ydelta, ymax + ydelta);
    }

    public void run() {
        System.out.println("run() suspended=" + suspended);

        boolean redraw = true;

        while (!exitRequested) {
            if (!suspended && !exitRequested) {

                redraw = a.step(timeStep, resolution);

                time = time + timeStep;
                steps = steps + 1;

                if (redraw) {
                    try {
                        Thread.sleep(sleepMilliseconds);
                    } catch (InterruptedException ie) {
                    }
                    if (centerOfGravity != null) {
                        updateCenterOfGravity();
                        if (centerCog) {
                            moveCenterToCog();
                        }
                    }
                    sFrame.setTitle(String.format("time: %6.2f steps %d", time, steps));
                    MovingParticles.zPlane.blitPaint();
                }
            }
            while (suspended && !exitRequested) {
                try {
                    Thread.sleep(25);
                } catch (InterruptedException ie) {
                }
            }

        }

        System.out.println("exit request for  animation runner...");
        for (Point p : particles) {
            wipeTrajectory(p);
        }
        if (centerOfGravity != null) {
            MovingParticles.Drawing.deleteMarker(centerOfGravity);
        }
        sFrame.dispose();
        System.out.println("exited.");
    }
}
