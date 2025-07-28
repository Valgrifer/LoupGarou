package fr.valgrifer.loupgarou.roles;

import lombok.Getter;

import java.util.ArrayList;
import java.util.Objects;

public class RoleSort {
    @Getter
    private static final ArrayList<RoleSort> values = new ArrayList<>();

    static {
        register(RDogWolf.class);
        register(RChildWild.class);
        register(RAngelV2.class);
        register(RCupid.class);
        register(RAngelV2Guardian.class);
        register(RAngelV2Fallen.class);
        register(RGardien.class);
        register(RSurvivor.class);
        register(RClairvoyant.class);
        register(RWolfClairvoyant.class);
        register(RDetective.class);
        register(RDictator.class);
        register(RFox.class);
        register(RPriest.class);
        register(RWereWolf.class);
        register(RBlackWerewolf.class);
        register(RBigBadWolf.class);
        register(RWhiteWerewolf.class);
        register(RAssassin.class);
        register(RPyromaniac.class);
        register(RVampireHunter.class);
        register(RVampire.class);
        register(RPirate.class);
        register(RJester.class);
        register(RWitch.class);
        register(RRaven.class);
    }

    @Getter
    private final String name;
    private RoleSort(String name) {
        this.name = name.replaceAll("\\W", "");
    }

    public static RoleSort registerAt(Class<? extends Role> clazz, int index) {
        return registerAt(clazz.getSimpleName().substring(1), index);
    }

    public static RoleSort registerAt(String name, int index) {
        RoleSort rolesort;
        if ((rolesort = getRoleSort(name)) != null) return rolesort;
        rolesort = new RoleSort(name);
        if (index == -1) values.add(rolesort);
        else values.add(index, rolesort);
        return rolesort;
    }

    public static RoleSort register(Class<? extends Role> clazz) {
        return registerAt(clazz.getSimpleName().substring(1), -1);
    }

    public static RoleSort register(String name) {
        return registerAt(name, -1);
    }

    public static RoleSort registerBefore(Class<? extends Role> clazz, Class<? extends Role> at) {
        return registerBefore(clazz.getSimpleName().substring(1), at.getSimpleName().substring(1));
    }

    public static RoleSort registerBefore(String name, String at) {
        return registerAt(name, indexOfRoleSort(at));
    }

    public static RoleSort registerAfter(Class<? extends Role> clazz, Class<? extends Role> at) {
        return registerAfter(clazz.getSimpleName().substring(1), at.getSimpleName().substring(1));
    }

    public static RoleSort registerAfter(String name, String at) {
        int index = indexOfRoleSort(at);
        if (index >= 0) index++;
        return registerAt(name, index);
    }

    public static RoleSort getRoleSort(String name) {
        for (RoleSort rolesort : getValues())
            if (rolesort.getName().equalsIgnoreCase(name)) return rolesort;
        return null;
    }

    public static int indexOfRoleSort(String name) {
        return values.indexOf(getRoleSort(name));
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{name='" + name + "}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RoleSort that = (RoleSort) o;
        return name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
