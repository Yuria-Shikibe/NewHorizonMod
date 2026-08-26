#define HIGHP

uniform sampler2D u_texture;
uniform vec2 u_texsize;

varying vec2 v_texCoords;

void main(){
    vec2 uv = v_texCoords;
    vec2 pixel = 1.0 / u_texsize;
    float h = pixel.x;
    vec3 sum = vec3(0.0);

    sum += texture2D(u_texture, uv + vec2(-4.0 * h, 0.0)).rgb * 0.05;
    sum += texture2D(u_texture, uv + vec2(-3.0 * h, 0.0)).rgb * 0.09;
    sum += texture2D(u_texture, uv + vec2(-2.0 * h, 0.0)).rgb * 0.12;
    sum += texture2D(u_texture, uv + vec2(-h, 0.0)).rgb * 0.15;
    sum += texture2D(u_texture, uv).rgb * 0.16;
    sum += texture2D(u_texture, uv + vec2(h, 0.0)).rgb * 0.15;
    sum += texture2D(u_texture, uv + vec2(2.0 * h, 0.0)).rgb * 0.12;
    sum += texture2D(u_texture, uv + vec2(3.0 * h, 0.0)).rgb * 0.09;
    sum += texture2D(u_texture, uv + vec2(4.0 * h, 0.0)).rgb * 0.05;

    gl_FragColor = vec4(sum / 0.98, 1.0);
}
