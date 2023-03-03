package net.minecraft.util;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableList;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Decoder;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Base64;
import java.util.BitSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Stream;
import net.minecraft.Util;
import net.minecraft.core.HolderSet;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.lang3.mutable.MutableObject;
import org.joml.Vector3f;

public class ExtraCodecs {
   public static final Codec<JsonElement> JSON = Codec.PASSTHROUGH.xmap((p_253507_) -> {
      return p_253507_.convert(JsonOps.INSTANCE).getValue();
   }, (p_253513_) -> {
      return new Dynamic<>(JsonOps.INSTANCE, p_253513_);
   });
   public static final Codec<Component> COMPONENT = JSON.flatXmap((p_253516_) -> {
      try {
         return DataResult.success(Component.Serializer.fromJson(p_253516_));
      } catch (JsonParseException jsonparseexception) {
         return DataResult.error(jsonparseexception.getMessage());
      }
   }, (p_253498_) -> {
      try {
         return DataResult.success(Component.Serializer.toJsonTree(p_253498_));
      } catch (IllegalArgumentException illegalargumentexception) {
         return DataResult.error(illegalargumentexception.getMessage());
      }
   });
   public static final Codec<Vector3f> VECTOR3F = Codec.FLOAT.listOf().comapFlatMap((p_253502_) -> {
      return Util.fixedSize(p_253502_, 3).map((p_253489_) -> {
         return new Vector3f(p_253489_.get(0), p_253489_.get(1), p_253489_.get(2));
      });
   }, (p_253503_) -> {
      return ImmutableList.of(p_253503_.x(), p_253503_.y(), p_253503_.z());
   });
   public static final Codec<Integer> NON_NEGATIVE_INT = intRangeWithMessage(0, Integer.MAX_VALUE, (p_184429_) -> {
      return "Value must be non-negative: " + p_184429_;
   });
   public static final Codec<Integer> POSITIVE_INT = intRangeWithMessage(1, Integer.MAX_VALUE, (p_184375_) -> {
      return "Value must be positive: " + p_184375_;
   });
   public static final Codec<Float> POSITIVE_FLOAT = floatRangeMinExclusiveWithMessage(0.0F, Float.MAX_VALUE, (p_184373_) -> {
      return "Value must be positive: " + p_184373_;
   });
   public static final Codec<Pattern> PATTERN = Codec.STRING.comapFlatMap((p_216188_) -> {
      try {
         return DataResult.success(Pattern.compile(p_216188_));
      } catch (PatternSyntaxException patternsyntaxexception) {
         return DataResult.error("Invalid regex pattern '" + p_216188_ + "': " + patternsyntaxexception.getMessage());
      }
   }, Pattern::pattern);
   public static final Codec<Instant> INSTANT_ISO8601 = instantCodec(DateTimeFormatter.ISO_INSTANT);
   public static final Codec<byte[]> BASE64_STRING = Codec.STRING.comapFlatMap((p_216184_) -> {
      try {
         return DataResult.success(Base64.getDecoder().decode(p_216184_));
      } catch (IllegalArgumentException illegalargumentexception) {
         return DataResult.error("Malformed base64 string");
      }
   }, (p_216180_) -> {
      return Base64.getEncoder().encodeToString(p_216180_);
   });
   public static final Codec<ExtraCodecs.TagOrElementLocation> TAG_OR_ELEMENT_ID = Codec.STRING.comapFlatMap((p_216169_) -> {
      return p_216169_.startsWith("#") ? ResourceLocation.read(p_216169_.substring(1)).map((p_216182_) -> {
         return new ExtraCodecs.TagOrElementLocation(p_216182_, true);
      }) : ResourceLocation.read(p_216169_).map((p_216165_) -> {
         return new ExtraCodecs.TagOrElementLocation(p_216165_, false);
      });
   }, ExtraCodecs.TagOrElementLocation::decoratedId);
   public static final Function<Optional<Long>, OptionalLong> toOptionalLong = (p_216176_) -> {
      return p_216176_.map(OptionalLong::of).orElseGet(OptionalLong::empty);
   };
   public static final Function<OptionalLong, Optional<Long>> fromOptionalLong = (p_216178_) -> {
      return p_216178_.isPresent() ? Optional.of(p_216178_.getAsLong()) : Optional.empty();
   };
   public static final Codec<BitSet> BIT_SET = Codec.LONG_STREAM.xmap((p_253514_) -> {
      return BitSet.valueOf(p_253514_.toArray());
   }, (p_253493_) -> {
      return Arrays.stream(p_253493_.toLongArray());
   });
   private static final Codec<Property> PROPERTY = RecordCodecBuilder.create((p_253491_) -> {
      return p_253491_.group(Codec.STRING.fieldOf("name").forGetter(Property::getName), Codec.STRING.fieldOf("value").forGetter(Property::getValue), Codec.STRING.optionalFieldOf("signature").forGetter((p_253490_) -> {
         return Optional.ofNullable(p_253490_.getSignature());
      })).apply(p_253491_, (p_253494_, p_253495_, p_253496_) -> {
         return new Property(p_253494_, p_253495_, p_253496_.orElse((String)null));
      });
   });
   @VisibleForTesting
   public static final Codec<PropertyMap> PROPERTY_MAP = Codec.either(Codec.unboundedMap(Codec.STRING, Codec.STRING.listOf()), PROPERTY.listOf()).xmap((p_253515_) -> {
      PropertyMap propertymap = new PropertyMap();
      p_253515_.ifLeft((p_253506_) -> {
         p_253506_.forEach((p_253500_, p_253501_) -> {
            for(String s : p_253501_) {
               propertymap.put(p_253500_, new Property(p_253500_, s));
            }

         });
      }).ifRight((p_253509_) -> {
         for(Property property : p_253509_) {
            propertymap.put(property.getName(), property);
         }

      });
      return propertymap;
   }, (p_253504_) -> {
      return Either.right(p_253504_.values().stream().toList());
   });
   public static final Codec<GameProfile> GAME_PROFILE = RecordCodecBuilder.create((p_253497_) -> {
      return p_253497_.group(Codec.mapPair(UUIDUtil.AUTHLIB_CODEC.xmap(Optional::of, (p_253517_) -> {
         return p_253517_.orElse((UUID)null);
      }).optionalFieldOf("id", Optional.empty()), Codec.STRING.xmap(Optional::of, (p_253492_) -> {
         return p_253492_.orElse((String)null);
      }).optionalFieldOf("name", Optional.empty())).flatXmap(ExtraCodecs::mapIdNameToGameProfile, ExtraCodecs::mapGameProfileToIdName).forGetter(Function.identity()), PROPERTY_MAP.optionalFieldOf("properties", new PropertyMap()).forGetter(GameProfile::getProperties)).apply(p_253497_, (p_253518_, p_253519_) -> {
         p_253519_.forEach((p_253511_, p_253512_) -> {
            p_253518_.getProperties().put(p_253511_, p_253512_);
         });
         return p_253518_;
      });
   });

