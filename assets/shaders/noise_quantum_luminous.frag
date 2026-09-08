#define HIGHP

uniform sampler2D u_texture;
uniform sampler2D u_blur;
uniform sampler2D u_noise;
uniform sampler2D u_mouseTex;
uniform sampler2D u_floorTex;
uniform vec2 u_resolution;
uniform float u_time;
uniform vec4 u_mouse;
uniform int u_frame;

varying vec2 v_texCoords;

vec2 complexMul(vec2 factorA, vec2 factorB){
    return vec2(factorA.x * factorB.x - factorA.y * factorB.y,
        factorA.x * factorB.y + factorA.y * factorB.x);
}

float sigmoid(float value){
    return 2.0 / (1.0 + exp(-value)) - 1.0;
}

float coneTip(vec2 uv, vec2 pos, float size, float minimum){
    vec2 aspect = vec2(1.0, u_resolution.y / u_resolution.x);
    return max(minimum, 1.0 - length((uv - pos) * aspect / size));
}

float warpFilter(vec2 uv, vec2 pos, float size, float ramp){
    return 0.5 + sigmoid(coneTip(uv, pos, size, -16.0) * ramp) * 0.5;
}

vec2 vortexWarp(vec2 uv, vec2 pos, float size, float ramp, vec2 rotation){
    vec2 aspect = vec2(1.0, u_resolution.y / u_resolution.x);
    vec2 correctedPos = 0.5 + (pos - 0.5);
    vec2 rotatedUv = correctedPos + complexMul((uv - correctedPos) * aspect,
        rotation) / aspect;
    return mix(uv, rotatedUv, warpFilter(uv, correctedPos, size, ramp));
}

vec2 vortexPairWarp(vec2 uv, vec2 pos, vec2 velocity){
    vec2 aspect = vec2(1.0, u_resolution.y / u_resolution.x);
    const float ramp = 5.0;
    const float separation = 0.200000003;
    float speed = length(velocity);
    vec2 firstPos = pos;
    vec2 secondPos = pos;

    if(speed > 0.0){
        vec2 normal = normalize(velocity.yx * vec2(-1.0, 1.0)) / aspect;
        firstPos = pos - normal * separation * 0.5;
        secondPos = pos + normal * separation * 0.5;
    }

    float angle = speed / separation * 2.0;
    vec2 clockwise = vortexWarp(uv, firstPos, separation, ramp,
        vec2(cos(angle), sin(angle)));
    vec2 counterClockwise = vortexWarp(uv, secondPos, separation, ramp,
        vec2(cos(-angle), sin(-angle)));
    return (clockwise + counterClockwise) * 0.5;
}

vec2 mouseDelta(){
    vec2 pixelSize = 1.0 / u_resolution;
    const float eighth = 0.125;
    vec4 oldMouse = texture2D(u_mouseTex,
        vec2(7.5 * eighth, 2.5 * eighth));
    vec4 nowMouse = vec4(u_mouse.xy / u_resolution, u_mouse.zw / u_resolution);

    float oldActiveX = step(pixelSize.x, oldMouse.z);
    float oldActiveY = step(pixelSize.y, oldMouse.w);
    float nowActiveX = step(pixelSize.x, nowMouse.z);
    float nowActiveY = step(pixelSize.y, nowMouse.w);

    vec2 movedDelta = nowMouse.xy - oldMouse.xy;
    return movedDelta * oldActiveX * oldActiveY * nowActiveX * nowActiveY;
}

void main(){
    vec2 uv = v_texCoords;
    uv = clamp(uv, 0.0, 1.0);
    vec2 pixelSize = 1.0 / u_resolution;
    vec2 mouseVelocity = mouseDelta();
    vec2 aspect = vec2(1.0, u_resolution.y / u_resolution.x);
    vec2 scaledVelocity = mouseVelocity * aspect * 1.39999998;
    uv = vortexPairWarp(uv, u_mouse.xy, scaledVelocity);
    vec4 noise = texture2D(u_noise, uv + fract(vec2(42.0, 56.0) * u_time));
    vec2 d = pixelSize * 4.0;
    vec4 dx = (texture2D(u_blur, fract(uv + vec2(d.x, 0.0))) -
        texture2D(u_blur, fract(uv - vec2(d.x, 0.0)))) * 0.5;
    vec4 dy = (texture2D(u_blur, fract(uv + vec2(0.0, d.y))) -
        texture2D(u_blur, fract(uv - vec2(0.0, d.y)))) * 0.5;
    vec2 uvRed = uv + vec2(dx.x, dy.x) * pixelSize * 8.0;
    vec2 noiseOffset = (noise.xy - 0.5) * pixelSize;
    float newRed = texture2D(u_texture, fract(uvRed)).x +
        (noise.x - 0.5) * 0.00249999994 - 0.00200000009;
    newRed -= (texture2D(u_blur, fract(uvRed + noiseOffset)).x -
        texture2D(u_texture, fract(uvRed + noiseOffset)).x) * 0.0469999984;
    float targetRed = mix(0.30, 0.72, smoothstep(0.24, 0.86, noise.g));
    newRed += (targetRed - newRed) * 0.011;

    if(u_frame < 10){
        gl_FragColor = noise;
    }else{
        gl_FragColor = texture2D(u_texture, uv);
        gl_FragColor.x = clamp(newRed, 0.0, 1.0);
    }
}
