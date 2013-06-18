%function [onTimes,offTimes,finalX,finalXC,endTime] = LEAP_x0xc0_rk4_LSNoPlot09Apr2013(DaysimeterDataFile,CBTminTarget,X0,XC0,time0,AvailStartTime,AvailEndTime,tau,maskLightLevel,onTime0,onTime1,onTime2,offTime0,offTime1,offTime2)
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

CBTminTarget = str2num(CBTminTarget);
X0 = str2num(X0);
XC0 = str2num(XC0);
time0 = datenum(time0,'dd-mmm-yyyy HH:MM');
AvailStartTime = str2num(AvailStartTime);
AvailEndTime = str2num(AvailEndTime);
tau = str2num(tau);
maskLightLevel = str2num(maskLightLevel);

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
increment = 0.25; % Hours 
nsteps = 30; % number of steps used for ODE solver for each time increment

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%%%%%%%%%%%%%%%%%%%%%%%% Run Daysimeter Data %%%%%%%%%%%%%%%%%%%%%%%%%%%
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

% Read Daysimeter data from file
[dateStr,timeStr,~,~,CS,~] = textread(pathFileName,'%s%s%f%f%f%f','headerlines',1);
%CS = ones(size(CS))*0.4;
%CS(end-1000:end-500) = 0.001;
Time = zeros(size(CS));
for i1 = 1:length(dateStr)  % Convert date and time strings into Malab time format (days since Jan 1, 0000)
    Time(i1) = datenum([dateStr{i1} ' ' timeStr{i1}]);
end

% Work with relative time, in hours, with starting and ending times always rounded to the nearest increment of an hour
initialStartTime = (datenum(time0) - floor(datenum(time0)))*24; % Daysimeter start time, hours;
initialStartTime = round(initialStartTime/increment)*increment; % rounded to nearest simulation increment
index = find(Time>=initialStartTime,1,'first');
Time = Time(index:end);
CS = CS(index:end);
if (initialStartTime >=24)
    initialStartTime = initialStartTime - 24;
    absTimeOffset = floor(Time(1)) +1;
else
    absTimeOffset = floor(Time(1));
end
sRate = 1/(24*(Time(2)-Time(1))); % sample rate, 1/hours
csTimeRelHours = (Time - floor(Time(1)))*24;

% Resample CS: average value of CS during increment centered on increments
CSavg = []; %Averages for each increment
k = 1;
index2First = round((initialStartTime+increment/2 - csTimeRelHours(1))*sRate);
index2 = index2First;
CSavg(k) = mean(CS(1:index2First));
deltaIndex = round(increment*sRate);
while (index2 < length(CS) - increment*sRate)
    k = k+1;
    index1 = round(index2First + 1 + (k-2)*increment*sRate);
    index2 = index1 + deltaIndex;
    CSavg(k) = mean(CS(index1:index2));
end
if ((length(CS) - index2)*sRate > increment/2)
    CSavg(k+1) = mean(CS(index2+1:end));
    timeCSavg = ((0:k)*increment + initialStartTime)/24 + floor(Time(1));
else
    timeCSavg = ((0:k-1)*increment + initialStartTime)/24 + floor(Time(1));
end

% Add in Light Mask exposure times
redFlag = round(100*(maskLightLevel*100 - floor(maskLightLevel*100)))== 12;
if not(redFlag) % if maskLightLevel*100 has a remainder of 0.12 it's a red mask so do nothing
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
numIterations = numOfDaysLEAP*24/increment;
CS = zeros(numIterations,1);

%xTotal= zeros(numIterations,1); % Only for plotting
%xcTotal = zeros(numIterations,1); % Only for plotting
%xTargetTotal = zeros(numIterations,1); % Only for plotting
%xcTargetTotal = zeros(numIterations,1); % Only for plotting
%pLoopTimeTotal = zeros(numIterations,1); % Only for plotting