   public static <F, S> Codec<Either<F, S>> xor(Codec<F> p_144640_, Codec<S> p_144641_) {
      return new ExtraCodecs.XorCodec<>(p_144640_, p_144641_);
   }

   public static <P, I> Codec<I> intervalCodec(Codec<P> p_184362_, String p_184363_, String p_184364_, BiFunction<P, P, DataResult<I>> p_184365_, Function<I, P> p_184366_, Function<I, P> p_184367_) {
      Codec<I> codec = Codec.list(p_184362_).comapFlatMap((p_184398_) -> {
         return Util.fixedSize(p_184398_, 2).flatMap((p_184445_) -> {
            P p = p_184445_.get(0);
            P p1 = p_184445_.get(1);
            return p_184365_.apply(p, p1);
         });
      }, (p_184459_) -> {
         return ImmutableList.of(p_184366_.apply(p_184459_), p_184367_.apply(p_184459_));
      });
      Codec<I> codec1 = RecordCodecBuilder.<Pair<P,P>>create((p_184360_) -> {
         return p_184360_.group(p_184362_.fieldOf(p_184363_).forGetter(Pair::getFirst), p_184362_.fieldOf(p_184364_).forGetter(Pair::getSecond)).apply(p_184360_, Pair::of);
      }).comapFlatMap((p_184392_) -> {
         return p_184365_.apply((P)p_184392_.getFirst(), (P)p_184392_.getSecond());
      }, (p_184449_) -> {
         return Pair.of(p_184366_.apply(p_184449_), p_184367_.apply(p_184449_));
      });
      Codec<I> codec2 = (new ExtraCodecs.EitherCodec<>(codec, codec1)).xmap((p_184355_) -> {
         return p_184355_.map((p_184461_) -> {
            return p_184461_;
         }, (p_184455_) -> {
            return p_184455_;
         });
      }, Either::left);
      return Codec.either(p_184362_, codec2).comapFlatMap((p_184389_) -> {
         return p_184389_.map((p_184395_) -> {
            return p_184365_.apply(p_184395_, p_184395_);
         }, DataResult::success);
      }, (p_184411_) -> {
         P p = p_184366_.apply(p_184411_);
         P p1 = p_184367_.apply(p_184411_);
         return Objects.equals(p, p1) ? Either.left(p) : Either.right(p_184411_);
      });
   }

