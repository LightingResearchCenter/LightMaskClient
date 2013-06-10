function [ Time ] = ReadDaysimDataFromFile( dateStr, timeStr, CS )
%Read Daysimeter data from file
Time = zeros(size(CS));
for i1 = 1:2  % Convert date and time strings into Matlab time format (days since Jan 1, 0000)
    Time(i1) = datenum([dateStr{i1} ' ' timeStr{i1}]);
end
inc = Time(2) - Time(1);
for i1 = 3:length(CS)
    Time(i1) = Time(i1-1) + inc;
end

end

