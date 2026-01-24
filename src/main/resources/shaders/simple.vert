#version 330 core

layout (location = 0) in vec3 aPos;
layout (location = 1) in vec3 aNorm;
layout (location = 2) in vec2 aUV;

out vec3 vNormal;
out vec2 vUV;

uniform mat4 uProj;
uniform mat4 uView;
uniform mat4 uModel;

void main() {
  vUV = aUV;
  vNormal = mat3(transpose(inverse(uModel))) * aNorm;
  gl_Position = uProj * uView * uModel * vec4(aPos, 1.0);
}
