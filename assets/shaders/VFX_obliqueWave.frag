#define HIGHP

uniform sampler2D u_texture;
uniform vec2 u_texsize;
uniform vec2 u_invsize;
uniform float u_time;

varying vec2 v_texCoords;

void main(){
    vec2 T = v_texCoords.xy;
    vec2 coords = (T * u_texsize);
    vec4 color = texture2D(u_texture, T);

    float timeFactor = u_time / 15.0;
    float sinFactor = abs(sin(timeFactor)) * 0.05;
    float baseAlpha = 0.37 + sinFactor;

    float stripe1 = step(mod(coords.x + coords.y + u_time / 4.0, 10.0), 3.0);
    float stripe2 = step(mod(coords.x - coords.y + u_time / 4.0, 10.0), 3.0);

    float alpha = max(baseAlpha + 0.2 * stripe1, baseAlpha + 0.2 * stripe2);

    color.a *= alpha;

    gl_FragColor = color;
}