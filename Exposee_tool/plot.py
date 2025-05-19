# coding=utf-8
import matplotlib.pyplot as plt
import numpy as np
import sys

time_to_bw = dict()
bw_list = []

def plot_bw_per_time():
    xpoints = [x for x in time_to_bw.keys()]
    ypoints = [x[1] for x in time_to_bw.items()]

    fig, ax = plt.subplots()

    ax.stem(xpoints, ypoints, markerfmt=' ')
    plt.xlabel("Time in msec")
    plt.ylabel("Bandwidth KiB/s")
    plt.savefig("log_graph.svg")
    plt.show()

def plot_bw_frequency():
    bw_setlist = list(set(bw_list))
    my_dict = {i:bw_list.count(i) for i in bw_setlist}

    xpoints = [x for x in my_dict.keys()]
    ypoints = [x[1] for x in my_dict.items()]

    fig, ax = plt.subplots()

    ax.stem(xpoints,ypoints, markerfmt=' ', basefmt='C2-') 
    plt.xlabel("I/O Speed in KiB/s")
    plt.ylabel("Frequency")
    plt.savefig("log_graph.svg")
    plt.show()

def get_date():
    f = open(args[1], "r")
    old_time = 0 # Da im log die Zeit in Millisekunden gleich ist
    new_time = 0 # wollte ich nichts verwerfen und habe sie mit der np.linspace Funktion aufgeteilt 
    sum_time = 0
    counter = 0
    bw_with_same_time = [] # bw = bandwidth
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
        sum_time += old_time
        counter += 1
        bw_list.append(int(splitted_line[1]))
    f.close()
    mean_speed = sum_time / counter
    print("Average Speed: " + str(mean_speed) + " KiB/s")


def main(args):
    get_date()

    if(args[2] == 'freq'):
        plot_bw_frequency()
    else:
        plot_bw_per_time()

if __name__ == "__main__":
    args = sys.argv # args[1] = Dateipfad des Logs
    if(len(args) != 3):
        print(args)
        print("Dateipfad fehlt!")         
    else:
        main(args)