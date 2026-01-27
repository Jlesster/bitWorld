#version 330 core

layout (location = 0) in vec3 aPos;
layout (location = 1) in vec3 aNorm;
layout (location = 2) in vec2 aUV;

out vec3 vNormal;
out vec2 vUV;
out float fragDistance;

uniform mat4 uProj;
uniform mat4 uView;
uniform mat4 uModel;
uniform vec3 uCameraPos;

void main() {
  vUV = aUV;
  vNormal = mat3(transpose(inverse(uModel))) * aNorm;
  vec4 worldPos = uModel * vec4(aPos, 1.0);
  fragDistance = distance(worldPos.xyz, uCameraPos);
  gl_Position = uProj * uView * uModel * vec4(aPos, 1.0);
}
