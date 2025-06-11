package de.unileipzig.atool;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class LogGenerator {

    public static void generate(File file, int runs, int linePerRun, int startSpeed, int steadySpeed, int warmupLines) {


//        File file = new File("D:\\warmup_speed_log.log");
//
//        LogGenerator.generate(
//                file,
//                50,       // Total 50 runs
//                500,        //line per runs
//                20000,     //  lines per run
//                60000,     // start speed
//                1500     // warm up lines
//        );

        long startTimeMillis = 1;
        int lineCount = runs * linePerRun;
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            for (int i = 0; i < lineCount; i++) {
                long timestamp = startTimeMillis + i;

                // During warmup, linearly interpolate between startSpeed and steadySpeed
                double speed;
                if (i < warmupLines) {
                    double progress = (double) i / warmupLines;
                    // Quadratic smooth warmup
                    speed = (startSpeed  + ( steadySpeed - startSpeed) * Math.sqrt(progress));
//                    speed = speed + (Math.random() * 1000 - 2);
                } else {
//                    speed = steadySpeed + (int)(Math.random() * 1000 - 2);  // small noise
                    boolean coinFlip = Math.random() < 0.5;
                    int sign = 1;
                    if(!coinFlip){
                        sign = -1;
                    }
                    speed = steadySpeed + ((Math.random() * 1000 - 2) * sign);  // small noise
                }

                // Format: timeMillis, speed, 1, 4096, 0
                writer.write(timestamp + ", " + (int) speed + ", 1, 4096, 0\n");
            }

            System.out.println("Warmup log generated: " + file.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}