#version 330 core

layout (location = 0) in vec3 aPos;
layout (location = 1) in vec2 aUV;

out vec2 vUV;

uniform mat4 uProj;
uniform mat4 uView;
uniform mat4 uModel;

void main() {
  vUV = aUV;
  gl_Position = uProj * uView * uModel * vec4(aPos, 1.0);
  // gl_Position = vec4(aPos.xy, 0.0, 1.0);
}
