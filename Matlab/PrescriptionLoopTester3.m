function [ onTimes, offTimes, xTotal, xcTotal, xTarget, xcTarget, xTargetTotal, xcTargetTotal, AbsLoopTimeTotal, CS ] = PrescriptionLoopTester3( numOfDaysLEAP, increment, pX, pXC, maskLightLevel, tau, t1, t2, nsteps, offLightLevel, CBTminTarget, AvailStartTime, AvailEndTime, onTimes, offTimes, onCount, offCount, MaxLightDuration, absTimeOffset, Time, plotOn )
%%%%%%%%%%%%%%%%%%%%%%%%%% PRESCRIPTION LOOP %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%%%%%%%%%%%%% Loop determines when to give or remove light%%%%%%%%%%%%%%%%%

numIterations = round(numOfDaysLEAP*24/increment);
CS = zeros(numIterations,1);
currLight = 0; %Used for keeping track of how much light the subject has received each night.

%%%%%%%%%%%%%%%%%%ForGraphing%%%%%%%%%%%%%%%%%%%
CBTmins = zeros(numIterations,1);
timeLight = zeros(numIterations,1);
lightTime = zeros(numIterations,1);
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

xTotal= zeros(numIterations,1); % Only for plotting
xcTotal = zeros(numIterations,1); % Only for plotting
xTargetTotal = zeros(numIterations,1); % Only for plotting
xcTargetTotal = zeros(numIterations,1); % Only for plotting
pLoopTimeTotal = zeros(numIterations,1); % Only for plotting

for i1 = 1:numIterations  
    
    [pX2 pXC2,tend,xarray,xcarray,t] = rk4stepperP(pX,pXC,offLightLevel,tau,t1,t2,nsteps);
    %L and P process functions
    %[pX1 pXC1] = LP(t1, t2, pX, pXC, n0, maskLightLevel, G, alpha0, beta, tau); %L/P Process Function for CS = 0.4
    %[pX2 pXC2] = LP(t1, t2, pX, pXC, n0, offLightLevel, G, alpha0, beta, tau); %L/P Process Function for CS = 0
    
    %Target Sinusoid
    xTarget = -cos(2*pi*(t1/24 - CBTminTarget/24)); %Real part of Target sinusoid
    xcTarget = sin(2*pi*(t1/24 - CBTminTarget/24)); %complex part of Target sinusoid
    
    xTargetTotal(i1) = xTarget; % Only for plotting
    xcTargetTotal(i1) = xcTarget; % Only for plotting
    pLoopTimeTotal(i1) = t1; %hours % Only for plotting
    
    %Absolute Loop Time (real time)
    AbsLoopTimeTotal = pLoopTimeTotal/24 + floor(Time(1)); % Only for plotting
    
    ToD = round(mod(t1,24)*10000)/10000; %Time of Day rounded to 4 decimal places back
   
    CBTmin = XXC2CBTmin((t1/24 + absTimeOffset), pX, pXC);
    ActEndTime = mod(CBTmin - 1, 24); %The treatment will end 1 hour prior to CBTmin
    
    if  (timeAdj(mod(AvailStartTime + 1,24)) < timeAdj(mod(ActEndTime - MaxLightDuration, 24)))
        ActStartTime = mod(ActEndTime - MaxLightDuration, 24); %If ActEndTime - MaxLightDuration is later than the AvailStartTime + 1 hour, start at ActEndTime - MaxLightDuration.
    else
        ActStartTime = mod(AvailStartTime + 1, 24); %Else start at AvailStartTime + 1 hour.
    end
    
    if (timeAdj(ActStartTime) > timeAdj(ActEndTime)) %Now ActEndTime is no more than MaxLightDuration ahead of ActStartTime. 
        Available = 0; %This will only occour if the ActStartTime is too close to the CBTmin
        %Available = ((ToD >= ActStartTime || ToD < ActEndTime));
    else 
        Available = (timeAdj(ToD) >= timeAdj(ActStartTime) && timeAdj(ToD) < timeAdj(ActEndTime) && (round(currLight*10000)/10000 < MaxLightDuration)); %Available if ActStartTime <= ToD < ActEndTime and the max dosage for the night has not been reached.
    end
    
    if (Available == 1)
        [pX1 pXC1,tend,xarray,xcarray,t] = rk4stepperP(pX,pXC,maskLightLevel,tau,t1,t2,nsteps);

        targetAngle = mod(atan2(xcTarget, xTarget)+pi, 2*pi); %Angle at target point
        withLightAngle = mod(atan2(pXC1, pX1)+pi, 2*pi); %Angle at predicted light point
        withoutLightAngle = mod(atan2(pXC2, pX2)+pi, 2*pi); %Angle at predicted without light point
        
        % CALCULATIONS TO SEE IF ADDING LIGHT BRINGS THE CYCLE CLOSER TO THE CBTmin
        withLight = pi - abs(abs(targetAngle - withLightAngle) - pi); %((pX1 - xTarget)^2) + ((pXC1 - xcTarget)^2);
        withoutLight = pi - abs(abs(targetAngle - withoutLightAngle) - pi); %((pX2 - xTarget)^2) + ((pXC2 - xcTarget)^2);
        %If adding Light Brings the Cycle Closer to the CBTmin, then Add Light. Else Add no Light
        %Update all Arrays and Initial Conditions
    
