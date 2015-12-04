package org.foo.bar;

import static com.google.common.hash.Hashing.md5;

public class AClass {
    public int getMd5Bits() {
        return md5().bits();
    }
}