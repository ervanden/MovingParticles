
import java.awt.event.*;
import java.awt.*;
import javax.swing.*;
import java.beans.*;
import java.util.ArrayList;

class ShapeDialog implements ActionListener, PropertyChangeListener {

    JFrame owningFrame;
    public double segmentLength;
    public double pointRadius;
    public boolean validValues = false; // false after "Cancel" or Window Close   
    double initSegmentLength; // value to reset to
    double initPointRadius;
    JDialog dialog;
    JTextField segmentLengthField = new JTextField(10);
        JTextField pointRadiusField = new JTextField(20);

    public void propertyChange(PropertyChangeEvent e) {
        String prop = e.getPropertyName();
        System.out.println(" property changed: " + prop);
    }

    public void actionPerformed(ActionEvent ae) {
        String lastButtonClicked = ae.getActionCommand();
        System.out.println(lastButtonClicked + " pressed!");
        if (ae.getActionCommand().equals("Enter")) {

            segmentLength = ParseDoubleField.parse(segmentLengthField);
            pointRadius = ParseDoubleField.parse(pointRadiusField);

            if (!Double.isNaN(segmentLength)
                    && !Double.isNaN(pointRadius) ) {
                validValues = true;
                dialog.dispose();
            };

        } else if (ae.getActionCommand().equals("Reset")) {
            segmentLengthField.setText(("" + initSegmentLength));
            segmentLengthField.setForeground(Color.black);
            pointRadiusField.setText(("" + initPointRadius));
            pointRadiusField.setForeground(Color.black);
        } else { // "Cancel"
            validValues = false;
            dialog.dispose();
        }
    }

    public void popUp(JFrame f, double s, double r) {
        owningFrame = f;
        initSegmentLength = s;
        segmentLengthField.setText(("" + initSegmentLength));
        initPointRadius = r;
        pointRadiusField.setText(("" + initPointRadius));

        Object[] array = {
           "point radius", pointRadiusField,         
            "segment length", segmentLengthField
        };

        JButton btnEnter = new JButton("Enter");
        JButton btnReset = new JButton("Reset");
        JButton btnCancel = new JButton("Cancel");
        btnEnter.addActionListener(this);
        btnEnter.setActionCommand("Enter");
        btnReset.addActionListener(this);
        btnReset.setActionCommand("Reset");
        btnCancel.addActionListener(this);
        btnCancel.setActionCommand("Cancel");

        Object[] options = {btnEnter, btnReset, btnCancel};

        //Create the JOptionPane.
        JOptionPane optionPane = new JOptionPane(array,
                JOptionPane.QUESTION_MESSAGE,
                JOptionPane.YES_NO_OPTION,
                null,
                options,
                null);

        dialog = new JDialog(owningFrame, "Grid Parameters", true);
        dialog.setContentPane(optionPane);
        dialog.pack();
        dialog.setVisible(true);

    }
}
