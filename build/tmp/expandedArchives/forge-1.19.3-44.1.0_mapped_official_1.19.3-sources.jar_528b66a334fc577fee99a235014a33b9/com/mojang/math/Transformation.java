package com.mojang.math;

import java.util.Objects;
import javax.annotation.Nullable;
import net.minecraft.Util;
import org.apache.commons.lang3.tuple.Triple;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Matrix4x3f;
import org.joml.Matrix4x3fc;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector3fc;

public final class Transformation implements net.minecraftforge.client.extensions.IForgeTransformation {
   private final Matrix4f matrix;
   private boolean decomposed;
   @Nullable
   private Vector3f translation;
   @Nullable
   private Quaternionf leftRotation;
   @Nullable
   private Vector3f scale;
   @Nullable
   private Quaternionf rightRotation;
   private static final Transformation IDENTITY = Util.make(() -> {
      Transformation transformation = new Transformation(new Matrix4f());
      transformation.getLeftRotation();
      return transformation;
   });

   public Transformation(@Nullable Matrix4f p_253689_) {
      if (p_253689_ == null) {
         this.matrix = IDENTITY.matrix;
      } else {
         this.matrix = p_253689_;
      }

   }

   public Transformation(@Nullable Vector3f p_253831_, @Nullable Quaternionf p_253846_, @Nullable Vector3f p_254502_, @Nullable Quaternionf p_253912_) {
      this.matrix = compose(p_253831_, p_253846_, p_254502_, p_253912_);
      this.translation = p_253831_ != null ? p_253831_ : new Vector3f();
      this.leftRotation = p_253846_ != null ? p_253846_ : new Quaternionf();
      this.scale = p_254502_ != null ? p_254502_ : new Vector3f(1.0F, 1.0F, 1.0F);
      this.rightRotation = p_253912_ != null ? p_253912_ : new Quaternionf();
      this.decomposed = true;
   }

   public static Transformation identity() {
      return IDENTITY;
   }

   public Transformation compose(Transformation p_121097_) {
      Matrix4f matrix4f = this.getMatrix();
      matrix4f.mul(p_121097_.getMatrix());
      return new Transformation(matrix4f);
   }

   @Nullable
   public Transformation inverse() {
      if (this == IDENTITY) {
         return this;
      } else {
         Matrix4f matrix4f = this.getMatrix().invert();
         return matrix4f.isFinite() ? new Transformation(matrix4f) : null;
      }
   }

   private void ensureDecomposed() {
      if (!this.decomposed) {
         Matrix4x3f matrix4x3f = MatrixUtil.toAffine(this.matrix);
         Triple<Quaternionf, Vector3f, Quaternionf> triple = MatrixUtil.svdDecompose((new Matrix3f()).set((Matrix4x3fc)matrix4x3f));
         this.translation = matrix4x3f.getTranslation(new Vector3f());
         this.leftRotation = new Quaternionf(triple.getLeft());
         this.scale = new Vector3f(triple.getMiddle());
         this.rightRotation = new Quaternionf(triple.getRight());
         this.decomposed = true;
      }

   }

   private static Matrix4f compose(@Nullable Vector3f p_254465_, @Nullable Quaternionf p_254416_, @Nullable Vector3f p_254499_, @Nullable Quaternionf p_254334_) {
      Matrix4f matrix4f = new Matrix4f();
      if (p_254465_ != null) {
         matrix4f.translation(p_254465_);
      }

      if (p_254416_ != null) {
         matrix4f.rotate(p_254416_);
      }

      if (p_254499_ != null) {
         matrix4f.scale(p_254499_);
      }

      if (p_254334_ != null) {
         matrix4f.rotate(p_254334_);
      }

      return matrix4f;
   }

   public Matrix4f getMatrix() {
      return new Matrix4f(this.matrix);
   }

   public Vector3f getTranslation() {
      this.ensureDecomposed();
      return new Vector3f((Vector3fc)this.translation);
   }

   public Quaternionf getLeftRotation() {
      this.ensureDecomposed();
      return new Quaternionf(this.leftRotation);
   }

   public Vector3f getScale() {
      this.ensureDecomposed();
      return new Vector3f((Vector3fc)this.scale);
   }

   public Quaternionf getRightRotation() {
      this.ensureDecomposed();
      return new Quaternionf(this.rightRotation);
   }

   public boolean equals(Object p_121108_) {
      if (this == p_121108_) {
         return true;
      } else if (p_121108_ != null && this.getClass() == p_121108_.getClass()) {
         Transformation transformation = (Transformation)p_121108_;
         return Objects.equals(this.matrix, transformation.matrix);
      } else {
         return false;
      }
   }

   public int hashCode() {
      return Objects.hash(this.matrix);
   }

    private Matrix3f normalTransform = null;
    public Matrix3f getNormalMatrix() {
        checkNormalTransform();
        return normalTransform;
    }
    private void checkNormalTransform() {
        if (normalTransform == null) {
            normalTransform = new Matrix3f(matrix);
            normalTransform.invert();
            normalTransform.transpose();
        }
    }

   public Transformation slerp(Transformation p_175938_, float p_175939_) {
      Vector3f vector3f = this.getTranslation();
      Quaternionf quaternionf = this.getLeftRotation();
      Vector3f vector3f1 = this.getScale();
      Quaternionf quaternionf1 = this.getRightRotation();
      vector3f.lerp(p_175938_.getTranslation(), p_175939_);
      quaternionf.slerp(p_175938_.getLeftRotation(), p_175939_);
      vector3f1.lerp(p_175938_.getScale(), p_175939_);
      quaternionf1.slerp(p_175938_.getRightRotation(), p_175939_);
      return new Transformation(vector3f, quaternionf, vector3f1, quaternionf1);
   }
}
