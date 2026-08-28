#define HIGHP

uniform sampler2D u_texture;
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

float complicatedNoise(float t, vec2 uv){
    float f1 = hash12(uv + floor(t) * 10.0) < 0.4 ? 1.0 : 0.0;
    float f2 = hash12(uv + floor(t + 1.0) * 10.0) < 0.4 ? 1.0 : 0.0;
    float f3 = hash12(uv + floor(t + 2.0) * 10.0) < 0.4 ? 1.0 : 0.0;
    return ((f1 + f2) * 0.5 + f3) * 0.5;
}

float easeQuartic(float t){
    if(t < 0.5){
        return 8.0 * t * t * t * t;
    }

    t -= 1.0;
    return 1.0 - 8.0 * t * t * t * t;
}

float getCellLevel(vec2 uv, float speed){
    float phase = u_time * speed + hash12(uv * 10.0);
    float previous = complicatedNoise(phase, uv);
    float next = complicatedNoise(phase + 1.0, uv);
    return mix(previous, next, easeQuartic(fract(phase)));
}

vec3 teamPalette(vec3 teamColor, float tone){
    tone = clamp(tone, 0.0, 1.0);
    return teamColor * mix(0.24, 1.0, tone);
}

float maskAt(vec2 uv){
    return texture2D(u_texture, uv).a;
}

void main(){
    vec2 worldPos = v_texCoords * u_resolution + u_campos;
    float centerMask = maskAt(v_texCoords);
    if(centerMask < 0.004) discard;

    vec3 teamTint = u_fieldColors[0].rgb;
    float nearestField = 999.0;
    float fieldGroup = -1.0;
    for(int i = 0; i < 24; i++){
        if(i >= u_fieldCount) break;
        float normalizedDistance = distance(worldPos, u_fields[i].xy) / max(u_fields[i].z, 0.001);
        if(normalizedDistance < nearestField){
            nearestField = normalizedDistance;
            teamTint = u_fieldColors[i].rgb;
            fieldGroup = u_fieldColors[i].a;
        }
    }

    // One absolute world-space equilateral-triangle layer.
    const float triangleSize = 24.0;
    const float triangleHeight = triangleSize * 0.8660254;
    float latticeV = worldPos.y / triangleHeight;
    float latticeU = worldPos.x / triangleSize - latticeV * 0.5;
    vec2 latticeCell = floor(vec2(latticeU, latticeV));
    vec2 localCell = fract(vec2(latticeU, latticeV));

    vec3 barycentric;
    vec2 triangleId;
    if(localCell.x + localCell.y >= 1.0){
        barycentric = vec3(1.0 - localCell.x, 1.0 - localCell.y, localCell.x + localCell.y - 1.0);
        triangleId = latticeCell * 2.0 + vec2(1.0);
    }else{
        barycentric = vec3(localCell.x, localCell.y, 1.0 - localCell.x - localCell.y);
        triangleId = latticeCell * 2.0;
    }

    float cellLevel = getCellLevel(triangleId, 0.65);
    float cellLight = 0.25 + cellLevel * 0.85;
    float cellBorder = 1.0 - smoothstep(0.025, 0.065, min(barycentric.x, min(barycentric.y, barycentric.z)));

    // Hit feedback is rendered only as a local expanding ring below.  The
    // shared field itself must not flash when any projector is hit.
    float hitBoost = 0.0;
    for(int i = 0; i < 24; i++){
        if(i >= u_eventCount) break;
        // A hit ring belongs to one shared field; never bleed into another group.
        if(fieldGroup < -0.5 || abs(u_events[i].w - fieldGroup) > 0.5) continue;
        float age = u_events[i].z;
        float radius = age * 260.0;
        float width = max(radius * 0.16 + 0.8, 0.001);
        float ring = exp(-pow((distance(worldPos, u_events[i].xy) - radius) / width, 2.0));
        float fade = clamp(1.0 - age, 0.0, 1.0);
        hitBoost += ring * fade * fade;
    }

    vec3 fillColor = teamPalette(teamTint, cellLevel) * (0.68 + cellLight * 0.32) * (1.0 + hitBoost * 1.10);
    fillColor += teamPalette(teamTint, 0.92) * cellBorder * 0.18;
    // Polygon outlines are drawn per projector by VortexHitRenderer. Keeping
    // the shader fill independent of the union mask prevents overlapping
    // enemy fields from producing a single blended outline.
    vec3 finalColor = fillColor;
    float alpha = (0.175 + cellLevel * 0.009 + cellBorder * 0.009) * centerMask * 0.65;

    // Intermediate blur buffers operate on premultiplied color.
    gl_FragColor = vec4(finalColor * alpha, alpha);
}