   public static <A> Codec.ResultFunction<A> orElsePartial(final A p_184382_) {
      return new Codec.ResultFunction<A>() {
         public <T> DataResult<Pair<A, T>> apply(DynamicOps<T> p_184466_, T p_184467_, DataResult<Pair<A, T>> p_184468_) {
            MutableObject<String> mutableobject = new MutableObject<>();
            Optional<Pair<A, T>> optional = p_184468_.resultOrPartial(mutableobject::setValue);
            return optional.isPresent() ? p_184468_ : DataResult.error("(" + (String)mutableobject.getValue() + " -> using default)", Pair.of(p_184382_, p_184467_));
         }

         public <T> DataResult<T> coApply(DynamicOps<T> p_184470_, A p_184471_, DataResult<T> p_184472_) {
            return p_184472_;
         }

         public String toString() {
            return "OrElsePartial[" + p_184382_ + "]";
         }
      };
   }

   public static <E> Codec<E> idResolverCodec(ToIntFunction<E> p_184422_, IntFunction<E> p_184423_, int p_184424_) {
      return Codec.INT.flatXmap((p_184414_) -> {
         return Optional.ofNullable(p_184423_.apply(p_184414_)).map(DataResult::success).orElseGet(() -> {
            return DataResult.error("Unknown element id: " + p_184414_);
         });
      }, (p_184420_) -> {
         int i = p_184422_.applyAsInt(p_184420_);
         return i == p_184424_ ? DataResult.error("Element with unknown id: " + p_184420_) : DataResult.success(i);
      });
   }

   public static <E> Codec<E> stringResolverCodec(Function<E, String> p_184406_, Function<String, E> p_184407_) {
      return Codec.STRING.flatXmap((p_184404_) -> {
         return Optional.ofNullable(p_184407_.apply(p_184404_)).map(DataResult::success).orElseGet(() -> {
            return DataResult.error("Unknown element name:" + p_184404_);
         });
      }, (p_184401_) -> {
         return Optional.ofNullable(p_184406_.apply(p_184401_)).map(DataResult::success).orElseGet(() -> {
            return DataResult.error("Element with unknown name: " + p_184401_);
         });
      });
   }

   public static <E> Codec<E> orCompressed(final Codec<E> p_184426_, final Codec<E> p_184427_) {
      return new Codec<E>() {
         public <T> DataResult<T> encode(E p_184483_, DynamicOps<T> p_184484_, T p_184485_) {
            return p_184484_.compressMaps() ? p_184427_.encode(p_184483_, p_184484_, p_184485_) : p_184426_.encode(p_184483_, p_184484_, p_184485_);
         }

         public <T> DataResult<Pair<E, T>> decode(DynamicOps<T> p_184480_, T p_184481_) {
            return p_184480_.compressMaps() ? p_184427_.decode(p_184480_, p_184481_) : p_184426_.decode(p_184480_, p_184481_);
         }

         public String toString() {
            return p_184426_ + " orCompressed " + p_184427_;
         }
      };
   }

