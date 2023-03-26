package me.athlaeos.enchantssquared.domain;

import me.athlaeos.enchantssquared.EnchantsSquared;

public enum Version {
    MINECRAFT_1_13(1),
    MINECRAFT_1_14(2),
    MINECRAFT_1_15(3),
    MINECRAFT_1_16(4),
    MINECRAFT_1_17(5),
    MINECRAFT_1_18(6),
    MINECRAFT_1_19(7),
    MINECRAFT_1_20(8),
    MINECRAFT_1_21(9),
    MINECRAFT_1_22(10),
    MINECRAFT_1_23(11),
    INCOMPATIBLE(-1);

    private final int version;
    private static final Version serverVersion = getServerVersion();

    Version(int version){
        this.version = version;
    }

    public static boolean currentVersionOrOlderThan(Version version){
        if (serverVersion == Version.INCOMPATIBLE) return false;
        return serverVersion.getVersion() <= version.getVersion();
    }

    public static boolean currentVersionOrNewerThan(Version version){
        if (serverVersion == Version.INCOMPATIBLE) return false;
        return serverVersion.getVersion() >= version.getVersion();
    }

    private int getVersion() {
        return version;
    }

    private static Version getServerVersion(){
        String stringVersion = EnchantsSquared.getPlugin().getServer().getVersion();
        if (stringVersion.contains("1_13") || stringVersion.contains("1.13")) return Version.MINECRAFT_1_13;
        else if (stringVersion.contains("1_14") || stringVersion.contains("1.14")) return Version.MINECRAFT_1_14;
        else if (stringVersion.contains("1_15") || stringVersion.contains("1.15")) return Version.MINECRAFT_1_15;
        else if (stringVersion.contains("1_16") || stringVersion.contains("1.16")) return Version.MINECRAFT_1_16;
        else if (stringVersion.contains("1_17") || stringVersion.contains("1.17")) return Version.MINECRAFT_1_17;
        else if (stringVersion.contains("1_18") || stringVersion.contains("1.18")) return Version.MINECRAFT_1_18;
        else if (stringVersion.contains("1_19") || stringVersion.contains("1.19")) return Version.MINECRAFT_1_19;
        else if (stringVersion.contains("1_20") || stringVersion.contains("1.20")) return Version.MINECRAFT_1_20;
        else if (stringVersion.contains("1_21") || stringVersion.contains("1.21")) return Version.MINECRAFT_1_21;
        else if (stringVersion.contains("1_22") || stringVersion.contains("1.22")) return Version.MINECRAFT_1_22;
        else if (stringVersion.contains("1_23") || stringVersion.contains("1.23")) return Version.MINECRAFT_1_23;
        else return Version.INCOMPATIBLE;
    }
}
