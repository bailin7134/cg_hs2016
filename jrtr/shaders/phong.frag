#version 150
// GLSL version 1.50
// Fragment shader for diffuse shading in combination with a texture map

#define MAX_LIGHTS 8

// Uniform variables passed in from host program
uniform sampler2D myTexture;
uniform vec4 lightDirection[MAX_LIGHTS];
uniform vec4 lightPosition[MAX_LIGHTS];
uniform vec3 c_diffuse[MAX_LIGHTS];
uniform vec3 c_specular[MAX_LIGHTS];
uniform vec3 c_ambient[MAX_LIGHTS];
uniform int lightType[MAX_LIGHTS];
uniform int nLights;
uniform vec3 k_ambient;
uniform vec3 k_diffuse;
uniform vec3 k_specular;
uniform vec4 camera;
uniform float phong_exponent;

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
	
	for(int i=0; i<nLights; i++){
		
		vec3 lightColor = vec3(0,0,0);
		vec3 c_s = vec3(0,0,0);
		vec4 L = vec4(0,0,0,0);
	
		L = lightPosition[i]-frag_position;
		r = length(L);
		lightColor = c_diffuse[i]/(r*r);	
		c_s = c_specular[i]/(r*r);
		
		vec4 R = reflect(L, frag_normal);
		vec4 e = camera - frag_position;
		color += lightColor*k_diffuse*(dot(L,frag_normal)) + c_s*k_specular*(pow(max(dot(R,e),0), phong_exponent)) + c_ambient[i]*k_ambient;
	}
 
	// The built-in GLSL function "texture" performs the texture lookup
	frag_shaded = vec4(color,1) * texture(myTexture, frag_texcoord);
}
