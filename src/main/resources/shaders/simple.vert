#version 330 core

layout (location = 0) in vec3 aPos;
layout (location = 1) in vec3 aNorm;
layout (location = 2) in vec2 aUV;
layout (location = 3) in float aLayer;

uniform mat4 uProj;
uniform mat4 uView;
uniform mat4 uModel;

out vec2 vUV;
flat out int vLayer;

void main() {
  vUV = aUV;
  vLayer = int(aLayer);
  gl_Position = uProj * uView * uModel * vec4(aPos, 1.0);
}