%%%%%%%%%%%%%%%%%%%%%%ForTesting%%%%%%%%%%%%%%%%%%%%%%%%    
%     if (Available == 1); %For Testing purposes
%         currTime = datestr(t1/24 + absTimeOffset)
%         format long
%         aToD = timeAdj(ToD)
%         aActStartTime = timeAdj(ActStartTime)
%         currLight = currLight
%         CBTmin = CBTmin
%         ActStartTime = ActStartTime
%         ActEndTime = ActEndTime
%     end
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

        if ((withLight < withoutLight) && (Available == 1))
            CS(i1) = maskLightLevel;
            
            xTotal(i1) = pX1; % Only for plotting
            xcTotal(i1) = pXC1; % Only for plotting
            
            pX = pX1;
            pXC = pXC1;
            currLight = currLight + increment;
        else
            CS(i1) = offLightLevel;
            
            xTotal(i1) = pX2; % Only for plotting
            xcTotal(i1) = pXC2; % Only for plotting
            
            pX = pX2;
            pXC = pXC2;
        end
    
    else
        CS(i1) = offLightLevel;
        
        xTotal(i1) = pX2; % Only for plotting
        xcTotal(i1) = pXC2; % Only for plotting
        
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

%%%%%%%%%%%%%%%%%%%ForTesting%%%%%%%%%%%%%%%%%%%%%
%     if (t1 >= 94 && t1 < 95) %For testing
%         currCS = CS(i1)
%     end
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

%%%%%%%%%%%%%ForGraphing%%%%%%%%%%%%%%
    CBTmins(i1) = CBTmin;
    timeLight(i1) = CS(i1);
    lightTime(i1) = ToD;
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%    
    
    if (timeAdj(ToD) > timeAdj(CBTmin)) %Clears the amount of light received after CBTmin for the night has passed.
        currLight = 0;
    end
    
    % Increment Start and End Times for Each Iteration of the Loop
    t1 = t1+ increment;
    t2 = t2+ increment;
    
end % end P loop

%CBTmin Plot
if plotOn == 1
    x = 1:1:numIterations;
    y = CBTmins(x);
    figure(4)
    plot(x,y,'r');
    hold on
    z = timeLight(x);
    plot(x,z,'g');
    w = lightTime(x);
    plot(x,w,'b');
    xlabel('Relative Time','FontSize',14)
    ylabel('CBTmin','FontSize',14)
    
    legend('CBTmin Time','CS Values','Time of Day');
    hold off
end

end

