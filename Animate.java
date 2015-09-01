
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

public class Animate implements Runnable, ActionListener, ChangeListener {

    String animation;
    public boolean suspended = true;
    double timeStep = 0.001;
    int sleepMilliseconds = 1000;

    JFrame sFrame;
    JLabel frameDelayInfo;
    JLabel timeStepInfo;
    JButton goButton;
    JButton stopButton;
    JButton resetButton;
    JSlider sliderMsec;
    JSlider sliderTimeStep;

    public Animate(String animation) {
        animation = "gravitate";
        animation = "elastic";
        this.animation = animation;

        MovingParticles.Drawing.initializeAnimation(animation);

        sFrame = new JFrame("animaton settings");
        Container pane = sFrame.getContentPane();
        pane.setLayout(new BoxLayout(pane, BoxLayout.PAGE_AXIS));

        frameDelayInfo = new JLabel("frame delay", JLabel.CENTER);
        sliderMsec = new JSlider(0, 1000, 1000);
        sliderMsec.addChangeListener(this);

        timeStepInfo = new JLabel("time step", JLabel.CENTER);
        sliderTimeStep = new JSlider(-6000, -1000, -3000);
        sliderTimeStep.addChangeListener(this);

        pane.add(frameDelayInfo);
        pane.add(sliderMsec);
        pane.add(Box.createRigidArea(new Dimension(500, 20)));
        pane.add(timeStepInfo);
        pane.add(sliderTimeStep);
        pane.add(Box.createRigidArea(new Dimension(500, 20)));

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

    public void actionPerformed(ActionEvent e) {
        String action = e.getActionCommand();
        if (e.getSource().equals(goButton)) {
            suspended = false;
        } else if (e.getSource().equals(stopButton)) {
            suspended = true;
        } else if (e.getSource().equals(resetButton)) {
            suspended=true;
            MovingParticles.Drawing.resetAnimation(animation);
        }
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

                if (animation.equals("elastic")) {
                    redraw = MovingParticles.Drawing.elastic(timeStep);
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
