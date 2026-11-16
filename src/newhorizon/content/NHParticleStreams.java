package newhorizon.content;

import arc.graphics.Color;
import arc.struct.Seq;
import newhorizon.expand.type.ParticleStream;

public class NHParticleStreams {
    public static final Seq<ParticleStream> all = ParticleStream.all;
    public static ParticleStream particle;
    public static ParticleStream zetaPositive;
    public static ParticleStream zetaNegative;

    public static void load() {
        particle = new ParticleStream("particle-stream", Color.valueOf("ff8787"));
        zetaPositive = new ParticleStream("zeta-positive-stream", Color.valueOf("adff93"));
        zetaNegative = new ParticleStream("zeta-negative-stream", Color.valueOf("abf8ff"));
    }
}
