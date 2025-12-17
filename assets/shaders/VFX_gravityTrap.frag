#define HIGHP

#define LEN 12.0
#define THICK 2.0
#define SPACING 40.0

#define ALPHAIN 0.18
#define ALPHAOUT 0.58

uniform sampler2D u_texture;

uniform vec2 u_offset;
uniform vec2 u_texsize;
uniform vec2 u_invsize;
uniform float u_time;
uniform float u_scale;

varying vec2 v_texCoords;

void main(){
    vec2 T = v_texCoords;
    vec2 v = u_invsize;

    vec2 coords = (T * u_texsize) + u_offset;
    vec4 color = texture2D(u_texture, T);

    float step = THICK;

    vec4 outline = min(min(min(
        texture2D(u_texture, T + vec2( THICK,  THICK) * v),
        texture2D(u_texture, T + vec2( THICK, -THICK) * v)),
        texture2D(u_texture, T + vec2(-THICK,  THICK) * v)),
        texture2D(u_texture, T + vec2(-THICK, -THICK) * v)
    );

    if(color.a > 0.001){
        color.rgb *= 1.0 / color.a;

        float alpha = 0.18;
        color.a = ALPHAIN;

        float horizontal =
        step(mod(coords.y / u_scale - (LEN - THICK) / 2.0 + u_time / 4.0, SPACING), THICK) *
        step(mod(coords.x / u_scale + u_time / 4.0, SPACING), LEN);

        float vertical =
        step(mod(coords.x / u_scale - (LEN - THICK) / 2.0 + u_time / 4.0, SPACING), THICK) *
        step(mod(coords.y / u_scale + u_time / 4.0, SPACING), LEN);

        float oblique =
        0.05 * (0.47 + abs(sin(u_time / 15.0)) +
        0.9 * (step(mod(coords.x / u_scale + coords.y / u_scale + u_time / 4.0, 10.0), 3.0)));

        if (color.a == ALPHAIN) color.a *= 1.0 + 2.0 * max(max(horizontal, vertical), oblique);

        alpha *= 1.0 + sin(u_time / 18.0) * 0.1;
    }

    if(color.a > 0.001 && outline.a < color.a){
        gl_FragColor = vec4(color.rgb, 0.53);
    }else{
        gl_FragColor = color;
    }
}
