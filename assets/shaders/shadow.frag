#define HIGHP

uniform sampler2D u_texture;
uniform vec4 u_alpha;

varying vec2 v_texCoords;

void main(){
    gl_FragColor = u_alpha * (1.0 - step(texture2D(u_texture, v_texCoords).a, 0.001));
}
