function [xprime,xcprime] = xprime(x,xc,Bdrive,tau)

mu = 0.13;
q = 0; % 1/3;
k = 0; % 0.55;

xprime = pi/12*(mu*(x/3+4/3*x^3-256/105*x^7)+xc+Bdrive);
xcprime = pi/12*(q*Bdrive*xc-(24/(0.99729*tau))^2*x+k*Bdrive*x);