   public static <E> Codec<E> overrideLifecycle(Codec<E> p_184369_, final Function<E, Lifecycle> p_184370_, final Function<E, Lifecycle> p_184371_) {
      return p_184369_.mapResult(new Codec.ResultFunction<E>() {
         public <T> DataResult<Pair<E, T>> apply(DynamicOps<T> p_184497_, T p_184498_, DataResult<Pair<E, T>> p_184499_) {
            return p_184499_.result().map((p_184495_) -> {
               return p_184499_.setLifecycle(p_184370_.apply(p_184495_.getFirst()));
            }).orElse(p_184499_);
         }

         public <T> DataResult<T> coApply(DynamicOps<T> p_184501_, E p_184502_, DataResult<T> p_184503_) {
            return p_184503_.setLifecycle(p_184371_.apply(p_184502_));
         }

         public String toString() {
            return "WithLifecycle[" + p_184370_ + " " + p_184371_ + "]";
         }
      });
   }

   private static <N extends Number & Comparable<N>> Function<N, DataResult<N>> checkRangeWithMessage(N p_144645_, N p_144646_, Function<N, String> p_144647_) {
      return (p_184438_) -> {
         return p_184438_.compareTo(p_144645_) >= 0 && p_184438_.compareTo(p_144646_) <= 0 ? DataResult.success(p_184438_) : DataResult.error(p_144647_.apply(p_184438_));
      };
   }

   private static Codec<Integer> intRangeWithMessage(int p_144634_, int p_144635_, Function<Integer, String> p_144636_) {
      Function<Integer, DataResult<Integer>> function = checkRangeWithMessage(p_144634_, p_144635_, p_144636_);
      return Codec.INT.flatXmap(function, function);
   }

   private static <N extends Number & Comparable<N>> Function<N, DataResult<N>> checkRangeMinExclusiveWithMessage(N p_184431_, N p_184432_, Function<N, String> p_184433_) {
      return (p_184380_) -> {
         return p_184380_.compareTo(p_184431_) > 0 && p_184380_.compareTo(p_184432_) <= 0 ? DataResult.success(p_184380_) : DataResult.error(p_184433_.apply(p_184380_));
      };
   }

   private static Codec<Float> floatRangeMinExclusiveWithMessage(float p_184351_, float p_184352_, Function<Float, String> p_184353_) {
      Function<Float, DataResult<Float>> function = checkRangeMinExclusiveWithMessage(p_184351_, p_184352_, p_184353_);
      return Codec.FLOAT.flatXmap(function, function);
   }

   public static <T> Function<List<T>, DataResult<List<T>>> nonEmptyListCheck() {
      return (p_184442_) -> {
         return p_184442_.isEmpty() ? DataResult.error("List must have contents") : DataResult.success(p_184442_);
      };
   }

   public static <T> Codec<List<T>> nonEmptyList(Codec<List<T>> p_144638_) {
      return p_144638_.flatXmap(nonEmptyListCheck(), nonEmptyListCheck());
   }

   public static <T> Function<HolderSet<T>, DataResult<HolderSet<T>>> nonEmptyHolderSetCheck() {
      return (p_203975_) -> {
         return p_203975_.unwrap().right().filter(List::isEmpty).isPresent() ? DataResult.error("List must have contents") : DataResult.success(p_203975_);
      };
   }

   public static <T> Codec<HolderSet<T>> nonEmptyHolderSet(Codec<HolderSet<T>> p_203983_) {
      return p_203983_.flatXmap(nonEmptyHolderSetCheck(), nonEmptyHolderSetCheck());
   }

   public static <A> Codec<A> lazyInitializedCodec(Supplier<Codec<A>> p_184416_) {
      return new ExtraCodecs.LazyInitializedCodec<>(p_184416_);
   }

