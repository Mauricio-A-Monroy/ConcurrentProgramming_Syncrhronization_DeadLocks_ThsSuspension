package edu.eci.arsw.highlandersim;
import java.util.*;

public class ImmortalComparator implements Comparator<Immortal> {
    @Override
    public int compare(Immortal im1, Immortal im2) {
        return im1.getName().compareTo(im2.getName());
    }
}
