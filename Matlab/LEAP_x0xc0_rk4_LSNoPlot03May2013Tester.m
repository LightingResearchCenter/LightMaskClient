function [onTimes,offTimes,finalX,finalXC,endTime] = LEAP_x0xc0_rk4_LSNoPlot03May2013Tester(DaysimeterDataFile,CBTminTarget,X0,XC0,time0,AvailStartTime,AvailEndTime,tau,maskLightLevel,onTime0,onTime1,onTime2,offTime0,offTime1,offTime2, MaxLightDuration, maskColor )
% Input arguements
% DaysimeterDataFile: file name of calibrated Daysimeter data [dateStr,timeStr,Lux,CLA,CS,Activity]
% CBTminTarget: target CBTmin time in hours (0 <= CBTminTarget < 24)
% CBTminInitial: estimated time of CBTmin in hours (0 <= CBTmin < 24)
% X0: initial value of state variable corresponding to time0
% XC0: initial value of state variable corresponding to time0
% time0: initial time in Matlab datestr format (dd-mmm-yyyy HH:MM;SS)
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
X0 = -0.0654;
XC0 = 0.9979;
time0 = '19-Nov-2012 10:48:00';
%dataPath = '\\root\projects\Daysimeter and dimesimeter reference files\ONR winter 2013\Daysimeter Data\';
%}
%dataPath = 'C:\AndyOct2012\CircadianResearch\NIH_SleepMask\LRCmask\LEAP_Matlab\'; % Remove after testing
%dataFileName = DaysimeterDataFile;
%pathFileName = [dataPath dataFileName];
pathFileName = DaysimeterDataFile;

CBTminTarget = str2double(CBTminTarget);
X0 = str2double(X0);
XC0 = str2double(XC0);
time0 = datenum(time0,'dd-mmm-yyyy HH:MM');
AvailStartTime = str2double(AvailStartTime);
AvailEndTime = str2double(AvailEndTime);
tau = str2double(tau);
maskLightLevel = str2double(maskLightLevel);
MaxLightDuration = str2double(MaxLightDuration);

onTime0 = datenum(onTime0,'dd-mmm-yyyy HH:MM');
onTime1 = datenum(onTime1,'dd-mmm-yyyy HH:MM');
onTime2 = datenum(onTime2,'dd-mmm-yyyy HH:MM');
offTime0 = datenum(offTime0,'dd-mmm-yyyy HH:MM');
offTime1 = datenum(offTime1,'dd-mmm-yyyy HH:MM');
offTime2 = datenum(offTime2,'dd-mmm-yyyy HH:MM');

% Constants/Initial Conditions
%AvailStartTime = 23;
%AvailEndTime = 4;
%tau = 24.2; % (hours)
%maskLightLevel = 0.6; % Max Light Level (CS units)
offLightLevel = 0.0; %Min Light Level (CS units)
numOfDaysLEAP = 3;
increment = 1/12; % Hours 
nsteps = 30; % number of steps used for ODE solver for each time increment

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%%%%%%%%%%%%%%%%%%%%%%%% Run Daysimeter Data %%%%%%%%%%%%%%%%%%%%%%%%%%%
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

% Read Daysimeter data from file
[dateStr,timeStr,~,~,CS,~] = textread(pathFileName,'%s%s%f%f%f%f','headerlines',1);
%CS = ones(size(CS))*0.4;
%CS(end-1000:end-500) = 0.001;
[ Time, inc ] = ReadDaysimDataFromFile( dateStr, timeStr, CS ); %this needs to change for the new time section.

% crop Daysimeter data to begin at or after time0
index = find(Time>=time0,1,'first');
Time = Time(index:end);
CS = CS(index:end);

% Work with relative time, in hours, with starting and ending times always rounded to the nearest increment of an hour
%initialStartTime = (datenum(time0) - floor(datenum(time0)))*24; % Daysimeter start time, hours;
initialStartTime = (time0 - floor(time0))*24; % Previous end of Daysimeter data, relative time in hours;
initialStartTime = round(initialStartTime/increment)*increment; % rounded to nearest simulation increment

if (initialStartTime >=24) 
    initialStartTime = initialStartTime - 24;
    %absTimeOffset = floor(Time(1)) +1;
    absTimeOffset = floor(time0) +1;
else
    absTimeOffset = floor(time0);
