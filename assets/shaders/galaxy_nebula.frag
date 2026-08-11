#define HIGHP

uniform vec3 u_camera;
uniform float u_time;
uniform float u_seed;
uniform float u_alpha;
uniform float u_palette;

varying vec3 v_local;
varying vec3 v_world;
varying vec3 v_normal;

float hash31(vec3 p){
    p = fract(p * 0.1031);
    p += dot(p, p.yzx + 33.33);
    return fract((p.x + p.y) * p.z);
}

float noise3(vec3 p){
    vec3 cell = floor(p);
    vec3 f = fract(p);
    f = f * f * (3.0 - 2.0 * f);

    float n000 = hash31(cell + vec3(0.0, 0.0, 0.0));
    float n100 = hash31(cell + vec3(1.0, 0.0, 0.0));
    float n010 = hash31(cell + vec3(0.0, 1.0, 0.0));
    float n110 = hash31(cell + vec3(1.0, 1.0, 0.0));
    float n001 = hash31(cell + vec3(0.0, 0.0, 1.0));
    float n101 = hash31(cell + vec3(1.0, 0.0, 1.0));
    float n011 = hash31(cell + vec3(0.0, 1.0, 1.0));
    float n111 = hash31(cell + vec3(1.0, 1.0, 1.0));

    float nx00 = mix(n000, n100, f.x);
    float nx10 = mix(n010, n110, f.x);
    float nx01 = mix(n001, n101, f.x);
    float nx11 = mix(n011, n111, f.x);
    return mix(mix(nx00, nx10, f.y), mix(nx01, nx11, f.y), f.z);
}

float fbm(vec3 p){
    float value = 0.0;
    float amplitude = 0.58;
    for(int i = 0; i < 3; i++){
        value += noise3(p) * amplitude;
        p = p * 2.03 + vec3(7.1, 3.7, 5.3);
        amplitude *= 0.50;
    }
    return value;
}

vec3 nebulaPalette(float t){
    t = fract(t);
    vec3 deepBlue = vec3(0.055, 0.12, 0.72);
    vec3 cyan = vec3(0.04, 0.88, 1.0);
    vec3 violet = vec3(0.43, 0.08, 0.95);
    vec3 magenta = vec3(1.0, 0.06, 0.68);
    vec3 rose = vec3(1.0, 0.24, 0.38);
    vec3 gold = vec3(1.0, 0.64, 0.10);

    vec3 color = mix(deepBlue, cyan, smoothstep(0.00, 0.20, t));
    color = mix(color, violet, smoothstep(0.18, 0.37, t));
    color = mix(color, magenta, smoothstep(0.35, 0.56, t));
    color = mix(color, rose, smoothstep(0.54, 0.72, t));
    color = mix(color, gold, smoothstep(0.70, 0.91, t));
    color = mix(color, deepBlue, smoothstep(0.89, 1.00, t));
    return color;
}

void main(){
    float phase = u_seed * 0.071;
    vec3 flow = vec3(u_time * 0.020, -u_time * 0.013, u_time * 0.009);
    vec3 samplePos = v_local * 2.15 + flow + vec3(phase, phase * 1.7, -phase * 0.8);

    float broad = fbm(samplePos);
    float detail = noise3(samplePos * 3.35 - flow * 1.7 + 9.4);
    float dust = fbm(samplePos * 0.61 - flow * 0.35 + vec3(17.0, 3.0, 11.0));
    float density = smoothstep(0.39, 0.76, broad * 0.74 + detail * 0.26);
    float filament = pow(clamp(1.0 - abs(broad - detail) * 2.2, 0.0, 1.0), 3.0);
    float dustLane = smoothstep(0.55, 0.79, dust) * smoothstep(0.34, 0.72, 1.0 - detail);

    vec3 viewDir = normalize(u_camera - v_world);
    float softEdge = pow(clamp(abs(dot(viewDir, normalize(v_normal))), 0.0, 1.0), 0.62);
    float alpha = density * mix(0.42, 1.0, filament) * softEdge * u_alpha * (1.0 - dustLane * 0.72);
    if(alpha < 0.003) discard;

    float colorFlow = fract(u_palette + broad * 0.36 + detail * 0.17 + sin(phase + dust * 4.0) * 0.055);
    vec3 color = nebulaPalette(colorFlow);
    vec3 secondary = nebulaPalette(colorFlow + 0.19 + dust * 0.08);
    color = mix(color, secondary, smoothstep(0.36, 0.78, detail) * 0.46);

    vec3 stellarWhite = vec3(0.78, 0.92, 1.0);
    vec3 warmWhite = vec3(1.0, 0.91, 0.64);
    vec3 coreLight = mix(stellarWhite, warmWhite, smoothstep(0.38, 0.72, colorFlow));
    float emission = pow(clamp(density * filament, 0.0, 1.0), 2.2);
    color = mix(color, coreLight, emission * 0.62);
    color *= 0.95 + density * 1.35 + emission * 1.45;

    gl_FragColor = vec4(color, alpha);
}
