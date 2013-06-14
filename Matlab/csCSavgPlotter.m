function [ ] = csCSavgPlotter( Time, CS, timeCSavg, CSavg )
%Plot of CS and CSavg

figure(1)
plot(Time,CS,'r-')
hold on
plot(timeCSavg,CSavg,'b-')
hold off
datetick2('x')


end

