#define HIGHP

uniform vec2 u_offset;
uniform sampler2D u_texture;
uniform vec2 u_texsize;
uniform vec2 u_invsize;
uniform float u_time;
uniform float u_dp;
uniform float u_thick;

varying vec2 v_texCoords;

void main(){
    vec2 T = v_texCoords.xy;
    vec2 coords = (T * u_texsize) + u_offset;
    vec2 v = u_invsize;
    float step = u_thick;

    vec4 center = texture2D(u_texture, T);

    vec4 maxed =
        max(
            max(
                max(texture2D(u_texture, T + vec2(step, step) * v), texture2D(u_texture, T + vec2(step, -step) * v)
            ),
            texture2D(u_texture, T + vec2(-step, step) * v)
        ),
        texture2D(u_texture, T + vec2(-step, -step) * v)
    );

    if (center.a > 0.001) {
        gl_FragColor = vec4(0.0);
    } else if (maxed.a > 0.001) {
        gl_FragColor = vec4(maxed.rgb, 0.6);
    } else {
        gl_FragColor = vec4(0.0);
    }
}
