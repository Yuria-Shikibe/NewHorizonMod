//https://www.shadertoy.com/view/ttdXD8
uniform sampler2D u_texture;
uniform sampler2D u_noise;

uniform float u_time;
uniform float u_intensity;
uniform vec2 u_texsize;

varying vec2 v_texCoords;    // 0..1 UV

float rand(float n) {
    return fract(sin(n) * 43758.5453123);
}

float noise1D(float p) {
    float fl = floor(p);
    float fc = fract(p);
    return mix(rand(fl), rand(fl + 1.0), fc);
}

float blockyNoise(vec2 uv, float threshold, float scale, float seed) {
    float scroll = floor(u_time + sin(11.0 * u_time) + sin(u_time)) * 0.77;
    vec2 noiseUV = uv.yy / scale + scroll;

    float noise2 = texture2D(u_noise, noiseUV).r;
    float id = floor(noise2 * 20.0);
    id = noise1D(id + seed) - 0.5;

    if (abs(id) > threshold)
    id = 0.0;

    return id;
}

void main() {
    vec2 fragCoord = v_texCoords * u_texsize;
    vec2 uv = fragCoord / u_texsize;

    float rgbIntesnsity       = (0.1 + 0.1 * sin(u_time * 3.7)) * u_intensity;
    float displaceIntesnsity  = (0.2 + 0.3 * pow(sin(u_time * 1.2), 5.0)) * (u_intensity / 2.0);
    float interlaceIntesnsity = 0.01 * u_intensity;
    float dropoutIntensity    = 0.2 * (u_intensity / 3.0);

    float displace = blockyNoise(uv + vec2(uv.y, 0.0), displaceIntesnsity, 25.0, 66.6);
    displace *= blockyNoise(uv.yx + vec2(0.0, uv.x), displaceIntesnsity, 111.0, 13.7);

    uv.x += displace;

    vec2 offs = 0.1 * vec2(blockyNoise(uv.xy + vec2(uv.y, 0.0), rgbIntesnsity, 65.0, 341.0), 0.0);

    vec4 texR = texture2D(u_texture, uv - offs);
    vec4 texG = texture2D(u_texture, uv);
    vec4 texB = texture2D(u_texture, uv + offs);

    float alpha = texG.a;

    float colr = texR.r;
    float colg = texG.g;
    float colb = texB.b;

    float line = fract(fragCoord.y / 3.0);
    vec3 mask = vec3(3.0, 0.0, 0.0);
    if (line > 0.333) mask = vec3(0.0, 3.0, 0.0);
    if (line > 0.666) mask = vec3(0.0, 0.0, 3.0);

    float maskNoise = blockyNoise(uv, interlaceIntesnsity, 90.0, u_time) * max(displace, offs.x);
    maskNoise = 1.0 - maskNoise;
    if (maskNoise == 1.0)
    mask = vec3(1.0);

    float dropout = blockyNoise(uv, dropoutIntensity, 11.0, u_time) *
    blockyNoise(uv.yx, dropoutIntensity, 90.0, u_time);
    mask *= (1.0 - 5.0 * dropout);

    gl_FragColor = vec4(mask * vec3(colr, colg, colb), alpha);
}
