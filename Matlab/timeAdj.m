function AltTime = timeAdj(Time)
%timeAdj takes a time (value in hours), checks to see if it is less than 12.
%If it is timeAdj adds 24 to it. This is used to account for the 23 to 0
%roll over. 
if (Time < 12)
    AltTime = Time + 24;
else
    AltTime = Time;
end
end

