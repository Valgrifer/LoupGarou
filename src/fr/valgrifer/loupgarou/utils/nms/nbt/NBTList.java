package fr.valgrifer.loupgarou.utils.nms.nbt;

import java.util.Collection;

public interface NBTList {
    Object getHandler();

    void add(Object var1);

    void remove(Object var1);

    Object getValue(int var1);

    int size();

    Collection<Object> asCollection();
}
