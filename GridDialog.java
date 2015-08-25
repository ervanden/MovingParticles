import java.awt.event.*;
import java.awt.*;
import javax.swing.*;
import java.beans.*;
import java.util.ArrayList;

class ParseDoubleField {

    static double value;

    static double parse(JTextField field) {
        try {
            value = Double.parseDouble(field.getText());
            System.out.println("parsed value=" + value);
            return value;
        } catch (java.lang.NumberFormatException nfe) {
            field.setText(field.getText() + " ?");
            field.setForeground(Color.red);
            return Double.NaN;
        }
    }
}

class GridDialog implements ActionListener, PropertyChangeListener {

    JFrame owningFrame;
    public double xmin;
    public double xmax;
    public double ymin;
    public double ymax;
    public double delta;
    public boolean validValues=false; // false after "Cancel" or Window Close
    
    double initxmin, initxmax, initymin, initymax, initdelta; // value to reset to

    JDialog dialog;
    JTextField xminGridField = new JTextField(10);
    JTextField xmaxGridField = new JTextField(10);
    JTextField yminGridField = new JTextField(10);
    JTextField ymaxGridField = new JTextField(10);
    JTextField deltaField = new JTextField(10);

    ArrayList<JTextField> textFields = new ArrayList<JTextField>();

    public void propertyChange(PropertyChangeEvent e) {
        String prop = e.getPropertyName();
        System.out.println(" property changed: " + prop);
    }

    public void actionPerformed(ActionEvent ae) {
        String lastButtonClicked = ae.getActionCommand();
        System.out.println(lastButtonClicked + " pressed!");
        if (ae.getActionCommand().equals("Enter")) {

            int nrErrors = 0;
            JTextField field;

            xmin = ParseDoubleField.parse(xminGridField);
            xmax = ParseDoubleField.parse(xmaxGridField);
            ymin = ParseDoubleField.parse(yminGridField);
            ymax = ParseDoubleField.parse(ymaxGridField);
            delta = ParseDoubleField.parse(deltaField);

            if ((!Double.isNaN(xmin))
                    && (!Double.isNaN(xmax))
                    && (!Double.isNaN(ymin))
                    && (!Double.isNaN(ymax))
                    && (!Double.isNaN(delta))) {
                System.out.println("xmin=" + xmin);
                System.out.println("xmax=" + xmax);
                System.out.println("ymin=" + ymin);
                System.out.println("ymax=" + ymax);
                System.out.println("delta= " + delta);
                validValues=true;
                dialog.dispose();
            };

        } else if (ae.getActionCommand().equals("Reset")) {
            xminGridField.setText(("" + initxmin));
            xmaxGridField.setText(("" + initxmax));
            yminGridField.setText(("" + initymin));
            ymaxGridField.setText(("" + initymax));
            deltaField.setText(("" + initdelta));
            for (JTextField field : textFields) {
                field.setForeground(Color.black);
            };

        } else { // "Cancel"
            validValues=false;
            dialog.dispose();
        }
        ;
    }

    public void popUp(JFrame f, double xmin, double xmax, double ymin, double ymax, double delta) {
        owningFrame = f;
        initxmin = xmin;
        initxmax = xmax;
        initymin = ymin;
        initymax = ymax;
        initdelta= delta;

        textFields.clear();
        textFields.add(xminGridField);
        textFields.add(xmaxGridField);
        textFields.add(yminGridField);
        textFields.add(ymaxGridField);
        textFields.add(deltaField);

        xminGridField.setText(("" + initxmin));
        xmaxGridField.setText(("" + initxmax));
        yminGridField.setText(("" + initymin));
        ymaxGridField.setText(("" + initymax));
        deltaField.setText((""+initdelta));

        Object[] array = {"xmin", xminGridField,
            "xmax", xmaxGridField,
            "ymin", yminGridField,
            "ymax", ymaxGridField,
        "delta",deltaField};

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

        //       dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
  /*
         dialog.addWindowListener(new WindowAdapter() {
         public void windowClosing(WindowEvent we) {
         System.out.println("Thwarted user attempt to close window.");
         }
         });
         optionPane.addPropertyChangeListener(this);
   */
        dialog.pack();
        dialog.setVisible(true);

    }
}
