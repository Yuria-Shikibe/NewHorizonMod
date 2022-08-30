#define HIGHP

uniform sampler2D u_texture;
uniform vec2 u_texsize;
uniform vec2 u_invsize;
uniform float u_time;
uniform float u_dp;
uniform vec2 u_offset;
varying vec2 v_texCoords;

void main(){

    vec4 color = texture2D(u_texture, v_texCoords);

    if(color.a > 0.0001)gl_FragColor = vec4(1.0,1.0,1.0,1.0);


    color.a *= (0.37 + abs(sin(u_time / 15.0)) * .05 + 0.2 * (step(mod(coords.x / u_dp + coords.y / u_dp + u_time / 4.0, 10.0), 3.0)));
    gl_FragColor = color;
}
