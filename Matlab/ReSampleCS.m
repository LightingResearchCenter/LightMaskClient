function [ CSavg, timeCSavg ] = ReSampleCS( initialStartTime, increment, csTimeRelHours, sRate, CS, Time )
% Re-sample CS: average value of CS during increment centred on increments

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

end

