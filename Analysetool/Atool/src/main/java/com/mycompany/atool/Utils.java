/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.atool;

import java.util.Comparator;

/**
 *
 * @author meni1999
 */
public abstract class Utils {
    public static class SpeedComparator implements Comparator<DataPoint>{

        @Override
        public int compare(DataPoint lhs, DataPoint rhs) {
            if(lhs.getSpeed() > rhs.getSpeed()){
                return 1;
            } else if(lhs.getSpeed() < rhs.getSpeed()) {
                return -1;
            } else {
                return 0;
            }
        }
    }
}
