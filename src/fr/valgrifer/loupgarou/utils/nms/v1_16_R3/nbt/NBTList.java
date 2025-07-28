package fr.valgrifer.loupgarou.utils.nms.v1_16_R3.nbt;

import net.minecraft.server.v1_16_R3.NBTTagList;

import java.util.Collection;

public class NBTList implements fr.valgrifer.loupgarou.utils.nms.nbt.NBTList {
    private final NBTTagList handler;

    public NBTList(NBTTagList tag) {
        handler = tag;
    }

    public NBTList() {
        handler = new NBTTagList();
    }

    public NBTTagList getHandler() {
        return handler;
    }

    @Override
    public void add(Object var1) {

    }

    @Override
    public void remove(Object var1) {

    }

    @Override
    public Object getValue(int var1) {
        return null;
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public Collection<Object> asCollection() {
        return null;
    }
}
