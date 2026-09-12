#define HIGHP

uniform sampler2D u_texture;
uniform sampler2D u_simulation;
uniform sampler2D u_floorTex;

varying vec2 v_texCoords;

void main(){
    vec4 floorSource = texture2D(u_floorTex, v_texCoords);
    vec4 simulation = texture2D(u_simulation, v_texCoords);

    // Neutralize the copied violet base while retaining its terrain detail.
    float luma = dot(floorSource.rgb, vec3(0.299, 0.587, 0.114));
    float detail = clamp((luma - 0.48) * 1.60 + 0.14, 0.0, 1.0);
    vec3 base = mix(vec3(0.042, 0.048, 0.072), vec3(0.56, 0.61, 0.71), detail);

    // Exact structure from the supplied final shader:
    // rd = channel0.x * (0.7, 1.5, 2.0) - (0.3, 1.0, 1.0)
    float red = clamp(simulation.r, 0.0, 1.0);
    vec2 shift = (simulation.gb - 0.5) * 0.0035;
    float shiftedRed = texture2D(u_simulation, v_texCoords + shift).r;
    vec3 refracted = clamp(shiftedRed * vec3(0.70, 1.50, 2.00) -
        vec3(0.30, 1.00, 1.00), 0.0, 1.0);

    // Cool dendritic field. Deliberately removes the former yellow highlight.
    float fieldMask = smoothstep(0.06, 0.34, shiftedRed);
    vec3 field = refracted;
    field.b += fieldMask * 0.08;
    field.g += fieldMask * smoothstep(0.30, 0.55, shiftedRed) * 0.05;

    vec3 result = mix(base, field, clamp(fieldMask * 0.82, 0.0, 0.82));
    result += vec3(0.36, 0.48, 0.68) * pow(clamp((shiftedRed - 0.38) / 0.20, 0.0, 1.0), 2.0) * 0.18;

    gl_FragColor = vec4(clamp(result, 0.0, 1.0), floorSource.a);
}
