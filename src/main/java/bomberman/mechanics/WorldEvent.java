package bomberman.mechanics;

import bomberman.mechanics.interfaces.EntityType;
import bomberman.mechanics.interfaces.EventType;
import org.jetbrains.annotations.Nullable;

public class WorldEvent {
    public WorldEvent(EventType eventType, EntityType entityType, int entityID, float x, float y, @Nullable Integer initiator){
        this.eventType = eventType;
        this.entityType = entityType;
        this.entityID = entityID;
        this.x = x;
        this.y = y;
        this.initiator = initiator;
        timestamp = 0;
    }

    public WorldEvent(EventType eventType, @SuppressWarnings("SameParameterValue") EntityType entityType, int entityID, float x, float y, @Nullable Integer initiator, long timestamp) {
        this.eventType = eventType;
        this.entityType = entityType;
        this.entityID = entityID;
        this.x = x;
        this.y = y;
        this.initiator = initiator;
        this.timestamp = timestamp;
    }

    @Nullable
    public Integer getInitiator() {
        return initiator;
    }

    public EventType getEventType() {
        return eventType;
    }

    public EntityType getEntityType() {
        return entityType;
    }

    public int getEntityID() {
        return entityID;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public long getTimestamp() {
        return timestamp;
    }

    private final EventType eventType;
    private final EntityType entityType;
    private final int entityID;
    @SuppressWarnings("InstanceVariableNamingConvention")
    private final float x;
    @SuppressWarnings("InstanceVariableNamingConvention")
    private final float y;
    private final long timestamp;
    private final Integer initiator;
}
