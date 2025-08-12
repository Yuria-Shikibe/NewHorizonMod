uniform sampler2D u_texture;
uniform float u_time;
uniform vec2 u_texsize;

uniform float u_glitchIntensity; // 0.0 - 无效果, 1.0 - 完全效果
uniform float u_mix;            // 0.0 - 完全原图, 1.0 - 完全 glitch

varying vec2 v_texCoords;


float rand(vec2 p) {
    float t = floor(u_time / 20.0) / 50.0;
    return fract(sin(dot(p, vec2(t * 12.9898, t * 78.233))) * 43758.5453);
}

float noise(vec2 uv, float blockiness) {
    vec2 lv = fract(uv);
    vec2 id = floor(uv);
    float n1 = rand(id);
    float n2 = rand(id + vec2(1.0, 0.0));
    float n3 = rand(id + vec2(0.0, 1.0));
    float n4 = rand(id + vec2(1.0, 1.0));
    vec2 u = smoothstep(0.0, 1.0 + blockiness, lv);
    return mix(mix(n1, n2, u.x), mix(n3, n4, u.x), u.y);
}

float fbm(vec2 uv, int count, float blockiness, float complexity) {
    float val = 0.0;
    float amp = 0.5;

    float tStep = fract(sin(floor(u_time / 20) * 12.9898) * 43758.5453);

    int i = 0;
    while(i < count) {
        float tRand = rand(vec2(tStep + float(i), float(i) * 17.0));
        vec2 uvOffset = uv + tRand * 2.0 - 1.0;

        val += amp * noise(uvOffset, blockiness);

        amp *= 0.5;
        uv *= complexity;
        i++;
    }
    return val;
}


// --- main ---
void main() {
    vec2 uv = v_texCoords;
    vec2 uv2 = uv;

    vec4 baseColor = texture2D(u_texture, uv2);

    vec2 uv_base = uv * 5.0;

    float nR = smoothstep(0.4, 1.0, fbm(uv_base + vec2(0.0, 0.0) + fract(sin(floor(u_time * 0.36) * 12.9898)), 3, 3.0, 1.6));
    float nG = smoothstep(0.4, 1.0, fbm(uv_base + vec2(5.2, 1.3) + fract(sin(floor(u_time * 0.83) * 12.9898)), 3, 3.0, 1.4));
    float nB = smoothstep(0.4, 1.0, fbm(uv_base + vec2(-3.7, 2.7) + fract(sin(floor(u_time * 0.129) * 12.9898)), 3, 3.0, 1.8));

    vec2 offR = vec2(nR * 0.06, sin(u_time * 2.3 + nR * 6.28) * 0.004);
    vec2 offG = vec2(-nG * 0.05, cos(u_time * 1.7 + nG * 5.1) * 0.003);
    vec2 offB = vec2(nB * 0.04, sin(u_time * 2.9 + nB * 4.2) * 0.002);

    vec3 layerR = texture2D(u_texture, uv2 + offR).rgb * vec3(1.0, 0.0, 0.0);
    vec3 layerG = texture2D(u_texture, uv2 + offG).rgb * vec3(0.0, 1.0, 0.0);
    vec3 layerB = texture2D(u_texture, uv2 + offB).rgb * vec3(0.0, 0.0, 1.0);

    float maskR = nR;
    float maskG = nG;
    float maskB = nB;

    vec3 glitchCol = clamp(layerR * maskR + layerG * maskG + layerB * maskB, 0.0, 1.0);

    float overallMask = max(max(maskR, maskG), maskB);

    float effectiveMix = clamp(u_glitchIntensity * overallMask, 0.0, 1.0);

    vec3 mixed = mix(baseColor.rgb, glitchCol, effectiveMix);
    vec3 finalColor = mix(baseColor.rgb, mixed, clamp(u_mix, 0.0, 1.0));

    // 透明度 = glitch 强度，其他区域透明
    float outAlpha = effectiveMix;

    gl_FragColor = vec4(finalColor, outAlpha);
}
