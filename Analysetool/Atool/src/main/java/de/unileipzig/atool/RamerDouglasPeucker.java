package de.unileipzig.atool;

import java.util.ArrayList;
import java.util.List;

/**
 * Diese Klasse implementiert den Ramer-Douglas-Peucker-Algorithmus zur
 * Vereinfachung einer Kurve, die aus Punkten besteht.
 * Ziel ist es, eine ähnliche Kurve mit möglichst wenigen Punkten zu erzeugen,
 * ohne dabei die Gesamtform wesentlich zu verändern.
 */
public class RamerDouglasPeucker {

    private RamerDouglasPeucker() { }

    private static double sqr(double x) { 
        return Math.pow(x, 2);
    }

    private static double distanceBetweenPoints(double vx, double vy, double wx, double wy) {
        return sqr(vx - wx) + sqr(vy - wy);
    }

    /**
     * Berechnet das Quadrat der kürzesten Distanz eines Punktes (px,py) zum Liniensegment von (vx,vy) nach (wx,wy)
     */
    private static double distanceToSegmentSquared(double px, double py, double vx, double vy, double wx, double wy) {
        final double l2 = distanceBetweenPoints(vx, vy, wx, wy);  // Länge des Segments im Quadrat

        // Wenn das Segment nur ein Punkt ist
        if (l2 == 0) 
            return distanceBetweenPoints(px, py, vx, vy);

        // Berechne das Projektionsverhältnis t des Punktes auf die Linie
        final double t = ((px - vx) * (wx - vx) + (py - vy) * (wy - vy)) / l2;

        // Fall 1: Projektion liegt vor dem Startpunkt
        if (t < 0) 
            return distanceBetweenPoints(px, py, vx, vy);

        // Fall 2: Projektion liegt hinter dem Endpunkt
        if (t > 1) 
            return distanceBetweenPoints(px, py, wx, wy);

        // Fall 3: Projektion liegt auf dem Segment
        return distanceBetweenPoints(px, py, (vx + t * (wx - vx)), (vy + t * (wy - vy)));
    }

    // Berechnet die senkrechte (orthogonale) Distanz vom Punkt zum Liniensegment
    private static double perpendicularDistance(double px, double py, double vx, double vy, double wx, double wy) {
        return Math.sqrt(distanceToSegmentSquared(px, py, vx, vy, wx, wy));
    }

    /**
     * Ramer-Douglas-Peucker-Algorithmus
     *
     * @param list Ursprüngliche Punktliste
     * @param s Startindex
     * @param e Endindex
     * @param epsilon Toleranzschwelle (je größer, desto stärker wird vereinfacht)
     * @param resultList Ergebnisliste mit vereinfachten Punkten
     */
    private static void douglasPeucker(List<DataPoint> list, int s, int e, double epsilon, List<DataPoint> resultList) {
        // Maximaler Abstand initialisieren
        double dmax = 0;
        int index = 0;

        final int start = s;
        final int end = e - 1;

        // Finde den Punkt mit dem größten Abstand zur Linie
        for (int i = start + 1; i < end; i++) {
            final double px = list.get(i).getTime();
            final double py = list.get(i).getSpeed();
            final double vx = list.get(start).getTime();
            final double vy = list.get(start).getSpeed();
            final double wx = list.get(end).getTime();
            final double wy = list.get(end).getSpeed();

            final double d = perpendicularDistance(px, py, vx, vy, wx, wy);

            if (d > dmax) {
                index = i;
                dmax = d;
            }
        }

        // Wenn der maximale Abstand größer als die Toleranz ist, rekursiv teilen
        if (dmax > epsilon) {
            douglasPeucker(list, s, index, epsilon, resultList);
            douglasPeucker(list, index, e, epsilon, resultList);
        } else {
            // Wenn die Distanz innerhalb der Toleranz ist, nur Start- und Endpunkt übernehmen
            if ((end - start) > 0) {
                resultList.add(list.get(start));
                resultList.add(list.get(end));
            } else {
                resultList.add(list.get(start));
            }
        }
    }

    /**
     * Methode zur Vereinfachung einer Punktliste.
     * 
     * @param list Ursprüngliche Liste von DataPoints (Zeit, Geschwindigkeit)
     * @param epsilon Toleranz (je höher, desto stärker wird vereinfacht)
     * @return Vereinfachte Liste von DataPoints
     */
    public static final List<DataPoint> douglasPeucker(List<DataPoint> list, double epsilon) {
        final List<DataPoint> resultList = new ArrayList<>();
        douglasPeucker(list, 0, list.size(), epsilon, resultList);
        return resultList;
    }
}
