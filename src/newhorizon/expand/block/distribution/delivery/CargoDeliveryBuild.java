package newhorizon.expand.block.distribution.delivery;

import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.math.Mathf;
import arc.math.geom.Point2;
import arc.struct.IntSeq;
import arc.struct.ObjectSet;
import arc.struct.Queue;
import arc.struct.Seq;
import arc.util.Time;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.gen.Building;
import mindustry.graphics.Drawf;
import mindustry.graphics.Pal;

import static mindustry.Vars.net;
import static mindustry.Vars.tilesize;
import static mindustry.Vars.world;

public abstract class CargoDeliveryBuild extends Building {
    public final IntSeq links = new IntSeq();

    @Override
    public void created() {
        super.created();
        CargoDeliveryGraph.register(this);
    }

    @Override
    public void onRemoved() {
        unlinkAll();
        CargoDeliveryGraph.unregister(this);
        super.onRemoved();
    }

    @Override
    public void placed() {
        super.placed();
        if (net.client()) return;

        if (CargoDeliveryGraph.isRelay(this)) {
            if (relayLinkCount() == 0) {
                Point2[] points = autoLinkPoints();
                if (points.length > 0) configureAny(points);
            }
        } else if (CargoDeliveryGraph.isDeposit(this)) {
            linkNearbyRelays();
        }
    }

    @Override
    public void write(Writes write) {
        super.write(write);
        write.s(links.size);
        for (int i = 0; i < links.size; i++) {
            write.i(links.get(i));
        }
    }

    @Override
    public void read(Reads read, byte revision) {
        super.read(read, revision);
        links.clear();

        short amount = read.s();
        for (int i = 0; i < amount; i++) {
            links.add(read.i());
        }

        CargoDeliveryGraph.markDirty();
    }

    @Override
    public boolean onConfigureBuildTapped(Building other) {
        if (CargoDeliveryGraph.isRelay(this)) {
            if (other == this) {
                if (relayLinkCount() == 0) {
                    configure(autoLinkPoints());
                } else {
                    configure(new Point2[0]);
                }
                deselect();
                return false;
            }

            if (CargoDeliveryGraph.isRelay(other)) {
                if (links.contains(other.pos()) || relayLinkValid(other)) {
                    configure(other.pos());
                    return false;
                }
            } else if (CargoDeliveryGraph.isDeposit(other)) {
                if (links.contains(other.pos()) || CargoDeliveryGraph.connected(this, other)) {
                    configure(other.pos());
                    return false;
                }
            }
        } else if (CargoDeliveryGraph.isDeposit(this)) {
            if (CargoDeliveryGraph.isRelay(other)
                    && (links.contains(other.pos()) || CargoDeliveryGraph.connected(this, other))) {
                configure(other.pos());
                return false;
            }
        }

        return super.onConfigureBuildTapped(other);
    }

    public void linkTo(Building other) {
        if (other == null || other == this || other.team != team) return;

        boolean changed = false;
        if (!links.contains(other.pos())) {
            links.add(other.pos());
            changed = true;
        }

        if (other instanceof CargoDeliveryBuild build && !build.links.contains(pos())) {
            build.links.add(pos());
            changed = true;
        }

        if (changed) CargoDeliveryGraph.markDirty();
    }

    public void unlink(Building other) {
        if (other == null) return;

        boolean changed = links.removeValue(other.pos());
        if (other instanceof CargoDeliveryBuild build) {
            changed |= build.links.removeValue(pos());
        }

        if (changed) CargoDeliveryGraph.markDirty();
    }

    public void unlinkAll() {
        for (int i = links.size - 1; i >= 0; i--) {
            Building other = world.build(links.get(i));
            if (other != null) {
                unlink(other);
            }
        }
        links.clear();
    }

    public void clearRelayLinks() {
        for (int i = links.size - 1; i >= 0; i--) {
            Building other = world.build(links.get(i));
            if (CargoDeliveryGraph.isRelay(other)) {
                unlink(other);
            }
        }
    }

    public int relayLinkCount() {
        int count = 0;
        for (int i = 0; i < links.size; i++) {
            if (CargoDeliveryGraph.isRelay(world.build(links.get(i)))) count++;
        }
        return count;
    }

    public boolean relayLinkValid(Building other) {
        if (other == null || other == this || other.team != team
                || !CargoDeliveryGraph.isRelay(this) || !CargoDeliveryGraph.isRelay(other)) {
            return false;
        }
        if (!(other instanceof CargoDeliveryBuild otherBuild) || !CargoDeliveryGraph.connected(this, other)) {
            return false;
        }

        return relayLinkCount() < CargoDeliveryGraph.maxLinks(this)
                && otherBuild.relayLinkCount() < CargoDeliveryGraph.maxLinks(other);
    }

