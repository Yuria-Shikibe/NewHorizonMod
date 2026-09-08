#define HIGHP

uniform sampler2D u_texture;
uniform sampler2D u_blur;
uniform sampler2D u_noise;
uniform vec2 u_resolution;
uniform vec2 u_texsize;
uniform float u_time;
uniform int u_frame;

varying vec2 v_texCoords;

void main(){
    vec2 uv = v_texCoords;
    vec2 pixel = 1.0 / u_texsize;
    vec4 noise = texture2D(u_noise, uv + fract(vec2(42.0, 56.0) * u_time * 0.025));

    vec2 d = pixel * 4.0;
    vec4 dx = (texture2D(u_blur, fract(uv + vec2(d.x, 0.0))) -
        texture2D(u_blur, fract(uv - vec2(d.x, 0.0)))) * 0.5;
    vec4 dy = (texture2D(u_blur, fract(uv + vec2(0.0, d.y))) -
        texture2D(u_blur, fract(uv - vec2(0.0, d.y)))) * 0.5;
    vec2 gradient = vec2(dx.r, dy.r);

    vec2 shifted = fract(uv + gradient * 2.0);
    float previous = texture2D(u_texture, shifted).r;
    float blur = texture2D(u_blur, shifted).r;

    float value = previous + (noise.r - 0.5) * 0.0060 - 0.0022;
    value -= (blur - previous) * 0.075;
    value += (mix(0.26, 0.78, smoothstep(0.22, 0.88, noise.g)) - value) * 0.018;

    float branchTarget = smoothstep(0.48, 0.86, value + gradient.r * 2.5 + gradient.g * 2.5);
    float flowTarget = fract(noise.b + u_time * 0.012 + value * 0.18);
    float oldBranch = texture2D(u_texture, uv).g;
    float oldFlow = texture2D(u_texture, uv).b;
    float branch = mix(oldBranch, branchTarget, 0.035);
    float flow = mix(oldFlow, flowTarget, 0.012);

    if(u_frame < 10){
        gl_FragColor = noise;
    }else{
        gl_FragColor = texture2D(u_texture, uv);
        gl_FragColor.r = clamp(value, 0.0, 1.0);
        gl_FragColor.g = clamp(branch, 0.0, 1.0);
        gl_FragColor.b = clamp(flow, 0.0, 1.0);
    }
}
