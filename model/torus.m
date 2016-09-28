innerReso=4;
outerReso=6;
innerRad=2;
outerRad=4;

p = 2*pi/innerReso;
t = 2*pi/outerReso;


for i=0:1:outerReso-1
	for j=0:1:innerReso-1
		v((i*outerReso+j)*3+1)   = (outerRad+innerRad*(float)Math.cos(j*p))*(float)Math.cos(t*outerReso);
		v((i*outerReso+j)*3+2) = (outerRad+innerRad*(float)Math.cos(j*p))*(float)Math.sin(t*outerReso);
		v((i*outerReso+j)*3+3) = innerRad*(float)Math.sin(j*p);
	end
end

for i=1:1:outerReso*innerReso
	x(i)
