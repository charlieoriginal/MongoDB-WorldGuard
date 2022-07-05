package de.maxikg.mongowg.codec;

import com.sk89q.worldedit.math.BlockVector2;
import org.bson.BsonReader;
import org.bson.BsonType;
import org.bson.BsonWriter;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;

/**
 * {@link Codec} for {@link BlockVector2}.
 */
public class BlockVector2DCodec implements Codec<BlockVector2> {

    /**
     * Static global instance.
     */
    public static final BlockVector2DCodec INSTANCE = new BlockVector2DCodec();

    /**
     * {@inheritDoc}
     */
    @Override
    public BlockVector2 decode(BsonReader reader, DecoderContext decoderContext) {
        int x = 0;
        int z = 0;

        reader.readStartDocument();
        while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {
            String name = reader.readName();
            if ("x".equals(name))
                x = reader.readInt32();
            else if ("z".equals(name))
                z = reader.readInt32();
            else
                reader.skipValue();
        }
        reader.readEndDocument();

        return BlockVector2.at(x, z);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void encode(BsonWriter writer, BlockVector2 value, EncoderContext encoderContext) {
        writer.writeStartDocument();
        writer.writeName("x");
        writer.writeInt32(value.getBlockX());
        writer.writeName("z");
        writer.writeInt32(value.getBlockZ());
        writer.writeEndDocument();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<BlockVector2> getEncoderClass() {
        return BlockVector2.class;
    }
}
