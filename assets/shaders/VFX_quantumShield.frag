#define HIGHP

uniform sampler2D u_texture;
uniform sampler2D u_simulation;
uniform sampler2D u_noise;
uniform vec2 u_resolution;
uniform vec2 u_campos;
uniform vec2 u_texsize;
uniform vec2 u_texel;
uniform float u_time;
uniform vec4 u_fields[24];
uniform vec4 u_fieldColors[24];
uniform vec4 u_events[24];
uniform int u_fieldCount;
uniform int u_eventCount;

varying vec2 v_texCoords;

float quantumRandom(vec2 st){
    return fract(sin(dot(st, vec2(12.9898, 78.233))) * 142214.5453123);
}

float quantumNoise(vec2 st){
    vec2 ipos = floor(st);
    vec2 fpos = fract(st);

    float a = quantumRandom(ipos);
    float b = quantumRandom(ipos + vec2(1.0, 0.0));
    float c = quantumRandom(ipos + vec2(0.0, 1.0));
    float d = quantumRandom(ipos + vec2(1.0, 1.0));

    float x1 = mix(a, b, fpos.x);
    float x2 = mix(c, d, fpos.x);
    return mix(x1, x2, fpos.y);
}

vec3 quantumPalette(float t){
    t = fract(t);
    vec3 deepBlue = vec3(0.055, 0.12, 0.72);
    vec3 cyan = vec3(0.04, 0.88, 1.00);
    vec3 violet = vec3(0.43, 0.08, 0.95);
    vec3 magenta = vec3(1.00, 0.06, 0.68);
    vec3 rose = vec3(1.00, 0.24, 0.38);
    vec3 gold = vec3(1.00, 0.64, 0.10);

    vec3 color = mix(deepBlue, cyan, smoothstep(0.00, 0.22, t));
    color = mix(color, violet, smoothstep(0.18, 0.40, t));
    color = mix(color, magenta, smoothstep(0.36, 0.60, t));
    color = mix(color, rose, smoothstep(0.55, 0.75, t));
    color = mix(color, gold, smoothstep(0.72, 0.92, t));
    return color;
}

float maskAt(vec2 uv){
    return texture2D(u_texture, uv).a;
}

float outerEdge(vec2 uv){
    float nearest = 999.0;
    for(int x = -3; x <= 3; x++){
        for(int y = -3; y <= 3; y++){
            vec2 offset = vec2(float(x), float(y));
            if(maskAt(uv + offset * u_texel) > 0.45){
                nearest = min(nearest, length(offset));
            }
        }
    }
    return exp(-max(nearest - 0.45, 0.0) / 1.8);
}

float innerEdge(vec2 uv){
    float nearest = 999.0;
    for(int x = -3; x <= 3; x++){
        for(int y = -3; y <= 3; y++){
            vec2 offset = vec2(float(x), float(y));
            if(maskAt(uv + offset * u_texel) < 0.20){
                nearest = min(nearest, length(offset));
            }
        }
    }
    return exp(-max(nearest - 0.45, 0.0) / 2.4);
}

void main(){
    vec2 worldPos = (v_texCoords * u_resolution) + u_campos;
    float centerMask = maskAt(v_texCoords);
    vec4 simulation = texture2D(u_simulation, v_texCoords);
    float outsideEdge = outerEdge(v_texCoords);
    float insideEdge = innerEdge(v_texCoords);
    float edge = max(outsideEdge, insideEdge * centerMask);

    if(centerMask < 0.004 && edge < 0.004) discard;

    float fieldSeed = 0.0;
    for(int i = 0; i < 24; i++){
        if(i >= u_fieldCount) break;
        fieldSeed += u_fields[i].w + float(i) * 0.173;
    }

    vec2 patternCoords = worldPos / max(u_resolution.y, 1.0);
    patternCoords.x += 1.0;

    float lowNoise = quantumNoise(patternCoords * 5.0 + u_time);
    float mediumNoise = quantumNoise(patternCoords * 32.0);
    float highNoise = quantumNoise(patternCoords * 48.0);

    patternCoords.x += highNoise * 0.02;
    float pattern = sin(patternCoords.x * (64.0 + lowNoise * 8.0) + mediumNoise * 4.0) + 1.0;
    pattern += highNoise * 0.3;
    float dendrite = smoothstep(0.45, 0.46, pattern);
    float broad = simulation.r;
    float branchField = simulation.g;
    float flowField = simulation.b;

    float hitBoost = 0.0;
    vec2 warpDelta = vec2(0.0, 0.0);
    for(int i = 0; i < 24; i++){
        if(i >= u_eventCount) break;
        float eventX = u_events[i].x;
        float eventY = u_events[i].y;
        float age = u_events[i].z;
        float offsetX = worldPos.x - eventX;
        float offsetY = worldPos.y - eventY;
        float dist = sqrt(offsetX * offsetX + offsetY * offsetY);
        float waveRadius = age * 260.0;
        float width = max(waveRadius * 0.16 + 0.8, 0.001);
        float ring = exp(-pow((dist - waveRadius) / width, 2.0));
        float fade = clamp(1.0 - age, 0.0, 1.0);
        hitBoost += ring * fade * fade;
        float safeDist = max(dist, 0.001);
        warpDelta.x += offsetX / safeDist * ring * fade * 8.0;
        warpDelta.y += offsetY / safeDist * ring * fade * 8.0;
    }

    vec4 warpedSimulation = texture2D(u_simulation, fract(v_texCoords + warpDelta / u_texsize * 0.25));
    broad = mix(broad, max(broad, warpedSimulation.r), clamp(hitBoost, 0.0, 0.75));
    branchField = mix(branchField, max(branchField, warpedSimulation.g), clamp(hitBoost * 1.15, 0.0, 0.85));

    vec3 teamTint = u_fieldColors[0].rgb;
    vec3 quantumColor = quantumPalette(broad * 0.78 + branchField * 0.16 + flowField * 0.06 + abs(fieldSeed) * 0.037);
    vec3 fillColor = mix(teamTint, quantumColor, 0.48 + clamp(hitBoost * 0.35, 0.0, 0.38));
    fillColor += mix(teamTint, vec3(0.78, 0.92, 1.00), 0.55) * dendrite * (0.55 + hitBoost * 2.20);

    float fillAlpha = 0.18;
    vec3 outlineColor = mix(teamTint, vec3(0.82, 0.96, 1.00), 0.45);
    vec3 finalColor = mix(fillColor, outlineColor, clamp(edge * 1.15, 0.0, 1.0));
    float alpha = fillAlpha;
    if(centerMask < 0.90 && edge > 0.02) alpha = clamp(edge * 100.0, 0.0, 0.85);

    gl_FragColor = vec4(finalColor * (1.0 + dendrite * 0.35 + hitBoost * 1.15), alpha);
}
