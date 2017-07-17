package io.realm;


public interface PacketObjectRealmProxyInterface {
    public long realmGet$id();
    public void realmSet$id(long value);
    public long realmGet$mid();
    public void realmSet$mid(long value);
    public long realmGet$noteid();
    public void realmSet$noteid(long value);
    public long realmGet$pageid();
    public void realmSet$pageid(long value);
    public String realmGet$type();
    public void realmSet$type(String value);
    public String realmGet$body();
    public void realmSet$body(String value);
    public float realmGet$runtime();
    public void realmSet$runtime(float value);
    public boolean realmGet$isStatic();
    public void realmSet$isStatic(boolean value);
    public boolean realmGet$isPDFPage();
    public void realmSet$isPDFPage(boolean value);
    public boolean realmGet$isEditingMode();
    public void realmSet$isEditingMode(boolean value);
    public boolean realmGet$isAddPage();
    public void realmSet$isAddPage(boolean value);
    public boolean realmGet$isDrawn();
    public void realmSet$isDrawn(boolean value);
}
