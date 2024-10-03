import matplotlib.pyplot as plt
import numpy as np
import sys

def main(args):
    time_to_bw = dict()
    f = open(args, "r")
    old_time = 0
    new_time = 0
    bw_with_same_time = []
    for l in f:
        new_time = int(l.split(",")[0])
        if old_time == new_time:
            bw_with_same_time.append(l)
        else:
                linspace = np.linspace(0.0, 1.0, num=len(bw_with_same_time)+1)
                for i in range(0,len(bw_with_same_time)):
                    line = bw_with_same_time[i].split(",")
                    time_to_bw[float(float(line[0]) + linspace[i])] = int(line[1])
                bw_with_same_time = []
                bw_with_same_time.append(l)
        line_list = l.split(",")
        old_time = int(line_list[0])
    f.close()
    xpoints = [x for x in time_to_bw.keys()]
    ypoints = [x[1] for x in time_to_bw.items()]
    for i in range(0, 100):
        print(f"Time: {xpoints[i]} Bw: {ypoints[i]}")
    plt.plot(xpoints, ypoints)
    plt.xlabel("Time in ms")
    plt.ylabel("Bandwidth Mb/ms")
    plt.savefig("nm_mytest_bw.svg")
    plt.show()
if __name__ == "__main__":    
    args = sys.argv[1]
    main(args)