end
sRate = 1/inc; % sample rate, 1/hours
%csTimeRelHours = (Time - floor(Time(1)))*24;
csTimeRelHours = (Time - floor(time0))*24;

% Resample CS: average value of CS during increment centered on increments
[ CSavg, timeCSavg ] = ReSampleCS( initialStartTime, increment, csTimeRelHours, sRate, CS, Time );

% Add in Light Mask exposure times
%redFlag = round(100*(maskLightLevel*100 - floor(maskLightLevel*100)))== 12;
if strcmp(maskColor, 'red') ~= 1; %not(redFlag) % if maskLightLevel*100 has a remainder of 0.12 it's a red mask so do nothing
    q0 = (timeCSavg > onTime0 & timeCSavg < offTime0);
    CSavg(q0) = maskLightLevel;
    q1 = (timeCSavg > onTime1 & timeCSavg < offTime1);
    CSavg(q1) = maskLightLevel;
    q2 = (timeCSavg > onTime2 & timeCSavg < offTime2);
    CSavg(q2) = maskLightLevel;
end

%{
%Plot of CS and CSavg
figure(1)                   % Only for plotting
plot(Time,CS,'r-')          % Only for plotting
hold on                     % Only for plotting
plot(timeCSavg,CSavg,'b-')  % Only for plotting
hold off                    % Only for plotting
datetick2('x')              % Only for plotting
%}
% Initialize dLoop start and end times for each iteration
t1 = initialStartTime; % hours
t2 = t1 + increment; % simulation interval end time, hours

dX = X0; %daysimeter X starts at intital X given
dXC = XC0; %daysimeter XC starts at intital XC given

%%%%%%%%%%%%%%%%%%%%%%%%%%% Daysimeter Loop %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%%%%% loop calculates X and XC Values Based on Daysimeter CSavg values %%%%
%dXplot = dX; % Only for plotting
%dXCplot = dXC; % Only for plotting
%timePlot = t1; % Only for plotting
%Xclock = -cos(2*pi*(t1/24-CBTminTarget/24)); % Only for plotting

for i1 = 1:length(CSavg)-1
  CSDrive = (CSavg(i1)+CSavg(i1+1))/2;
  
  [dX1 dXC1,~,~,~,~] = rk4stepperP(dX,dXC,CSDrive,tau,t1,t2,nsteps);
  
  %update values with output from rk4stepperP function
  dX = dX1;
  dXC = dXC1;
  t1 = t2; %increment startTime to where endTime is
  t2 = (t1 + increment); %increment endTime by increment
  
  %dXplot(i1+1) = dX; % Only for plotting
  %dXCplot(i1+1) = dXC; % Only for plotting
  %timePlot(i1+1) = t1; % Only for plotting
  %Xclock(i1+1) = -cos(2*pi*(t1/24-CBTminTarget/24)); % Only for plotting
end 

finalX = dX;
finalXC = dXC;
endTime = t2/24 + absTimeOffset;
%{
%Plot Of x and xc values based on daysimeter CSavg values
timePlot = timePlot/24 + floor(Time(1)); % absolute time
figure(2)
plot(timePlot,dXplot,'g.-')
hold on
plot(timePlot,Xclock,'b.-')
plot(timeCSavg,CSavg,'g-')
hold off
%}
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
%%%%%%%%%%%%% Loop determines when to give or remove light%%%%%%%%%%%%%%%%%

[ onTimes, offTimes, xTotal, xcTotal, xTarget, xcTarget, xTargetTotal, xcTargetTotal, AbsLoopTimeTotal, CS ] = PrescriptionLoopTester3( numOfDaysLEAP, increment, pX, pXC, maskLightLevel, tau, t1, t2, nsteps, offLightLevel, CBTminTarget, AvailStartTime, AvailEndTime, onTimes, offTimes, onCount, offCount, MaxLightDuration, absTimeOffset, Time );

finalpX = pX; % Only for plotting
finalpXC = pXC; % Only for plotting

onTimes = onTimes/24 + absTimeOffset;
offTimes = offTimes/24 + absTimeOffset;

% Print On and Off time Arrays
PrintOnOffArrays( onTimes, offTimes, finalX, finalXC, endTime );

%rk4PlotsTester( AbsLoopTimeTotal, xTotal, xTargetTotal, CS, xcTotal, finalpX, finalpXC, xTarget, xcTarget, xcTargetTotal );
