import matplotlib.pyplot as plt
import numpy as np
import sys

def main(args):
    time_to_bw = dict()
    old_time = 0 # Da im log die Zeit in Millisekunden gleich ist
    new_time = 0 # wollte ich nichts verwerfen und habe sie mit der np.linspace Funktion aufgeteilt 
    bw_with_same_time = [] # bw = bandwidth

    f = open(args[1], "r")

    for line in f:
        splitted_line = line.split(",")
        new_time = int(splitted_line[0])

        if old_time == new_time:
            bw_with_same_time.append(line)
        else:
                linspace = np.linspace(0.0, 1.0, num=len(bw_with_same_time)+1)
                for i in range(0,len(bw_with_same_time)):
                    temp_line = bw_with_same_time[i].split(",")
                    time_to_bw[float(float(temp_line[0]) + linspace[i])] = int(temp_line[1])
                bw_with_same_time = []
                bw_with_same_time.append(line) # Füge die Zeile mit der neuen Zeit hinzu.

        old_time = int(splitted_line[0])
    
    f.close()
    xpoints = [x for x in time_to_bw.keys()]
    ypoints = [x[1] for x in time_to_bw.items()]
    
    # For loop diente zum Vergleichen mit der Log Datei. 
    # Nur die ersten 20 Zeilen werden ausgegeben.
    for i in range(0, 20): 
        print(f"Time: {xpoints[i]} Bw: {ypoints[i]}")
    
    plt.plot(xpoints, ypoints)
    plt.xlabel("Time in msec")
    plt.ylabel("Bandwidth KiB/s")
    plt.savefig("log_graph.svg")
    plt.show()

if __name__ == "__main__":
    args = sys.argv # args[1] = Dateipfad des Logs
    if(len(args) != 2):
        print("Dateipfad fehlt!") 
    else:
        main(args)