function PrintOnOffArrays( onTimes, offTimes, finalX, finalXC, endTime )
% Print On and Off time Arrays

if length(onTimes)>=3
    datestr(onTimes(1:3), 'dd-mmm-yyyy HH:MM:SS')
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

end

