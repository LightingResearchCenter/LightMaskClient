function [ Time ] = ReadDaysimDataFromFile( dateStr, timeStr, CS )
%Read Daysimeter data from file
Time = zeros(size(CS));
for i1 = 1:length(dateStr)  % Convert date and time strings into Matlab time format (days since Jan 1, 0000)
    Time(i1) = datenum([dateStr{i1} ' ' timeStr{i1}]);
end

end

