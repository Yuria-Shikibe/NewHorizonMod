#define HIGHP

//shades of cryofluid
const vec3 S1 = vec3(145.0, 96.0 , 237.0) / 255.0;
const vec3 S2 = vec3(176.0, 130.0, 245.0) / 255.0;
const vec3 S3 = vec3(206.0, 160.0, 255.0) / 255.0;


const float p1 = S1.x * S1.z;
const float p2 = S2.x * S2.z;
const float p3 = S3.x * S3.z * 1.15;

#define NSCALE 100.0 / 9.75

uniform sampler2D u_texture;
uniform sampler2D u_noise;

uniform vec2 u_campos;
uniform vec2 u_resolution;

uniform vec2 u_direction;

uniform float u_time;

varying vec2 v_texCoords;

const float mscl = 40.0;
const float mth = 7.0;

void main(){
    vec2 c = v_texCoords.xy;
    vec2 coords = vec2(c.x * u_resolution.x + u_campos.x, c.y * u_resolution.y + u_campos.y);

    float btime = u_time / 600.0;
    float noise = (texture2D(u_noise, (coords) / NSCALE * u_direction * length(u_direction) + vec2(btime) * u_direction).r + texture2D(u_noise, (coords) / NSCALE + vec2(btime * 1.1) * vec2(-0.8, -1.0) * u_direction).r) / 2.0;
    vec4 color = texture2D(u_texture, c);


    color.rgb *= smoothstep(0.3, 0.8, noise);
    color.a = texture2D(u_noise, (coords) / NSCALE + vec2(btime) * u_direction).r;

    gl_FragColor = color;
}
