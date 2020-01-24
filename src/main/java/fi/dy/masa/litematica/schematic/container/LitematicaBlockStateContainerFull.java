package fi.dy.masa.litematica.schematic.container;

import javax.annotation.Nullable;
import net.minecraft.block.state.IBlockState;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.Vec3i;
import io.netty.buffer.Unpooled;

public class LitematicaBlockStateContainerFull extends LitematicaBlockStateContainerBase implements IPaletteResizeHandler
{
    protected LitematicaBitArray storage;

    public LitematicaBlockStateContainerFull(Vec3i size)
    {
        this(size, 2, null);
    }

    protected LitematicaBlockStateContainerFull(Vec3i size, int bits, @Nullable long[] backingLongArray)
    {
        super(size, bits);

        this.setBackingArray(backingLongArray);
    }

    @Override
    protected void setBits(int bitsIn)
    {
        if (bitsIn != this.bits)
        {
            this.bits = bitsIn;

            if (this.bits <= MAX_BITS_LINEAR)
            {
                this.bits = Math.max(2, this.bits);
                this.palette = new LitematicaBlockStatePaletteLinear(this.bits, this);
            }
            else
            {
                this.palette = new LitematicaBlockStatePaletteHashMap(this.bits, this);
            }
        }
    }

    protected void setBackingArray(@Nullable long[] backingLongArray)
    {
        if (backingLongArray != null)
        {
            this.storage = new LitematicaBitArray(this.bits, this.totalVolume, backingLongArray);
        }
        else
        {
            this.storage = new LitematicaBitArray(this.bits, this.totalVolume);
        }
    }

    @Override
    public IBlockState getBlockState(int x, int y, int z)
    {
        IBlockState state = this.palette.getBlockState(this.storage.getAt(this.getIndex(x, y, z)));
        return state == null ? AIR_BLOCK_STATE : state;
    }

    @Override
    public void setBlockState(int x, int y, int z, IBlockState state)
    {
        int id = this.palette.idFor(state);
        this.storage.setAt(this.getIndex(x, y, z), id);
        this.hasSetBlockCounts = false; // Force a re-count when next queried
    }

    @Override
    public int onResize(int bits, IBlockState state, ILitematicaBlockStatePalette oldPalette)
    {
        LitematicaBitArray bitArray = this.storage;
        ILitematicaBlockStatePalette statePaletteOld = this.palette;
        this.setBits(bits);
        this.setBackingArray(null);

        for (int index = 0; index < bitArray.size(); ++index)
        {
            IBlockState stateTmp = statePaletteOld.getBlockState(bitArray.getAt(index));

            if (stateTmp != null)
            {
                this.set(index, stateTmp);
            }
        }

        return this.palette.idFor(state);
    }

    protected void set(int index, IBlockState state)
    {
        int id = this.palette.idFor(state);
        this.storage.setAt(index, id);
        this.hasSetBlockCounts = false; // Force a re-count when next queried
    }

    protected int getIndex(int x, int y, int z)
    {
        return (y * this.sizeLayer) + z * this.sizeX + x;
    }

    public long[] getBackingLongArray()
    {
        return this.storage.getBackingLongArray();
    }

    public byte[] getBackingArrayAsByteArray()
    {
        final int entrySize = PacketBuffer.getVarIntSize(this.palette.getPaletteSize() - 1);
        final long volume = this.storage.size();
        final long length = (long) entrySize * volume;

        if (length > Integer.MAX_VALUE)
        {
            throw new IndexOutOfBoundsException("Block data backing byte array length " + length + " exceeds the maximum value of " + Integer.MAX_VALUE);
        }

        byte[] arr = new byte[(int) length];
        PacketBuffer buf = new PacketBuffer(Unpooled.wrappedBuffer(arr));
        buf.writerIndex(0);

        for (int i = 0; i < volume; ++i)
        {
            buf.writeVarInt(this.storage.getAt(i));
        }

        return arr;
    }

    @Override
    protected void calculateBlockCountsIfNeeded()
    {
        if (this.hasSetBlockCounts == false)
        {
            long[] counts = this.storage.getValueCounts();
            this.setBlockCounts(counts);
        }
    }

    @Override
    public LitematicaBlockStateContainerFull copy()
    {
        LitematicaBlockStateContainerFull newContainer = new LitematicaBlockStateContainerFull(this.size, this.bits, this.storage.getBackingLongArray().clone());
        newContainer.palette = this.palette.copy(newContainer);

        return newContainer;
    }

    public static SpongeBlockstateConverterResults convertVarintByteArrayToPackedLongArray(Vec3i size, int bits, byte[] blockStates)
    {
        int volume = size.getX() * size.getY() * size.getZ();
        LitematicaBitArray bitArray = new LitematicaBitArray(bits, volume);
        PacketBuffer buf = new PacketBuffer(Unpooled.wrappedBuffer(blockStates));
        long[] blockCounts = new long[1 << bits];

        for (int i = 0; i < volume; ++i)
        {
            int id = buf.readVarInt();
            bitArray.setAt(i, id);
            ++blockCounts[id];
        }

        return new SpongeBlockstateConverterResults(bitArray.getBackingLongArray(), blockCounts);
    }

    @Nullable
    public static LitematicaBlockStateContainerFull createContainer(int paletteSize, long[] blockStates, Vec3i size)
    {
        int bits = Math.max(2, Integer.SIZE - Integer.numberOfLeadingZeros(paletteSize - 1));
        LitematicaBlockStateContainerFull container = new LitematicaBlockStateContainerFull(size, bits, blockStates);
        container.palette = createPalette(bits, container);
        return container;
    }

    @Nullable
    public static LitematicaBlockStateContainerFull createContainer(int paletteSize, byte[] blockData, Vec3i size)
    {
        int bits = Math.max(2, Integer.SIZE - Integer.numberOfLeadingZeros(paletteSize - 1));
        SpongeBlockstateConverterResults results = convertVarintByteArrayToPackedLongArray(size, bits, blockData);
        LitematicaBlockStateContainerFull container = new LitematicaBlockStateContainerFull(size, bits, results.backingArray);
        container.palette = createPalette(bits, container);
        container.setBlockCounts(results.blockCounts);
        return container;
    }

    public static class SpongeBlockstateConverterResults
    {
        public final long[] backingArray;
        public final long[] blockCounts;

        protected SpongeBlockstateConverterResults(long[] backingArray, long[] blockCounts)
        {
            this.backingArray = backingArray;
            this.blockCounts = blockCounts;
        }
    }
}
