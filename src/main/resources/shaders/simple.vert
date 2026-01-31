#version 330 core

layout (location = 0) in vec3 aPos;
layout (location = 1) in vec3 aNorm;
layout (location = 2) in vec2 aUV;
layout (location = 3) in float aLayer;

uniform mat4 uProj;
uniform mat4 uView;
uniform mat4 uModel;
uniform mat4 uLightSpace;

out vec2 vUV;
out vec3 vNormal;
out vec2 vTexCoord;
out vec4 vPosLightSpace;

flat out int vLayer;

void main() {
  vec4 worldPos = uModel * vec4(aPos, 1.0);
  vPos = worldPos.xyz;

  vNormal = mat3(transpose(inverse(uModel))) * aNormal;

  vTexCoord = aTexCoord;
  vLayer = aLayer;

  vPosLightSpace = uLigthSpace * worldPos;

  gl_Position = uProj * uView * worldPos;
}
