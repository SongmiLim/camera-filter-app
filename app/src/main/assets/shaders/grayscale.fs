#extension GL_OES_EGL_image_external : require
precision mediump float;

uniform samplerExternalOES uTexture;
varying vec2 vTexCoord;

void main() {
    vec4 color = texture2D(uTexture, vTexCoord);
    float gray = (color.r + color.g + color.b) / 3.0;
    gl_FragColor = vec4(vec3(gray), 1.0);
}