   public static <E> MapCodec<E> retrieveContext(final Function<DynamicOps<?>, DataResult<E>> p_203977_) {
      class ContextRetrievalCodec extends MapCodec<E> {
         public <T> RecordBuilder<T> encode(E p_203993_, DynamicOps<T> p_203994_, RecordBuilder<T> p_203995_) {
            return p_203995_;
         }

         public <T> DataResult<E> decode(DynamicOps<T> p_203990_, MapLike<T> p_203991_) {
            return p_203977_.apply(p_203990_);
         }

         public String toString() {
            return "ContextRetrievalCodec[" + p_203977_ + "]";
         }

         public <T> Stream<T> keys(DynamicOps<T> p_203997_) {
            return Stream.empty();
         }
      }

      return new ContextRetrievalCodec();
   }

   public static <E, L extends Collection<E>, T> Function<L, DataResult<L>> ensureHomogenous(Function<E, T> p_203985_) {
      return (p_203980_) -> {
         Iterator<E> iterator = p_203980_.iterator();
         if (iterator.hasNext()) {
            T t = p_203985_.apply(iterator.next());

            while(iterator.hasNext()) {
               E e = iterator.next();
               T t1 = p_203985_.apply(e);
               if (t1 != t) {
                  return DataResult.error("Mixed type list: element " + e + " had type " + t1 + ", but list is of type " + t);
               }
            }
         }

         return DataResult.success(p_203980_, Lifecycle.stable());
      };
   }

   public static <A> Codec<A> catchDecoderException(final Codec<A> p_216186_) {
      return Codec.of(p_216186_, new Decoder<A>() {
         public <T> DataResult<Pair<A, T>> decode(DynamicOps<T> p_216193_, T p_216194_) {
            try {
               return p_216186_.decode(p_216193_, p_216194_);
            } catch (Exception exception) {
               return DataResult.error("Cauch exception decoding " + p_216194_ + ": " + exception.getMessage());
            }
         }
      });
   }

   public static Codec<Instant> instantCodec(DateTimeFormatter p_216171_) {
      return Codec.STRING.comapFlatMap((p_216174_) -> {
         try {
            return DataResult.success(Instant.from(p_216171_.parse(p_216174_)));
         } catch (Exception exception) {
            return DataResult.error(exception.getMessage());
         }
      }, p_216171_::format);
   }

   public static MapCodec<OptionalLong> asOptionalLong(MapCodec<Optional<Long>> p_216167_) {
      return p_216167_.xmap(toOptionalLong, fromOptionalLong);
   }

   private static DataResult<GameProfile> mapIdNameToGameProfile(Pair<Optional<UUID>, Optional<String>> p_253764_) {
      try {
         return DataResult.success(new GameProfile(p_253764_.getFirst().orElse((UUID)null), p_253764_.getSecond().orElse((String)null)));
      } catch (Throwable throwable) {
         return DataResult.error(throwable.getMessage());
      }
   }

   private static DataResult<Pair<Optional<UUID>, Optional<String>>> mapGameProfileToIdName(GameProfile p_254220_) {
      return DataResult.success(Pair.of(Optional.ofNullable(p_254220_.getId()), Optional.ofNullable(p_254220_.getName())));
   }

   public static final class EitherCodec<F, S> implements Codec<Either<F, S>> {
      private final Codec<F> first;
      private final Codec<S> second;

      public EitherCodec(Codec<F> p_184508_, Codec<S> p_184509_) {
         this.first = p_184508_;
         this.second = p_184509_;
      }

      public <T> DataResult<Pair<Either<F, S>, T>> decode(DynamicOps<T> p_184530_, T p_184531_) {
         DataResult<Pair<Either<F, S>, T>> dataresult = this.first.decode(p_184530_, p_184531_).map((p_184524_) -> {
            return p_184524_.mapFirst(Either::left);
         });
         if (!dataresult.error().isPresent()) {
            return dataresult;
         } else {
            DataResult<Pair<Either<F, S>, T>> dataresult1 = this.second.decode(p_184530_, p_184531_).map((p_184515_) -> {
               return p_184515_.mapFirst(Either::right);
            });
            return !dataresult1.error().isPresent() ? dataresult1 : dataresult.apply2((p_184517_, p_184518_) -> {
               return p_184518_;
            }, dataresult1);
         }
      }