for i1 = 1:numIterations  
    [pX1 pXC1,~,~,~,~] = rk4stepperP(pX,pXC,maskLightLevel,tau,t1,t2,nsteps);
    [pX2 pXC2,~,~,~,~] = rk4stepperP(pX,pXC,offLightLevel,tau,t1,t2,nsteps);
    %L and P process functions
    %[pX1 pXC1] = LP(t1, t2, pX, pXC, n0, maskLightLevel, G, alpha0, beta, tau); %L/P Process Function for CS = 0.4
    %[pX2 pXC2] = LP(t1, t2, pX, pXC, n0, offLightLevel, G, alpha0, beta, tau); %L/P Process Function for CS = 0
    
    %Target Sinusoid
    xTarget = -cos(2*pi*(t1/24 - CBTminTarget/24)); %Real part of Target sinusoid
    xcTarget = sin(2*pi*(t1/24 - CBTminTarget/24)); %complex part of Target sinusoid
    %xTargetTotal(i1) = xTarget; % Only for plotting
    %xcTargetTotal(i1) = xcTarget; % Only for plotting
   % pLoopTimeTotal(i1) = t1; %hours % Only for plotting
    
    %Absolute Loop Time (real time)
    %AbsLoopTimeTotal = pLoopTimeTotal/24 + floor(Time(1)); % Only for plotting
  
  % CALCULATIONS TO SEE IF ADDING LIGHT BRINGS THE CYCLE CLOSER TO THE CBTmin
    withLight = ((pX1 - xTarget)^2) + ((pXC1 - xcTarget)^2);
    withoutLight = ((pX2 - xTarget)^2) + ((pXC2 - xcTarget)^2);
    %If adding Light Brings the Cycle Closer to the CBTmin, then Add Light. Else Add no Light
    %Update all Arrays and Initial Conditions
    
    ToD = mod(t1,24); %Time of Day
    if AvailStartTime > AvailEndTime 
        Available = (ToD > AvailStartTime || ToD < AvailEndTime);
    else 
        Available = (ToD > AvailStartTime && ToD < AvailEndTime);
    end
    
    if ((withLight < withoutLight) && (i1 > 2) && (Available == 1))
        CS(i1) = maskLightLevel;
        %xTotal(i1) = pX1; % Only for plotting
        %xcTotal(i1) = pXC1; % Only for plotting
        pX = pX1;
        pXC = pXC1;
    else
        CS(i1) = 0;
        %xTotal(i1) = pX2; % Only for plotting
        %xcTotal(i1) = pXC2; % Only for plotting
        pX = pX2;
        pXC = pXC2;
    end     
    
    % ARRAYS FOR THE LIGHT ON AND OFF TIMES
    if (CS(i1) == maskLightLevel && i1==1)
        onTimes(1) = t1; %(Time(1) - floor(Time(1)))*24;
        onCount = onCount + 1;
    end
    if i1 > 1
        if (CS(i1-1) == offLightLevel) && (CS(i1) == maskLightLevel)
            onTimes(onCount) = t1;
            onCount = onCount + 1;
        elseif (CS(i1-1) == maskLightLevel) && (CS(i1) == offLightLevel)
           offTimes(offCount) = t1;
            offCount = offCount + 1;
        end    
    end
    
    % Increment Start and End Times for Each Iteration of the Loop
    t1 = t1+ increment;
    t2 = t2+ increment;
    
end % end P loop

%finalpX = pX; % Only for plotting
%finalpXC = pXC; % Only for plotting

onTimes = onTimes/24 + absTimeOffset;
offTimes = offTimes/24 + absTimeOffset;
% Print On and Off time Arrays
if length(onTimes)>=3
    datestr(onTimes(1:3))
elseif length(onTimes)==2
    onTimes(3) = floor(onTimes(2))+1.5; % make third onTime the following day at noon
    datestr(onTimes)
elseif length(onTimes)==1
    onTimes(2) = floor(onTimes(1))+1.5; % make second onTime the following day at noon
    onTimes(3) = onTimes(2)+1; % make third onTime the following day at noon
    datestr(onTimes)
elseif length(onTimes)==0
    onTimes(1) = floor(endTime) + 1.5; % make first onTime following day at noon
    onTimes(2) = onTimes(1)+1; % make second onTime the following day, same time
    onTimes(3) = onTimes(2)+1; % make third onTime the following day, same time
    datestr(onTimes)
end
if length(offTimes)>=3 
    datestr(offTimes(1:3))
elseif length(offTimes)==2
    offTimes(3) = onTimes(3) + 6.9445e-04; % a minute after onTime
    datestr(offTimes)
elseif length(offTimes)==1
    offTimes(2) = onTimes(2) + 6.9445e-04; % a minute after onTime
    offTimes(3) = onTimes(3) + 6.9445e-04; % a minute after onTime
    datestr(offTimes)
elseif length(offTimes)==0
    offTimes(1) = onTimes(1) + 6.9445e-04; % a minute after onTime
    offTimes(2) = onTimes(2) + 6.9445e-04; % a minute after onTime
    offTimes(3) = onTimes(3) + 6.9445e-04; % a minute after onTime
    datestr(offTimes)
end
finalX + 0
finalXC + 0
datestr(endTime)

% PLOTS

%%%%%%%%SINUSOID WAVEFORM PLOT%%%%%%%%%
%timeSim = tcTotal/24+Time(1);
figure(2)
hold on
plot(AbsLoopTimeTotal,xTotal,'r-','LineWidth',2); %Waveform with Initial Conditions
hold on;
plot(AbsLoopTimeTotal,xTargetTotal,'b-','LineWidth',2); %Target Waveform
plot(AbsLoopTimeTotal,CS,'g')
%datetick2('x')
hold off;
xlabel('Time','FontSize',14)
ylabel('Relative CBT deviation','FontSize',14)

%%%%%%%%%%%%%POLAR PLOT%%%%%%%%%%%%%%%%
figure(3)
plot(xTotal, xcTotal,'r-','LineWidth',2); axis equal; % Waveform with Initial Conditions
hold on;
plot(finalpX, finalpXC,'ro','LineWidth',2); axis equal;
plot(xTarget, xcTarget,'bs','LineWidth',2); axis equal;
plot(xTargetTotal, xcTargetTotal,'b-','LineWidth',2); axis equal; %Target Waveform
hold off;
xlabel('X','FontSize',14)
ylabel('Xc','FontSize',14)

% Display Legends
figure(2)
datetick2('x');
legend('IC Waveform','Target Waveform','CS Values' );
hold off;
figure(3)
h = legend('IC Polar Plot','IC Current Point','Target Waveform' );
P1 = get(h,'Position');
P2 = [0.4 0.45 P1(3:4)];
set(h,'Position',P2);
hold off;

