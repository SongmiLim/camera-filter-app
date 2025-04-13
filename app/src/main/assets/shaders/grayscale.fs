precision mediump float;
varying vec2 vTexCoord;

void main() {
    float gray = (vTexCoord.x + vTexCoord.y) * 0.5;
    gl_FragColor = vec4(gray, gray, gray, 1.0);
}
