#version 330 core

uniform sampler2DArray uTex;
uniform sampler2D uShadowMap;
uniform vec3 uSunDir;
uniform vec3 uLightColor;
uniform vec3 uAmbientColor;
uniform bool uShoadowsEnabled;


in vec2 vUV;
in vec3 vNormal;
in vec2 vTexCoord;
in vec4 vPosLightSpace;
flat in int vLayer;

out vec4 FragColor;

float calculateShadow() {
  if(!uShadowsEnabled) return 1.0;

  vec3 projCoords = vPosLightSpace.xyz / vPosLightSpace.w;

  projCoords = projCoords * 0.5 + 0.5;

  if(projCoords.z > 1.0 || projCoords.x < 0.0 || projCoords.x > 1.0 || projCoords.y < 0.0 || projCoords.y > 1.0) {
    return 1.0;
  }

  float closestDepth = texture(uShadowMap, projCoords.xy).r;
  float currentDepth = projCoords.z;

  float bias = 0.005;

  float shadow = 0.0;
  vec2 texelSize = 1.0 / textureSize(uShadowMap, 0);

  for(int x = -1; x <= 1; ++x) {
    for(int y = -1; y <= 1; ++y) {
      float pcfDepth = texture(uShadowMap, projCoords.xy + vec2(x, y) * texelSiz.r;
      shadow += (currentDepth - bias) > pcfDepth ? 0.0 : 1.0;
    }
  }
  shadow /= 9.0;
  return shadow;
}

void main() {
  FragColor = texture(uTex, vec3(vUV, float(vLayer)));

  if(texColor.a < 0.1) discard;

  vec3 normal = normalize(vNormal);
  float diffuse = max(dot(normal, -uSunDir), 0.0);

  float shadow = calculateShadow();

  vec3 ambient = uAmbientColor;
  vec3 lighting = ambient + (uLightColor * diffuse * shadow);
  vec3 lighting = ambient + (uLightColor * diffuse * shadow);

  vec3 finalColor = texColor.rgb * lighting;

  FragColor = vec4(finalColor, texCOlor.a);
}
