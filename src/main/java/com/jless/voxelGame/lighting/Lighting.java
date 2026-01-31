package com.jless.voxelGame.lighting;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL14.*;
import static org.lwjgl.opengl.GL30.*;

import java.lang.Math;
import java.nio.*;

import org.lwjgl.*;

import org.joml.*;

import com.jless.voxelGame.*;

public class Lighting {

  private float timeOfDay = LightingConsts.START_TIME;
  private boolean shadowsEnabled = LightingConsts.ENABLE_SHADOWS;

  private final Matrix4f lightProj = new Matrix4f();
  private final Matrix4f lightView = new Matrix4f();
  private final Matrix4f lightSpaceMatrix = new Matrix4f();

  private final Vector3f sunDir = new Vector3f();
  private final Vector3f skyColor = new Vector3f();
  private final Vector3f lightColor = new Vector3f();
  private final Vector3f ambientColor = new Vector3f();

  private int shadowFrameBuffer = 0;
  private int shadowDepthTexture = 0;

  private final FloatBuffer matrixBuffer = BufferUtils.createFloatBuffer(16);
  private final FloatBuffer vectorBuffer = BufferUtils.createFloatBuffer(4);

  public Lighting() {
    updateLightingCalculations(0.0f);
  }

  public void initShadowMapping() {
    if(!shadowsEnabled) {
      System.out.println("Shadows disabled");
      return;
    }
    System.out.println("Initializing shadow mapping");
    shadowFrameBuffer = glGenFramebuffers();
    shadowDepthTexture = glGenTextures();
    glBindFramebuffer(GL_FRAMEBUFFER, shadowFrameBuffer);
    glBindTexture(GL_TEXTURE_2D, shadowDepthTexture);

    glTexImage2D(
      GL_TEXTURE_2D,
      0,
      GL_DEPTH_COMPONENT,
      LightingConsts.SHADOW_MAP_SIZE,
      LightingConsts.SHADOW_MAP_SIZE,
      0,
      GL_DEPTH_COMPONENT,
      GL_FLOAT,
      (ByteBuffer)null
    );

    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);

    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_COMPARE_MODE, GL_COMPARE_REF_TO_TEXTURE);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_COMPARE_FUNC, GL_LEQUAL);

    glFramebufferTexture2D(
      GL_FRAMEBUFFER,
      GL_DEPTH_ATTACHMENT,
      GL_TEXTURE_2D,
      shadowDepthTexture,
      0
    );

    glDrawBuffer(GL_NONE);
    glReadBuffer(GL_NONE);

    int status = glCheckFramebufferStatus(GL_FRAMEBUFFER);
    if(status != GL_FRAMEBUFFER_COMPLETE) {
      System.err.println("Err: Shadow framebuffer incomplete");
      shadowsEnabled = false;
    } else {
      System.out.println("Shadow mapping completed");
    }

    glBindFramebuffer(GL_FRAMEBUFFER, 0);
    glBindTexture(GL_TEXTURE_2D, 0);
  }

  public void update(float dt) {
    timeOfDay += dt / LightingConsts.DAY_LENGTH;

    if(timeOfDay >= 1.0f) {
      timeOfDay -= 1.0f;
    }

    updateLightingCalculations(dt);
  }

  private void updateLightingCalculations(float f) {
    float angle = timeOfDay * (float)Math.PI * 2.0f;
    float sunY = (float)Math.sin(angle);
    float sunX = (float)Math.cos(angle);

    sunDir.set(sunX, -sunY, LightingConsts.SUN_TILT).normalize();

    updateColors();
  }

  private void updateColors() {
    float sunHeight = -sunDir.y;

    if(sunHeight > 0) {
      float brightness = Math.min(1.0f, sunHeight * 1.5f);

      lightColor.set(
        brightness * LightingConsts.SUN_COLOR[0],
        brightness * LightingConsts.SUN_COLOR[1],
        brightness * LightingConsts.SUN_COLOR[2]
      );
    } else {
      float moonBrightness = Math.max(0.0f, -sunHeight * LightingConsts.MOON_MAX_BRIGHTNESS);

      lightColor.set(
        moonBrightness * LightingConsts.MOON_COLOR[0],
        moonBrightness * LightingConsts.MOON_COLOR[1],
        moonBrightness * LightingConsts.MOON_COLOR[2]
      );
    }

    float ambientStrength = lerp(
      LightingConsts.AMBIENT_NIGHT,
      LightingConsts.AMBIENT_DAY,
      Math.max(0.0f, sunHeight)
    );

    ambientColor.set(
      ambientStrength,
      ambientStrength * 0.9f,
      ambientStrength * 0.85f
    );

    if(sunHeight > 0.2f) {
      skyColor.set(
        LightingConsts.SKY_DAY[0],
        LightingConsts.SKY_DAY[1],
        LightingConsts.SKY_DAY[2]
      );
    } else if(sunHeight > -0.1f) {
      float t = (sunHeight + 0.1f) / 0.3f;

      skyColor.set(
        lerp(LightingConsts.SKY_SUNSET[0], LightingConsts.SKY_SUNSET[0], t),
        lerp(LightingConsts.SKY_SUNSET[1], LightingConsts.SKY_SUNSET[1], t),
        lerp(LightingConsts.SKY_SUNSET[2], LightingConsts.SKY_SUNSET[2], t)
      );
    } else {
      float nightness = Math.min(1.0f, -sunHeight * 3.0f);

      skyColor.set(
        LightingConsts.SKY_NIGHT[0] * (1.0f - nightness * 0.5f),
        LightingConsts.SKY_NIGHT[1] * (1.0f - nightness * 0.5f),
        LightingConsts.SKY_NIGHT[2] * (1.0f - nightness * 0.5f)
      );
    }
  }

  public void beginShadowPass(Vector3f playerPos) {
    if(!shadowsEnabled) return;

    glBindFramebuffer(GL_FRAMEBUFFER, shadowFrameBuffer);
    glClear(GL_DEPTH_BUFFER_BIT);

    glViewport(0, 0, LightingConsts.SHADOW_MAP_SIZE, LightingConsts.SHADOW_MAP_SIZE);

    float shadowSize = LightingConsts.SHADOW_FRUSTUM_SIZE;
    lightProj.setOrtho(
      -shadowSize, shadowSize,
      -shadowSize, shadowSize,
      LightingConsts.SHADOW_NEAR,
      LightingConsts.SHADOW_FAR
    );

    Vector3f lightPosition = new Vector3f(sunDir).mul(-150.0f).add(playerPos);

    Vector3f lookAt = new Vector3f(playerPos);
    Vector3f up = new Vector3f(0, 1, 0);
    if(Math.abs(sunDir.dot(up)) > 0.999f) {
      up = new Vector3f(1, 0, 0);
    }

    lightView.setLookAt(lightPosition, lookAt, up);
    lightSpaceMatrix.set(lightProj).mul(lightView);
  }

  public Matrix4f getLightViewMatrix() {
    return new Matrix4f(lightView);
  }

  public Matrix4f getLightProjMatrix() {
    return new Matrix4f(lightProj);
  }

  public void endShadowPass(int wWidth, int wHeight) {
    if(!shadowsEnabled) return;

    glBindFramebuffer(GL_FRAMEBUFFER, 0);
    glViewport(0, 0, wWidth, wHeight);
  }

  public void bindShadowMap(int texUnit) {
    if(!shadowsEnabled) return;

    glActiveTexture(GL_TEXTURE0 + texUnit);
    glBindTexture(GL_TEXTURE_2D, shadowDepthTexture);
  }

  public Vector3f getSunDir() {
    return new Vector3f(sunDir);
  }

  public Vector3f getLightColor() {
    return new Vector3f(lightColor);
  }

  public Vector3f getAmbientColor() {
    return new Vector3f(ambientColor);
  }

  public Vector3f getSkyColor() {
    return new Vector3f(skyColor);
  }

  public Matrix4f getLightSpaceMatrix() {
    return new Matrix4f(lightSpaceMatrix);
  }

  public float getTimeOfDay() {
    return timeOfDay;
  }

  public boolean areShadowsEnabled() {
    return shadowsEnabled;
  }

  public void setTimeOfDay(float time) {
    this.timeOfDay = Math.max(0.0f, Math.min(1.0f, time));
    updateLightingCalculations(0.0f);
  }

  public void setShadowsEnabled(boolean enabled) {
    this.shadowsEnabled = enabled;
  }

  public void cleanup() {
    if(shadowDepthTexture != 0) {
      glDeleteTextures(shadowDepthTexture);
      shadowDepthTexture = 0;
    }
    if(shadowFrameBuffer != 0) {
      glDeleteFramebuffers(shadowFrameBuffer);
      shadowFrameBuffer = 0;
    }
    System.out.println("lighting cleaned");
  }

  private float lerp(float a, float b, float t) {
    t = Math.max(0.0f, Math.min(1.0f, t));
    return a + (b - a) * t;
  }
}
