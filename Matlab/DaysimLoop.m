function [ t1, t2, tend, xarray, xcarray, dX, dXC ] = DaysimLoop( CSavg, dX, dXC, tau, t1, t2, nsteps, increment )
%%%%%%%%%%%%%%%%%%%%%%%%%%% Daysimeter Loop %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%%%%% loop calculates X and XC Values Based on Daysimeter CSavg values %%%%
%dXplot = dX; % Only for plotting
%dXCplot = dXC; % Only for plotting
%timePlot = t1; % Only for plotting
%Xclock = -cos(2*pi*(t1/24-CBTminTarget/24)); % Only for plotting

for i1 = 1:length(CSavg)-1
  CSDrive = (CSavg(i1)+CSavg(i1+1))/2;
  
  [dX1 dXC1,tend,xarray,xcarray,t] = rk4stepperP(dX,dXC,CSDrive,tau,t1,t2,nsteps);
  
  %update values with output from rk4stepperP function
  dX = dX1;
  dXC = dXC1;
  t1 = t2; %increment startTime to where endTime is
  t2 = (t1 + increment); %increment endTime by increment
  
%   dXplot(i1+1) = dX; % Only for plotting
%   dXCplot(i1+1) = dXC; % Only for plotting
%   timePlot(i1+1) = t1; % Only for plotting
%   Xclock(i1+1) = -cos(2*pi*(t1/24-CBTminTarget/24)); % Only for plotting

end 

%Plot Of x and xc values based on daysimeter CSavg values
% timePlot = timePlot/24 + floor(Time(1)); % absolute time
% figure(2)
% plot(timePlot,dXplot,'g.-')
% hold on
% plot(timePlot,Xclock,'b.-')
% plot(timeCSavg,CSavg,'g-')
% hold off

end

