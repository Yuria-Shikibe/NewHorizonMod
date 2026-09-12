#define HIGHP

uniform sampler2D u_texture;
uniform vec2 u_texsize;
uniform float u_radius;

varying vec2 v_texCoords;

void main(){
    vec2 uv = v_texCoords;
    vec2 pixel = 1.0 / u_texsize;
    float v = pixel.y * u_radius;
    vec4 sum = vec4(0.0);

    sum += texture2D(u_texture, uv + vec2(0.0, -4.0 * v)) * 0.05;
    sum += texture2D(u_texture, uv + vec2(0.0, -3.0 * v)) * 0.09;
    sum += texture2D(u_texture, uv + vec2(0.0, -2.0 * v)) * 0.12;
    sum += texture2D(u_texture, uv + vec2(0.0, -v)) * 0.15;
    sum += texture2D(u_texture, uv) * 0.16;
    sum += texture2D(u_texture, uv + vec2(0.0, v)) * 0.15;
    sum += texture2D(u_texture, uv + vec2(0.0, 2.0 * v)) * 0.12;
    sum += texture2D(u_texture, uv + vec2(0.0, 3.0 * v)) * 0.09;
    sum += texture2D(u_texture, uv + vec2(0.0, 4.0 * v)) * 0.05;

    gl_FragColor = sum / 0.98;
}
