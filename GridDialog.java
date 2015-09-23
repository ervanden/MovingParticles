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

class ParseIntField {

    static int value;

    static int parse(JTextField field) {
        try {
            value = Integer.parseInt(field.getText());
            System.out.println("parsed value=" + value);
            return value;
        } catch (java.lang.NumberFormatException nfe) {
            field.setText(field.getText() + " ?");
            field.setForeground(Color.red);
            return Integer.MAX_VALUE;
        }
    }
}

class GridDialog implements ActionListener, PropertyChangeListener {

    JFrame owningFrame;
    public double xmin;
    public double xdelta;
    public double ymin;
    public double ydelta;
    public int xn;
    public int yn;
    public boolean validValues=false; // false after "Cancel" or Window Close
    
    double initxmin, initxdelta, initymin, initydelta; // value to reset to
    int initxn, inityn;

    JDialog dialog;
    JTextField xminGridField = new JTextField(10);
    JTextField xdeltaGridField = new JTextField(10);
    JTextField yminGridField = new JTextField(10);
    JTextField ydeltaGridField = new JTextField(10);
    JTextField xnField = new JTextField(10);
        JTextField ynField = new JTextField(10);

    ArrayList<JTextField> textFields = new ArrayList<JTextField>();

    public void propertyChange(PropertyChangeEvent e) {
        String prop = e.getPropertyName();
        System.out.println(" property changed: " + prop);
    }

    public void actionPerformed(ActionEvent ae) {
        String lastButtonClicked = ae.getActionCommand();
        System.out.println(lastButtonClicked + " pressed!");
        if (ae.getActionCommand().equals("Enter")) {

            xmin = ParseDoubleField.parse(xminGridField);
            xdelta = ParseDoubleField.parse(xdeltaGridField);
            ymin = ParseDoubleField.parse(yminGridField);
            ydelta = ParseDoubleField.parse(ydeltaGridField);
            xn = ParseIntField.parse(xnField);
                        yn = ParseIntField.parse(ynField);

            if ((!Double.isNaN(xmin))
                    && (!Double.isNaN(xdelta))
                    && (!Double.isNaN(ymin))
                    && (!Double.isNaN(ydelta))
                    && (xdelta != Integer.MAX_VALUE)
                      && (ydelta != Integer.MAX_VALUE)                  
                    ) {
                System.out.println("xmin=" + xmin);
                System.out.println("xdelta=" + xdelta);
                System.out.println("ymin=" + ymin);
                System.out.println("ydelta=" + ydelta);
                System.out.println("xdelta= " + xdelta);
                 System.out.println("ydelta= " + ydelta);
                validValues=true;
                dialog.dispose();
            };

        } else if (ae.getActionCommand().equals("Reset")) {
            xminGridField.setText(("" + initxmin));
            xdeltaGridField.setText(("" + initxdelta));
            yminGridField.setText(("" + initymin));
            ydeltaGridField.setText(("" + initydelta));
            xnField.setText(("" + initxn));
                        ynField.setText(("" + inityn));
            for (JTextField field : textFields) {
                field.setForeground(Color.black);
            };

        } else { // "Cancel"
            validValues=false;
            dialog.dispose();
        }
        ;
    }

    public void popUp(JFrame f, double xmin, double ymin, double xdelta, double ydelta, int xn, int yn) {
        owningFrame = f;
        initxmin = xmin;
        initxdelta = xdelta;
        initymin = ymin;
        initydelta = ydelta;
        initxn= xn;
        inityn=yn;

        textFields.clear();
        textFields.add(xminGridField);
        textFields.add(xdeltaGridField);
        textFields.add(yminGridField);
        textFields.add(ydeltaGridField);
        textFields.add(xnField);
        textFields.add(ynField);

        xminGridField.setText(("" + initxmin));
        xdeltaGridField.setText(("" + initxdelta));
        yminGridField.setText(("" + initymin));
        ydeltaGridField.setText(("" + initydelta));
        xnField.setText((""+initxn));
          ynField.setText((""+inityn));

        Object[] array = {"xmin", xminGridField,
            "xdelta", xdeltaGridField,
            "ymin", yminGridField,
            "ydelta", ydeltaGridField,
        "xn",xnField,
        "yn",ynField};

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
