#version 330 core

in vec2 vUV;
in float fragDistance;

out vec4 FragColor;

uniform sampler2D uTex;
uniform vec3 uFogColor;
uniform float uFogStart;
uniform float uFogEnd;


void main() {
  vec4 texColor = texture(uTex, vUV);
  float fogFactor = clamp((uFogEnd - fragDistance) / (uFogEnd - uFogStart) , 0.0, 1.0);
  vec3 finalColor = mix(uFogColor, texColor.rgb, fogFactor);
  FragColor = vec4(finalColor, texColor.a);
}
