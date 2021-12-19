#define HIGHP

const float THICK = 1.05;
const float LEN = 8.0;
const float SPACING = 25.0;

const float ALPHA = 0.33;
const float STEP = 2.5;

const vec4 ALLY = vec4(0.57, 0.68, 1.0, 1.0);
const vec4 HOSTILE = vec4(1.0, 0.44, 0.41, 1.0);
const vec4 BOTH = vec4(1, 0.82, 0.49, 1.0);

uniform sampler2D u_texture;
uniform vec2 u_texsize;
uniform vec2 u_invsize;
uniform float u_time;
uniform float u_dp;
uniform vec2 u_offset;

varying vec2 v_texCoords;

void main(){
    vec2 T = v_texCoords.xy;
    vec2 coords = (T * u_texsize) + u_offset;

//    T += vec2(sin(coords.y / 3.0 + u_time / 20.0), sin(coords.x / 3.0 + u_time / 20.0)) / u_texsize;
//
    vec4 color = texture2D(u_texture, T);
    vec2 v = u_invsize;
//
    vec4 maxed =
        max(
            max(
                max(texture2D(u_texture, T + vec2(0.0, STEP) * v), texture2D(u_texture, T + vec2(0.0, -STEP) * v)
            ),
            texture2D(u_texture, T + vec2(STEP, 0.0) * v)
        ),
        texture2D(u_texture, T + vec2(-STEP, 0.0) * v)
    );

    if(color.a >= 0.001){
        if(color.r > 0.0 && color.b > 0.0)color = BOTH;
        else{
            if(color.r > 0.0)color = HOSTILE;
            else if(color.b > 0.0)color = ALLY;
        }

        color.a = ALPHA;

        color.a *= 1.0 + 2.0 * (step(mod(coords.y / u_dp - (LEN - THICK) / 2.0 + u_time / 4.0, SPACING), THICK) * step(mod(coords.x / u_dp + u_time / 4.0, SPACING), LEN));
        if(color.a == ALPHA)color.a *= 1.0 + 2.0 * (step(mod(coords.x  / u_dp - (LEN - THICK) / 2.0 + u_time / 4.0, SPACING), THICK) * step(mod(coords.y / u_dp + u_time / 4.0, SPACING), LEN));

        color.a *= 1.0 + sin(u_time / 18.0) * 0.1;
    }

    if(texture2D(u_texture, T).a < 0.9 && color.a < 0.001){
        if(maxed.r > 0.0 && maxed.b > 0.0)maxed = BOTH;
        else{
            if(maxed.r > 0.0)maxed = HOSTILE;
            else if(maxed.b > 0.0)maxed = ALLY;
        }
        gl_FragColor = maxed;
    }else{
        gl_FragColor = color;
    }
}
