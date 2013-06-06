function [onTimes , offTimes, finalX, finalXC, endTime, MaxLightDuration] = LEAP_CBTmin_rk4_NoPlot31May2013Tester(DaysimeterDataFile,CBTminTarget,CBTminInitial,AvailStartTime,AvailEndTime,tau,maskLightLevel, MaxLightDuration, maskColor)
% Input arguements
% DaysimeterDataFile: file name of calibrated Daysimeter data [dateStr,timeStr,Lux,CLA,CS,Activity]
% CBTminTarget: target CBTmin time in hours (0 <= CBTminTarget < 24)
% CBTminInitial: estimated time of CBTmin in hours (0 <= CBTmin < 24)
% AvailStartTime: the hour (0 to 23) when subject is available for light treatment
% AvailEndTime: the hour(0 to 23) when the subject is no longer available for light treatment
% tau: the subject's dark free-running pacemaker period (hours e.g, 24.2)
% maskLightLevel: The CS of the mask (e.g. 0.4)
% Output values
% onTimes: starting times (in MatLab datenum format) for light therapy
% offTimes: ending times (in MatLab datenum format) for light therapy
% finalX: state variable value at time when Daysimeter data ends
% finalXC: state variable value at time when Daysimeter data ends
% endTime: time when Daysimeter data ends (in MatLab datenum format)

%{
%For running as a script
clear
DaysimeterDataFile = 'Day18_121125_1028_processed.txt'; %'Daysim33_130115_0352_Sub29_processed.txt'; 'Day18_121125_1028_processed.txt'
CBTminTarget = 5;
CBTminInitial = 2.5;
%dataPath = '\\root\projects\Daysimeter and dimesimeter reference files\ONR winter 2013\Daysimeter Data\';
%}
%dataPath = 'C:\AndyOct2012\CircadianResearch\NIH_SleepMask\LRCmask\LEAP_Matlab\'; % Remove after testing
%dataFileName = DaysimeterDataFile;
%pathFileName = [dataPath dataFileName];
pathFileName = DaysimeterDataFile;

CBTminTarget = str2num(CBTminTarget);
CBTminInitial = str2num(CBTminInitial);
AvailStartTime = str2num(AvailStartTime);
AvailEndTime = str2num(AvailEndTime);
tau = str2num(tau);
maskLightLevel = str2num(maskLightLevel);
MaxLightDuration = str2num(MaxLightDuration);

% Constants/Initial Conditions
offLightLevel = 0.0; %Min Light Level (CS units)
numOfDaysLEAP = 3;
increment = 0.25; % Hours 
nsteps = 30; % number of steps used for ODE solver for each time increment

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%%%%%%%%%%%%%%%%%%%%%%%% Run Daysimeter Data %%%%%%%%%%%%%%%%%%%%%%%%%%%
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

% Read Daysimeter data from file
[dateStr,timeStr,~,~,CS,~] = textread(pathFileName,'%s%s%f%f%f%f','headerlines',1);

[ Time ] = ReadDaysimDataFromFile( dateStr, timeStr, CS );

% Work with relative time, in hours, with starting and ending times always rounded to the nearest increment of an hour 
initialStartTime = (Time(1) - floor(Time(1)))*24; % Daysimeter start time, hours
initialStartTime = round(initialStartTime/increment)*increment; % rounded to nearest simulation increment
if (initialStartTime >=24)
    initialStartTime = initialStartTime - 24;
    absTimeOffset = floor(Time(1)) +1;
else
    absTimeOffset = floor(Time(1));
end
sRate = 1/(24*(Time(2)-Time(1))); % sample rate, 1/hours
csTimeRelHours = (Time - floor(Time(1)))*24;

% Resample CS: average value of CS during increment centered on increments
[ CSavg, timeCSavg ] = ReSampleCS( initialStartTime, increment, csTimeRelHours, sRate, CS, Time );

%Plot of CS and CSavg
%figure(1)
%plot(Time,CS,'r-')
%hold on
%plot(timeCSavg,CSavg,'b-')
%hold off
%datetick2('x')

[ t1, t2, dX, dXC ] = InitializedLoop( initialStartTime, increment, CBTminInitial );

%%%%%%%%%%%%%%%%%%%%%%%%%%% Daysimeter Loop %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
[ t1, t2, tend, xarray, xcarray, dX, dXC ] = DaysimLoop( CSavg, dX, dXC, tau, t1, t2, nsteps, increment );

finalX = dX;
finalXC = dXC;
endTime = t2/24 + absTimeOffset;

%Plot Of x and xc values based on daysimeter CSavg values
%timePlot = timePlot/24 + floor(Time(1)); % absolute time
%figure(2)
%plot(timePlot,dXplot,'g.-')
%hold on
%plot(timePlot,Xclock,'b.-')
%plot(timeCSavg,CSavg,'g-')
%hold off

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%%%%%%%%%%%%%%%%%%%%%%% START PRESCRIPTION %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

%Prescription Arrays and Counters
onTimes = []; %Array of times to turn light on
offTimes = []; %Array of times to turn light off
onCount = 1; %Array element for OnTimes
offCount = 1; %Array element for OffTimes

%Initialize prescription x and xc
pX = dX; %Prescription initial x starts where the daysimeter x ended
pXC = dXC; %Prescription intital xc starts where the daysimeter xc ended

%Initialize prescription loop start and end times
t1 = t2; % Prescription loop start time initially starts where the daysimeter experiment ends (hours)
t2 = t1 + increment; %Prescription loop end time initially starts where the daysimeter experiment ends + increment

%%%%%%%%%%%%%%%%%%%%%%%%%% PRESCRIPTION LOOP %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

[ onTimes, offTimes ] = PrescriptionLoopTester3( numOfDaysLEAP, increment, pX, pXC, maskLightLevel, tau, t1, t2, nsteps, offLightLevel, CBTminTarget, AvailStartTime, AvailEndTime, onTimes, offTimes, onCount, offCount, MaxLightDuration, absTimeOffset );

%finalpX = pX; % Only for plotting
%finalpXC = pXC; % Only for plotting

onTimes = onTimes/24 + absTimeOffset;
offTimes = offTimes/24 + absTimeOffset;

% If final offTime is missing (beyond end of light prescription range)
if (length(offTimes) < length(onTimes))
    offTimes = [offTimes (endTime+numOfDaysLEAP)];
end

PrintOnOffArrays( onTimes, offTimes, finalX, finalXC, endTime );
