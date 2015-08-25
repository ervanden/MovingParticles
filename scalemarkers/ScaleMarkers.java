package scalemarkers;

import java.lang.Math;
import java.util.*;




public class ScaleMarkers {

    public static java.util.List<String> createScale(double xmin, double xmax) {
        java.util.List<String> scaleList = new ArrayList<String>();
        scaleList.clear();

        // if the width of the window is between 10 and 100, we put marks every 10
        // if the width of the window is between 100 and 1000, we put marks every 100
        // >> if log10(width) is between 1 and 2 , we put marks every 10^1
        // >> if log10(width) is between 2 and 3, we put marks every 10^2
        // so we first calculate floor(log10(xmax-xmin))
        double x = xmax - xmin;

        double l = Math.log10(x);
        double f = Math.floor(l);
        int fi = (int) f;

//        System.out.println("x=" + x + " logx=" + l + " floor=" + fi);

        // if we put marks every 100, we find 
        //      is1 = the smallest multiple of 100 that is > xmin 
        //  and is2 = the largest multiple of 100 that is < xmax
        double s1 = xmin;
        double s2 = xmax;
        int is1;
        int is2;
        int step;
        if (fi >= 0) {
            for (int d = 1; d <= fi; d++) {
                s1 = s1 / 10;
                s2 = s2 / 10;
            };

            is1 = (int) Math.floor(s1) + 1;
            is2 = (int) Math.floor(s2);
            step = 1;

            for (int d = 1; d <= fi; d++) {
                is1 = is1 * 10;
                is2 = is2 * 10;
                step = step * 10;
            };

            for (int mark = is1; mark <= is2; mark = mark + step) {
                scaleList.add("" + mark);
            };

        } else {
            double factor = 1;
            for (int d = 1; d <= Math.abs(fi); d++) {
                s1 = s1 * 10;
                s2 = s2 * 10;
                factor = factor / 10;
            };

            is1 = (int) Math.floor(s1) + 1;
            is2 = (int) Math.floor(s2);
            step = 1;

//            System.out.println("is1=" + is1 + " is2=" + is2 + " factor = " + factor);

            for (int mark = is1; mark <= is2; mark = mark + 1) {
                String label;
                if (fi == -1) {label = String.format("%.1f", (double) mark * factor);}
                else if (fi == -2) {label = String.format("%.2f", (double) mark * factor);}
                else if (fi == -3) {label = String.format("%.3f", (double) mark * factor);}
                else if (fi == -4) {label = String.format("%.4f", (double) mark * factor);} 
                else if (fi == -5) {label = String.format("%.5f", (double) mark * factor);}                
                else if (fi == -6) {label = String.format("%.6f", (double) mark * factor);}               
                else if (fi == -7) {label = String.format("%.7f", (double) mark * factor);}                
                else if (fi == -8) {label = String.format("%.8f", (double) mark * factor);} 
                else               {label = String.format("%.9f", (double) mark * factor);} 

                label=label.replace(',', '.');
                               scaleList.add(label);
               

            };

        };
        return scaleList;
    }
}


/*
public class TestScaleMarkers {

    public static void main(String[] args) {
        java.util.List<String> scaleList = ScaleMarkers.createScale(100.00333, 108);

        for (String marker : scaleList) {
            double value = Double.parseDouble(marker);
            System.out.println("marker at " + value + " = " + marker);
        }
    }
}
*/
