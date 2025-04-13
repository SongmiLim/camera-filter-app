#extension GL_OES_EGL_image_external : require
precision mediump float;

uniform samplerExternalOES uTexture;
varying vec2 vTexCoord;

void main() {
    vec4 color = texture2D(uTexture, vTexCoord);
    float brightness = 0.5;
    color.rgb += brightness;
    gl_FragColor = vec4(color.rgb, 1.0);
}