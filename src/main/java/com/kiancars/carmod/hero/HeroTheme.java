package com.kiancars.carmod.hero;

import java.util.List;

/**
 * A "flavor" the AI knows about. When you search for something, the AI
 * matches your words to its themes and builds gear out of their powers.
 *
 * @param hue a spot on the color wheel (0..1) the gear is tinted around
 */
public record HeroTheme(String id, List<String> keywords, List<HeroAbility> abilities, List<HeroPower> passives,
                        List<HeroPower> hits, float hue) {

    private static HeroTheme t(String id, String keywords, List<HeroAbility> abilities, List<HeroPower> passives,
                               List<HeroPower> hits, float hue) {
        return new HeroTheme(id, List.of(keywords.split(",")), abilities, passives, hits, hue);
    }

    public static final List<HeroTheme> ALL = List.of(
            t("teleport", "teleport,portal,warp,blink,ender,dimension,nightcrawler,strange,sorcer,wizard,magic,mage",
                    List.of(HeroAbility.TELEPORT, HeroAbility.PHASE, HeroAbility.GRAVITY_PULL, HeroAbility.SHIELD),
                    List.of(HeroPower.NIGHT_EYES, HeroPower.FEATHER_FALL), List.of(HeroPower.WEAKEN, HeroPower.SHOCKWAVE), 0.78F),
            t("phase", "phase,ghost,intangible,atom,intelligen,smart,genius,brain,science,scientist,quantum,invisible,wall,spirit,ant",
                    List.of(HeroAbility.PHASE, HeroAbility.SENSE, HeroAbility.CLOAK, HeroAbility.TELEPORT),
                    List.of(HeroPower.NIGHT_EYES, HeroPower.LUCKY), List.of(HeroPower.WEAKEN, HeroPower.DECAY), 0.5F),
            t("spider", "spider,web,parker,climb,arachnid,swing,tarantula",
                    List.of(HeroAbility.WEB, HeroAbility.SUPER_JUMP, HeroAbility.SENSE, HeroAbility.DASH),
                    List.of(HeroPower.SWIFT, HeroPower.LEAP, HeroPower.FEATHER_FALL), List.of(HeroPower.VENOM, HeroPower.FROSTBITE), 0.0F),
            t("flight", "iron man,ironman,stark,tony,fly,flight,wing,bird,eagle,angel,falcon,rocket,jet,sky,air,cloud,superman,super man,hawk",
                    List.of(HeroAbility.FLIGHT, HeroAbility.FIREBALL, HeroAbility.DASH, HeroAbility.EXPLODE, HeroAbility.SHIELD),
                    List.of(HeroPower.FEATHER_FALL, HeroPower.IRON_SKIN, HeroPower.GUARDIAN_SHIELD), List.of(HeroPower.SHOCKWAVE, HeroPower.IGNITE), 0.02F),
            t("thunder", "thor,thunder,lightning,storm,zeus,electric,electro,volt,shock,tesla,spark,static,hammer",
                    List.of(HeroAbility.LIGHTNING_STRIKE, HeroAbility.FLIGHT, HeroAbility.DASH, HeroAbility.FORCE_PUSH),
                    List.of(HeroPower.TITAN_STRENGTH, HeroPower.SWIFT), List.of(HeroPower.LIGHTNING, HeroPower.SHOCKWAVE), 0.6F),
            t("speed", "flash,speed,fast,sonic,quick,rapid,run,sprint,quicksilver,cheetah,turbo,racer,race,car",
                    List.of(HeroAbility.DASH, HeroAbility.SUPER_JUMP, HeroAbility.TIME_FREEZE, HeroAbility.PHASE),
                    List.of(HeroPower.SWIFT, HeroPower.QUICK_HANDS, HeroPower.LEAP), List.of(HeroPower.SMASH, HeroPower.WEAKEN), 0.14F),
            t("strength", "hulk,strong,strength,smash,giant,titan,muscle,power,brute,rage,angry,beast,gorilla,bear,monster,kong,tank",
                    List.of(HeroAbility.SLAM, HeroAbility.SUPER_JUMP, HeroAbility.EXPLODE, HeroAbility.FORCE_PUSH),
                    List.of(HeroPower.TITAN_STRENGTH, HeroPower.VITALITY, HeroPower.IRON_SKIN), List.of(HeroPower.SMASH, HeroPower.EXECUTE), 0.3F),
            t("fire", "fire,flame,burn,lava,sun,solar,ember,dragon,inferno,volcano,magma,blaze,heat,hot,torch,phoenix",
                    List.of(HeroAbility.FIREBALL, HeroAbility.EXPLODE, HeroAbility.DASH, HeroAbility.FLIGHT),
                    List.of(HeroPower.FIREPROOF, HeroPower.TITAN_STRENGTH), List.of(HeroPower.IGNITE, HeroPower.SHOCKWAVE), 0.04F),
            t("ice", "ice,frost,cold,winter,freeze,snow,elsa,blizzard,glacier,arctic,polar,frozen,chill,penguin,yeti",
                    List.of(HeroAbility.FREEZE_BLAST, HeroAbility.SHIELD, HeroAbility.TIME_FREEZE, HeroAbility.DASH),
                    List.of(HeroPower.IRON_SKIN, HeroPower.DOLPHIN_GRACE), List.of(HeroPower.FROSTBITE, HeroPower.WEAKEN), 0.55F),
            t("shadow", "shadow,ninja,stealth,night,dark,batman,bat,assassin,thief,rogue,black,spy,sneak,void",
                    List.of(HeroAbility.CLOAK, HeroAbility.DASH, HeroAbility.TELEPORT, HeroAbility.SENSE),
                    List.of(HeroPower.NIGHT_EYES, HeroPower.SWIFT, HeroPower.FEATHER_FALL), List.of(HeroPower.EXECUTE, HeroPower.VENOM), 0.72F),
            t("force", "magnet,magneto,gravity,force,jedi,telekinesis,space,star,galaxy,cosmic,planet,alien,sith,psychic,mind,telepath",
                    List.of(HeroAbility.FORCE_PUSH, HeroAbility.GRAVITY_PULL, HeroAbility.FLIGHT, HeroAbility.SHIELD),
                    List.of(HeroPower.GUARDIAN_SHIELD, HeroPower.FEATHER_FALL), List.of(HeroPower.SMASH, HeroPower.SHOCKWAVE), 0.66F),
            t("vampire", "vampire,blood,drain,dracula,leech,zombie,undead,death,reaper,skeleton,necro,curse,evil,demon",
                    List.of(HeroAbility.DRAIN, HeroAbility.CLOAK, HeroAbility.DASH, HeroAbility.FLIGHT),
                    List.of(HeroPower.REGROWTH, HeroPower.NIGHT_EYES), List.of(HeroPower.LIFESTEAL, HeroPower.DECAY), 0.97F),
            t("time", "time,clock,chrono,hour,future,past,timelord,who,hourglass,eternal,fate,destiny",
                    List.of(HeroAbility.TIME_FREEZE, HeroAbility.TELEPORT, HeroAbility.HEAL_BURST, HeroAbility.SHIELD),
                    List.of(HeroPower.LUCKY, HeroPower.REGROWTH), List.of(HeroPower.WEAKEN, HeroPower.EXECUTE), 0.12F),
            t("nature", "heal,medic,nature,forest,plant,druid,tree,flower,life,green,leaf,garden,farm,bee,wolf,animal,doctor",
                    List.of(HeroAbility.HEAL_BURST, HeroAbility.SHIELD, HeroAbility.SENSE, HeroAbility.SUPER_JUMP),
                    List.of(HeroPower.REGROWTH, HeroPower.VITALITY, HeroPower.LUCKY), List.of(HeroPower.VENOM, HeroPower.LIFESTEAL), 0.33F),
            t("water", "water,ocean,aqua,sea,fish,shark,wave,tide,river,mermaid,diver,kraken,rain,flood",
                    List.of(HeroAbility.DASH, HeroAbility.FORCE_PUSH, HeroAbility.HEAL_BURST, HeroAbility.FREEZE_BLAST),
                    List.of(HeroPower.DOLPHIN_GRACE, HeroPower.GILLS, HeroPower.REGROWTH), List.of(HeroPower.SMASH, HeroPower.FROSTBITE), 0.58F),
            t("earth", "earth,rock,stone,mountain,quake,sand,miner,dwarf,golem,crystal,gem,diamond,metal,steel,iron,armor,knight,shield,captain",
                    List.of(HeroAbility.SLAM, HeroAbility.SHIELD, HeroAbility.FORCE_PUSH, HeroAbility.SUPER_JUMP),
                    List.of(HeroPower.IRON_SKIN, HeroPower.GUARDIAN_SHIELD, HeroPower.VITALITY), List.of(HeroPower.SMASH, HeroPower.SHOCKWAVE), 0.08F));

    public int score(String normalizedQuery) {
        int score = 0;
        for (String keyword : keywords) {
            if (normalizedQuery.contains(keyword)) {
                score += keyword.length();
            }
        }
        return score;
    }
}