      public <T> DataResult<T> encode(Either<F, S> p_184511_, DynamicOps<T> p_184512_, T p_184513_) {
         return p_184511_.map((p_184528_) -> {
            return this.first.encode(p_184528_, p_184512_, p_184513_);
         }, (p_184522_) -> {
            return this.second.encode(p_184522_, p_184512_, p_184513_);
         });
      }

      public boolean equals(Object p_184537_) {
         if (this == p_184537_) {
            return true;
         } else if (p_184537_ != null && this.getClass() == p_184537_.getClass()) {
            ExtraCodecs.EitherCodec<?, ?> eithercodec = (ExtraCodecs.EitherCodec)p_184537_;
            return Objects.equals(this.first, eithercodec.first) && Objects.equals(this.second, eithercodec.second);
         } else {
            return false;
         }
      }

      public int hashCode() {
         return Objects.hash(this.first, this.second);
      }

      public String toString() {
         return "EitherCodec[" + this.first + ", " + this.second + "]";
      }
   }

   static record LazyInitializedCodec<A>(Supplier<Codec<A>> delegate) implements Codec<A> {
      LazyInitializedCodec {
         delegate = Suppliers.memoize(delegate::get);
      }

      public <T> DataResult<Pair<A, T>> decode(DynamicOps<T> p_184545_, T p_184546_) {
         return this.delegate.get().decode(p_184545_, p_184546_);
      }

      public <T> DataResult<T> encode(A p_184548_, DynamicOps<T> p_184549_, T p_184550_) {
         return this.delegate.get().encode(p_184548_, p_184549_, p_184550_);
      }
   }

   public static record TagOrElementLocation(ResourceLocation id, boolean tag) {
      public String toString() {
         return this.decoratedId();
      }

      private String decoratedId() {
         return this.tag ? "#" + this.id : this.id.toString();
      }
   }

   static final class XorCodec<F, S> implements Codec<Either<F, S>> {
      private final Codec<F> first;
      private final Codec<S> second;

      public XorCodec(Codec<F> p_144660_, Codec<S> p_144661_) {
         this.first = p_144660_;
         this.second = p_144661_;
      }

      public <T> DataResult<Pair<Either<F, S>, T>> decode(DynamicOps<T> p_144679_, T p_144680_) {
         DataResult<Pair<Either<F, S>, T>> dataresult = this.first.decode(p_144679_, p_144680_).map((p_144673_) -> {
            return p_144673_.mapFirst(Either::left);
         });
         DataResult<Pair<Either<F, S>, T>> dataresult1 = this.second.decode(p_144679_, p_144680_).map((p_144667_) -> {
            return p_144667_.mapFirst(Either::right);
         });
         Optional<Pair<Either<F, S>, T>> optional = dataresult.result();
         Optional<Pair<Either<F, S>, T>> optional1 = dataresult1.result();
         if (optional.isPresent() && optional1.isPresent()) {
            return DataResult.error("Both alternatives read successfully, can not pick the correct one; first: " + optional.get() + " second: " + optional1.get(), optional.get());
         } else {
            return optional.isPresent() ? dataresult : dataresult1;
         }
      }

      public <T> DataResult<T> encode(Either<F, S> p_144663_, DynamicOps<T> p_144664_, T p_144665_) {
         return p_144663_.map((p_144677_) -> {
            return this.first.encode(p_144677_, p_144664_, p_144665_);
         }, (p_144671_) -> {
            return this.second.encode(p_144671_, p_144664_, p_144665_);
         });
      }

      public boolean equals(Object p_144686_) {
         if (this == p_144686_) {
            return true;
         } else if (p_144686_ != null && this.getClass() == p_144686_.getClass()) {
            ExtraCodecs.XorCodec<?, ?> xorcodec = (ExtraCodecs.XorCodec)p_144686_;
            return Objects.equals(this.first, xorcodec.first) && Objects.equals(this.second, xorcodec.second);
         } else {
            return false;
         }
      }

      public int hashCode() {
         return Objects.hash(this.first, this.second);
      }

      public String toString() {
         return "XorCodec[" + this.first + ", " + this.second + "]";
      }
   }
}