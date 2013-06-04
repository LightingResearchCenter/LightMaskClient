function [ initialStartTime, absTimeOffset, sRate, csTimeRelHours ] = RelTimeSet( Time, increment )
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

end

