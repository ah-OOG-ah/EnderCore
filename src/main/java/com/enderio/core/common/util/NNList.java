package com.enderio.core.common.util;

import java.security.InvalidParameterException;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;

public class NNList<E> extends NonNullList<E> {

  public static final @Nonnull NNList<EnumFacing> FACING = NNList.of(EnumFacing.class);

  public static final @Nonnull NNList<EnumFacing> FACING_HORIZONTAL = new NNList<EnumFacing>(EnumFacing.HORIZONTALS);

  public static final @Nonnull NNList<BlockRenderLayer> RENDER_LAYER = NNList.of(BlockRenderLayer.class);

  public static final @Nonnull NNList<BlockPos> SHELL = new NNList<>();
  static {
    for (int y = -1; y <= 1; y++) {
      for (int z = -1; z <= 1; z++) {
        for (int x = -1; x <= 1; x++) {
          if (x != 0 || y != 0 || z != 0) {
            SHELL.add(new BlockPos(x, y, z));
          }
        }
      }
    }
    Collections.shuffle(SHELL);
  }

  public NNList() {
    super();
  }

  public NNList(Collection<E> fillWith) {
    super();
    addAll(fillWith);
  }

  public NNList(int size, @Nonnull E fillWith) {
    super();
    for (int i = 0; i < size; i++) {
      add(fillWith);
    }
  }

  @SafeVarargs
  public NNList(E... fillWith) {
    super();
    Collections.addAll(this, fillWith);
  }

  protected NNList(List<E> list, E defaultElement) {
    super(list, defaultElement);
  }

  public @Nonnull NNList<E> copy() {
    return new NNList<E>(this);
  }

  public static @Nonnull <X> NNList<X> wrap(List<X> list) {
    return list instanceof NNList ? (NNList<X>) list : new NNList<X>(list, null);
  }

  public static @Nonnull <X extends Enum<?>> NNList<X> of(Class<X> e) {
    NNList<X> list = new NNList<X>(e.getEnumConstants());
    return list;
  }

  /**
   * Finds the element after the given element.
   * <p>
   * Please note that this does do identity, not equality, checks and cannot handle multiple occurrences of the same element in the list.
   *
   * @throws InvalidParameterException
   *           if the given element is not part of the list.
   */
  public @Nonnull E next(E current) {
    for (int i = 0; i < size(); i++) {
      if (get(i) == current) {
        if (i + 1 < size()) {
          return get(i + 1);
        } else {
          return get(0);
        }
      }
    }
    throw new InvalidParameterException();
  }

  /**
   * Finds the element before the given element.
   * <p>
   * Please note that this does do identity, not equality, checks and cannot handle multiple occurrences of the same element in the list.
   *
   * @throws InvalidParameterException
   *           if the given element is not part of the list.
   */
  public @Nonnull E prev(E current) {
    for (int i = 0; i < size(); i++) {
      if (get(i) == current) {
        if (i > 0) {
          return get(i - 1);
        } else {
          return get(size() - 1);
        }
      }
    }
    throw new InvalidParameterException();
  }

  public NNList<E> apply(@Nonnull Callback<E> callback) {
    for (E e : this) {
      if (e == null) {
        throw new NullPointerException();
      }
      callback.apply(e);
    }
    return this;
  }

  public static interface Callback<E> {
    void apply(@Nonnull E e);
  }

  public boolean apply(@Nonnull ShortCallback<E> callback) {
    for (E e : this) {
      if (e == null) {
        throw new NullPointerException();
      }
      if (callback.apply(e)) {
        return true;
      }
    }
    return false;
  }

  public static interface ShortCallback<E> {
    boolean apply(@Nonnull E e);
  }

  @Override
  public @Nonnull NNIterator<E> iterator() {
    return new ItrImpl<E>(super.iterator());
  }

  /**
   * Creates a fast iterator for read-only lists. Do not use on lists that may
   * be changed.
   */
  public @Nonnull NNIterator<E> fastIterator() {
    return new FastItrImpl();
  }

  public static interface NNIterator<E> extends Iterator<E> {

    @Override
    @Nonnull
    E next();

  }

  private static class ItrImpl<E> implements NNIterator<E> {

    private final Iterator<E> parent;

    public ItrImpl(Iterator<E> iterator) {
      parent = iterator;
    }

    @Override
    public boolean hasNext() {
      return parent.hasNext();
    }

    @Override
    public @Nonnull E next() {
      final E next = parent.next();
      if (next == null) {
        throw new NullPointerException();
      }
      return next;
    }

    @Override
    public void remove() {
      parent.remove();
    }

  }

  private class FastItrImpl implements NNIterator<E> {
    int cursor = 0;

    @Override
    public boolean hasNext() {
      return cursor != size();
    }

    @Override
    public @Nonnull E next() {
      try {
        return get(cursor++);
      } catch (IndexOutOfBoundsException e) {
        throw new NoSuchElementException();
      }
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException();
    }

  }

  private static final @Nonnull NNList<Object> EMPTY = new NNList<Object>() {
    @Override
    public void add(int p_add_1_, Object p_add_2_) {
      throw new UnsupportedOperationException();
    }
  };

  @SuppressWarnings("unchecked")
  public static @Nonnull <X> NNList<X> emptyList() {
    return (NNList<X>) EMPTY;
  }

  @SafeVarargs
  public final NNList<E> addAll(E... el) {
    for (E e : el) {
      add(e);
    }
    return this;
  }

  public NNList<E> addIf(@Nullable E e) {
    if (e != null) {
      add(e);
    }
    return this;
  }

  @SuppressWarnings("null")
  @Override
  public @Nonnull <T> T[] toArray(T[] a) {
    return super.toArray(a);
  }

  public NNList<E> removeAllByClass(Class<? extends E> clazz) {
    for (NNIterator<E> iterator = iterator(); iterator.hasNext();) {
      if (clazz.isAssignableFrom(iterator.next().getClass())) {
        iterator.remove();
      }
    }
    return this;
  }

}
