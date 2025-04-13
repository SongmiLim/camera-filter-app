precision mediump float;
varying vec2 vTexCoord;

void main() {
    vec3 base = vec3(vTexCoord.x, vTexCoord.y, 1.0);
    vec3 bright = base + vec3(0.3);
    gl_FragColor = vec4(min(bright, 1.0), 1.0);
}
