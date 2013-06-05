function [ onTimes, offTimes ] = PrescriptionLoop( numOfDaysLEAP, increment, pX, pXC, maskLightLevel, tau, t1, t2, nsteps, offLightLevel, CBTminTarget, AvailStartTime, AvailEndTime, onTimes, offTimes, onCount, offCount )
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

end