    public void toggleLink(Building other) {
        if (other == null || other == this) return;

        boolean linked = links.contains(other.pos());

        if (CargoDeliveryGraph.isRelay(this) && CargoDeliveryGraph.isRelay(other)) {
            if (linked) {
                unlink(other);
            } else if (relayLinkValid(other)) {
                linkTo(other);
            }
        } else if ((CargoDeliveryGraph.isRelay(this) && CargoDeliveryGraph.isDeposit(other))
                || (CargoDeliveryGraph.isDeposit(this) && CargoDeliveryGraph.isRelay(other))) {
            if (linked) {
                unlink(other);
            } else if (CargoDeliveryGraph.connected(this, other)) {
                linkTo(other);
            }
        }
    }

    public void applyLinkPoints(Point2[] points) {
        if (!CargoDeliveryGraph.isRelay(this)) return;

        clearRelayLinks();

        if (points == null) return;

        for (Point2 point : points) {
            Building other = world.build(tileX() + point.x, tileY() + point.y);
            if (other == null) continue;

            if (CargoDeliveryGraph.isRelay(other)) {
                if (!sameComponent(this, other) && relayLinkValid(other)) {
                    linkTo(other);
                }
            } else if (CargoDeliveryGraph.isDeposit(other) && CargoDeliveryGraph.connected(this, other)) {
                linkTo(other);
            }
        }
    }

    public Point2[] autoLinkPoints() {
        if (!CargoDeliveryGraph.isRelay(this)) return new Point2[0];

        CargoDeliveryGraph graph = CargoDeliveryGraph.get(team);
        Seq<Building> candidates = new Seq<>();

        for (int i = 0; i < graph.relays.size; i++) {
            Building other = graph.relays.get(i);
            if (other == this || !relayLinkValid(other) || sameComponent(this, other)) continue;
            candidates.add(other);
        }

        candidates.sort((a, b) -> {
            int dst = Float.compare(dst2(a), dst2(b));
            return dst != 0 ? dst : Integer.compare(a.id, b.id);
        });

        Seq<Building> selected = new Seq<>();
        int max = CargoDeliveryGraph.maxLinks(this);

        for (int i = 0; i < candidates.size && selected.size < max; i++) {
            Building other = candidates.get(i);
            if (sameComponent(this, other)) continue;

            boolean sameAsSelected = false;
            for (int j = 0; j < selected.size; j++) {
                if (sameComponent(selected.get(j), other)) {
                    sameAsSelected = true;
                    break;
                }
            }

            if (!sameAsSelected) selected.add(other);
        }

        Seq<Point2> points = new Seq<>();
        for (int i = 0; i < selected.size; i++) {
            points.add(relativePoint(selected.get(i)));
        }

        for (int i = 0; i < graph.deposits.size; i++) {
            CargoDeliveryDeposit.CargoDeliveryDepositBuild deposit = graph.deposits.get(i);
            if (CargoDeliveryGraph.connected(this, deposit) && !links.contains(deposit.pos())) {
                points.add(relativePoint(deposit));
            }
        }

        return points.toArray(Point2.class);
    }

    protected Point2 relativePoint(Building other) {
        return new Point2(other.tileX() - tileX(), other.tileY() - tileY());
    }

    protected static boolean sameComponent(Building a, Building b) {
        if (a == b) return true;

        ObjectSet<Building> visited = new ObjectSet<>();
        Queue<Building> queue = new Queue<>();
        queue.addLast(a);
        visited.add(a);

        while (!queue.isEmpty()) {
            Building current = queue.removeFirst();
            if (!(current instanceof CargoDeliveryBuild build)) continue;

            for (int i = 0; i < build.links.size; i++) {
                Building next = world.build(build.links.get(i));
                if (next == null || next.team != a.team || !CargoDeliveryGraph.isRelay(next) || !visited.add(next)) {
                    continue;
                }
                if (next == b) return true;
                queue.addLast(next);
            }
        }

        return false;
    }

    protected void linkNearbyRelays() {
        if (!CargoDeliveryGraph.isDeposit(this)) return;

        CargoDeliveryGraph graph = CargoDeliveryGraph.get(team);
        for (int i = 0; i < graph.relays.size; i++) {
            Building relay = graph.relays.get(i);
            if (!CargoDeliveryGraph.connected(this, relay) || links.contains(relay.pos())) continue;
            if (relay instanceof CargoDeliveryBuild build && build.links.contains(pos())) continue;

            relay.configureAny(pos());
        }
    }

    @Override
    public void drawConfigure() {
        if (!CargoDeliveryGraph.isRelay(this)) return;

        Draw.color(Pal.accent);
        Lines.stroke(1f);
        Drawf.circles(x, y, block.size * tilesize / 2f + 1f + Mathf.absin(Time.time, 4f, 1f));
        Drawf.circles(x, y, CargoDeliveryGraph.range(this));

        for (int i = 0; i < links.size; i++) {
            Building other = world.build(links.get(i));
            if (CargoDeliveryGraph.isRelay(other) && other.team == team) {
                Drawf.square(other.x, other.y, other.block.size * tilesize / 2f + 1f, Pal.place);
            }
        }

        Draw.reset();
    }
}
