#define HIGHP

#define THICK 1.2
#define LEN 12.0
#define SPACING 40.0

#define ALPHA 0.18
#define STEP 2.5

#define ALLY vec4(0.57, 0.68, 1.0, 1.0)
#define HOSTILE vec4(1.0, 0.44, 0.41, 1.0)
#define BOTH vec4(1.0, 0.86, 0.55, 1.0)

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
    vec4 color = texture2D(u_texture, v_texCoords);
    vec2 v = u_invsize;
//


    vec4 maxed =
        max(
            max(
                max(texture2D(u_texture, T + vec2(STEP, STEP) * v), texture2D(u_texture, T + vec2(STEP, -STEP) * v)
            ),
            texture2D(u_texture, T + vec2(-STEP, STEP) * v)
        ),
        texture2D(u_texture, T + vec2(-STEP, -STEP) * v)
    );

    if(color.a >= 0.001){
        if(color.r > 0.0 && color.b > 0.0)color = BOTH;
        else{
            if(color.r > 0.0)color = HOSTILE;
            else if(color.b > 0.0)color = ALLY;
        }

        color.a = ALPHA;

        if(color.rgb == ALLY.rgb){
            color.a *= 1.0 + 2.0 * (step(mod(coords.y / u_dp - (LEN - THICK) / 2.0 + u_time / 4.0, SPACING), THICK) * step(mod(coords.x / u_dp + u_time / 4.0, SPACING), LEN));
            if (color.a == ALPHA)color.a *= 1.0 + 2.0 * (step(mod(coords.x  / u_dp - (LEN - THICK) / 2.0 + u_time / 4.0, SPACING), THICK) * step(mod(coords.y / u_dp + u_time / 4.0, SPACING), LEN));
        }else if(color.rgb == HOSTILE.rgb){
            color.a *= (0.47 + abs(sin(u_time / 15.0)) * .05 + 0.9 * (step(mod(coords.x / u_dp + coords.y / u_dp + u_time / 4.0, 10.0), 3.0)));
        }else{
            color.a *= 1.0 + 2.0 * (step(mod(coords.y / u_dp - (LEN - THICK) / 2.0 + u_time / 4.0, SPACING), THICK) * step(mod(coords.x / u_dp + u_time / 4.0, SPACING), LEN));
            if(color.a == ALPHA)color.a *= 1.0 + 2.0 * (step(mod(coords.x  / u_dp - (LEN - THICK) / 2.0 + u_time / 4.0, SPACING), THICK) * step(mod(coords.y / u_dp + u_time / 4.0, SPACING), LEN));
            if(color.a == ALPHA)color.a *= (0.37 + abs(sin(u_time / 15.0)) * .05 + 0.9 * (step(mod(coords.x / u_dp + coords.y / u_dp + u_time / 4.0, 10.0), 3.0)));
        }

        color.a *= 1.0 + sin(u_time / 18.0) * 0.1;
    }

    if(texture2D(u_texture, T).a < 0.005 && maxed.a > 0.001){
        if(maxed.r > 0.0 && maxed.b > 0.0)maxed = BOTH;
        else{
            if(maxed.r > 0.0)maxed = HOSTILE;
            else if(maxed.b > 0.0)maxed = ALLY;
        }

        maxed.a = 0.53;

        gl_FragColor = maxed;
    }else{
        gl_FragColor = color;
    }
}
