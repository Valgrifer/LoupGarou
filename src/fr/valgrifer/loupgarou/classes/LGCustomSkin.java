package fr.valgrifer.loupgarou.classes;

import com.comphenix.protocol.wrappers.WrappedSignedProperty;

import lombok.Getter;

import java.util.ArrayList;
import java.util.Objects;

public class LGCustomSkin
{
    @Getter
    private static final ArrayList<LGCustomSkin> values = new ArrayList<>();
    public static LGCustomSkin register(String name, WrappedSignedProperty property)
    {
        LGCustomSkin cause;
        if((cause = getSkin(name)) != null)
            return cause;
        return new LGCustomSkin(name, property);
    }

    public static LGCustomSkin getSkin(String name)
    {
        for (LGCustomSkin cause : getValues())
            if(cause.getName().equalsIgnoreCase(name))
                return cause;
        return null;
    }



    public static final LGCustomSkin VILLAGER = register("VILLAGER", new WrappedSignedProperty("textures", "eyJ0aW1lc3RhbXAiOjE1NzcwNTE5MDIyNDgsInByb2ZpbGVJZCI6Ijc4YWQzMjI2YmJkYTQwMGZiNTExMjAzMDY4MGNjN2I0IiwicHJvZmlsZU5hbWUiOiJTaHl0b29zIiwidGV4dHVyZXMiOnsiU0tJTiI6eyJ1cmwiOiJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzYxZmE3Y2UyYmQxMmJmMzBjOTcxZjUwZjJiMGIyYTZkZWUyOWU4MWM5MmJkZmFhZjFkMDJlNjM5YzJiZjMzNWEiLCJtZXRhZGF0YSI6eyJtb2RlbCI6InNsaW0ifX19fQ==", "Qk3Gh9IbVwqIvzv0t3vNrGhzW1ejvKls/7n1vqC7UozJYFotECsBla3DFrxrLvTVsCMAQ3cXGq9QjfFF1U/q0NYvWNXVPbrIxX6LH2t2SI9F1+WLQ7LGYwwABzMGDClJXkuUr0yEOPtCwoTgvinVkCZmNRrN0VDAwd+ie3LCnuBjo3iabjA4dxXLE59mnvCGKo5pGdqThn/KikUldvYGBjpQa+X5F+jQhj7TucZ8t0VJ2t05TkKHS+bcIcmCD+UorGG1N5uiNkQe89BMXZKQEebydtnH5RT8efqHfYT5aZY/6IJKTMqIeclcWy2f6LIfRb97AeifOPPHTfjl2Q6wjvOdnvGuNa+Xk6biNM7cLz1KQzdMM0CvkiZZ9XtFul5+PXEobx2IjIEcrStZX0fl/NZid+kf+2RXbPKx4Z+07q5PbeCzr1UD3DMWlrEMQQppIB26uhPLS9mh3IvM9bhx+vIbByr1Z5p3aT4J2mbhwwmf/LUpNUyMn6oHt19zX6cEcSbncGBLgEHOrUXgxdK/Sm4lJKA1Mr3M0o8CSt7ldLc6mA4oBPMK/OV/c0rwb1W2GWOUyw99dNT/jFSFXyyjCHW0+5FkAW+H+8Y0dZ3U4UWaAmX4JNfdxiih97eTBKEvIC8V48hiFS/UV9A9LBVPNkca/4lanh/jNZ7VRQ+/vU8="));
    public static final LGCustomSkin MAYOR = register("MAYOR", new WrappedSignedProperty("textures", "eyJ0aW1lc3RhbXAiOjE1NzcwNTE4NjcxMDcsInByb2ZpbGVJZCI6Ijc4YWQzMjI2YmJkYTQwMGZiNTExMjAzMDY4MGNjN2I0IiwicHJvZmlsZU5hbWUiOiJTaHl0b29zIiwidGV4dHVyZXMiOnsiU0tJTiI6eyJ1cmwiOiJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzUzYTk5NjljMjU0YWRkYmZkNjdmNDJlZmMxODNkZDg5ZWE3ZTliOGE4NDY2YWNkOWZhMWZmMTYzMTQ0MTczMDMifX19", "fq/bN78Y70k2clbfg6sLRuj4mhvDTz58fKI3/0XvzJH/bQF5Mf+jcFwpXJN+ZiupMaHFHI1bBQ20lrzlGvtrCoYu9+Bx4otin0NQVxc3pOEewikzYub64niMPq8irGJrS2pB1B2Uym5F+/wX8TXOkySyAo1r3lzh6qmfe1bpNG7gcc9+ulYdAbnyhxTko3d8poBsjLnadpo0/rBNMkNIua9ryIE3WHW+DOxLwpLpd3TAB15RnA3/qvyBlqQBny1M24ccjmzxjiUbzF66EtTC+BRs3eS8or/vxvdmi70e295gMoERZJ5pFukxe9LB0IEg9cLHwuEl7OC8FcuLUQFnm1EVPjfSlSF36Sd+iVwjFE/x1zINKYsDHwF/TM+yAWOv+PwgGxgQj1C97VaWEkrHXDMEzgxi8DAcPbDBwcyBZXRgOJTHRSH2ATvGDqeT7iQbPrP+y0YNFUDXyBrKYgscgA7bMtBgWvW75SdvcIhqQEVe/jMivMWDuvwhQF4lUGonHc0B5wi1216fqTypEcOeaa5ab+siNssFXpEsaFP9l+1o6uYjNbBpptEAsIlKbnjDaVSkCTvsW7ICaPAZcZCavpyST5MYTv6C7n/bwl3nfzzI7U1LYvYi/gMEHKWfNwLs4T+4VQ3xZD0DpkEQgDrax+Wt182K6OvrAcOcCAfWySo="));
    public static final LGCustomSkin WEREWOLF = register("WEREWOLF", new WrappedSignedProperty("textures", "eyJ0aW1lc3RhbXAiOjE1NzcwNTE3NTU1NDgsInByb2ZpbGVJZCI6Ijc4YWQzMjI2YmJkYTQwMGZiNTExMjAzMDY4MGNjN2I0IiwicHJvZmlsZU5hbWUiOiJTaHl0b29zIiwidGV4dHVyZXMiOnsiU0tJTiI6eyJ1cmwiOiJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2U1NGE2MmRhNGMzZTI5YjdiZWM4ZTI0YzhiNTAyZGZjNWNmMTNmYjc0NjFhN2I5YWY2NGY2Yjc1ZGFhNmE2MDUifX19", "k0cT/rDkBW3QMqSavKc0Nbu39ft3i0TLLhxLht7LKKuA+Q+amCmzw/NZjZRtJc/R87uyl2B+JHLTWTOcbDQX2bE7fIzEvYFhvbb2dasxeYA03wBwZ+OuJlhhxWSSX2NoAI6gEGcWQ3k4xcf+xhtD+5eddjL5k+Dv/kVW6qCWprfPSmd4+KQ0aVwyBkbheyTBSaamtpPj24UYM1EL9POF/PrbmiipfSthp7DkwL4PprW+0Z3pzFMgY46Ay2VJYxa+Q6q3IlYqSXMkjJccf3msyOTGe3JSiQc1Du/djffvLIoyjybolNb5rQ/OrnYIEtnqCuXstJJd3J+a6F8w4DkS2ZMVY4lvidb4coVeFmm9Nk/afqqyG99TDmvZNFWFSeaaxGSWeZPkneAEFuFQXn76sl1xdtXkgXmP8H0yvs9SwA9UnGoldIay6D1HzAOGo3n3stV6188K0ucey1KsH6nNlZfT2hnqvI1lfKD67oQNFfl4kDLRwArfZJUmcQOJh9/fk6/bJvgYuElEkGy++0FkuKKkRJHPIW5+kPZguNo96cAAGxhSH3IIscLoxDdn53iSnngpKttWfG6ccI/XMGTWp4wCq6IC2IBZcmKEqK+fN5dVbR4tn79pXlQ2Yyk7kvXaZjdVHrKC1Buw1gjjC1EvmiJjTRZVHPYlCTkfy+hdqzE="));




    @Getter
    private final String name;
    @Getter
    private final WrappedSignedProperty property;
    private LGCustomSkin(String name, WrappedSignedProperty property) {
        this.name = name.replaceAll("[^\\w]", "");
        this.property = property;

        values.add(this);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName()+"{name='" + name + "}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LGCustomSkin that = (LGCustomSkin) o;
        return name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
