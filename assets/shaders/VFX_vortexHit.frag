#define HIGHP

uniform sampler2D u_texture;
uniform vec2 u_offset;
uniform vec2 u_texsize;
uniform vec2 u_invsize;
uniform float u_time;
uniform float u_scale;
uniform float u_hits[48];
uniform float u_hitAngles[48];
uniform int u_hitCount;

varying vec2 v_texCoords;

vec2 vortexWarp(vec2 uv, vec2 hitPos, float radius, float angle, float strength){
    vec2 delta = uv - hitPos;
    float distance = length(delta);
    if(distance >= radius) return uv;

    float local = 1.0 - distance / radius;
    local = pow(local, 2.0);
    float rotation = angle + strength * local * (1.0 - local) * 12.0;
    vec2 rotated = hitPos + vec2(
        delta.x * cos(rotation) - delta.y * sin(rotation),
        delta.x * sin(rotation) + delta.y * cos(rotation)
    );
    return mix(uv, rotated, smoothstep(0.0, 1.0, local));
}

void main(){
    vec2 uv = v_texCoords;
    vec4 source = texture2D(u_texture, uv);

    for(int i = 0; i < 24; i++){
        if(i >= u_hitCount) break;
        vec2 hitPos = vec2(u_hits[i * 2], u_hits[i * 2 + 1]);
        float angle = u_hitAngles[i * 2];
        float age = u_hitAngles[i * 2 + 1];
        float radius = mix(0.08, 0.34, age);
        float strength = (1.0 - age) * (0.55 - 0.18 * age);
        float swirl = sin(age * 9.42) * 0.5 + 0.5;
        uv = vortexWarp(uv, hitPos, radius, angle + age * 7.0, strength * swirl);
    }

    vec4 color = texture2D(u_texture, uv);

    vec3 luminous = vec3(0.36, 0.82, 1.00);
    vec3 violet = vec3(0.58, 0.22, 1.00);
    vec3 gold = vec3(1.00, 0.72, 0.28);

    for(int i = 0; i < 24; i++){
        if(i >= u_hitCount) break;
        vec2 hitPos = vec2(u_hits[i * 2], u_hits[i * 2 + 1]);
        float angle = u_hitAngles[i * 2];
        float age = u_hitAngles[i * 2 + 1];
        float radius = mix(0.06, 0.30, age);
        vec2 delta = v_texCoords - hitPos;
        float dist = length(delta);
        float mask = exp(-pow((dist - radius * 0.62) / max(radius * 0.20, 0.001), 2.0));
        float spiral = sin(atan(delta.y, delta.x) * 3.0 + angle + age * 10.0 + dist * 32.0);
        spiral = 0.65 + 0.35 * spiral;
        float fade = pow(clamp(1.0 - age, 0.0, 1.0), 1.6) * mask * spiral;
        vec3 palette = mix(violet, luminous, smoothstep(0.15, 0.75, fade));
        palette = mix(palette, gold, pow(clamp(1.0 - age, 0.0, 1.0), 3.0) * 0.35);
        color.rgb += palette * fade * 0.85;
        color.a = clamp(color.a + fade * 0.55, 0.0, 1.0);
    }

    gl_FragColor = clamp(color, 0.0, 1.0);
}
