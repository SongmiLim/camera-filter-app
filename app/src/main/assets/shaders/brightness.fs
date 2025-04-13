// assets/shaders/brightness.fs
#extension GL_OES_EGL_image_external : require
precision mediump float;

uniform samplerExternalOES uTexture;
varying vec2 vTexCoord;

void main() {
    vec4 color = texture2D(uTexture, vTexCoord);
    gl_FragColor = vec4(min(color.rgb + 0.2, 1.0), 1.0);
}
