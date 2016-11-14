#version 150
// GLSL version 1.50
// Fragment shader for diffuse shading in combination with a texture map

#define MAX_LIGHTS 8

// Uniform variables passed in from host program
uniform sampler2D myTexture;
uniform vec4 lightDirection[MAX_LIGHTS];
uniform vec4 lightPosition[MAX_LIGHTS];
uniform vec3 c_diffuse[MAX_LIGHTS];
uniform int lightType[MAX_LIGHTS];
uniform int nLights;
uniform vec3 k_diffuse;
uniform sampler2D myNormalMap;	// for bump
uniform mat4 modelview;	// for bump

// Variables passed in from the vertex shader
in vec2 frag_texcoord;
in vec4 frag_position;
in vec4 frag_normal;

// Output variable, will be written to framebuffer automatically
out vec4 frag_shaded;

void main()
{		

	float r = 0;
	vec3 color = vec3(0,0,0);
	vec3 lightColor;
	
	for(int i=0; i<nLights; i++){
		
		vec4 L = vec4(0,0,0,0);
		vec4 n = frag_normal;
		
		// point light source:
		L = lightPosition[i]-frag_position;
		r = length(L);	// distance to light source
		lightColor = c_diffuse[i]/(r*r);
		
		//The Cg Tutorial
		color += lightColor*k_diffuse*(max(dot(L,n),0));
	}
 
	// The built-in GLSL function "texture" performs the texture lookup
	frag_shaded = vec4(color,1) * texture(myTexture, frag_texcoord);
}
