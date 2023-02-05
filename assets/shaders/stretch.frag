#define HIGHP

uniform sampler2D u_texture;
uniform vec2 u_texsize;
uniform vec2 u_invsize;
uniform vec4 u_data;
uniform int u_length;
uniform float u_dp;
uniform vec2 u_offset;

varying vec2 v_texCoords;

float curve(float f, float from, float to){
    if(f < from){
        return 0.0;
    }else if(f > to){
        return 1.0;
    }else{
        return (f - from) / (to - from);
    }
}

void main(){
    vec2 coords = (v_texCoords.xy * u_texsize) + u_offset;

    vec2 sum = vec2(0, 0);
//    for(int idx = 0; idx < u_length; idx+=4){
        float x = u_data.r;//[idx];
        float y = u_data.g;//[idx + 1];
        float radius = u_data.b;//[idx + 2];
        float fin = u_data.a;//[idx + 3];

        vec2 into = vec2(x - coords.x, y - coords.y);
        float d = length(into);

//    if(d < radius * 1.25){
        into *= fin * (max(d / radius * 2.0, 1.005));
        sum += into;
//    }

//    }/

//    sum /= u.data.length / 4;
    sum *= u_invsize;
    gl_FragColor = texture2D(u_texture, sum + v_texCoords.xy);
}
