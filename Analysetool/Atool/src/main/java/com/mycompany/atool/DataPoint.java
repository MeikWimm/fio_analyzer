/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.atool;


/**
 *
 * @author meni1999
 */
public class DataPoint {
    private double speed;
    private double time;
    private int flag; 
    public double rank;
    
    public DataPoint(double x, double y) {
        this.x = x;
        this.y = y;
    }
    
    public void setX(double x){
        this.x = x;
    }

    public void setY(double y){
        this.y = y;
    }
    
    public double getX(){
        return this.x;
    }

    public double getY(){
        return this.y;
    }
    
    
    
}
