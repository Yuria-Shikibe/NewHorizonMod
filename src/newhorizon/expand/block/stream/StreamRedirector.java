package newhorizon.expand.block.stream;

public class StreamRedirector extends StreamRepeater {
    public StreamRedirector(String name) {
        super(name);
    }

    public class StreamRedirectorBuild extends StreamRepeaterBuild {
        @Override
        public void created() {
            super.created();
            stream.beamLength = 3;
        }

        @Override
        public boolean acceptStream(StreamBeam stream) {
            return (stream.getRotation() + 2) % 4 != rotation;
        }
    }
}
