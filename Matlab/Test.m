


pathFileName = '\\root\projects\NIH Light Mask\daysimeter_data\processed_files\sub03_130513-130520_sn93_processed.txt';

dayFile = fopen(pathFileName);
C = textscan(dayFile,'%s %*f %*f %f %*f','HeaderLines', 1, 'Delimiter', '\t');
Time = datenum(C{1}, 'dd/mm/yyyy HH:MM:SS');
CS = C{2};
fclose(dayFile);

