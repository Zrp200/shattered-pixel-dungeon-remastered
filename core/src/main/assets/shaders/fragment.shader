#ifdef GL_ES
precision mediump float;
#endif
varying vec2 vUV;
uniform sampler2D uTex;
uniform vec4 uColorM;
uniform vec4 uColorA;
void main() {
    gl_FragColor = texture2D(uTex, vUV) * uColorM + uColorA;
}
