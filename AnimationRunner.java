
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class AnimationRunner implements Runnable, ActionListener, ChangeListener {

    String animationType;
    Animation a;

    public boolean suspended = true;
    public boolean exitRequested = false;
    boolean trajectoryOn = false;
    double timeStep = 0.001;
    int sleepMilliseconds = 1000;

    JFrame sFrame;
    JLabel frameDelayInfo;
    JLabel timeStepInfo;
    JButton initializeButton;
    JButton goSuspendButton;
    JButton resetButton;
    JButton trajectoryButton;
    JSlider sliderMsec;
    JSlider sliderTimeStep;
    JComboBox animationBox;

    public AnimationRunner() {

        sFrame = new JFrame("animaton settings");
        Container pane = sFrame.getContentPane();
        pane.setLayout(new BoxLayout(pane, BoxLayout.PAGE_AXIS));

        String[] animationTypes = {"rotation", "gravity", "elastic"};

        animationBox = new JComboBox(animationTypes);
        animationBox.setSelectedIndex(2);
        animationType = (String) animationBox.getSelectedItem();
        animationBox.addActionListener(this);

        frameDelayInfo = new JLabel("frame delay", JLabel.CENTER);
        sliderMsec = new JSlider(0, 1000, 1000);
        sliderMsec.addChangeListener(this);

        timeStepInfo = new JLabel("time step", JLabel.CENTER);
        sliderTimeStep = new JSlider(-6000, -1000, -3000);
        sliderTimeStep.addChangeListener(this);

        pane.add(animationBox);
        pane.add(Box.createRigidArea(new Dimension(500, 20)));
        pane.add(frameDelayInfo);
        pane.add(sliderMsec);
        pane.add(Box.createRigidArea(new Dimension(500, 20)));
        pane.add(timeStepInfo);
        pane.add(sliderTimeStep);
        pane.add(Box.createRigidArea(new Dimension(500, 20)));

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
        trajectoryButton = new JButton("Trajectory on/off");
        trajectoryButton.addActionListener(this);
        goPanel.add(trajectoryButton);

        resetButton.setEnabled(false);
        trajectoryButton.setEnabled(false);
        goSuspendButton.setEnabled(false);

        pane.add(goPanel);
        sFrame.pack();
        sFrame.setVisible(true);
    }

    public void stateChanged(ChangeEvent e) {
        if (e.getSource().equals(sliderMsec)) {
            sleepMilliseconds = sliderMsec.getValue();
            frameDelayInfo.setText("frame delay " + sleepMilliseconds + " msec");
        } else if (e.getSource().equals(sliderTimeStep)) {
            timeStep = Math.pow(10.0, (double) sliderTimeStep.getValue() / 1000);
            String timeStepString = String.format("%f", timeStep);
            timeStepInfo.setText("time step " + timeStepString + " sec");
        }
    }

    private void wipeTrajectory(Point p) {                 // trajectory is set to null 
        if (p.trajectory != null) {
            p.trajectory.clear();
            p.trajectory = null;
        }
    }

    private void createTrajectory(Point p) {
        if (p.trajectory == null) {
            p.trajectory = MovingParticles.Drawing.addShape();
        } else {
            System.out.println("createTrajectory() : trajectory already exists");
        }
        MovingParticles.Drawing.addPointToShape(p.trajectory, p.x, p.y);
    }

    public void reset() {

        for (Point p : a.getParticles()) {

            wipeTrajectory(p);

            // reset initial position
            p.x = p.x_init;
            p.y = p.y_init;
            // reset initial speed
            p.xspeed = p.velocity * Math.cos((p.angle / 180) * Math.PI);
            p.yspeed = p.velocity * Math.sin((p.angle / 180) * Math.PI);

            if (trajectoryOn) {
                createTrajectory(p);
            }
        }

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
            suspended = true;
            initializeButton.setEnabled(false);
            resetButton.setEnabled(true);
            trajectoryButton.setEnabled(true);
            goSuspendButton.setEnabled(true);

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

        } else if (e.getSource().equals(trajectoryButton)) {

            trajectoryOn = !trajectoryOn;
            for (Point p : a.getParticles()) {
                wipeTrajectory(p);
                if (trajectoryOn) {
                    createTrajectory(p);
                }
            }
        }
    }

    public void run() {
        System.out.println("run() suspended=" + suspended);

        boolean redraw = true;
        double time = 0;
        int steps = 0;
        while (!exitRequested) {
            if (!suspended && !exitRequested) {

                redraw = a.step(timeStep);

                time = time + timeStep;
                steps = steps + 1;
                sFrame.setTitle(String.format("time: %6.2f steps %d", time, steps));

                if (redraw) {
                    try {
                        Thread.sleep(sleepMilliseconds);
                    } catch (InterruptedException ie) {
                    }
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
        for (Point p : a.getParticles()) {
            wipeTrajectory(p);
        }
        sFrame.dispose();
        a.cleanup();
        System.out.println("exited.");
    }
}
