import sys
import re
import statistics
import math
import fileinput


############
### VARS ###
############
data = []
windowSize = 128

#######################
### PARSE this shit ###
#######################
regex = r'ACCELEROMETER::t=(\d+);sT=\d+;cT=\d+;x=(-?\d*\.?\d+);y=(-?\d*\.?\d+);z=(-?\d*\.?\d+)'

for line in fileinput.input():
	if(fileinput.isfirstline()):
		dataClass = re.search(r'_([^_]+)\.log$', fileinput.filename()).group(1)
# for line in sys.stdin:
	match = re.search(regex, line)
	if(match):
		# time = int(match.group(1))
		x = float(match.group(2))
		y = float(match.group(3))
		z = float(match.group(4))
		data.append((x, y, z, dataClass))

print("@RELATION HURRR")
print("@ATTRIBUTE min NUMERIC")
print("@ATTRIBUTE max NUMERIC")
print("@ATTRIBUTE dev NUMERIC")
print("@ATTRIBUTE class {walking,running}")
print("@DATA")

for window in [data[i:i+windowSize] for i in range(0, len(data)-windowSize, int(windowSize/2))]:
	eucNorms = [math.sqrt(values[0]*values[0] + values[1]*values[1] + values[2]*values[2]) for values in window]
		
	eucMin = min(eucNorms)
	eucMax = max(eucNorms)
	eucDev = statistics.stdev(eucNorms)

	print(str(eucMin)+','+str(eucMax)+','+str(eucDev)+','+str(window[0][3]))