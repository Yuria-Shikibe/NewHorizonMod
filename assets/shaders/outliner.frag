#define HIGHP

#define THICK 0.8
#define LEN 8.0
#define SPACING 32.0

#define ALPHA 0.18

#define ALLY vec4(0.57, 0.68, 1.0, 1.0)
#define HOSTILE vec4(1.0, 0.44, 0.41, 1.0)
#define BOTH vec4(1.0, 0.86, 0.55, 1.0)

uniform vec2 u_offset;
uniform sampler2D u_texture;
uniform vec2 u_texsize;
uniform vec2 u_invsize;
uniform float u_time;
uniform float u_dp;

uniform vec4 u_color;

varying vec2 v_texCoords;

void main(){
    vec2 T = v_texCoords.xy;
    vec2 coords = (T * u_texsize) + u_offset;
    vec4 color = texture2D(u_texture, T);
    vec2 v = u_invsize;

    float step = 1.5 + mod(u_time, 5.0);
//
    vec4 maxed =
        max(
            max(
                max(texture2D(u_texture, T + vec2(step, step) * v), texture2D(u_texture, T + vec2(step, -step) * v)
            ),
            texture2D(u_texture, T + vec2(-step, step) * v)
        ),
        texture2D(u_texture, T + vec2(-step, -step) * v)
    );

    if(maxed.a > 0.001){
        color = mix(color, u_color, 0.5);
        color *= 1.5;

        gl_FragColor = maxed;
    }else{
        gl_FragColor = color;
    }
}
