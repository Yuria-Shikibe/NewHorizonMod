#define HIGHP

#define MIN_DIST 0.82
#define MAX_DIST 0.96

uniform sampler2D u_texture;
uniform vec2 u_texsize;
uniform vec2 u_invsize;
uniform float u_time;

varying vec2 v_texCoords;

void main() {
    float width = u_texsize.x;
    float height = u_texsize.y;

    vec2 center = vec2(0.5, 0.5);
    vec2 position = v_texCoords;

    vec2 dir = position - center;
    float normalizedX = dir.x * 2.0;
    float normalizedY = dir.y * 2.0;
    float distance = sqrt(normalizedX * normalizedX + normalizedY * normalizedY);

    float scale;

    if (distance < MIN_DIST) {
        scale = 0.0;
    } else if (distance < MAX_DIST) {
        scale = (distance - MIN_DIST) / (MAX_DIST - MIN_DIST);
    } else {
        scale = 1.0;
    }

    vec4 color = texture2D(u_texture, position);
    float alpha = color.a;

    color.a *= (alpha * scale);
    color.a = clamp(color.a, 0.0, 1.0);

    gl_FragColor = color;
}