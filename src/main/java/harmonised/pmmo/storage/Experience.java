package harmonised.pmmo.storage;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import harmonised.pmmo.config.Config;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;

import java.util.List;

public class Experience {
    private XpLevel level = new XpLevel();
    private long xp = 0;

    public static final Codec<Experience> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.LONG.fieldOf("level").xmap(XpLevel::new, XpLevel::getLevel).forGetter(Experience::getLevel),
            Codec.LONG.fieldOf("xp").forGetter(Experience::getXp)
    ).apply(instance, Experience::new));
    public static final StreamCodec<ByteBuf, Experience> STREAM_CODEC = new StreamCodec<ByteBuf, Experience>() {
        @Override
        public Experience decode(ByteBuf buf) {return new Experience(new XpLevel(buf.readLong()), buf.readLong());}
        @Override
        public void encode(ByteBuf buf, Experience xp) {buf.writeLong(xp.getLevel().getLevel()); buf.writeLong(xp.getXp());}
    };

    public Experience() {
        this(new XpLevel(), 0L);
    }
    public Experience(long xpOnly) {
        this(new XpLevel(), xpOnly);
    }
    public Experience(XpLevel level, long xp) {
        this.level = level;
        this.xp = xp;
    }

    public XpLevel getLevel() {
        return level;
    }

    public void setLevel(long level) {
        this.level = new XpLevel(level);
    }

    public void addLevel(long level) {
        this.level.addLevel(level);
    }

    public long getXp() {
        return xp;
    }

    public boolean addXp(long xpIn) {
        return setXp(xp + xpIn);
    }

    public boolean setXp(long xp) {
        this.xp = xp;
        while (xp < 0) {
            if (this.level.getLevel() <= 0) {
                this.level.setLevel(0);
                this.xp = 0;
                return false;
            }
            this.level.decrement();
            xp += this.level.xpToNext;
        }
        boolean leveledUp = false;
        while (this.xp > this.level.xpToNext) {
            this.xp -= this.level.xpToNext;
            this.level.increment();
            leveledUp = true;
        }
        return leveledUp;
    }

    public Experience merge(Experience other) {
        this.addXp(other.getXp());
        this.getLevel().merge(other.getLevel());
        return this;
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof Experience oXp) {
            return xp == oXp.xp && level.getLevel() == oXp.level.getLevel();
        }
        return false;
    }

    @Override
    public String toString() {
        return "Experience{" +
                "level=" + level +
                ", xp=" + xp +
                '}';
    }

    public static class XpLevel {
        long level = 0;
        long xpToNext;
        long xpToGain;

        public XpLevel() {this(0L);}
        public XpLevel(long level) {
            this.level = level;
            xpToNext = getXpForNextLevel(level);
            xpToGain = level > 0L ? getXpForNextLevel(level - 1L) : 0L;
        }

        public XpLevel merge(XpLevel other) {
            level += other.getLevel();
            return this;
        }

        public long getLevel() {return level;}
        public void setLevel(long level) {
            if (level > this.level) {
                for (long i = 0; i < level - this.level; i++)
                    increment();
            }
            else if (level < this.level) {
                for (long i = 0; i < this.level - level; i++)
                    decrement();
            }
        }

        public long getXpToNext() {
            return xpToNext;
        }

        public long getXpToGain() {
            return xpToGain;
        }

        public void addLevel(long level) {
            final boolean isNegative = level < 0;
            for (long i = 0; i < Math.abs(level); i++) {
                if (isNegative) decrement();
                else increment();
            }
        }

        public void increment() {
            level++;
            xpToGain = xpToNext;
            xpToNext = getXpForNextLevel(level);
        }

        public void decrement() {
            level--;
            xpToNext = xpToGain;
            xpToGain = level > 0 ? getXpForNextLevel(level - 1) : 0;
        }

        public static long getXpForNextLevel(long level) {
            List<Long> staticLvls = Config.server().levels().staticLevels();
            if (staticLvls.get(0) != -1)
                return staticLvls.size() < level ? staticLvls.get((int)level) : Long.MAX_VALUE;
            long min = Config.server().levels().xpMin();
            double base = Config.server().levels().xpBase();
            double lvl = Config.server().levels().perLevel();
            return min + Double.valueOf(Math.pow(base, lvl * level)).longValue();
        }

        @Override
        public String toString() {
            return "XpLevel{" +
                    "level=" + level +
                    ", xpToNext=" + xpToNext +
                    ", xpToGain=" + xpToGain +
                    '}';
        }
    }
}
