import matplotlib.pyplot as plt
import numpy as np

def main():
    time_to_bw = dict()
    f = open("..\\fio\\logs\\mytest_bw.1.log", "r")
    counter = 0
    old_time = -1
    new_time = -1
    bw_with_same_time = []
    for x in f:
        new_time = x.split(",")[0]
        if old_time == new_time:
            counter += 1
            bw_with_same_time.append(x)
        else:
            if counter != 0:
                linspace = np.linspace(0.0, 1.0, num=counter)
                for i in range(0,counter):
                    line = bw_with_same_time[i].split(",")
                    time_to_bw[float(float(line[0]) + linspace[i])] = int(line[1])
            counter = 0
            bw_with_same_time = []
        line_list = x.split(",")
        old_time = line_list[0]
    f.close()
    xpoints = [x for x in time_to_bw.keys()]
    ypoints = [x[1] for x in time_to_bw.items()]
    print(max(ypoints))
    plt.plot(xpoints, ypoints)
    plt.savefig("nm_mytest_bw.svg")
    plt.show()
main()