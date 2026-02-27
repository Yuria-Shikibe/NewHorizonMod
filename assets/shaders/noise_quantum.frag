#define HIGHP

const vec4 C0 = vec4(155.0, 163.0, 250.0, 255.0) / 255.0;

#define NSCALE 100.0 / 5.75

uniform sampler2D u_texture;
uniform sampler2D u_noise;
uniform sampler2D u_noise_2;

uniform vec2 u_campos;
uniform vec2 u_resolution;
uniform float u_time;

varying vec2 v_texCoords;

float softCurve(float x){
    x = clamp(x, 0.0, 1.0);
    float s = x * x * (3.0 - 2.0 * x);
    return mix(s, sqrt(s), 0.35);
}


void main(){
    vec2 coords = (v_texCoords.xy * u_resolution) + u_campos;
    vec4 color = texture2D(u_texture, v_texCoords.xy);

    float btime = u_time / 4000.0;
    float wave = abs(sin(coords.x / 5.0 + coords.y / 5.0) + 0.2 * sin(0.5 * coords.x) + 0.2 * sin(coords.y * 0.8)) / 20.0;

    float noise1 = wave + texture2D(u_noise, (coords) / NSCALE + vec2(btime) * vec2(-0.3, 0.7) + vec2(sin(btime * 12.0 + coords.y * 0.006) / 10.0, cos(btime * 8.0 + coords.x * 0.008) / 12.0)).r;
    float noise2 = wave + texture2D(u_noise, (coords) / NSCALE + vec2(btime) * vec2(1.2, -0.5) + vec2(cos(btime * -12.0 + coords.x * 0.01) / 10.0, sin(btime * 10.0 + coords.x * 0.008) / 12.0)).r * 0.85;

    float lerp = smoothstep(- wave + sin(1.15 * (coords.x + coords.y) + btime * 100.0) / 4.0,
        wave + 1.0 - cos(1.15 * (coords.x - coords.y) + btime * 200.0) / 4.0, noise1);

    float gray = 0.2126 * color.r + 0.7152 * color.g + 0.0722 * color.b;

    if (gray > 0.38){
        float n1 = softCurve(noise1);
        float n2 = softCurve(noise2);

        float singleNoise = (n1 + n2) * 0.5;
        float noisePower = singleNoise * 0.5;
        float softLerp = mix(1.0, lerp, 0.35);

        float waveR = wave * 0.3;
        float waveG = wave * 0.25;
        float waveB = wave * 0.35;

        float r = softLerp * 0.6 + 0.40 + waveR * sin(btime * 1.5);
        float g = softLerp * 0.6 + 0.42 + waveG * sin(btime * 1.2);
        float b = softLerp * 0.6 + 0.45 + waveB * sin(btime * 1.7);

        color.r *= r;
        color.g *= g;
        color.b *= b;

        float colorMixFactor = noisePower;
        color.rgb = mix(color.rgb, C0.rgb, colorMixFactor);

        float bright =
        1.0 +
        softLerp * 0.15 +
        noisePower * 0.35;

        color.rgb *= bright;
    }

    gl_FragColor = color;
}
