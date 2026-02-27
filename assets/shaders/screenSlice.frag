#define HIGHP
precision highp float;

uniform sampler2D u_texture;
uniform int u_count;
uniform vec3 u_cuts[16]; // (x, y, scl)

varying vec2 v_texCoords;

void main(){
    vec2 uv = v_texCoords;
    vec2 offset = vec2(0.0);

    for(int i = 0; i < 16; i++){
        if(i >= u_count) break;

        vec3 c = u_cuts[i].rgb;
        vec2 center = c.xy;
        float scl = c.z;

        vec2 d = uv - center;

        float p1 = d.x + d.y;
        float p2 = d.x - d.y;

        float m1 = max(0.0, 1.0 - abs(p1) / scl);
        float m2 = max(0.0, 1.0 - abs(p2) / scl);

        m1 *= m1;
        m2 *= m2;

        vec2 n1 = normalize(vec2( 1.0, -1.0));
        vec2 n2 = normalize(vec2( 1.0,  1.0));

        offset += n1 * sign(p1) * m1 * scl * 0.15;
        offset += n2 * sign(p2) * m2 * scl * 0.15;
    }

    gl_FragColor = texture2D(u_texture, uv + offset);
}
