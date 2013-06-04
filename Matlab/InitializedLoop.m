function [ t1, t2, dX, dXC ] = InitializedLoop( initialStartTime, increment, CBTminInitial )
% Initialize dLoop start and end times for each iteration

t1 = initialStartTime; % hours
t2 = t1 + increment; % simulation interval end time, hours

X0 = -cos(2*pi*(t1/24-CBTminInitial/24)); %Initial value X of the sinusoid that is trying to mimic the Target
XC0 = sin(2*pi*(t1/24-CBTminInitial/24)); %Initial value Xc of the sinusoid that is trying to mimic the Target

dX = X0; %daysimeter X starts at initial X given
dXC = XC0; %daysimeter XC starts at initial XC given

end

