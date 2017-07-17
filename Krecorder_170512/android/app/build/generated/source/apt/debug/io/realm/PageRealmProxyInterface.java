package io.realm;


public interface PageRealmProxyInterface {
    public long realmGet$id();
    public void realmSet$id(long value);
    public long realmGet$noteid();
    public void realmSet$noteid(long value);
    public int realmGet$pagenum();
    public void realmSet$pagenum(int value);
    public float realmGet$runtime();
    public void realmSet$runtime(float value);
    public boolean realmGet$isStatic();
    public void realmSet$isStatic(boolean value);
    public boolean realmGet$isPDFPage();
    public void realmSet$isPDFPage(boolean value);
    public float realmGet$scale();
    public void realmSet$scale(float value);
    public float realmGet$focalPointX();
    public void realmSet$focalPointX(float value);
    public float realmGet$focalPointY();
    public void realmSet$focalPointY(float value);
}
