package com.mojang.math;

import com.mojang.datafixers.util.Pair;
import net.minecraft.util.Mth;
import org.apache.commons.lang3.tuple.Triple;
import org.joml.Matrix3f;
import org.joml.Matrix3fc;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Matrix4x3f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class MatrixUtil {
   private static final float G = 3.0F + 2.0F * (float)Math.sqrt(2.0D);
   private static final float CS = (float)Math.cos((Math.PI / 8D));
   private static final float SS = (float)Math.sin((Math.PI / 8D));

   private MatrixUtil() {
   }

   public static Matrix4f mulComponentWise(Matrix4f p_254173_, float p_253864_) {
      return p_254173_.set(p_254173_.m00() * p_253864_, p_254173_.m01() * p_253864_, p_254173_.m02() * p_253864_, p_254173_.m03() * p_253864_, p_254173_.m10() * p_253864_, p_254173_.m11() * p_253864_, p_254173_.m12() * p_253864_, p_254173_.m13() * p_253864_, p_254173_.m20() * p_253864_, p_254173_.m21() * p_253864_, p_254173_.m22() * p_253864_, p_254173_.m23() * p_253864_, p_254173_.m30() * p_253864_, p_254173_.m31() * p_253864_, p_254173_.m32() * p_253864_, p_254173_.m33() * p_253864_);
   }

   private static Pair<Float, Float> approxGivensQuat(float p_253741_, float p_253858_, float p_253941_) {
      float f = 2.0F * (p_253741_ - p_253941_);
      if (G * p_253858_ * p_253858_ < f * f) {
         float f1 = Mth.fastInvSqrt(p_253858_ * p_253858_ + f * f);
         return Pair.of(f1 * p_253858_, f1 * f);
      } else {
         return Pair.of(SS, CS);
      }
   }

   private static Pair<Float, Float> qrGivensQuat(float p_253897_, float p_254413_) {
      float f = (float)Math.hypot((double)p_253897_, (double)p_254413_);
      float f1 = f > 1.0E-6F ? p_254413_ : 0.0F;
      float f2 = Math.abs(p_253897_) + Math.max(f, 1.0E-6F);
      if (p_253897_ < 0.0F) {
         float f3 = f1;
         f1 = f2;
         f2 = f3;
      }

      float f4 = Mth.fastInvSqrt(f2 * f2 + f1 * f1);
      f2 *= f4;
      f1 *= f4;
      return Pair.of(f1, f2);
   }

   private static Quaternionf stepJacobi(Matrix3f p_254176_) {
      Matrix3f matrix3f = new Matrix3f();
      Quaternionf quaternionf = new Quaternionf();
      if (p_254176_.m01 * p_254176_.m01 + p_254176_.m10 * p_254176_.m10 > 1.0E-6F) {
         Pair<Float, Float> pair = approxGivensQuat(p_254176_.m00, 0.5F * (p_254176_.m01 + p_254176_.m10), p_254176_.m11);
         Float f = pair.getFirst();
         Float f1 = pair.getSecond();
         Quaternionf quaternionf1 = new Quaternionf(0.0F, 0.0F, f, f1);
         float f2 = f1 * f1 - f * f;
         float f3 = -2.0F * f * f1;
         float f4 = f1 * f1 + f * f;
         quaternionf.mul(quaternionf1);
         matrix3f.m00 = f2;
         matrix3f.m11 = f2;
         matrix3f.m01 = -f3;
         matrix3f.m10 = f3;
         matrix3f.m22 = f4;
         p_254176_.mul(matrix3f);
         matrix3f.transpose();
         matrix3f.mul(p_254176_);
         p_254176_.set((Matrix3fc)matrix3f);
      }

      if (p_254176_.m02 * p_254176_.m02 + p_254176_.m20 * p_254176_.m20 > 1.0E-6F) {
         Pair<Float, Float> pair1 = approxGivensQuat(p_254176_.m00, 0.5F * (p_254176_.m02 + p_254176_.m20), p_254176_.m22);
         float f5 = -pair1.getFirst();
         Float f7 = pair1.getSecond();
         Quaternionf quaternionf2 = new Quaternionf(0.0F, f5, 0.0F, f7);
         float f9 = f7 * f7 - f5 * f5;
         float f11 = -2.0F * f5 * f7;
         float f13 = f7 * f7 + f5 * f5;
         quaternionf.mul(quaternionf2);
         matrix3f.m00 = f9;
         matrix3f.m22 = f9;
         matrix3f.m02 = f11;
         matrix3f.m20 = -f11;
         matrix3f.m11 = f13;
         p_254176_.mul(matrix3f);
         matrix3f.transpose();
         matrix3f.mul(p_254176_);
         p_254176_.set((Matrix3fc)matrix3f);
      }

      if (p_254176_.m12 * p_254176_.m12 + p_254176_.m21 * p_254176_.m21 > 1.0E-6F) {
         Pair<Float, Float> pair2 = approxGivensQuat(p_254176_.m11, 0.5F * (p_254176_.m12 + p_254176_.m21), p_254176_.m22);
         Float f6 = pair2.getFirst();
         Float f8 = pair2.getSecond();
         Quaternionf quaternionf3 = new Quaternionf(f6, 0.0F, 0.0F, f8);
         float f10 = f8 * f8 - f6 * f6;
         float f12 = -2.0F * f6 * f8;
         float f14 = f8 * f8 + f6 * f6;
         quaternionf.mul(quaternionf3);
         matrix3f.m11 = f10;
         matrix3f.m22 = f10;
         matrix3f.m12 = -f12;
         matrix3f.m21 = f12;
         matrix3f.m00 = f14;
         p_254176_.mul(matrix3f);
         matrix3f.transpose();
         matrix3f.mul(p_254176_);
         p_254176_.set((Matrix3fc)matrix3f);
      }

      return quaternionf;
   }

   public static Triple<Quaternionf, Vector3f, Quaternionf> svdDecompose(Matrix3f p_253947_) {
      Quaternionf quaternionf = new Quaternionf();
      Quaternionf quaternionf1 = new Quaternionf();
      Matrix3f matrix3f = new Matrix3f(p_253947_);
      matrix3f.transpose();
      matrix3f.mul(p_253947_);

      for(int i = 0; i < 5; ++i) {
         quaternionf1.mul(stepJacobi(matrix3f));
      }

      quaternionf1.normalize();
      Matrix3f matrix3f4 = new Matrix3f(p_253947_);
      matrix3f4.rotate(quaternionf1);
      float f = 1.0F;
      Pair<Float, Float> pair = qrGivensQuat(matrix3f4.m00, matrix3f4.m01);
      Float f1 = pair.getFirst();
      Float f2 = pair.getSecond();
      float f3 = f2 * f2 - f1 * f1;
      float f4 = -2.0F * f1 * f2;
      float f5 = f2 * f2 + f1 * f1;
      Quaternionf quaternionf2 = new Quaternionf(0.0F, 0.0F, f1, f2);
      quaternionf.mul(quaternionf2);
      Matrix3f matrix3f1 = new Matrix3f();
      matrix3f1.m00 = f3;
      matrix3f1.m11 = f3;
      matrix3f1.m01 = f4;
      matrix3f1.m10 = -f4;
      matrix3f1.m22 = f5;
      f *= f5;
      matrix3f1.mul(matrix3f4);
      pair = qrGivensQuat(matrix3f1.m00, matrix3f1.m02);
      float f6 = -pair.getFirst();
      Float f7 = pair.getSecond();
      float f8 = f7 * f7 - f6 * f6;
      float f9 = -2.0F * f6 * f7;
      float f10 = f7 * f7 + f6 * f6;
      Quaternionf quaternionf3 = new Quaternionf(0.0F, f6, 0.0F, f7);
      quaternionf.mul(quaternionf3);
      Matrix3f matrix3f2 = new Matrix3f();
      matrix3f2.m00 = f8;
      matrix3f2.m22 = f8;
      matrix3f2.m02 = -f9;
      matrix3f2.m20 = f9;
      matrix3f2.m11 = f10;
      f *= f10;
      matrix3f2.mul(matrix3f1);
      pair = qrGivensQuat(matrix3f2.m11, matrix3f2.m12);
      Float f11 = pair.getFirst();
      Float f12 = pair.getSecond();
      float f13 = f12 * f12 - f11 * f11;
      float f14 = -2.0F * f11 * f12;
      float f15 = f12 * f12 + f11 * f11;
      Quaternionf quaternionf4 = new Quaternionf(f11, 0.0F, 0.0F, f12);
      quaternionf.mul(quaternionf4);
      Matrix3f matrix3f3 = new Matrix3f();
      matrix3f3.m11 = f13;
      matrix3f3.m22 = f13;
      matrix3f3.m12 = f14;
      matrix3f3.m21 = -f14;
      matrix3f3.m00 = f15;
      f *= f15;
      matrix3f3.mul(matrix3f2);
      f = 1.0F / f;
      quaternionf.mul((float)Math.sqrt((double)f));
      Vector3f vector3f = new Vector3f(matrix3f3.m00 * f, matrix3f3.m11 * f, matrix3f3.m22 * f);
      return Triple.of(quaternionf, vector3f, quaternionf1);
   }

   public static Matrix4x3f toAffine(Matrix4f p_253900_) {
      float f = 1.0F / p_253900_.m33();
      return (new Matrix4x3f()).set((Matrix4fc)p_253900_).scaleLocal(f, f, f);
   }
}