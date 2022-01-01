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
uniform float u_time;

varying vec2 v_texCoords;

void main(){
    vec2 c = v_texCoords.xy;
    vec2 coords = vec2(c.x * u_resolution.x + u_campos.x, c.y * u_resolution.y + u_campos.y);

    float btime = u_time / 4000.0;
    float wave = abs(sin(coords.x / 5.0 + coords.y / 5.0) + 0.2 * sin(0.5 * coords.x) + 0.2 * sin(coords.y * 0.8)) / 20.0;
    float noise = wave + (texture2D(u_noise, (coords) / NSCALE + vec2(btime) * vec2(-0.3, 0.7)).r + texture2D(u_noise, (coords) / NSCALE + vec2(btime * 1.6) * vec2(0.6, -1.3)).r) / 1.7;
    vec4 color = texture2D(u_texture, c);

//    color.rgb *= noise + 0.8f;

    float lerp = smoothstep(- wave + sin(1.15 * (coords.x + coords.y) + btime * 100.0) / 4.0, wave + 1.0 - cos(1.15 * (coords.x - coords.y) + btime * 200.0) / 4.0, noise);


//    if(noise > 0.54 && noise < 0.57){
//        color.rgb = S2;
//    }else if (noise > 0.49 && noise < 0.62){
//        color.rgb = S1;
//    }

//    color.rgb *= lerp + 0.4f;

    color.r *= lerp + 0.375;
    color.g *= lerp + 0.4 + wave / 3.0 * sin(btime * 2.5);
    color.b *= lerp + 0.45 + wave * sin(btime * 3.5);

//    if(color.r >= S3.x)color.r = S3.x;
//    else if(color.r >= S2.x)color.r = S2.x;
//    else color.r = S1.x;
//
//    if(color.g >= S3.y)color.g = S3.y;
//    else if(color.g >= S2.y)color.g = S2.y;
//    else color.g = S1.y;
//
//    if(color.b >= S3.z)color.b = S3.z;
//    else if(color.b >= S2.z)color.b = S2.z;
//    else color.b = S1.z;

//    float p = color.r * color.b;
//
//    if(p > p3)color.rgb = S3;
//    else if(p >= p2)color.rgb = S2;
//    else color.rgb = S1;

    gl_FragColor = color;
}
