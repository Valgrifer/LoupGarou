package fr.valgrifer.loupgarou.utils.nms.v1_16_R3.nbt;

import net.minecraft.server.v1_16_R3.NBTTagCompound;

import java.util.Set;

public class NBTCompound implements fr.valgrifer.loupgarou.utils.nms.nbt.NBTCompound {
    private final NBTTagCompound handler;

    public NBTCompound(NBTTagCompound tag) {
        handler = tag;
    }

    public NBTCompound() {
        handler = new NBTTagCompound();
    }


    public NBTTagCompound getHandler() {
        return handler;
    }


    @Override
    public boolean containsKey(String var1) {
        return handler.hasKey(var1);
    }

    @Override
    public Set<String> getKeys() {
        return handler.getKeys();
    }

    @Override
    public String getString(String var1) {
        return handler.getString(var1);
    }

    @Override
    public String getStringOrDefault(String var1, String var2) {
        return containsKey(var1) ? handler.getString(var1) : var2;
    }

    @Override
    public fr.valgrifer.loupgarou.utils.nms.nbt.NBTCompound put(String var1, String var2) {
        handler.setString(var1, var2);
        return this;
    }

    @Override
    public byte getByte(String var1) {
        return handler.getByte(var1);
    }

    @Override
    public byte getByteOrDefault(String var1, Byte var2) {
        return containsKey(var1) ? handler.getByte(var1) : var2;
    }

    @Override
    public fr.valgrifer.loupgarou.utils.nms.nbt.NBTCompound put(String var1, byte var2) {
        handler.setByte(var1, var2);
        return this;
    }

    @Override
    public Short getShort(String var1) {
        return handler.getShort(var1);
    }

    @Override
    public short getShortOrDefault(String var1, Short var2) {
        return containsKey(var1) ? handler.getByte(var1) : var2;
    }

    @Override
    public fr.valgrifer.loupgarou.utils.nms.nbt.NBTCompound put(String var1, short var2) {
        handler.setShort(var1, var2);
        return this;
    }

    @Override
    public int getInteger(String var1) {
        return handler.getByte(var1);
    }

    @Override
    public int getIntegerOrDefault(String var1, Integer var2) {
        return containsKey(var1) ? handler.getByte(var1) : var2;
    }

    @Override
    public fr.valgrifer.loupgarou.utils.nms.nbt.NBTCompound put(String var1, int var2) {
        handler.setInt(var1, var2);
        return this;
    }

    @Override
    public long getLong(String var1) {
        return handler.getByte(var1);
    }

    @Override
    public long getLongOrDefault(String var1, Long var2) {
        return containsKey(var1) ? handler.getByte(var1) : var2;
    }

    @Override
    public fr.valgrifer.loupgarou.utils.nms.nbt.NBTCompound put(String var1, long var2) {
        handler.setLong(var1, var2);
        return this;
    }

    @Override
    public float getFloat(String var1) {
        return handler.getByte(var1);
    }

    @Override
    public float getFloatOrDefault(String var1, Float var2) {
        return containsKey(var1) ? handler.getByte(var1) : var2;
    }

    @Override
    public fr.valgrifer.loupgarou.utils.nms.nbt.NBTCompound put(String var1, float var2) {
        handler.setFloat(var1, var2);
        return this;
    }

    @Override
    public double getDouble(String var1) {
        return handler.getByte(var1);
    }

    @Override
    public double getDoubleOrDefault(String var1, Double var2) {
        return containsKey(var1) ? handler.getByte(var1) : var2;
    }

    @Override
    public fr.valgrifer.loupgarou.utils.nms.nbt.NBTCompound put(String var1, double var2) {
        handler.setDouble(var1, var2);
        return this;
    }

    @Override
    public byte[] getByteArray(String var1) {
        return handler.getByteArray(var1);
    }

    @Override
    public fr.valgrifer.loupgarou.utils.nms.nbt.NBTCompound put(String var1, byte[] var2) {
        handler.setByteArray(var1, var2);
        return this;
    }

    @Override
    public int[] getIntegerArray(String var1) {
        return handler.getIntArray(var1);
    }

    @Override
    public fr.valgrifer.loupgarou.utils.nms.nbt.NBTCompound put(String var1, int[] var2) {
        handler.setIntArray(var1, var2);
        return this;
    }

    @Override
    public fr.valgrifer.loupgarou.utils.nms.nbt.NBTCompound getCompound(String var1) {
        return new NBTCompound(handler.getCompound(var1));
    }

    @Override
    public fr.valgrifer.loupgarou.utils.nms.nbt.NBTCompound getCompoundOrDefault(String var1, fr.valgrifer.loupgarou.utils.nms.nbt.NBTCompound var2) {
        return containsKey(var1) ? new NBTCompound(handler.getCompound(var1)) : var2;
    }

    @Override
    public fr.valgrifer.loupgarou.utils.nms.nbt.NBTCompound put(String var1, fr.valgrifer.loupgarou.utils.nms.nbt.NBTCompound var2) {
        handler.set(var1, (NBTTagCompound) var2.getHandler());
        return this;
    }

    @Override
    public fr.valgrifer.loupgarou.utils.nms.nbt.NBTList getList(String var1) {
        return new NBTList(handler.getList(var1, 0));
    }

    @Override
    public fr.valgrifer.loupgarou.utils.nms.nbt.NBTList getListOrDefault(String var1, fr.valgrifer.loupgarou.utils.nms.nbt.NBTList var2) {
        return containsKey(var1) ? new NBTList(handler.getList(var1, 0)) : var2;
    }

    @Override
    public fr.valgrifer.loupgarou.utils.nms.nbt.NBTCompound put(String var1, fr.valgrifer.loupgarou.utils.nms.nbt.NBTList var2) {
        handler.set(var1, ((NBTList) var2).getHandler());
        return this;
    }
}
