//#define HIGHP
//
//varying vec2 v_texCoords;
//
//uniform sampler2D u_texture;
//uniform sampler2D u_grayMap;
//uniform vec2 u_campos;
//uniform vec2 u_resolution;
//uniform float u_distortX;
//uniform float u_distortY;
//uniform float u_time;
//
//void main() {
//    vec2 worldCoords = (v_texCoords * u_resolution) - (u_resolution / 2.0) + u_campos;
//    vec2 timeOffset = vec2(sin(u_time / 3), cos(u_time / 3)) * 0.1;
//    vec2 grayTexCoords = clamp(v_texCoords + timeOffset, 0.0, 1.0);
//    float distortionValue = texture(u_grayMap, grayTexCoords).r;
//    vec2 distortionOffset = (distortionValue - 0.5) * vec2(u_distortX, u_distortY);
//    vec2 distortedTexCoords = clamp(v_texCoords + distortionOffset, 0.0, 1.0);
//    vec4 color = texture(u_texture, distortedTexCoords);
//
//    gl_FragColor = color;
//}

#define HIGHP

#define u_center vec2(0.5, 0.5)

varying vec2 v_texCoords;

uniform sampler2D u_texture;
uniform sampler2D u_grayMap;
uniform vec2 u_campos;
uniform vec2 u_resolution;
uniform float u_time;

void main() {
    vec2 worldCoords = (v_texCoords * u_resolution) - (u_resolution / 2.0) + u_campos;
    vec2 normalizedCoords = v_texCoords * u_resolution;
    vec2 centerCoords = u_center * u_resolution;
    vec2 direction = normalizedCoords - centerCoords;
    float distance = length(direction);

    vec2 directionNormalized = normalize(direction);

    vec2 timeOffset = vec2(sin(u_time), cos(u_time)) * 0.1;
    vec2 grayTexCoords = clamp(v_texCoords + timeOffset, 0.0, 1.0);
    float distortionValue = texture(u_grayMap, grayTexCoords).r;

    vec2 radialDistortion = directionNormalized * (distortionValue - 0.5) * u_distort * distance / u_resolution.x;

    vec2 distortedTexCoords = v_texCoords + radialDistortion / u_resolution;

    distortedTexCoords = clamp(distortedTexCoords, 0.0, 1.0);

    vec4 color = texture(u_texture, distortedTexCoords);

    gl_FragColor = color;
}

