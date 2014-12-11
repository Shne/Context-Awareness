import sys
import re
import statistics
import math
import fileinput

############
### VARS ###
############
dataDict = dict()
windowSize = 128

#######################
### PARSE this shit ###
#######################
regex = r'ACCELEROMETER::t=\d+;sT=\d+;cT=(\d+);x=(-?\d*\.?\d+);y=(-?\d*\.?\d+);z=(-?\d*\.?\d+)'

for line in fileinput.input():
	if(fileinput.isfirstline()):
		filename = fileinput.filename()
		dataDict[filename] = []
		dataClass = re.search(r'_([^_]+)\.(log|csv)$', fileinput.filename()).group(1)

	if not 'csv' in filename:
		match = re.search(regex, line)
		if(match):
			time = int(match.group(1))
			x = float(match.group(2))
			y = float(match.group(3))
			z = float(match.group(4))
			dataDict[filename].append((x, y, z, dataClass, time))
	else:
		row = line.split(',')
		time = int(row[1])
		x = float(row[11])
		y = float(row[12])
		z = float(row[13])
		dataDict[filename].append((x,y,z,dataClass,time))

print("@RELATION HURRR")
print("@ATTRIBUTE min NUMERIC")
print("@ATTRIBUTE max NUMERIC")
print("@ATTRIBUTE dev NUMERIC")
print("@ATTRIBUTE class {still,walking,running,biking,driving}")
print("@DATA")

cutlines = 0
for (_, data) in dataDict.items():
	for window in [data[i:i+windowSize] for i in range(cutlines, len(data)-windowSize-cutlines, int(windowSize/2))]:
		eucNorms = [math.sqrt(values[0]*values[0] + values[1]*values[1] + values[2]*values[2]) for values in window]

		eucMin = min(eucNorms)
		eucMax = max(eucNorms)
		eucDev = statistics.stdev(eucNorms)

		print(str(eucMin)+','+str(eucMax)+','+str(eucDev)+','+str(window[0][3]))
