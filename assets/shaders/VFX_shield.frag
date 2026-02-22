#define HIGHP

uniform sampler2D u_texture;

uniform vec2 u_offset;
uniform vec2 u_texsize;
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
    vec2 coords = (T * u_texsize) + u_offset;
    vec4 color = texture2D(u_texture, T);

    float t = u_time / 30.0;
    vec2 uv = coords / u_scale;

    // 应用六边形效果的变形 - 对30°, 90°, 150°三个方向进行扭曲处理
    vec2 dir30 = vec2(0.8660254, 0.5);  // 30°
    vec2 dir90 = vec2(0.0, 1.0);  // 90°
    vec2 dir150 = vec2(-0.8660254, 0.5);  // 150°
    
    float dist30 = dot(uv, dir30);
    float dist90 = dot(uv, dir90);
    float dist150 = dot(uv, dir150);
    
    vec2 distortion = dir30 * sin(dist30 * 5.0 + t) * 0.05 +
                      dir90 * sin(dist90 * 5.0 + t) * 0.05 +
                      dir150 * sin(dist150 * 5.0 + t) * 0.05;
    
    vec2 uv1 = uv + distortion;
    vec2 uv2 = 0.5 * uv1 + 0.5 * uv + distortion * 0.16;

    // 上下左右的波动效果（对uv1，uv2均进行计算）
    vec2 wave1 = vec2(sin(uv1.y * 5.0 + t) * 0.02, sin(uv1.x * 5.0 + t) * 0.02);
    vec2 wave2 = vec2(sin(uv2.y * 5.0 + t) * 0.02, sin(uv2.x * 5.0 + t) * 0.02);
    uv1 += wave1;
    uv2 += wave2;

    // 计算六边形效果强度
    float hex1 = smoothstep(0.05, 0.0, HexCoords(uv1 / 10).y);
    float hex2 = smoothstep(0.1, 0.0, HexCoords(uv2 / 2).y);
    float intensity = dot(sin(uv * vec2(cos(uv.x * 1.3), 7.0) + t * 2.0), vec2(0.7, 0.55974)) * 1.2 + 3.0;

    // 使用采样纹理的颜色进行计算，预乘alpha还原以获得真实颜色
    vec3 sampledColor = color.a > 0.001 ? (color.rgb / color.a) : color.rgb;

    // 计算六边形效果颜色
    vec3 col = vec3(0.0);
    col += hex1 * sampledColor * 0.5;
    col += hex2 * sampledColor * 0.3;
    col *= intensity;

    // 透明度检查：保存采样颜色的透明度通道
    float alpha = color.a;

    if (color.a > 0.001) {
        color.rgb *= 1.0 / color.a;
        color.rgb = mix(color.rgb, col, 0.5);
        color.a = alpha;
    } else {
        color = vec4(col, alpha);
    }

    gl_FragColor = color;
}

