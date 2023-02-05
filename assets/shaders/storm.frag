#define HIGHP

#define NSCALE 100.0 / 25.3
#define SCL 470.0


uniform vec2 u_direction;

uniform vec4 u_color_sec;
uniform vec4 u_color_pri;

uniform mat2 u_rotator;
uniform mat2 u_scaler;

uniform sampler2D u_noise;

uniform vec2 u_campos;
uniform vec2 u_resolution;
uniform float u_time;

varying vec2 v_texCoords;

void main(){
    float btime = u_time / 1800.0;
    float intensity = length(u_direction * 5.0);

    vec2 n = normalize(u_direction);
    vec2 v = vec2(n.y, -n.x);
    vec2 coords = v_texCoords.xy * u_resolution + u_campos;

    vec2 stretch = (dot(v, coords) * v + dot(v, coords) * n / SCL / pow(intensity, 3.0)) / SCL;

    float noise1 = (
        texture2D(u_noise, coords / NSCALE + stretch +
        vec2(btime * intensity) * -n +
        vec2(cos(btime / SCL + (coords.y + stretch.x) * 0.004) / 10.0, sin(btime / SCL * 1.5 + (coords.x + stretch.y) * 0.004) / 8.0) * n
    ).r - texture2D(u_noise, coords / NSCALE + n * intensity + vec2(btime / 70.0) * (v + n)).r * 0.6) * 2.2;

    float noise2 = texture2D(u_noise, coords / NSCALE * 1.8 + stretch -n * intensity + vec2(btime / 10.0 * sqrt(intensity)) * -n + vec2(0, sin(btime / 10.0 + coords.x * 0.008) / 18.0) * v).r;

    float noise3 = texture2D(u_noise, coords / NSCALE + vec2(stretch.y, stretch.x) * v + vec2(-btime / 20.0) * n + vec2(0.0, sin(btime / 10.0 + coords.x * 0.008) / 18.0) * n).r;

    vec4 color = u_color_pri;

    color.a *= noise1 + noise2 * 1.5 - noise3 * 0.4;
    color.rgb *= max(1.0 + (noise1 * (1.6 - noise3)) * 0.6 - pow(1.0 - color.a * 0.8, 2.0) * 1.4, 0.8);

    color.rgb = mix(color.rgb, u_color_sec.rgb, noise2);
    color.rgb *= max(noise2 + color.a * 0.5, 0.8);
    color.rgb *= clamp(color.rgb, 0.758, 0.955);

    if(color.r + color.g + color.b > 0.5 * 3.0){
        gl_FragColor = color;
    }else{
        gl_FragColor = vec4(0.0);
    }

    gl_FragColor = color;
}
