package de.maxikg.mongowg.codec;

import com.sk89q.worldedit.math.BlockVector3;
import org.bson.BsonReader;
import org.bson.BsonType;
import org.bson.BsonWriter;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;

/**
 * {@link Codec} for {@link BlockVector3}.
 */
public class BlockVectorCodec implements Codec<BlockVector3> {

    /**
     * Static global instance.
     */
    public static final BlockVectorCodec INSTANCE = new BlockVectorCodec();

    /**
     * {@inheritDoc}
     */
    @Override
    public BlockVector3 decode(BsonReader reader, DecoderContext decoderContext) {
        int x = 0;
        int y = 0;
        int z = 0;

        reader.readStartDocument();
        while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {
            String name = reader.readName();
            if ("x".equals(name))
                x = reader.readInt32();
            else if ("y".equals(name))
                y = reader.readInt32();
            else if ("z".equals(name))
                z = reader.readInt32();
            else
                reader.skipValue();
        }
        reader.readEndDocument();

        return BlockVector3.at(x, y, z);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void encode(BsonWriter writer, BlockVector3 value, EncoderContext encoderContext) {
        writer.writeStartDocument();
        writer.writeName("x");
        writer.writeInt32(value.getBlockX());
        writer.writeName("y");
        writer.writeInt32(value.getBlockY());
        writer.writeName("z");
        writer.writeInt32(value.getBlockZ());
        writer.writeEndDocument();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<BlockVector3> getEncoderClass() {
        return BlockVector3.class;
    }
}
