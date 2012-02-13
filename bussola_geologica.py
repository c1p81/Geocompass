import android, time,math
droid = android.Android()
droid.startSensingTimed(1,1000)
time.sleep(1)
for x in range(0,1):
	azimuth = droid.sensorsReadOrientation().result[0]
	pitch = droid.sensorsReadOrientation().result[1]
	roll = droid.sensorsReadOrientation().result[2]
	pimezzi = math.pi/2
	conv = (2*math.pi)/360
 
	cosalfa = math.cos(roll-pimezzi)
	cosbeta = math.cos(pitch-pimezzi)
	
	dir_maxpendenza = math.degrees(math.atan(cosalfa/cosbeta))
	if (pitch < 0):
                dir_maxpendenza = dir_maxpendenza + 180
        if ((pitch > 0) and (roll < 0)):
                dir_maxpendenza = dir_maxpendenza + 360
        print "Az "+ str(math.degrees(azimuth))
	print "Dir.MaxPendenza : " + str(dir_maxpendenza)
        dir_maxpendenza2 = (math.degrees(azimuth)+dir_maxpendenza)% 360 
	print "Corr.MaxPendenza : " + str(dir_maxpendenza2)

	ang_maxpendenza = 90-math.degrees(math.acos(math.sqrt((cosalfa*cosalfa)+(cosbeta*cosbeta))))
	print "Ang.MaxPendenza: "+ str(ang_maxpendenza)
	time.sleep(1)
droid.stopSensing()
