#version 330 core

uniform sampler2DArray uTex;
uniform sampler2D uShadowMap;
uniform vec3 uSunDir;
uniform vec3 uLightColor;
uniform vec3 uAmbientColor;
uniform bool uShadowsEnabled;


in vec3 vPos;
in vec2 vUV;
in vec3 vNormal;
in vec2 vTexCoord;
in vec4 vPosLightSpace;
flat in int vLayer;

out vec4 FragColor;

float calculateShadow() {
  if(!uShadowsEnabled) return 1.0;

  // Perspective divide
  vec3 projCoords = vPosLightSpace.xyz / vPosLightSpace.w;

  // Transform to [0,1] range (NDC to texture coordinates)
  projCoords = projCoords * 0.5 + 0.5;

  // Check if fragment is outside shadow frustum
  if(projCoords.z > 1.0 || projCoords.x < 0.0 || projCoords.x > 1.0 ||
     projCoords.y < 0.0 || projCoords.y > 1.0) {
    return 1.0;  // No shadow outside frustum
  }

  // Get current fragment depth from light's perspective
  float currentDepth = projCoords.z;

  // Calculate bias based on surface angle to light
  vec3 normal = normalize(vNormal);
  float cosTheta = clamp(dot(normal, -uSunDir), 0.0, 1.0);
  float bias = max(0.005 * (1.0 - cosTheta), 0.001);

  // PCF (Percentage Closer Filtering) for soft shadows
  float shadow = 0.0;
  vec2 texelSize = 1.0 / textureSize(uShadowMap, 0);

  for(int x = -1; x <= 1; ++x) {
    for(int y = -1; y <= 1; ++y) {
      vec2 offset = vec2(x, y) * texelSize;
      // Using texture() with sampler2DShadow automatically does depth comparison
      float pcfDepth = texture(uShadowMap, projCoords.xy + offset).r;

      // If current depth (minus bias) is greater than stored depth, we're in shadow
      shadow += (currentDepth - bias) > pcfDepth ? 0.0 : 1.0;
    }
  }
  shadow /= 9.0;

  return shadow;
}

void main() {
  vec4 texColor = texture(uTex, vec3(vUV, float(vLayer)));

  if(texColor.a < 0.1) discard;

  vec3 normal = normalize(vNormal);

  // Calculate diffuse lighting (N dot L)
  float diffuse = max(dot(normal, -uSunDir), 0.0);

  // Calculate shadow factor (1.0 = fully lit, 0.0 = fully shadowed)
  float shadow = calculateShadow();

  // Ambient light is always present
  vec3 ambient = uAmbientColor;

  // Diffuse light is affected by shadows
  vec3 lighting = ambient + (uLightColor * diffuse * shadow);

  vec3 finalColor = texColor.rgb * lighting;

  FragColor = vec4(finalColor, texColor.a);
}
