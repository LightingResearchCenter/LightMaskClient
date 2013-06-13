function [ ] = rk4PlotsTester( AbsLoopTimeTotal, xTotal, xTargetTotal, CS, xcTotal, finalpX, finalpXC, xTarget, xcTarget, xcTargetTotal )
% PLOTS

%%%%%%%%SINUSOID WAVEFORM PLOT%%%%%%%%%
%timeSim = tcTotal/24+Time(1);
figure(2)
hold on
plot(AbsLoopTimeTotal,xTotal,'r-','LineWidth',2); %Waveform with Initial Conditions
hold on;
plot(AbsLoopTimeTotal,xTargetTotal,'b-','LineWidth',2); %Target Waveform
plot(AbsLoopTimeTotal,CS,'g')
%datetick2('x')
hold off;
xlabel('Time','FontSize',14)
ylabel('Relative CBT deviation','FontSize',14)

%%%%%%%%%%%%%POLAR PLOT%%%%%%%%%%%%%%%%
figure(3)
plot(xTotal, xcTotal,'r-','LineWidth',2); axis equal; % Waveform with Initial Conditions
hold on;
plot(finalpX, finalpXC,'ro','LineWidth',2); axis equal;
plot(xTarget, xcTarget,'bs','LineWidth',2); axis equal;
plot(xTargetTotal, xcTargetTotal,'b-','LineWidth',2); axis equal; %Target Waveform
hold off;
xlabel('X','FontSize',14)
ylabel('Xc','FontSize',14)

% Display Legends
figure(2)
datetick2('x');
legend('IC Waveform','Target Waveform','CS Values' );
hold off;
figure(3)
h = legend('IC Polar Plot','IC Current Point','Target Waveform');
P1 = get(h,'Position');
P2 = [0.4 0.45 P1(3:4)];
set(h,'Position',P2);
hold off;

end

