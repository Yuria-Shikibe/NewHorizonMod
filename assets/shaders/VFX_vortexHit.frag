#define HIGHP

uniform sampler2D u_texture;
uniform sampler2D u_noise;
uniform vec4 u_color;
uniform vec2 u_center;
uniform vec2 u_resolution;
uniform float u_time;
uniform float u_radius;
uniform float u_rotation;
uniform float u_intensity;
uniform vec4 u_uv;
uniform int u_eventCount;
uniform float u_events[120]; // x, y, age, seed, strength

varying vec2 v_texCoords;

float hash12(vec2 p){
    vec3 p3 = fract(vec3(p.xyx) * 0.1031);
    p3 += dot(p3, p3.yzx + 33.33);
    return fract((p3.x + p3.y) * p3.z);
}

void main(){
    float localX = (v_texCoords.x - u_uv.x) / max(u_uv.z - u_uv.x, 0.0001) - 0.5;
    float localY = (v_texCoords.y - u_uv.y) / max(u_uv.w - u_uv.y, 0.0001) - 0.5;
    vec2 local = vec2(localX, localY);
    vec2 worldPos = u_center + local * u_radius * 2.0;
    float dist = length(local) * u_radius;
    if(dist > u_radius) discard;

    float ang = atan(local.y, local.x) + u_rotation;
    vec2 spiralUv = vec2(ang / 6.2831853 + u_time * 0.018, dist / max(u_radius, 0.001) * 2.6 - u_time * 0.07);

    vec4 noiseTex = texture2D(u_noise, spiralUv * 2.15 + fract(vec2(42.0, 56.0) * u_time * 0.01));
    vec2 noiseOffset = (noiseTex.xy - 0.5) * 0.045;

    vec2 eventDelta = vec2(0.0);
    float hitBoost = 0.0;
    for(int i = 0; i < 24; i++){
        if(i >= u_eventCount) break;
        vec2 eventPos = vec2(u_events[i * 5], u_events[i * 5 + 1]);
        float eventAge = u_events[i * 5 + 2];
        vec2 d = worldPos - eventPos;
        float len = length(d);
        float waveRadius = eventAge * u_radius * 0.82;
        float ring = exp(-pow((len - waveRadius) / max(waveRadius * 0.16 + 0.8, 0.001), 2.0));
        float fade = clamp(1.0 - eventAge, 0.0, 1.0);
        hitBoost += ring * fade * fade;
        eventDelta += normalize(d + vec2(0.0001)) * ring * fade * 0.055;
    }

    vec2 sampleUv = spiralUv + eventDelta + noiseOffset;
    float flow = texture2D(u_noise, sampleUv).r;
    float detail = hash12(floor(spiralUv * 180.0));
    float dendrite = smoothstep(0.34, 0.78, flow * 0.76 + detail * 0.24);
    float filament = pow(clamp(1.0 - abs(flow - detail) * 2.1, 0.0, 1.0), 3.0);

    vec3 deepBlue = vec3(0.055, 0.12, 0.72);
    vec3 cyan = vec3(0.04, 0.88, 1.0);
    vec3 violet = vec3(0.43, 0.08, 0.95);
    vec3 magenta = vec3(1.0, 0.06, 0.68);
    vec3 rose = vec3(1.0, 0.24, 0.38);
    vec3 gold = vec3(1.0, 0.64, 0.10);

    vec3 quantumColor = mix(deepBlue, cyan, smoothstep(0.0, 0.22, flow));
    quantumColor = mix(quantumColor, violet, smoothstep(0.18, 0.40, flow));
    quantumColor = mix(quantumColor, magenta, smoothstep(0.36, 0.60, flow));
    quantumColor = mix(quantumColor, rose, smoothstep(0.55, 0.75, flow));
    quantumColor = mix(quantumColor, gold, smoothstep(0.72, 0.92, flow));

    vec3 teamTint = u_color.rgb;
    vec3 color = mix(teamTint, quantumColor, clamp(0.42 + hitBoost * 0.38, 0.0, 0.86));
    color += vec3(0.78, 0.92, 1.0) * pow(dendrite * filament, 2.2) * (1.25 + hitBoost * 2.4);

    float edge = smoothstep(u_radius, u_radius * 0.80, dist);
    float baseAlpha = u_intensity * edge * (0.30 + dendrite * 0.50 + hitBoost * 0.65);
    gl_FragColor = vec4(color * (1.0 + dendrite * 0.55 + hitBoost * 1.35), clamp(baseAlpha, 0.0, 0.95));
}
