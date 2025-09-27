package newhorizon.expand.game;

import arc.Events;
import arc.struct.ObjectMap;
import arc.util.Log;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.ctype.UnlockableContent;
import mindustry.game.EventType;
import mindustry.game.Team;
import mindustry.io.SaveFileReader;
import mindustry.type.PayloadSeq;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import static mindustry.Vars.content;

public class TeamPayloadData implements SaveFileReader.CustomChunk {
    public ObjectMap<Team, PayloadSeq> teamPayloadData = new ObjectMap<>();

    public TeamPayloadData() {
        Events.on(EventType.ResetEvent.class, event -> {
            teamPayloadData.clear();
        });
    }

    public void addPayload(Team team, UnlockableContent content, int count) {
        PayloadSeq payload = getPayload(team);
        payload.add(content, count);
        teamPayloadData.put(team, payload);
    }

    public void removePayload(Team team, UnlockableContent content, int count) {
        PayloadSeq payload = getPayload(team);
        if (payload.get(content) < count) return;
        payload.remove(content, count);
        teamPayloadData.remove(team);
    }

    public PayloadSeq getPayload(Team team) {
        PayloadSeq payload = teamPayloadData.get(team);
        if (payload == null) {
            payload = new PayloadSeq();
            teamPayloadData.put(team, payload);
        }
        return payload;
    }

    public void display() {
        StringBuilder sb = new StringBuilder();
        sb.append("Team Payload Data\n");
        teamPayloadData.each((team, payload) -> {
            sb.append("-----").append(team.name).append("-----").append("\n");
            content.each(content -> {
                if (content instanceof UnlockableContent uc) {
                    if (payload.get(uc) != 0) {
                        sb.append(uc.name).append(" ").append(payload.get(uc)).append(" ").append("\n");
                    }
                }
            });
        });
        Log.info(sb.toString());
    }

    @Override
    public void write(DataOutput stream) throws IOException {
        try (Writes write = new Writes(stream)) {
            write.b(teamPayloadData.size);
            teamPayloadData.each((team, payloadSeq) -> {
                write.b(team.id);
                payloadSeq.write(write);
            });
        }
    }

    @Override
    public void read(DataInput stream) throws IOException {
        teamPayloadData.clear();
        try (Reads read = new Reads(stream)) {
            int size = read.b();
            for (int i = 0; i < size; i++) {
                Team team = Team.get(read.b());
                PayloadSeq payloadSeq = new PayloadSeq();
                payloadSeq.read(read);
                teamPayloadData.put(team, payloadSeq);
            }
        }
    }

    @Override
    public void read(DataInput stream, int length) throws IOException {
        read(stream);
    }
}
