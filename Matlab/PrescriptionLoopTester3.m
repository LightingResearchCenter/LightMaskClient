function [ onTimes, offTimes ] = PrescriptionLoopTester3( numOfDaysLEAP, increment, pX, pXC, maskLightLevel, tau, t1, t2, nsteps, offLightLevel, CBTminTarget, AvailStartTime, AvailEndTime, onTimes, offTimes, onCount, offCount, MaxLightDuration, absTimeOffset )
%%%%%%%%%%%%%%%%%%%%%%%%%% PRESCRIPTION LOOP %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%%%%%%%%%%%%% Loop determines when to give or remove light%%%%%%%%%%%%%%%%%

numIterations = numOfDaysLEAP*24/increment;
CS = zeros(numIterations,1);
currLight = 0; %Used for keeping track of how much light the subject has received each night.

%%%%%%%%%%%%%%%%%%ForGraphing%%%%%%%%%%%%%%%%%%%
% CBTs = zeros(numIterations,1);
% timeLight = zeros(numIterations,1);
% lightTime = zeros(numIterations,1);
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

%xTotal= zeros(numIterations,1); % Only for plotting
%xcTotal = zeros(numIterations,1); % Only for plotting
%xTargetTotal = zeros(numIterations,1); % Only for plotting
%xcTargetTotal = zeros(numIterations,1); % Only for plotting
%pLoopTimeTotal = zeros(numIterations,1); % Only for plotting

for i1 = 1:numIterations  
    [pX1 pXC1,tend,xarray,xcarray,t] = rk4stepperP(pX,pXC,maskLightLevel,tau,t1,t2,nsteps);
    [pX2 pXC2,tend,xarray,xcarray,t] = rk4stepperP(pX,pXC,offLightLevel,tau,t1,t2,nsteps);
    %L and P process functions
    %[pX1 pXC1] = LP(t1, t2, pX, pXC, n0, maskLightLevel, G, alpha0, beta, tau); %L/P Process Function for CS = 0.4
    %[pX2 pXC2] = LP(t1, t2, pX, pXC, n0, offLightLevel, G, alpha0, beta, tau); %L/P Process Function for CS = 0
    
    %Target Sinusoid
    xTarget = -cos(2*pi*(t1/24 - CBTminTarget/24)); %Real part of Target sinusoid
    xcTarget = sin(2*pi*(t1/24 - CBTminTarget/24)); %complex part of Target sinusoid
    %xTargetTotal(i1) = xTarget; % Only for plotting
    %xcTargetTotal(i1) = xcTarget; % Only for plotting
    %pLoopTimeTotal(i1) = t1; %hours % Only for plotting
    
    %Absolute Loop Time (real time)
    %AbsLoopTimeTotal = pLoopTimeTotal/24 + floor(Time(1)); % Only for plotting
  
  % CALCULATIONS TO SEE IF ADDING LIGHT BRINGS THE CYCLE CLOSER TO THE CBTmin
    withLight = ((pX1 - xTarget)^2) + ((pXC1 - xcTarget)^2);
    withoutLight = ((pX2 - xTarget)^2) + ((pXC2 - xcTarget)^2);
    %If adding Light Brings the Cycle Closer to the CBTmin, then Add Light. Else Add no Light
    %Update all Arrays and Initial Conditions
    
    ToD = mod(t1,24); %Time of Day
   
    CBTmin = XXC2CBTmin((t1/24 + absTimeOffset), pX, pXC);
    ActEndTime = mod(CBTmin - 1, 24); %The treatment will end 1 hour prior to CBTmin
    
    if  (timeAdj(mod(AvailStartTime,24)) < timeAdj(mod(ActEndTime - MaxLightDuration, 24)))
        ActStartTime = mod(ActEndTime - MaxLightDuration, 24); %If ActEndTime - MaxLightDuration is later than the AvailStartTime + increment, start at ActEndTime - MaxLightDuration.
    else
        ActStartTime = mod(AvailStartTime, 24); %Else start at AvailStartTime + increment.
    end
    
    if (timeAdj(ActStartTime) > timeAdj(ActEndTime)) %Now ActEndTime is no more than MaxLightDuration ahead of ActStartTime. 
        Available = 0; %This will only occour if the ActStartTime is too close to the CBTmin
        %Available = ((ToD >= ActStartTime || ToD < ActEndTime));
    else 
        Available = (timeAdj(ToD) >= timeAdj(ActStartTime) && timeAdj(ToD) < timeAdj(ActEndTime) && (currLight < MaxLightDuration)); %Available if ActStartTime <= ToD < ActEndTime and the max dosage for the night has not been reached.
    end
 
%%%%%%%%%%%%%%%%%%%%%%ForTesting%%%%%%%%%%%%%%%%%%%%%%%    
%     if (Available == 1) %For Testing purposes
%         currTime = datestr(t1/24 + absTimeOffset)
%         ToD = ToD
%         currLight = currLight
%         CBTmin = CBTmin
%         ActStartTime = ActStartTime
%         ActEndTime = ActEndTime
%     end
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
    
    if ((withLight < withoutLight) && (Available == 1))
        CS(i1) = maskLightLevel;
        %xTotal(i1) = pX1; % Only for plotting
        %xcTotal(i1) = pXC1; % Only for plotting
        pX = pX1;
        pXC = pXC1;
        currLight = currLight + increment;
    else
        CS(i1) = offLightLevel;
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

%%%%%%%%%%%%%%%%%%%ForTesting%%%%%%%%%%%%%%%%%%%%%
%     if (Available == 1) %For testing
%         currCS = CS(i1)
%     end
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

%%%%%%%%%%%%%ForGraphing%%%%%%%%%%%%%%
%     CBTs(i1) = CBTmin;
%     timeLight(i1) = CS(i1);
%     lightTime(i1) = ToD;
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%    
    
    if (timeAdj(ToD) > timeAdj(CBTmin)) %Clears the amount of light received after CBTmin for the night has passed.
        currLight = 0;
    end
    
    % Increment Start and End Times for Each Iteration of the Loop
    t1 = t1+ increment;
    t2 = t2+ increment;
    
end % end P loop

%%%%%%%%%%%%%%ForGraphing%%%%%%%%%%%%%%
% x = 1:1:numIterations;
% y = CBTs(x);
% plot(x,y);
% hold all
% z = timeLight(x);
% plot(x,z);
% hold all
% w = lightTime(x);
% plot(x,w);
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

end

