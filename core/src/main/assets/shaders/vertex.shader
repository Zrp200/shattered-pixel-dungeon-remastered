uniform mat4 uCamera;
uniform mat4 uModel;
attribute vec4 aXYZW;
attribute vec2 aUV;
varying vec2 vUV;
void main() {
    gl_Position = uCamera * uModel * aXYZW;
    vUV = aUV;
}
