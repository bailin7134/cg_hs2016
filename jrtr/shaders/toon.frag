#version 150
// GLSL version 1.50
// Fragment shader for diffuse shading in combination with a texture map

#define MAX_LIGHTS 8

// Uniform variables passed in from host program
uniform vec4 lightDirection[MAX_LIGHTS];
uniform vec4 lightPosition[MAX_LIGHTS];
uniform int lightType[MAX_LIGHTS];
uniform int nLights;
uniform vec4 camera;

// Variables passed in from the vertex shade
in vec4 frag_position;
in vec4 frag_normal;

// Output variable, will be written to framebuffer automatically
out vec4 frag_shaded;

//varying float intensity;


void main()
{
	float intensity = 0;
	for(int i=0; i<nLights; i++){
		vec4 L = vec4(0,0,0,0);
		// directional light source (default light source):
		if(lightType[i]==0){
			L = -lightDirection[i];
		}
		// point light source:
		if(lightType[i]==1){
			L = lightPosition[i]-frag_position;
		}
		vec3 normalVector = normalize(vec3(frag_normal));
		vec3 lightVector = normalize(vec3(L));
    	vec3 viewVector = normalize(vec3(camera - frag_position));
    	float diffuse = clamp(dot(lightVector, normalVector), 0, 1);
    	vec3 reflectedVector = reflect(-lightVector, normalVector);
   		float specular = 0;
   		if(diffuse > 0) {
    		specular = pow(dot(viewVector, reflectedVector), 32);
   		}
   		intensity = intensity + diffuse + specular;
	}
 
    float ambient = 0.025f;
 	intensity = clamp(intensity+ambient, 0, 1);
 	
	vec4 color; 
	if (intensity > 0.95) color = vec4(1.0,0.5,0.5,1.0); 
	else if (intensity > 0.5) color = vec4(0.6,0.3,0.3,1.0); 
	else if (intensity > 0.25) color = vec4(0.4,0.2,0.2,1.0); 
	else color = vec4(0.2,0.1,0.1,1.0); 

	frag_shaded= color;
}
