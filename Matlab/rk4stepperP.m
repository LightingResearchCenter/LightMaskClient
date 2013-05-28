function [xend,xcend,tend,xarray,xcarray,t] = rk4stepperP(x0,xc0,Bdrive,tau,ti,tf,nsteps)

xarray = zeros(nsteps,1);
xcarray = zeros(nsteps,1);
t = zeros(nsteps,1);

Bdrive = 0.56*Bdrive;
deltat = (tf-ti)/nsteps;
for i1 = 1:nsteps
    [x0, xc0] = rk4P(x0,xc0,Bdrive,deltat,tau);
    xarray(i1) = x0;
    xcarray(i1) = xc0;
    t(i1) = i1*deltat+ti;
end
xend = x0;
xcend = xc0;
tend = t(nsteps);
    
    