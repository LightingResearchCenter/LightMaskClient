function CBTmin = XXC2CBTmin(Time,X,XC)
% Calculate the state of the pacemaker in terms of the time of CBTmin
% Time = time in units of days (Matlab datenum format)
% X = state variable of pacemaker at time = Time
% XC = state variable of pacemaker at time = Time
% CBTmin = time of day of CBTmin in units of hours (0<= CBTmin < 24)

previousTimeCBTmin = Time - (pi - angle(X+XC*1i))*(1/(2*pi)); % 1/(2*pi) converts to units of days
%nextTimeCBTmin = Time + (pi + angle(X+XC*1i))*(1/(2*pi)); % 1/(2*pi) converts to units of days

%disp(['previouTimeCBTmin = ' datestr(previousTimeCBTmin)]);
%disp(['nextTimeCBTmin = ' datestr(nextTimeCBTmin)]);

% convert to relative time of day in hours (0 <= time < 24)
CBTmin = (previousTimeCBTmin - floor(previousTimeCBTmin))*24;

%CBTmin = mod(CBTmin + 12, 24)

end

