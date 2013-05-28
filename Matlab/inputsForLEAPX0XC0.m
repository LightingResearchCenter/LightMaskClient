


DaysimeterDataFile = 'C:\Daysimeter2012\NIH_LightMaskStudy\Day97_130502_0911_processed.txt';
CBTminTarget = '6';
X0 = '-0.4725';
XC0 = '-1.0053';
time0 = '01-May-2013 00:15:00';
AvailStartTime = '0.5';
AvailEndTime = '3.5';
tau = '24.2';
maskLightLevel = '0.43';
onTime0 = '01-May-2013 00:45:00';
onTime1 = '02-May-2013 00:45:00';
onTime2 = '03-May-2013 00:45:00';
offTime0 = '01-May-2013 03:30:00';
offTime1 = '02-May-2013 03:30:00';
offTime2 = '03-May-2013 02:15:00';

[onTimes,offTimes,finalX,finalXC,endTime] = LEAP_x0xc0_rk4_LSNoPlot09Apr2013(DaysimeterDataFile,CBTminTarget,X0,XC0,time0,AvailStartTime,AvailEndTime,tau,maskLightLevel,onTime0,onTime1,onTime2,offTime0,offTime1,offTime2);

datestr(onTimes)
datestr(offTimes)