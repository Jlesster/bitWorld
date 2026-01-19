package com.jless.voxelGame.render;

import org.joml.Matrix4f;
import org.joml.Vector3f;

public class Camera {

  private final Matrix4f projection = new Matrix4f();
  private final Matrix4f view = new Matrix4f();

  private final Vector3f forward = new Vector3f();
  private final Vector3f right = new Vector3f();
  private final Vector3f up = new Vector3f(0, 1, 0);

  public Camera() {}

  public void setGluPersp(float fovDeg, float aspect, float near, float far) {
    projection.identity().perspective((float)Math.toRadians(fovDeg), aspect, near, far);
  }

  public void updateView(Vector3f pos, float yawDeg, float pitchDeg) {
    forward.set(
      (float)(Math.cos(Math.toRadians(yawDeg)) * Math.cos(Math.toRadians(pitchDeg))),
      (float)(Math.sin(Math.toRadians(pitchDeg))),
      (float)(Math.sin(Math.toRadians(yawDeg)) * Math.cos(Math.toRadians(pitchDeg)))
    ).normalize();

    right.set(forward).cross(up).normalize();

    Vector3f center = new Vector3f(pos).add(forward);
    view.identity().lookAt(pos, center, up);
  }

  public Matrix4f projection() { return projection; }
  public Matrix4f view() { return view; }

  public Vector3f forward() { return forward; }
  public Vector3f right() { return right; }
}
