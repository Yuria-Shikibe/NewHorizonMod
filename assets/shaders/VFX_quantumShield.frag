#define HIGHP

uniform sampler2D u_texture;
uniform sampler2D u_noise;
uniform vec2 u_resolution;
uniform vec2 u_campos;
uniform vec2 u_texel;
uniform float u_time;
uniform vec4 u_fields[24];
uniform vec4 u_fieldColors[24];
uniform vec4 u_events[24];
uniform int u_fieldCount;
uniform int u_eventCount;

varying vec2 v_texCoords;

float hash12(vec2 p){
    vec3 p3 = fract(vec3(p.xyx) * 0.1031);
    p3 += dot(p3, p3.yzx + 33.33);
    return fract((p3.x + p3.y) * p3.z);
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
    float outsideEdge = outerEdge(v_texCoords);
    float insideEdge = innerEdge(v_texCoords);
    float edge = max(outsideEdge, insideEdge * centerMask);

    if(centerMask < 0.004 && edge < 0.004) discard;

    float fieldSeed = 0.0;
    for(int i = 0; i < 24; i++){
        if(i >= u_fieldCount) break;
        fieldSeed += u_fields[i].w + float(i) * 0.173;
    }

    vec2 flowUv = worldPos / 180.0;
    flowUv += fract(vec2(42.0, 56.0) * u_time * 0.01);
    float broad = texture2D(u_noise, flowUv).r;
    float detail = hash12(floor(flowUv * 190.0));
    float dendrite = smoothstep(0.34, 0.78, broad * 0.76 + detail * 0.24);
    float filament = pow(clamp(1.0 - abs(broad - detail) * 2.1, 0.0, 1.0), 3.0);

    float hitBoost = 0.0;
    vec2 warpDelta = vec2(0.0);
    for(int i = 0; i < 24; i++){
        if(i >= u_eventCount) break;
        vec4 eventData = u_events[i];
        vec2 eventPos = eventData.xy;
        float age = eventData.z;
        vec2 delta = worldPos - eventPos;
        float dist = length(delta);
        float waveRadius = age * 260.0;
        float ring = exp(-pow((dist - waveRadius) / max(waveRadius * 0.16 + 0.8, 0.001), 2.0));
        float fade = clamp(1.0 - age, 0.0, 1.0);
        hitBoost += ring * fade * fade;
        warpDelta += normalize(delta + vec2(0.0001)) * ring * fade * 8.0;
    }

    vec2 warpedUv = flowUv + warpDelta / 180.0;
    float warpedFlow = texture2D(u_noise, warpedUv).r;
    broad = mix(broad, warpedFlow, clamp(hitBoost, 0.0, 0.75));

    vec3 teamTint = u_fieldColors[0].rgb;
    vec3 quantumColor = quantumPalette(broad + detail * 0.16 + abs(fieldSeed) * 0.037);
    vec3 fillColor = mix(teamTint, quantumColor, 0.48 + clamp(hitBoost * 0.35, 0.0, 0.38));
    fillColor += vec3(0.78, 0.92, 1.00) * pow(dendrite * filament, 2.2) * (0.55 + hitBoost * 2.20);

    float fillMask = smoothstep(0.04, 0.35, centerMask);
    float fillAlpha = fillMask * (0.10 + dendrite * 0.13 + hitBoost * 0.42);
    vec3 outlineColor = mix(teamTint, vec3(0.82, 0.96, 1.00), 0.45);
    vec3 finalColor = mix(fillColor, outlineColor, clamp(edge * 1.15, 0.0, 1.0));
    float alpha = max(fillAlpha, edge * 0.82);

    gl_FragColor = vec4(finalColor * (1.0 + dendrite * 0.35 + hitBoost * 1.15), alpha);
}
