#define HIGHP

const vec4 C0 = vec4(155.0, 163.0, 250.0, 255.0) / 255.0;

#define NSCALE 100.0 / 5.75

uniform sampler2D u_texture;
uniform sampler2D u_noise;
uniform sampler2D u_noise_2;

uniform vec2 u_campos;
uniform vec2 u_resolution;
uniform float u_time;

varying vec2 v_texCoords;

void main(){
    vec2 c = v_texCoords.xy;
    vec2 coords = vec2(c.x * u_resolution.x + u_campos.x, c.y * u_resolution.y + u_campos.y) + (u_campos + u_resolution / 2.0) / 30.0;

    float btime = u_time / 4000.0;
    float wave = abs(sin(coords.x / 5.0 + coords.y / 5.0) + 0.2 * sin(0.5 * coords.x) + 0.2 * sin(coords.y * 0.8)) / 20.0;

    float noise1 = wave + texture2D(u_noise, (coords) / NSCALE + vec2(btime) * vec2(-0.3, 0.7) + vec2(sin(btime * 12.0 + coords.y * 0.006) / 10.0, cos(btime * 8.0 + coords.x * 0.008) / 12.0)).r;
    float noise2 = wave + texture2D(u_noise, (coords) / NSCALE + vec2(btime) * vec2(1.2, -0.5) + vec2(cos(btime * -12.0 + coords.x * 0.01) / 10.0, sin(btime * 10.0 + coords.x * 0.008) / 12.0)).r * 0.85;
    float noise3 = pow(texture2D(u_noise, (coords) / NSCALE / 2.0 + vec2(btime) * vec2(-0.8, -0.5) + vec2(cos(btime * 8.0 + coords.x * 0.005) / 16.0, cos(btime * 12.0 + coords.y * 0.006) / 18.0)).r, 2.5);

    vec4 color = texture2D(u_texture, c);

    float lerp = smoothstep(- wave + sin(1.15 * (coords.x + coords.y) + btime * 100.0) / 4.0, wave + 1.0 - cos(1.15 * (coords.x - coords.y) + btime * 200.0) / 4.0, noise1) - noise3;


    color.r *= lerp + 0.375;
    color.g *= lerp + 0.4 + wave / 3.0 * sin(btime * 2.5);
    color.b *= lerp + 0.45 + wave * sin(btime * 3.5);

    color.rgb = mix(color.rgb, C0.rgb, noise2);

    color.rgb *= lerp * 0.3 + 1.0 + noise2 * 0.66 - noise3;

    gl_FragColor = color;
}
