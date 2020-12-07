/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Khusus Skripsi Fuzzy
 */
public class Coba {
    public static void main(String[] args) {
        double a =  -0.3727;
        double b = 9.136;
        double c = 1.772;
        
        double hasil = a*Math.exp(-Math.pow((0-b)/c,2));
        double hasil2 = a*Math.exp(-Math.pow((4.4-b)/c,2));
        double hasil3 = a*Math.exp(-Math.pow((10-b)/c,2));
//        double hasil = a*Math.exp(-b*0)+c;
//        double hasil2 = a*Math.exp(-b*4.49)+c;        
//        double hasil3 = a*Math.exp(-b*10)+c;
        double Lm = hasil2-hasil;
        double Rm = hasil3-hasil2;
        System.out.println(Lm/Rm);
        
    }
}
