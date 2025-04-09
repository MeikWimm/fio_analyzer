/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.atool;

import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 *
 * @author meni1999
 */
public class CustomFormatter extends Formatter{

    @Override
    public String format(LogRecord record) {
        StringBuilder msg = new StringBuilder();
        msg.append(String.format("[LOG, %s] ", record.getLevel()));
        msg.append(record.getMessage());
        msg.append("\n");
        return msg.toString();
    }
    
}
