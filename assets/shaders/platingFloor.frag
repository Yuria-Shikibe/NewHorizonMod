uniform sampler2D u_texture;
uniform sampler2D u_noise;

uniform vec2 u_campos;
uniform vec2 u_resolution;

varying vec2 v_texCoords;

#define NSCALE 100.0 / 2


void main(){
    vec2 c = v_texCoords;
    vec2 v = vec2(1.0/u_resolution.x, 1.0/u_resolution.y);
    vec2 coords = vec2(c.x / v.x + u_campos.x, c.y / v.y + u_campos.y);

    vec4 color = texture2D(u_texture, c);

    float noise = texture2D(u_noise, (coords) / NSCALE).r;

    color.r *= noise + 0.4;
    color.g *= noise + 0.4;
    color.b *= noise + 0.4;

    color.rgb = mix(color.rgb, vec4(0.45, 0.45, 0.45, 1).rgb, noise);

    gl_FragColor = color;
}
