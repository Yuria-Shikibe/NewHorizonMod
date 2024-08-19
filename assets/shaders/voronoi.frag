uniform sampler2D u_texture;
uniform vec2 u_resolution;
uniform float u_time;

varying vec2 v_texCoords;

float hash1(float n) {
    return fract(sin(n) * 43758.5453);
}

vec2 hash2(vec2 p) {
    p = vec2(dot(p, vec2(127.1, 311.7)), dot(p, vec2(269.5, 183.3)));
    return fract(sin(p) * 43758.5453);
}

// The parameter w controls the smoothness
vec4 voronoi(in vec2 x, float w) {
    vec2 n = floor(x);
    vec2 f = fract(x);

    vec4 m = vec4(8.0, 0.0, 0.0, 0.0);
    for (int j = -2; j <= 2; j++)
    for (int i = -2; i <= 2; i++) {
        vec2 g = vec2(float(i), float(j));
        vec2 o = hash2(n + g);

        // animate
        o = 0.5 + 0.5 * sin(u_time + 6.2831 * o);

        // distance to cell
        float d = length(g - f + o);

        // cell color
        vec3 col = 0.5 + 0.5 * sin(hash1(dot(n + g, vec2(7.0, 113.0))) * 2.5 + 3.5 + vec3(2.0, 3.0, 0.0));
        // in linear space
        col = col * col;

        // do the smooth min for colors and distances
        float h = smoothstep(-1.0, 1.0, (m.x - d) / w);
        m.x = mix(m.x, d, h) - h * (1.0 - h) * w / (1.0 + 3.0 * w); // distance
        m.yzw = mix(m.yzw, col, h) - h * (1.0 - h) * w / (1.0 + 3.0 * w); // color
    }

    return m;
}

void main() {
    vec2 p = v_texCoords / u_texture.y;
    float c = 0.5 * u_texture.x / u_texture.y;

    vec4 v = voronoi(6.0 * p, p.x < c ? 0.001 : 0.3);

    // gamma correction
    vec3 voronoiCol = sqrt(v.yzw);

    voronoiCol *= 1.0 - 0.8 * v.x * step(p.y, 0.33);
    voronoiCol *= mix(v.x, 1.0, step(p.y, 0.66));

    voronoiCol *= smoothstep(0.003, 0.005, abs(p.y - 0.33));
    voronoiCol *= smoothstep(0.003, 0.005, abs(p.y - 0.66));
    voronoiCol *= smoothstep(0.003, 0.005, abs(p.x - c));

    // Sample the texture
    vec4 texColor = texture2D(u_texture, v_texCoords);

    // Mix the voronoi color with the texture color
    vec3 finalColor = mix(texColor.rgb, voronoiCol, 0.5);

    gl_FragColor = vec4(finalColor, 1.0);
}