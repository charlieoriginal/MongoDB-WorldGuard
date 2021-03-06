package de.maxikg.mongowg.codec;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.managers.storage.RegionDatabaseUtils;
import com.sk89q.worldguard.protection.regions.GlobalProtectedRegion;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedPolygonalRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionType;
import de.maxikg.mongowg.model.ProcessingProtectedRegion;
import org.bson.BsonReader;
import org.bson.BsonType;
import org.bson.BsonWriter;
import org.bson.Document;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.types.ObjectId;
import org.bukkit.util.BlockVector;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * {@link Codec} for {@link ProcessingProtectedRegion}.
 */
public class ProcessingProtectedRegionCodec implements Codec<ProcessingProtectedRegion> {

    /**
     * Static global instance.
     */
    private final CodecRegistry registry;

    /**
     * Constructor for {@code ProcessingProtectedRegionCodec}.
     *
     * @param registry The {@link CodecRegistry} which should be used
     */
    public ProcessingProtectedRegionCodec(CodecRegistry registry) {
        this.registry = registry;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ProcessingProtectedRegion decode(BsonReader reader, DecoderContext decoderContext) {
        ObjectId objectId = null;
        RegionType type = null;
        String id = null;
        String world = null;
        String parent = null;
        int priority = 0;
        BlockVector3 min = null;
        Integer minY = null;
        BlockVector3 max = null;
        Integer maxY = null;
        List<BlockVector2> points = Lists.newArrayList();
        Map<String, Object> flags = Collections.emptyMap();
        DefaultDomain owners = null;
        DefaultDomain members = null;

        reader.readStartDocument();
        while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {
            String name = reader.readName();
            if ("_id".equals(name)) {
                objectId = reader.readObjectId();
            } else if ("name".equals(name)) {
                id = reader.readString();
            } else if ("world".equals(name)) {
                world = reader.readString();
            } else if ("parent".equals(name)) {
                parent = readStringOrNull(reader);
            } else if ("priority".equals(name)) {
                priority = reader.readInt32();
            } else if ("type".equals(name)) {
                type = RegionType.valueOf(reader.readString());
            } else if ("max".equals(name)) {
                max = registry.get(BlockVector3.class).decode(reader, decoderContext);
            } else if ("max_y".equals(name)) {
                maxY = reader.readInt32();
            } else if ("min".equals(name)) {
                min = registry.get(BlockVector3.class).decode(reader, decoderContext);
            } else if ("min_y".equals(name)) {
                minY = reader.readInt32();
            } else if ("flags".equals(name)) {
                flags = registry.get(Document.class).decode(reader, decoderContext);
            } else if ("points".equals(name)) {
                Codec<BlockVector2> vector2DCodec = registry.get(BlockVector2.class);
                reader.readStartArray();
                while (reader.readBsonType() != BsonType.END_OF_DOCUMENT)
                    points.add(vector2DCodec.decode(reader, decoderContext));
                reader.readEndArray();
            } else if ("owners".equals(name)) {
                owners = registry.get(DefaultDomain.class).decode(reader, decoderContext);
            } else if ("members".equals(name)) {
                members = registry.get(DefaultDomain.class).decode(reader, decoderContext);
            } else {
                reader.skipValue();
            }
        }
        reader.readEndDocument();

        Preconditions.checkNotNull(id, "name must be set in document.");
        Preconditions.checkNotNull(type, "type must be set in document.");
        ProtectedRegion region;
        switch (type) {
            case CUBOID:
                Preconditions.checkNotNull(min, "min must be set in document.");
                Preconditions.checkNotNull(max, "max must be set in document.");
                region = new ProtectedCuboidRegion(id, min, max);
                break;
            case GLOBAL:
                region = new GlobalProtectedRegion(id);
                break;
            case POLYGON:
                Preconditions.checkNotNull(minY, "minY must be set in document.");
                Preconditions.checkNotNull(maxY, "maxY must be set in document.");
                region = new ProtectedPolygonalRegion(id, points, minY, maxY);
                break;
            default:
                throw new UnsupportedOperationException("Unknown region type: " + type);
        }
        region.setPriority(priority);
        region.setOwners(owners);
        region.setMembers(members);

        return new ProcessingProtectedRegion(region, parent, objectId, world);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void encode(BsonWriter writer, ProcessingProtectedRegion value, EncoderContext encoderContext) {
        ProtectedRegion region = value.getRegion();
        int priority = region.getPriority();
        Map<Flag<?>, Object> flags = region.getFlags();
        ProtectedRegion parent = region.getParent();

        writer.writeStartDocument();
        writer.writeString("name", region.getId());
        writer.writeString("world", value.getWorld());
        if (parent != null)
            writer.writeString("parent", parent.getId());
        if (priority != 0)
            writer.writeInt32("priority", priority);
        writer.writeString("type", region.getType().name());
        if (region instanceof ProtectedCuboidRegion) {
            Codec<BlockVector3> blockVectorCodec = registry.get(BlockVector3.class);
            writer.writeName("min");
            blockVectorCodec.encode(writer, region.getMinimumPoint(), encoderContext);
            writer.writeName("max");
            blockVectorCodec.encode(writer, region.getMaximumPoint(), encoderContext);
        } else if (region instanceof ProtectedPolygonalRegion) {
            Codec<BlockVector2> blockVector2DCodec = registry.get(BlockVector2.class);
            writer.writeStartArray("points");
            for (BlockVector2 point : region.getPoints())
                blockVector2DCodec.encode(writer, point, encoderContext);
            writer.writeEndArray();
            writer.writeInt32("min_y", region.getMinimumPoint().getBlockY());
            writer.writeInt32("max_y", region.getMaximumPoint().getBlockY());
        }
        if (!flags.isEmpty()) {
            writer.writeName("flags");
            registry.get(Document.class).encode(writer, toMapValues(flags), encoderContext);
        }
        Codec<DefaultDomain> defaultDomainCodec = registry.get(DefaultDomain.class);
        writer.writeName("owners");
        defaultDomainCodec.encode(writer, region.getOwners(), encoderContext);
        writer.writeName("members");
        defaultDomainCodec.encode(writer, region.getMembers(), encoderContext);
        writer.writeEndDocument();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<ProcessingProtectedRegion> getEncoderClass() {
        return ProcessingProtectedRegion.class;
    }

    private static Document toMapValues(Map<Flag<?>, Object> value) {
        Document document = new Document();
        for (Map.Entry<Flag<?>, Object> entry : value.entrySet())
            document.put(entry.getKey().getName(), marshal(entry.getKey(), entry.getValue()));
        return document;
    }

    @SuppressWarnings("unchecked")
    private static <T> Object marshal(Flag<T> flag, Object value) {
        return flag.marshal((T) value);
    }

    private static String readStringOrNull(BsonReader reader) {
        if (reader.getCurrentBsonType() == BsonType.NULL)
            reader.readNull();
        else
            return reader.readString();
        return null;
    }
}
