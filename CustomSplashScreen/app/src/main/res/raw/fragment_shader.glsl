precision highp float;

uniform float u_Time;
uniform vec2 u_Resolution;

const float PI_2 = 6.2831853;

void main() {
    // Surface position
    vec2 uv = ( gl_FragCoord.xy - 0.5*u_Resolution.xy ) / u_Resolution.y;
    // Accumulate color following sine and cosine functions
    float color = 0.0;
    color += sin(uv.x * cos(u_Time / 15.0) * 80.0) + cos(uv.y * cos(u_Time / 15.0) * 10.0);
    color += sin(uv.y * sin(u_Time / 10.0) * 40.0) + cos(uv.x * cos(u_Time / 25.0) * 40.0);
    color += sin(uv.x * sin(u_Time / 5.0) * 10.0) + sin(uv.y * cos(u_Time / 35.0) * 80.0);
    color *= sin(u_Time / 10.0) * 0.5;
    vec3 baseColor = vec3(1.0, 0.5, 0.75);
    gl_FragColor = vec4(vec3(color * baseColor.r, color * baseColor.g, sin(color + u_Time / 3.0) * baseColor.b), 1.0 );
}