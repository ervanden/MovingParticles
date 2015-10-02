
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
import javax.swing.JTextField;

public class PropertyFrame implements ActionListener {

    JFrame pFrame;
    Container pane = null;
    JPanel topPanel = null;
    JPanel pointPanel;
    JButton goButton;
    ArrayList<Point> pointList;
    ArrayList<Point> displayList;

    public PropertyFrame() {
        pFrame = new JFrame("properties");
        pane = pFrame.getContentPane();
        pane.setLayout(new BoxLayout(pane, BoxLayout.PAGE_AXIS));
        goButton = new JButton("Go");
    }

    public void display(ArrayList<Point> plist) {

        pointList = plist;
        if (plist.size() > 1) {
            Point p_all = new Point(0, 0);
            p_all.name = "all";
            displayList = new ArrayList<>();
            displayList.add(p_all);
            if (plist.size() <= 10) {
                displayList.addAll(plist);
            }
        } else {
            displayList = plist;
        }

        if (topPanel != null) {
            pane.remove(topPanel);
        }

        topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.PAGE_AXIS));

        topPanel.add(Box.createRigidArea(new Dimension(500, 20)));

        int i = 0; // index of p in plist

        for (Point p : displayList) {

            System.out.println("add to panel : " + p.name);
            pointPanel = new JPanel();
            pointPanel.setLayout(new BoxLayout(pointPanel, BoxLayout.LINE_AXIS));
            pointPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
            pointPanel.add(Box.createRigidArea(new Dimension(40, 0)));
            pointPanel.add(new JLabel(p.name));
            addTextField(i, "Radius", p.radius);
            if (p.filled) {
                addTextField(i, "filled", 1);
            } else {
                addTextField(i, "filled", 0);
            }
            addTextField(i, "Mass", p.mass);
            addTextField(i, "Velocity", p.velocity);
            addTextField(i, "Angle", p.angle);
            topPanel.add(pointPanel);
            i++;
        }
        topPanel.add(goButton);

        pane.add(topPanel);

        pFrame.pack();
        pFrame.setVisible(true);
    }

    private void addTextField(int i, String label, double value) {
        pointPanel.add(Box.createRigidArea(new Dimension(20, 0)));
        pointPanel.add(new JLabel(label));
        pointPanel.add(Box.createRigidArea(new Dimension(20, 0)));
        JTextField field = new JTextField(String.format("%.2f", value));
        field.addActionListener(this);
        field.setActionCommand(i + "|" + label);
        pointPanel.add(field);
    }

    public void actionPerformed(ActionEvent e) {

        String action = e.getActionCommand();

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
            String pName = displayList.get(particleNr).name;
            field.setBackground(Color.white);

            if (attribute.equals("Radius")) {
                if (particleNr == 0) {
                    for (Point p : pointList) {
                        p.radius = value;
                    }
                }
                displayList.get(particleNr).radius = value;
                System.out.println(pName + "." + attribute + "=" + value);
                field.setBackground(Color.green);
            }
            if (attribute.equals("filled")) {
                if (particleNr == 0) {
                    for (Point p : pointList) {
                        p.filled = (value > 0);
                    }
                }
                displayList.get(particleNr).filled = (value > 0);
                System.out.println(pName + "." + attribute + "=" + value);
                field.setBackground(Color.green);
            }
            if (attribute.equals("Mass")) {
                if (particleNr == 0) {
                    for (Point p : pointList) {
                        p.mass = value;
                    }
                }
                displayList.get(particleNr).mass = value;
                System.out.println(pName + "." + attribute + "=" + value);

                field.setBackground(Color.green);
            }
            if (attribute.equals("Velocity")) {
                if (particleNr == 0) {
                    for (Point p : pointList) {
                        p.velocity = value;
                    }
                }
                displayList.get(particleNr).velocity = value;
                System.out.println(pName + "." + attribute + "=" + value);
                field.setBackground(Color.green);
            }
            if (attribute.equals("Angle")) {
                if (particleNr == 0) {
                    for (Point p : pointList) {
                        p.angle = value;
                    }
                }
                displayList.get(particleNr).angle = value;
                System.out.println(pName + "." + attribute + "=" + value);
                field.setBackground(Color.green);
            }
            if (attribute.equals("Velocity") || attribute.equals("Angle")) {
                for (Point p : pointList) {
                    p.vx = p.velocity * Math.cos((p.angle / 180) * Math.PI);
                    p.vy = p.velocity * Math.sin((p.angle / 180) * Math.PI);
                }
            }
        }
        MovingParticles.repaint();
    }
}
