package moe.gensoukyo.mcgproject.common.item.gun;

import moe.gensoukyo.mcgproject.common.item.gun.api.IGunMCG;
import moe.gensoukyo.mcgproject.core.MCGProject;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldSavedData;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.*;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * @deprecated 转为使用Capability
 */
@Deprecated
public final class GunsDeprecated {
    private GunsDeprecated() {}

    /**
     * 持久化一把枪的实例
     * @see java.io.Serializable
     */
    public static byte[] serializeGun(IGunMCG obj) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(obj);
        return bos.toByteArray();
    }

    /**
     * 将持久化的枪反序列化为对象
     * @see java.io.Serializable
     */
    @Nullable
    public static IGunMCG deserializeGun(byte[] rawGunInstance)
            throws IOException, ClassNotFoundException {
        ByteArrayInputStream bis = new ByteArrayInputStream(rawGunInstance);
        ObjectInputStream ois = new ObjectInputStream(bis);
        return (IGunMCG) ois.readObject();
    }

    /**
     * 根据uuid获取已创建的枪实例
     * @param uuid 枪的id，通常储存在ItemStack内
     * @return 枪的实例
     */
    public static IGunMCG fromUUID(UUID uuid) {
        return getStorage().GUNS.get(uuid);
    }

    /**
     * 生成一个枪实例
     * @return 新生成的枪实例，uuid总是与现有的枪械实例不同
     */
    public IGunMCG create() {
        UUID newGunId = UUID.randomUUID();
        while (getStorage().GUNS.containsKey(newGunId)) {
            newGunId = UUID.randomUUID();
        }
        return //TODO
    }

    public void attachToItem(ItemStack stack, IGunMCG gun) {
        if (! (stack.getItem() instanceof ItemMCGGun)) {
            MCGProject.logger.warn("Creating gun instance for non gun item stacks!");
            return;
        }
        NBTTagCompound nbt = stack.getTagCompound();
        nbt = (nbt == null) ? new NBTTagCompound() : nbt;
        nbt.setString(TAG_GUN_ID, gun.getInstanceId().toString());
        stack.setTagCompound(nbt);
    }

    private static final String TAG_GUN_ID = "gun_id";

    /**
     * 从ItemStack获取枪械实例
     * @param stack 道具堆
     * @return 如果ItemStack不是一把枪的实例，那么返回empty，否则永远不会返回empty
     */
    @Nonnull
    public Optional<IGunMCG> fromItemStack(ItemStack stack) {
        if (! (stack.getItem() instanceof ItemMCGGun)) {
            return Optional.empty();
        }
        NBTTagCompound tag = stack.getTagCompound();
        IGunMCG result = null;
        if ((tag != null) && (tag.hasKey(TAG_GUN_ID)) ) {
            try {
                result = fromUUID(UUID.fromString(tag.getString(TAG_GUN_ID)));
            } catch (IllegalArgumentException ignored) {
            }
        }
        //如果对象没有枪械实例则创建新实例
        if (result == null) {
            IGunMCG gun = create();
            attachToItem(stack, gun);
            return Optional.of(gun);
        }
        return Optional.of(result);
    }

    static class Storage extends WorldSavedData {
        static final String NAME = MCGProject.ID + ".saved_guns";

        final Map<UUID, IGunMCG> GUNS;
        public Storage(String name) {
            super(name);
            GUNS = new LinkedHashMap<>();
        }

        @Override
        public void readFromNBT(NBTTagCompound nbt) {
            nbt.getKeySet().forEach(nbtKey -> {
                try {
                    UUID id = UUID.fromString(nbtKey);
                    byte[] rawValue = nbt.getByteArray(nbtKey);
                    IGunMCG value = deserializeGun(rawValue);
                    GUNS.put(id, value);
                } catch (Exception e) {
                    MCGProject.logger.error(
                            "Exception in saving gun instance " + nbtKey, e);
                }
            });
        }

        @Override
        @Nonnull
        public NBTTagCompound writeToNBT(@Nonnull NBTTagCompound compound) {
            GUNS.forEach((uuid, iGunMCG) -> {
                String key = uuid.toString();
                try {
                    byte[] value = serializeGun(iGunMCG);
                    compound.setByteArray(key, value);
                } catch (IOException e) {
                    MCGProject.logger.error(
                            "Exception in saving gun instance " + key, e);
                }
            });
            return compound;
        }
    }

    private static Storage getStorage() {
        return getStorage(MCGProject.SERVER.worlds[0]);
    }

    private static Storage getStorage(World world) {
        MapStorage storage = world.getPerWorldStorage();
        Storage data = (Storage) storage.getOrLoadData(Storage.class, Storage.NAME);
        if (data == null) {
            data = new Storage(Storage.NAME);
            storage.setData(Storage.NAME, data);
        }
        return data;
    }
}
