package newhorizon.expand.block.stream;

public interface StreamBeamBuild {
    default boolean acceptStream(StreamBeam stream) {
        return true;
    }

    default void handleStream(StreamBeam stream) {}
}
