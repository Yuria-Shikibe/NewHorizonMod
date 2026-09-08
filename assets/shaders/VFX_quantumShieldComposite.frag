#define HIGHP

uniform sampler2D u_texture;
uniform sampler2D u_glowNear;
uniform sampler2D u_glowFar;

varying vec2 v_texCoords;

void main(){
    vec4 mainColor = texture2D(u_texture, v_texCoords);
    vec4 glowNear = texture2D(u_glowNear, v_texCoords);
    vec4 glowFar = texture2D(u_glowFar, v_texCoords);

    // Supplied second component, adapted from mip LOD samples to explicit blur passes.
    vec3 color = mainColor.a > 0.0001 ? mainColor.rgb / mainColor.a : vec3(0.0);
    color += glowFar.rgb;
    color += pow(glowNear.rgb, vec3(2.0));
    color += pow(glowFar.rgb, vec3(4.0));

    float alpha = clamp(mainColor.a + glowNear.a * 0.18 + glowFar.a * 0.22, 0.0, 1.0);
    gl_FragColor = vec4(color, alpha);
}
