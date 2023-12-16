package io.github.itskillerluc.recrafted_creatures.entity.ai;

public interface EggLaying {
    boolean hasEgg();
    void setHasEgg(boolean hasEgg);
    int getEggLayCounter();
    void setEggLayCounter(int count);
    void setEggLaying(boolean isLayingEgg);
    boolean getEggLaying();
    void setInLove(int time);
}
