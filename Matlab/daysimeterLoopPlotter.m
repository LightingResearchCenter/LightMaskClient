function [  ] = daysimeterLoopPlotter( timePlot, Time, dXplot, Xclock, timeCSavg, CSavg )
%Plot Of x and xc values based on daysimeter CSavg values

timePlot = timePlot/24 + floor(Time(1)); % absolute time
figure(2)
plot(timePlot,dXplot,'g.-')
hold on
plot(timePlot,Xclock,'b.-')
plot(timeCSavg,CSavg,'g-')
hold off


end

