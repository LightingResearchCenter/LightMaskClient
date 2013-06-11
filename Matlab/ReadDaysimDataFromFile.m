function [ Time, inc ] = ReadDaysimDataFromFile( dateStr, timeStr, CS )
%Read Daysimeter data from file
Time = zeros(size(CS));
s30 = 1/86400*30; %30 seconds
for i1 = 1:2  % Convert date and time strings into Matlab time format (days since Jan 1, 0000)
    Time(i1) = datenum([dateStr{i1} ' ' timeStr{i1}]);
end
inc = round((Time(2) - Time(1))/s30)*s30;
for i1 = 3:length(CS)
    Time(i1) = Time(i1-1) + inc;
end
inc = inc *24;

end

