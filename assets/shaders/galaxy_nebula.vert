#define HIGHP

attribute vec3 a_position;

uniform mat4 u_proj;
uniform mat4 u_trans;
uniform vec3 u_camera;
uniform float u_time;
uniform float u_seed;
uniform float u_warp;

varying vec3 v_local;
varying vec3 v_world;
varying vec3 v_normal;

void main(){
    vec3 normal = normalize(a_position);
    float phase = u_seed * 0.173;
    float wave =
        sin(a_position.x * 5.1 + a_position.y * 2.7 + u_time * 0.23 + phase) * 0.50 +
        sin(a_position.z * 6.3 - a_position.x * 3.2 - u_time * 0.17 + phase * 1.7) * 0.32 +
        sin((a_position.x + a_position.y + a_position.z) * 8.7 + u_time * 0.11) * 0.18;

    float lobe =
        sin(a_position.x * 2.3 - a_position.z * 3.1 + phase * 2.0) *
        cos(a_position.y * 3.7 + a_position.x * 1.9 - u_time * 0.08) * 0.38;

    vec3 local = a_position * (1.0 + (wave + lobe) * u_warp);
    local += vec3(
        sin(a_position.y * 4.0 + u_time * 0.13 + phase),
        cos(a_position.z * 3.0 - u_time * 0.09 + phase),
        sin(a_position.x * 3.5 + u_time * 0.07 - phase)
    ) * u_warp * 0.19;

    vec4 world = u_trans * vec4(local, 1.0);
    v_local = local;
    v_world = world.xyz;
    v_normal = normalize((u_trans * vec4(normal, 0.0)).xyz);
    gl_Position = u_proj * world;
}
