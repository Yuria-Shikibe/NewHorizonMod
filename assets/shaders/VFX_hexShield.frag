#define HIGHP

#define STEP 2.0

uniform sampler2D u_texture;

uniform vec2 u_offset;
uniform vec2 u_texsize;
uniform vec2 u_invsize;
uniform float u_time;
uniform float u_scale;

varying vec2 v_texCoords;

float HexDist(vec2 p) {
    p = abs(p);

    float c = dot(p, normalize(vec2(1.0, 1.73)));
    c = max(c, p.x);

    return c;
}

vec4 HexCoords(vec2 uv) {
    const vec2 r = vec2(1.0, 1.73);
    const vec2 h = r * 0.5;

    vec2 a = mod(uv, r) - h;
    vec2 b = mod(uv - h, r) - h;

    vec2 gv = dot(a, a) < dot(b, b) ? a : b;

    float x = atan(gv.x, gv.y);
    float y = 0.5 - HexDist(gv);
    vec2 id = uv - gv;
    return vec4(x, y, id.x, id.y);
}

void main() {
    vec2 T = v_texCoords;
    vec2 v = u_invsize;
    vec2 coords = (T * u_texsize) + u_offset;
    vec4 color = texture2D(u_texture, T);

    if (color.a <= 0.01) {
        gl_FragColor = vec4(0.0, 0.0, 0.0, 0.0);
        return;
    }

    float t = u_time / 30.0;
    vec2 uv = coords / u_scale;

    vec2 uv1 = uv;
    vec2 uv2 = 0.5 * uv1 + 0.5 * uv;

    float hex1 = smoothstep(0.05, 0.0, HexCoords(uv1 / 12).y);
    float hex2 = smoothstep(0.1, 0.0, HexCoords(uv2 / 3).y);
    float intensity = dot(sin(uv * vec2(cos(uv.x * 1.3), 7.0) + t * 2.0), vec2(0.7, 0.55974)) * 1.2 + 3.0;

    float onHexGrid = max(hex1, hex2);

    if (onHexGrid <= 0.001) {
        gl_FragColor = vec4(0.0, 0.0, 0.0, 0.0);
        return;
    }

    vec3 sampledColor = color.a > 0.001 ? (color.rgb / color.a) : color.rgb;

    vec3 col = vec3(0.0);
    col += hex1 * sampledColor * 0.5;
    col += hex2 * sampledColor * 0.3;
    col *= intensity;

    vec3 finalColor = color.a > 0.001 ? mix(color.rgb / color.a, col, 0.5) : col;

    float finalAlpha = color.a;

    float hexIntensity = max(hex1, hex2);
    finalAlpha *= 1.0 + 2.0 * hexIntensity;

    vec2 T_wave = T + vec2(sin(coords.y / 0.75 + u_time / 20.0) * 0.125, sin(coords.x / 0.75 + u_time / 20.0) * 0.125) / u_texsize;
    vec4 color_wave = texture2D(u_texture, T_wave);

    vec2 coords_wave = (T_wave * u_texsize) + u_offset;
    vec2 uv_wave = coords_wave / u_scale;

    vec2 uv1_wave = uv_wave;
    vec2 uv2_wave = 0.5 * uv1_wave + 0.5 * uv_wave;

    float hex1_wave = smoothstep(0.05, 0.0, HexCoords(uv1_wave / 6).y);
    float hex2_wave = smoothstep(0.1, 0.0, HexCoords(uv2_wave / 1.5).y);
    float onHexGrid_wave = max(hex1_wave, hex2_wave);

    if (onHexGrid_wave <= 0.001) {
        gl_FragColor = vec4(0.0, 0.0, 0.0, 0.0);
        return;
    }

    vec3 sampledColor_wave = color_wave.a > 0.001 ? (color_wave.rgb / color_wave.a) : color_wave.rgb;
    vec3 col_wave = vec3(0.0);
    col_wave += hex1_wave * sampledColor_wave * 0.5;
    col_wave += hex2_wave * sampledColor_wave * 0.3;
    col_wave *= intensity;
    vec3 finalColor_wave = color_wave.a > 0.001 ? mix(color_wave.rgb / color_wave.a, col_wave, 0.5) : col_wave;
    float hexIntensity_wave = max(hex1_wave, hex2_wave);
    float finalAlpha_wave = color_wave.a * (1.0 + 2.0 * hexIntensity_wave) * 0.125;

    if (color_wave.a > 0.001) {
        gl_FragColor = vec4(finalColor_wave, finalAlpha_wave);
    } else {
        gl_FragColor = vec4(0.0, 0.0, 0.0, 0.0);
    }
}
