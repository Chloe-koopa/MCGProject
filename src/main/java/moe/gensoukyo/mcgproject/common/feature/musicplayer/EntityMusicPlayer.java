package moe.gensoukyo.mcgproject.common.feature.musicplayer;

import moe.gensoukyo.mcgproject.common.entity.MCGEntity;
import moe.gensoukyo.mcgproject.core.MCGProject;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

@MCGEntity("music_player")
public class EntityMusicPlayer extends EntityMinecart {

    public static final DataParameter<Boolean> IS_PLAYING = EntityDataManager.createKey(EntityMusicPlayer.class, DataSerializers.BOOLEAN);
    public static final DataParameter<String> URL = EntityDataManager.createKey(EntityMusicPlayer.class, DataSerializers.STRING);
    public static final DataParameter<String> OWNER = EntityDataManager.createKey(EntityMusicPlayer.class, DataSerializers.STRING);
    public static final DataParameter<Float> VOLUME = EntityDataManager.createKey(EntityMusicPlayer.class, DataSerializers.FLOAT);

    public boolean isPlaying = false;
    public boolean isInvalid = false;
    public String streamURL = "";
    public float volume = 1.0f;
    public MP3Player mp3Player;
    public String owner = "";

    public EntityMusicPlayer(World worldIn) {
        super(worldIn);
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        dataManager.register(IS_PLAYING, isPlaying);
        dataManager.register(URL, streamURL);
        dataManager.register(OWNER, owner);
        dataManager.register(VOLUME, volume);
    }

    @Override
    public boolean attackEntityFrom(DamageSource damagesource, float i) {
        Entity source = damagesource.getTrueSource();
        this.owner = dataManager.get(OWNER);
        if (world.isRemote) {
            return true;
        }
        if(source instanceof EntityPlayer && (source.getName().equals(owner) || this.owner.isEmpty())) {
            this.setDead();
        }
        return true;
    }

    @Override
    public void setDead() {
        this.stopStream();
        super.setDead();
        isDead = true;
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        if (!world.isRemote && this.ticksExisted % 10 == 0) {
            this.dataManager.set(IS_PLAYING, isPlaying);
            this.dataManager.set(URL, streamURL);
            this.dataManager.set(OWNER, owner);
            this.dataManager.set(VOLUME, volume);
        }
        if (world.isRemote) {

            if (this.ticksExisted % 10 == 0 && !this.isPlaying() && this.dataManager.get(IS_PLAYING)) {
                this.streamURL = this.dataManager.get(URL);
                this.startStream();
            }
            if ((Minecraft.getMinecraft().player != null) && (this.mp3Player != null) && (!isInvalid)) {
                volume = dataManager.get(VOLUME);
                float vol = (float) getDistanceSq(Minecraft.getMinecraft().player.posX,
                        Minecraft.getMinecraft().player.posY, Minecraft.getMinecraft().player.posZ);
                if (vol >= (volume * 1000.0F)) {
                    this.mp3Player.setVolume(0.0F);
                } else {
                    float v2 = 10000.0F / vol / 100.0F;
                    if (v2 > 1.0F) {
                        this.mp3Player.setVolume(volume);
                    } else {
                        float v1 = 1.0f - volume;
                        if (v2 - v1 > 0) {
                            v2 = v2 - v1;
                        } else {
                            v2 = 0.0f;
                        }
                        this.mp3Player.setVolume(v2);
                    }
                }
                if (vol == 0) {
                    this.invalidate();
                }
                if (this.isPlaying && rand.nextInt(5) == 0 && (this.mp3Player != null && this.mp3Player.isPlaying())) {
                    int random2 = rand.nextInt(24) + 1;
                    world.spawnParticle(EnumParticleTypes.NOTE, posX, posY + 1.2D, posZ, random2 / 24.0D, 0.0D, 0.0D);
                }
            }
        }
    }

    public void receivePacket(String url, boolean playing, float volume, String owner) {
        this.streamURL = url;
        this.isPlaying = playing;
        this.owner = owner;
        this.volume = volume;
        this.dataManager.set(IS_PLAYING, isPlaying);
        this.dataManager.set(URL, streamURL);
        this.dataManager.set(OWNER, owner);
        this.dataManager.set(VOLUME, volume);
    }


    @SideOnly(Side.CLIENT)
    public void invalidate() {
        isInvalid = true;
        stopStream();
    }

    public void startStream() {

        if (!this.isPlaying) {
            this.isPlaying = true;
            if (world.isRemote) {
                this.mp3Player = new MP3Player(this.streamURL);
                mp3Player.setVolume(0);
                MCGProject.proxy.playerList.add(this.mp3Player);
            }
        }

    }

    public void stopStream() {
        if (this.isPlaying) {
            this.isPlaying = false;
            if (world.isRemote && this.mp3Player != null) {
                this.mp3Player.stop();
                MCGProject.proxy.playerList.remove(this.mp3Player);
            }
        }
    }

    public boolean isPlaying() {
        return this.isPlaying;
    }

    @Override
    public boolean processInitialInteract(@NotNull EntityPlayer entityPlayer, @NotNull EnumHand hand) {
        this.owner = dataManager.get(OWNER);
        if (!this.owner.isEmpty() && !entityPlayer.getName().equals(this.owner)) {
            if (!world.isRemote)
                entityPlayer.sendMessage(new TextComponentString("已锁定"));
            return true;
        }
        if (world.isRemote) {
            new DisplayGuiScreenTask(entityPlayer, this);
        }
        return true;
    }

    @Override
    protected void writeEntityToNBT(@NotNull NBTTagCompound nbttagcompound) {
        super.writeEntityToNBT(nbttagcompound);
        nbttagcompound.setString("StreamUrl", this.streamURL);
        nbttagcompound.setBoolean("isPlaying", this.isPlaying());
        nbttagcompound.setString("owner", this.owner);
        nbttagcompound.setFloat("volume", this.volume);
    }

    @NotNull
    @Override
    public Type getType() {
        return Type.RIDEABLE;
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound nbttagcompound) {
        super.readEntityFromNBT(nbttagcompound);
        this.streamURL = nbttagcompound.getString("StreamUrl");
        this.isPlaying = nbttagcompound.getBoolean("isPlaying");
        this.owner = nbttagcompound.getString("owner");
        this.volume = nbttagcompound.getFloat("volume");
        this.dataManager.set(URL, streamURL);
        this.dataManager.set(IS_PLAYING, isPlaying);
        this.dataManager.set(OWNER, owner);
        this.dataManager.set(VOLUME, volume);
    }
}