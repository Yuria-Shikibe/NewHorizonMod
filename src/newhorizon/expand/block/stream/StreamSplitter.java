package newhorizon.expand.block.stream;

public class StreamSplitter extends StreamRepeater{
    public StreamSplitter(String name) {
        super(name);
    }

    public class StreamSplitterBuild extends StreamRepeaterBuild{
        public StreamBeam streamForward;
        public int lastDirection;

        @Override
        public void created() {
            super.created();
            efficiency = 1f;
            stream.beamLength = 3;
            stream.amountCap = 0.1f;

            streamForward = new StreamBeam(this);
        }

        @Override
        public void update() {
            stream.update();

            streamForward.rotationOffset = lastDirection;
            streamForward.update();
        }

        @Override
        public boolean acceptStream(StreamBeam stream) {
            return (stream.getRotation() % 2) != (rotation % 2);
        }

        @Override
        public void handleStream(StreamBeam stream) {
            lastDirection = (stream.getRotation() - rotation);
        }

        @Override
        public void draw() {
            super.draw();
            streamForward.draw();
        }
    }
}
