#define HIGHP

#define STEP 0.00105
#define WHITE vec4(0.85, 0.85, 0.85, 1.0)

uniform sampler2D u_texture;

varying vec2 v_texCoords;

void main(){
    vec3 c;
    vec4 color = texture2D(u_texture, v_texCoords);

    if(
        color.a > 0.9 &&
        ((c = texture2D(u_texture, v_texCoords + vec2(STEP, STEP)).rgb) != color.rgb ||
        (c = texture2D(u_texture, v_texCoords + vec2(-STEP, -STEP)).rgb) != color.rgb ||
        (c = texture2D(u_texture, v_texCoords + vec2(-STEP, STEP)).rgb) != color.rgb ||
        (c = texture2D(u_texture, v_texCoords + vec2(STEP, -STEP)).rgb) != color.rgb)
    ){
        if(c.r * c.g * c.b > color.r * color.g * color.b){
            color.gb *= 1.025;
        }
    }

    gl_FragColor = color;
}