#version 330 core

uniform sampler2DArray uTex;

in vec2 vUV;
flat in int vLayer;

out vec4 FragColor;

void main() {
  FragColor = texture(uTex, vec3(vUV, float(vLayer)));
  // FragColor = vec4(float(vLayer) / 300.0, 0.0, 0.0, 1.0);
  // FragColor = vec4(float(vLayer) / 288.0, 0.0, 0.0, 1.0);
}
